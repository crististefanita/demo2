package atf.api.tests.api.reporting;

import atf.api.core.http.ApiResponse;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.execution.routing.ApiRoutes;
import atf.api.execution.ApiModuleFactory;
import atf.api.reporting.ExtentReportSession;
import atf.api.reporting.ReportArtifactInspector;
import atf.api.tests.support.TestReportPaths;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndContractTest {
    @Test
    void semanticServiceAndReportUseTheSameRouteContract() throws Exception {
        var transport = (atf.api.core.http.ApiTransport) (request, url, auth) ->
                CompletableFuture.completedFuture(new ApiResponse(200, "{\"id\":1}", Map.of(), Duration.ofMillis(3), url));
        var api = ApiModuleFactory.fromTransport(transport, URI.create("https://example.test"));
        var response = api.users().get(1);

        assertEquals(200, response.response().status());
        assertEquals("https://example.test/public/v2/users/1", response.response().url());

        Instant startedAt = Instant.now();
        Path report;
        report = TestReportPaths.privateReport("api-atf-e2e-internal.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UsersTests", "item contract", List.of("api"));
            var phase = scenario.startPhase("TEST BODY");
            var request = phase.startRequest("item contract", ApiRoutes.userItem().method(), ApiRoutes.userItem().resolvePath(Map.of("id", 1)));
            request.execution(response);
            request.detail("response.status", Integer.toString(response.response().status()));
            request.detail("response.duration.ms", Long.toString(response.response().duration().toMillis()));
            request.json("RESPONSE JSON", response.response().body());
            var validation = request.startValidation("Validate user contract");
            validation.assertions(List.of(
                    new AtfAssertion("HTTP status", "200", Integer.toString(response.response().status()), true),
                    new AtfAssertion("JSON body", "contains id=1", response.response().body(),
                            response.response().body().contains("\"id\":1"))));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        assertTrue(Files.exists(report), "Configured Extent report must be written when the session closes");
        String html = Files.readString(report);
        var inspection = ReportArtifactInspector.inspectFresh(report, startedAt);
        assertTrue(inspection.isCompliant(), () -> "Configured report violations: " + inspection.violations());
        assertAll(
                () -> assertTrue(html.contains("GET /public/v2/users/1")),
                () -> assertTrue(html.contains("TECHNICAL TRACE")),
                 () -> assertTrue(html.contains("execution-evidence")),
                 () -> assertTrue(html.contains("response.duration.ms")),
                () -> assertTrue(html.contains("ASSERTION")),
                () -> assertTrue(html.contains("Validate user contract")),
                () -> assertTrue(html.contains("atf-tag-audit")),
                () -> assertFalse(html.contains("spark/")));
    }
}
