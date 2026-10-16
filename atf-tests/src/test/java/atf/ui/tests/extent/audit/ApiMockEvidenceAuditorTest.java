package atf.ui.tests.extent.audit;

import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.MockRoute;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiMockEvidenceAuditorTest {
    @Test
    void matchesConfiguredResponseTreesWithoutDependingOnPrettyPrinting() {
        var configuration = new AtfMockConfiguration(List.of(
                new MockRoute("GET", "/users", 200,
                        "{\"items\":[{\"id\":1,\"name\":\"ATF User\"}]}",
                        Map.of("Content-Type", "application/json"), Duration.ZERO)),
                new MockRoute("GET", "/*", 500, "{}", Map.of(), Duration.ZERO));
        String html = """
                <textarea readonly class='code-block'>{
                  &quot;items&quot;: [{ &quot;id&quot;: 1, &quot;name&quot;: &quot;ATF User&quot; }]
                }</textarea>
                """;

        var result = new ApiMockEvidenceAuditor().inspectHtml(html, configuration);

        assertAll(
                () -> assertTrue(result.compliant(), () -> result.violations().toString()),
                () -> assertTrue(result.renderedJsonDocuments() >= 1));
    }

    @Test
    void reportsWhenAConfiguredRouteBodyIsMissingFromTheArtifact() {
        var configuration = new AtfMockConfiguration(List.of(
                new MockRoute("GET", "/users", 200, "{\"id\":1}", Map.of(), Duration.ZERO)),
                new MockRoute("GET", "/*", 500, "{}", Map.of(), Duration.ZERO));

        var result = new ApiMockEvidenceAuditor().inspectHtml(
                "<textarea class='code-block'>{\"id\":2}</textarea>", configuration);

        assertTrue(!result.compliant());
        assertTrue(result.violations().getFirst().contains("GET /users"));
    }

    @Test
    void comparesTheResponseWithTheRouteSelectedByTheEffectiveUrl() {
        var configuration = new AtfMockConfiguration(List.of(
                new MockRoute("GET", "/users", 200, "{\"id\":1}", Map.of(), Duration.ZERO)),
                new MockRoute("GET", "/*", 500, "{}", Map.of(), Duration.ZERO));
        var auditor = new ApiMockEvidenceAuditor();

        assertAll(
                () -> assertTrue(auditor.compareResponse("GET", "https://atf.local/users",
                        "{\"id\":1}", configuration).isEmpty()),
                () -> assertTrue(auditor.compareResponse("GET", "https://atf.local/users",
                        "{\"id\":2}", configuration).getFirst().contains("does not match")),
                () -> assertTrue(auditor.compareResponse("GET", "https://atf.local/missing",
                        "null", configuration).getFirst().contains("no matching")));
    }
}
