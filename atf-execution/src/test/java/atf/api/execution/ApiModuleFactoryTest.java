package atf.api.execution;

import atf.api.core.config.ApiSettings;
import atf.api.core.http.ApiExecutionException;
import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiResponse;
import atf.api.core.http.ApiTransport;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiModuleFactoryTest {
    @Test
    void composesOneScopedGraphAndPreservesSettingsSnapshot() {
        ApiTransport transport = (request, url, auth) -> CompletableFuture.completedFuture(
                new ApiResponse(200, "[]", Map.of(), Duration.ZERO, url));
        var module = ApiModuleFactory.fromTransport(transport, URI.create("https://example.test"));

        assertEquals(URI.create("https://example.test"), module.settings().baseUri());
        assertEquals(200, module.users().getCollection().response().status());
        assertEquals(200, module.posts().getCollection().response().status());
    }

    @Test
    void moduleAndExecutorAreComposedFromTheSameImmutableSnapshot() {
        ApiTransport transport = (request, url, auth) -> CompletableFuture.completedFuture(
                new ApiResponse(200, url, Map.of(), Duration.ZERO, url));
        var configured = new ApiSettings(URI.create("https://snapshot.example"), "snapshot",
                Duration.ofSeconds(3), java.util.Set.of(429), 2);
        var module = ApiModuleFactory.fromTransport(transport, configured);

        var result = module.executor().execute(ApiRequest.of("GET", "/health"));

        assertEquals("https://snapshot.example/health", result.response().url());
        assertEquals(Duration.ofSeconds(3), module.settings().timeout());
        assertEquals(2, module.settings().maxAttempts());
    }

    @Test
    void composedExecutorUsesConfiguredTimeoutForTheDefaultExecutePath() {
        ApiTransport pending = (request, url, auth) -> new CompletableFuture<>();
        var settings = new ApiSettings(URI.create("https://example.test"), "default",
                Duration.ofMillis(80), java.util.Set.of(), 1);
        var module = ApiModuleFactory.fromTransport(pending, settings);

        assertThrows(ApiExecutionException.class, () -> module.executor().execute(ApiRequest.of("GET", "/pending")));
    }

    @Test
    void parallelModulesKeepIndependentSnapshotsAndTransportUrls() throws Exception {
        ApiTransport transport = (request, url, auth) -> CompletableFuture.completedFuture(
                new ApiResponse(200, url, Map.of(), Duration.ZERO, url));
        var firstSettings = new ApiSettings(URI.create("https://first.example"), "first");
        var secondSettings = new ApiSettings(URI.create("https://second.example"), "second");

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> ApiModuleFactory.fromTransport(transport, firstSettings)
                    .executor().execute(ApiRequest.of("GET", "/health")));
            var second = executor.submit(() -> ApiModuleFactory.fromTransport(transport, secondSettings)
                    .executor().execute(ApiRequest.of("GET", "/health")));

            assertEquals("https://first.example/health", first.get(5, TimeUnit.SECONDS).response().url());
            assertEquals("https://second.example/health", second.get(5, TimeUnit.SECONDS).response().url());
        }
    }
}
