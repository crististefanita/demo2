package atf.ui.tests.extent.audit;

import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.MockRoute;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockReportContractAuditorTest {
    @Test
    void reconcilesRenderedRequestResponseAndExecutionMetadataWithTheMockRoute() {
        AtfMockConfiguration mock = new AtfMockConfiguration(List.of(
                new MockRoute("POST", "/demo", 202,
                        "{\"ok\":true}",
                        Map.of("Content-Type", "application/json", "X-Mock", "demo"), Duration.ZERO,
                        "api-key", Map.of("mode", "validate"),
                        Map.of("Content-Type", "application/json", "X-Client", "atf-java"),
                         "json", "{\"name\":\"atf\"}")),
                new MockRoute("*", "/*", 500, "{}", Map.of(), Duration.ZERO));
        var evidence = new ApiJsonEvidenceAuditor.Result(
                "{\"name\":\"atf\"}", "{\"ok\":true}", List.of());
        var rendered = new MockReportContractAuditor.RenderedEvidence(
                Map.of("AUTH PROFILE", "api-key",
                        "REQUEST HEADERS", "{Content-Type=application/json, X-Client=atf-java}"),
                Map.of("HTTP STATUS", "202", "CONTENT-TYPE", "application/json",
                        "RESPONSE HEADERS", "[Content-Type=application/json, X-Mock=demo]"),
                Map.of("response.status", "202", "response.url", "https://atf.local/demo?mode=validate"));

        var result = new MockReportContractAuditor(mock).inspect(
                "POST /demo", "https://atf.local/demo?mode=validate", 202, evidence, rendered);

        assertTrue(result.compliant(), () -> result.violations().toString());
    }

    @Test
    void rejectsTransportMetadataDriftEvenWhenTheJsonBodiesStillMatch() {
        AtfMockConfiguration mock = new AtfMockConfiguration(
                List.of(new MockRoute("GET", "/demo", 200, "{\"ok\":true}",
                        Map.of("Content-Type", "application/json", "X-Mock", "demo"), Duration.ZERO,
                        "bearer", Map.of("page", "2"), Map.of("Accept", "application/json"))),
                new MockRoute("*", "/*", 500, "{}", Map.of(), Duration.ZERO));
        var rendered = new MockReportContractAuditor.RenderedEvidence(
                Map.of("AUTH PROFILE", "custom", "REQUEST HEADERS", "{Accept=text/plain}"),
                Map.of("HTTP STATUS", "200", "CONTENT-TYPE", "text/plain",
                        "RESPONSE HEADERS", "[Content-Type=text/plain]"),
                Map.of("response.status", "500", "response.url", "https://atf.local/other"));

        var result = new MockReportContractAuditor(mock).inspect(
                "GET /demo", "https://atf.local/demo?page=1", 200,
                new ApiJsonEvidenceAuditor.Result("null", "{\"ok\":true}", List.of()), rendered);

        assertFalse(result.compliant());
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("AUTH PROFILE")));
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("query page")));
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("request header")));
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("response header")));
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("execution evidence response.status")));
    }

    @Test
    void comparesTheRenderedResponseAsAJsonTreeFromTheMockRoute() {
        AtfMockConfiguration mock = new AtfMockConfiguration(List.of(
                new MockRoute("POST", "/demo", 202,
                        "{\"job\":{\"state\":\"accepted\"},\"items\":[1,2]}",
                        Map.of("Content-Type", "application/json"), Duration.ZERO)),
                new MockRoute("*", "/*", 500, "{\"message\":\"fallback\"}", Map.of(), Duration.ZERO));
        var evidence = new ApiJsonEvidenceAuditor.Result(
                "{\"request\":true}",
                "{ \"items\" : [1, 2], \"job\" : { \"state\" : \"accepted\" } }",
                List.of());

        var result = new MockReportContractAuditor(mock)
                .inspect("POST /demo", 202, evidence);

        assertTrue(result.compliant(), () -> result.violations().toString());
        assertTrue(result.route().isPresent());
    }

    @Test
    void reportsStatusAndBodyDriftWithoutHardcodedPayloadTokens() {
        AtfMockConfiguration mock = new AtfMockConfiguration(List.of(
                new MockRoute("GET", "/demo", 200, "{\"ok\":true}", Map.of(), Duration.ZERO)),
                new MockRoute("*", "/*", 500, "{\"message\":\"fallback\"}", Map.of(), Duration.ZERO));
        var evidence = new ApiJsonEvidenceAuditor.Result("null", "{\"ok\":false}", List.of());

        var result = new MockReportContractAuditor(mock)
                .inspect("GET /demo", 500, evidence);

        assertFalse(result.compliant());
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("expected HTTP 200")));
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("differs from mock YAML body")));
    }

    @Test
    void requiresJsonRequestEvidenceForPostAndPutRoutes() {
        AtfMockConfiguration mock = new AtfMockConfiguration(List.of(
                new MockRoute("PUT", "/demo", 200, "{\"ok\":true}", Map.of(), Duration.ZERO)),
                new MockRoute("*", "/*", 500, "{}", Map.of(), Duration.ZERO));

        var result = new MockReportContractAuditor(mock)
                .inspect("PUT /demo", 200,
                        new ApiJsonEvidenceAuditor.Result("null", "{\"ok\":true}", List.of()));

        assertFalse(result.compliant());
        assertTrue(result.violations().stream().anyMatch(value -> value.contains("non-empty JSON request body")));

        var responseMissing = new MockReportContractAuditor(mock)
                .inspect("PUT /demo", 200,
                        new ApiJsonEvidenceAuditor.Result("{\"id\":1}", "null", List.of()));
        assertFalse(responseMissing.compliant());
        assertTrue(responseMissing.violations().stream()
                .anyMatch(value -> value.contains("non-empty JSON response body")));
    }

    @Test
    void rejectsReportRequestsThatCannotBeResolvedToTheConfiguredMockCatalog() {
        AtfMockConfiguration mock = new AtfMockConfiguration(List.of(),
                new MockRoute("*", "/*", 500, "{}", Map.of(), Duration.ZERO));

        var result = new MockReportContractAuditor(mock)
                .inspect("GET /not-configured", 500,
                        new ApiJsonEvidenceAuditor.Result("null", "{}", List.of()));

        assertFalse(result.compliant());
        assertTrue(result.violations().getFirst().contains("no response contract"));
    }
}
