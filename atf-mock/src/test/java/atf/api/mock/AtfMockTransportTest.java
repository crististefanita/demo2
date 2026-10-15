package atf.api.mock;

import atf.api.core.http.ApiRequest;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtfMockTransportTest {
    @Test
    void acceptsJsonRequestBodyAndReturnsTheConfiguredResponse() {
        String body = "{\"name\":\"ATF user\",\"email\":\"atf@example.test\"}";
        var request = new ApiRequest(HttpRequest.BodyPublishers.ofString(body), "POST",
                "/public/v2/users", Map.of(), Map.of("Content-Type", "application/json"),
                "none", null, body);

        var response = AtfMockTransport.defaults()
                .send(request, "https://atf.local/public/v2/users", "none")
                .join();

        assertAll(
                () -> assertEquals(401, response.status()),
                () -> assertEquals("application/json", response.header("Content-Type")),
                () -> assertEquals("{\"message\":\"Authentication required\"}", response.body()));
    }

    @Test
    void builtInFixturePreservesAcceptanceNegativeRoute() {
        var response = AtfMockTransport.defaults()
                .send(ApiRequest.of("POST", "/public/v2/users"),
                        "https://atf.local/public/v2/users", "none")
                .join();

        assertAll(
                () -> assertEquals(401, response.status()),
                () -> assertEquals("application/json", response.header("Content-Type")),
                () -> assertEquals("{\"message\":\"Authentication required\"}", response.body()),
                () -> assertEquals("https://atf.local/public/v2/users", response.url()));
    }

    @Test
    void yamlRouteCatalogControlsStatusBodyHeadersAndDuration() throws Exception {
        Path config = Files.createTempFile("atf-mock-", ".yaml");
        try {
            Files.writeString(config, """
                    mock:
                      routes:
                        - method: GET
                          path: /demo
                          status: 202
                          duration: PT0.007S
                          headers:
                            Content-Type: application/custom+json
                          body: |-
                            {\"accepted\":true}
                      fallback:
                        status: 418
                        body: |-
                          {\"message\":\"not configured\"}
                    """);

            var transport = AtfMockTransport.fromConfiguration(config);
            var matched = transport.send(ApiRequest.of("GET", "/demo"),
                    "https://atf.local/demo", "none").join();
            var fallback = transport.send(ApiRequest.of("GET", "/missing"),
                    "https://atf.local/missing", "none").join();

            assertAll(
                    () -> assertEquals(202, matched.status()),
                    () -> assertEquals("{\"accepted\":true}", matched.body()),
                    () -> assertEquals("application/custom+json", matched.header("Content-Type")),
                    () -> assertEquals(7, matched.duration().toMillis()),
                    () -> assertEquals(418, fallback.status()),
                    () -> assertTrue(fallback.body().contains("not configured")));
        } finally {
            Files.deleteIfExists(config);
        }
    }

    @Test
    void yamlRouteCanRequireAuthProfileAndQueryParameters() throws Exception {
        Path config = Files.createTempFile("atf-mock-auth-query-", ".yaml");
        try {
            Files.writeString(config, """
                    mock:
                      routes:
                        - method: GET
                          path: /search
                          auth-profile: bearer
                          query:
                            page: "2"
                            include: details
                          status: 200
                          body: |-
                            {"results":[{"id":1,"attributes":{"active":true}}]}
                      fallback:
                        status: 418
                        body: '{"message":"unmatched"}'
                    """);

            var route = new atf.api.core.http.ApiRoute(
                    "search", "GET", "/search", "search", "read", "bearer");
            var request = route.request(Map.of(),
                    Map.of("page", "2", "include", "details"), Map.of());
            var matched = AtfMockTransport.fromConfiguration(config)
                    .send(request, "https://atf.local/search?page=2&include=details", "bearer")
                    .join();

            assertAll(
                    () -> assertEquals(200, matched.status()),
                    () -> assertTrue(matched.body().contains("\"active\":true")),
                    () -> assertEquals("application/json", matched.header("Content-Type")));
        } finally {
            Files.deleteIfExists(config);
        }
    }

    @Test
    void yamlRouteCanRequireCaseInsensitiveRequestHeadersAndReturnResponseHeaders() throws Exception {
        Path config = Files.createTempFile("atf-mock-request-headers-", ".yaml");
        try {
            Files.writeString(config, """
                    mock:
                      routes:
                        - method: GET
                          path: /headers
                          request-headers:
                            X-Correlation-Id: corr-001
                            Accept: application/json
                          status: 200
                          headers:
                            Content-Type: application/json
                            X-Request-Id: response-001
                            ETag: W/catalog-1
                          body: '{"ok":true}'
                      fallback:
                        status: 428
                        body: '{"message":"required headers missing"}'
                    """);

            var route = new atf.api.core.http.ApiRoute(
                    "headers", "GET", "/headers", "headers", "read", "none");
            var accepted = route.request(Map.of(), Map.of(),
                    Map.of("x-correlation-id", "corr-001", "accept", "application/json"));
            var rejected = route.request(Map.of(), Map.of(),
                    Map.of("Accept", "application/json"));
            var transport = AtfMockTransport.fromConfiguration(config);
            var matched = transport.send(accepted, "https://atf.local/headers", "none").join();
            var fallback = transport.send(rejected, "https://atf.local/headers", "none").join();

            assertAll(
                    () -> assertEquals(200, matched.status()),
                    () -> assertEquals("response-001", matched.header("x-request-id")),
                    () -> assertEquals("W/catalog-1", matched.header("ETag")),
                    () -> assertEquals(428, fallback.status()));
        } finally {
            Files.deleteIfExists(config);
        }
    }

    @Test
    void discoversMockOwnedCatalogWithoutReadingGenericAtfYaml() throws Exception {
        Path root = Files.createTempDirectory("atf-mock-discovery-");
        Path nested = Files.createDirectories(root.resolve("nested").resolve("tests"));
        try {
            Files.writeString(root.resolve("atf.yaml"), """
                    api:
                      baseUri: https://example.test
                    reporting:
                      extent:
                        enabled: true
                    """);
            Path mockConfig = root.resolve("atf-mock.yaml");
            Files.writeString(mockConfig, """
                    mock:
                      routes:
                        - method: GET
                          path: /owned
                          status: 204
                          body: ''
                    """);

            assertEquals(mockConfig.toAbsolutePath().normalize(),
                    AtfMockConfigurationLoader.discover(nested));
            var response = AtfMockTransport.fromConfiguration(mockConfig)
                    .send(ApiRequest.of("GET", "/owned"), "https://atf.local/owned", "none")
                    .join();
            assertEquals(204, response.status());
        } finally {
            Files.walk(root)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (java.io.IOException error) {
                            throw new RuntimeException(error);
                        }
                    });
        }
    }

    @Test
    void rejectsGenericAtfYamlWhenExplicitlySelectedAsMockCatalog() throws Exception {
        Path config = Files.createTempFile("atf-generic-", ".yaml");
        try {
            Files.writeString(config, """
                    api:
                      baseUri: https://example.test
                    reporting:
                      extent:
                        enabled: true
                    """);

            var error = org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> AtfMockConfigurationLoader.load(config));
            assertTrue(error.getMessage().contains("root 'mock' section"));
        } finally {
            Files.deleteIfExists(config);
        }
    }
}
