package atf.ui.tests.autoex.component.api;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.html.HtmlReportSession;
import atf.api.reporting.model.ReportExecutionModel;
import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.api.AutoExApiRequestFactory;
import atf.ui.tests.autoex.api.AutoExApiContractReportExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protects AutoEx responseCode/message mapping and the consumer's published-data boundary. */
@AtfComponentTest
class AutoExApiContractReportPublisherComponentTest {
    private static final String REPORT_DIRECTORY = "custom/api-reports";
    private static final String EXPECTED_MESSAGE = "User exists!";
    private static final String SECRET = "synthetic-report-password";
    private static final String RESPONSE_BODY = "{\"responseCode\":200,\"message\":\"User exists!\"}";

    @Test
    void publisherForwardsTheAutoExContractAndDoesNotExposeFormCredentials(@TempDir Path directory)
            throws Exception {
        Path report = directory.resolve("api-contract.html");
        var execution = execution(200, RESPONSE_BODY);
        try (var session = new HtmlReportSession(report)) {
            publish(session, execution);
            var request = publishedRequest(session);
            var validation = (ReportExecutionModel.Validation) request.children().getFirst();
            assertAll(
                    () -> assertEquals("API 7 valid login", request.title()),
                    () -> assertEquals("POST", request.method()),
                    () -> assertEquals("/api/verifyLogin", request.path()),
                    () -> assertTrue(request.execution().request().headers().values()
                            .contains("application/x-www-form-urlencoded")),
                    () -> assertTrue(request.execution().response().headers().values()
                            .contains("text/html; charset=utf-8")),
                    () -> assertEquals("Validate documented JSON response contract", validation.title()),
                    () -> assertEquals(List.of("JSON responseCode", "JSON message", "HTTP transport status"),
                            validation.assertions().stream().map(AtfAssertion::name).toList()),
                    () -> assertEquals("200", validation.assertions().getFirst().actual()),
                    () -> assertEquals(EXPECTED_MESSAGE, validation.assertions().get(1).actual()),
                    () -> assertEquals(AtfReportStatus.PASS, validation.status()));
        }
        Path extentReport = directory.resolve("extent-contract.html");
        try (var session = new ExtentReportSession(extentReport)) {
            publish(session, execution);
        }
        for (Path published : List.of(report, extentReport)) {
            assertTrue(Files.readString(published).contains("***REDACTED***"));
            assertFalse(Files.readString(published).contains(SECRET),
                    "the AutoEx request factory and publisher must not expose the form password");
        }
    }

    @ParameterizedTest
    @MethodSource("invalidContracts")
    void transportSuccessCannotHideAnInvalidAutoExContractOrAdditionalAssertion(
            int httpStatus, String responseBody, @TempDir Path directory) {
        try (var session = new HtmlReportSession(directory.resolve("invalid-contract.html"))) {
            publish(session, execution(httpStatus, responseBody));
            var request = publishedRequest(session);
            var validation = (ReportExecutionModel.Validation) request.children().getFirst();
            assertEquals(AtfReportStatus.FAIL, request.status());
            assertEquals(AtfReportStatus.FAIL, validation.status());
            assertTrue(validation.assertions().stream().anyMatch(assertion -> !assertion.passed()));
        }
    }

    static Stream<Arguments> invalidContracts() {
        return Stream.of(
                Arguments.of(200, "{\"responseCode\":401,\"message\":\"User not found!\"}"),
                Arguments.of(200, "{\"responseCode\":200,\"message\":\"Unexpected message\"}"),
                Arguments.of(200, "<html>upstream error</html>"),
                Arguments.of(503, RESPONSE_BODY));
    }

    @Test
    void reportDirectoryUsesTheAutoExDefaultOrConfiguredProjectRelativeOverride(@TempDir Path projectRoot) {
        assertAll(
                () -> assertEquals(projectRoot.resolve("artifacts/reports/api/auto-ex"),
                        AutoExApiContractReportExtension.resolveDirectory(projectRoot, null)),
                () -> assertEquals(projectRoot.resolve(REPORT_DIRECTORY),
                        AutoExApiContractReportExtension.resolveDirectory(projectRoot, REPORT_DIRECTORY)),
                () -> assertTrue(AutoExApiContractReportExtension.resolveDirectory(
                        projectRoot, REPORT_DIRECTORY).startsWith(projectRoot)));
    }

    @Test
    void executionFailureForwardsTheActualCauseChainAndSuppressionsWithoutLeakingSecrets(
            @TempDir Path directory) throws Exception {
        Path report = directory.resolve("api-contract-failure.html");
        String secret = "synthetic-api-token-should-not-leak";
        try (var session = new HtmlReportSession(report)) {
            var scenario = session.startScenario("AutoExApiReadOnlyLiveContractTest",
                    "read-only API evidence", List.of("api", "auto-ex", "component"));
            var phase = scenario.startPhase("TEST BODY");
            var failure = new IllegalStateException("request failed", new java.io.IOException(
                    "authorization: Bearer " + secret));
            failure.addSuppressed(new IllegalArgumentException("cleanup failure"));
            AutoExApiContractReportExtension.recordExecutionFailure(phase, failure);
            phase.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
            var fields = session.executionModel().scenarios().getFirst().phases().getFirst().details();
            assertEquals(1, fields.size());
            assertEquals("JUnit failure", fields.getFirst().name());
            String message = fields.getFirst().value();
            assertAll(
                    () -> assertTrue(message.contains("IllegalStateException: request failed")),
                    () -> assertTrue(message.contains("IOException: authorization: Bearer")),
                    () -> assertTrue(message.contains("IllegalArgumentException: cleanup failure")));
        }
        assertFalse(Files.readString(report).contains(secret), "the published diagnostic must redact credentials");
    }

    private static ApiExecutionResult execution(int status, String body) {
        var request = new AutoExApiRequestFactory().verifyLogin("report@example.test", SECRET);
        var response = new ApiResponse(status, body,
                Map.of("content-type", "text/html; charset=utf-8"), Duration.ofMillis(4),
                "https://automationexercise.invalid/api/verifyLogin");
        return new ApiExecutionResult(response, 1, Duration.ofMillis(4), List.of(), List.of(),
                request, ApiTransportProvenance.ACCEPTANCE_MOCK);
    }

    private static void publish(AtfReportPorts.Session session, ApiExecutionResult execution) {
        var scenario = session.startScenario("AutoExApiLiveContractTest", "verify login report contract",
                List.of("api", "auto-ex", "component"));
        var phase = scenario.startPhase("TEST BODY");
        int status = execution.response().status();
        AutoExApiContractReportExtension.publish(phase, "API 7 valid login", execution, 200,
                EXPECTED_MESSAGE, List.of(new AtfAssertion("HTTP transport status", "200",
                        Integer.toString(status), status == 200)));
        phase.complete(AtfReportStatus.PASS);
        scenario.complete(AtfReportStatus.PASS);
    }

    private static ReportExecutionModel.Request publishedRequest(HtmlReportSession session) {
        return (ReportExecutionModel.Request) session.executionModel().scenarios().getFirst()
                .phases().getFirst().children().getFirst();
    }
}
