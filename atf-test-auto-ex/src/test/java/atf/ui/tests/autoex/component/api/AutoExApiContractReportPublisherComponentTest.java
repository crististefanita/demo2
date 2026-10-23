package atf.ui.tests.autoex.component.api;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.api.reporting.api.ApiManualReplay;
import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.api.AutoExApiRequestFactory;
import atf.ui.tests.autoex.api.AutoExApiContractReportExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the consumer API-report publisher offline, including form-secret redaction. */
@AtfComponentTest
class AutoExApiContractReportPublisherComponentTest {
    private static final String REPORT_DIRECTORY = "custom/api-reports";
    private static final String EXPECTED_MESSAGE = "User exists!";
    private static final String SECRET = "synthetic-report-password";
    private static final String RESPONSE_BODY = "{\"responseCode\":200,\"message\":\"User exists!\"}";

    @Test
    void reportPublisherWritesAuditableExchangeAndRedactsFormCredentials(@TempDir Path temporaryDirectory)
            throws Exception {
        Path report = temporaryDirectory.resolve("api-contract.html");
        var request = new AutoExApiRequestFactory().verifyLogin("report@example.test", SECRET);
        var response = new ApiResponse(200, RESPONSE_BODY,
                Map.of("content-type", "application/json"), Duration.ofMillis(4),
                "https://automationexercise.invalid/api/verifyLogin");
        var execution = new ApiExecutionResult(response, 1, Duration.ofMillis(4), List.of(), List.of(),
                request, ApiTransportProvenance.ACCEPTANCE_MOCK);
        String replay = ApiManualReplay.describe(ReportTransportProvenance.LIVE_HTTP,
                ReportModelAdapters.request(request), response.url(), request.bodyText());

        try (ExtentReportSession session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("AutoExApiLiveContractTest", "verify login report contract",
                    List.of("api", "auto-ex", "contract"));
            var phase = scenario.startPhase("TEST BODY");
            AutoExApiContractReportExtension.publish(phase, "API 7 valid login", execution, 200,
                    EXPECTED_MESSAGE);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("API 7 valid login")),
                () -> assertTrue(html.contains("POST")),
                () -> assertTrue(html.contains("/api/verifyLogin")),
                () -> assertTrue(html.contains("application/x-www-form-urlencoded")),
                () -> assertTrue(html.contains("Validate documented JSON response contract")),
                () -> assertTrue(html.contains("JSON responseCode")),
                () -> assertTrue(html.contains("JSON message")),
                () -> assertTrue(html.contains("User exists!")),
                () -> assertTrue(html.contains("***REDACTED***")),
                () -> assertFalse(html.contains(SECRET), "the form password must not appear in the report"),
                () -> assertTrue(replay.contains("***REDACTED***")),
                () -> assertFalse(replay.contains(SECRET), "the manual replay must redact form credentials"));
    }

    @Test
    void reportDirectoryMustRemainInsideTheReactor(@TempDir Path projectRoot) {
        Path expected = projectRoot.resolve(REPORT_DIRECTORY).toAbsolutePath().normalize();

        assertAll(
                () -> assertEquals(expected,
                        AutoExApiContractReportExtension.resolveDirectory(projectRoot, REPORT_DIRECTORY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> AutoExApiContractReportExtension.resolveDirectory(projectRoot, "../outside")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> AutoExApiContractReportExtension.resolveDirectory(projectRoot,
                                projectRoot.resolve("outside").toString())));
    }

    @Test
    void executionFailureIsVisibleAsACompactRedactedReportDiagnostic(@TempDir Path temporaryDirectory)
            throws Exception {
        Path report = temporaryDirectory.resolve("api-contract-failure.html");
        String secret = "synthetic-api-token-should-not-leak";
        try (ExtentReportSession session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("AutoExApiReadOnlyLiveContractTest",
                    "read-only API evidence", List.of("api", "auto-ex", "live"));
            AtfReportPorts.Phase phase = scenario.startPhase("TEST BODY");
            var failure = new IllegalStateException("request failed", new java.io.IOException(
                    "authorization: Bearer " + secret));
            failure.addSuppressed(new IllegalArgumentException("cleanup failure"));
            AutoExApiContractReportExtension.recordExecutionFailure(phase, failure);
            phase.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("Execution diagnostics")),
                () -> assertTrue(html.contains("JUnit failure")),
                () -> assertTrue(html.contains("IllegalStateException: request failed")),
                () -> assertTrue(html.contains("IOException: authorization: Bearer ***REDACTED***")),
                () -> assertTrue(html.contains("IllegalArgumentException: cleanup failure")),
                () -> assertFalse(html.contains(secret), "failure details must not leak bearer credentials"));
    }
}
