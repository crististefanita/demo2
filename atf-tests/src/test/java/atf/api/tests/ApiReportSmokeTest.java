package atf.api.tests;

import atf.api.core.http.ApiResponse;
import atf.api.core.http.AuthProfile;
import atf.api.core.http.ConfiguredApiAuthenticator;
import atf.api.core.http.ApiTransport;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.execution.ApiModuleFactory;
import atf.api.reporting.ExtentReportSession;
import atf.api.reporting.ReportArtifactInspector;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/** Deterministic API contract proving the API-to-report path end to end. */
class ApiReportSmokeTest {
    @Test
    void apiRequestValidationAndLifecycleAreRenderedIntoOneReport() throws Exception {
        Instant startedAt = Instant.now();
        Path report = TestReportPaths.privateReport("api-atf-smoke-internal.html");

        var authenticator = new ConfiguredApiAuthenticator(Map.of("default", AuthProfile.none("default")));
        ApiTransport transport = (request, url, auth) -> CompletableFuture.completedFuture(
                new ApiResponse(200, "{\"id\":1,\"name\":\"ATF User\"}", Map.of("Content-Type", "application/json"), Duration.ofMillis(3), url));
        var api = ApiModuleFactory.fromTransport(transport, java.net.URI.create("https://local.test"));

        try (var session = new ExtentReportSession(report)) {
                var scenario = session.startScenario("UsersApiTests", "GET user — item contract", List.of("api", "contract", "smoke"));

                var setup = scenario.startPhase("SETUP");
                var setupRequest = setup.startRequest("health check", "GET", "/public/v2/users/1");
                setupRequest.detail("auth.mode", authenticator.describeMode("default"));
                setupRequest.complete(AtfReportStatus.PASS);
                setup.complete(AtfReportStatus.PASS);

                var body = scenario.startPhase("TEST BODY");
                var execution = api.users().get(1);
                var request = body.startRequest("item contract", "GET", "/public/v2/users/1");
                request.detail("response.status", Integer.toString(execution.response().status()));
                request.detail("response.duration.ms", Long.toString(execution.response().duration().toMillis()));
                request.json("RESPONSE JSON", execution.response().body());
                var validation = request.startValidation("Validate user contract");
                validation.assertions(List.of(
                        new AtfAssertion("HTTP status", "200", Integer.toString(execution.response().status()), execution.response().status() == 200),
                        new AtfAssertion("Content-Type", "application/json", execution.response().header("Content-Type"), "application/json".equals(execution.response().header("Content-Type"))),
                        new AtfAssertion("JSON body", "contains id=1", execution.response().body(), execution.response().body().contains("\"id\":1"))));
                validation.complete(AtfReportStatus.PASS);
                request.complete(AtfReportStatus.PASS);
                body.complete(AtfReportStatus.PASS);

                var teardown = scenario.startPhase("TEARDOWN");
                var teardownRequest = teardown.startRequest("cleanup", "GET", "/public/v2/users/1");
                teardownRequest.complete(AtfReportStatus.PASS);
                teardown.complete(AtfReportStatus.PASS);
                scenario.complete(AtfReportStatus.PASS);
        }

        assertTrue(Files.exists(report), "API test must produce an HTML report");
        String html = Files.readString(report);
        var inspection = ReportArtifactInspector.inspectFresh(report, startedAt);
        assertTrue(inspection.isCompliant(),
                () -> "Default Extent report violations: " + inspection.violations());
        assertAll(
                () -> assertTrue(html.contains("GET /public/v2/users/1")),
                () -> assertTrue(html.contains("Validate user contract")),
                () -> assertTrue(html.contains("RESPONSE JSON")),
                () -> assertTrue(html.contains("HTTP status")),
                () -> assertTrue(html.contains("TECHNICAL TRACE")),
                () -> assertTrue(html.contains("RESPONSE DURATION (ms)")),
                () -> assertTrue(html.contains("ASSERTION")),
                () -> assertTrue(html.contains("id=\"nav-fails\"")),
                () -> assertTrue(html.contains("data-atf-tab=\"overview\"")),
                () -> assertFalse(html.contains("spark/")),
                () -> assertFalse(Files.exists(report.getParent().resolve("spark"))));
    }

    private static int count(String value, String token) {
        int count = 0, index = 0;
        while ((index = value.indexOf(token, index)) >= 0) { count++; index += token.length(); }
        return count;
    }
}
