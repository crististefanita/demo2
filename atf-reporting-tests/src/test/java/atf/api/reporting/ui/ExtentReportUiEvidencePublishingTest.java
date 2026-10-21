package atf.api.reporting.ui;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.api.reporting.ui.network.ReportNetworkInspector;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static atf.api.reporting.ui.ExtentReportUiEvidenceTestSupport.EXTENT_UI_TEST_PACKAGE;
import static atf.api.reporting.contract.AtfGherkinKeyword.AND;
import static atf.api.reporting.contract.AtfGherkinKeyword.GIVEN;
import static atf.api.reporting.contract.AtfGherkinKeyword.THEN;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportUiEvidencePublishingTest {
    @Test
    void publishesUiAuditWithoutImplicitTeardownPhase() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-no-teardown-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-no-teardown.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.api.tests", "visual audit", List.of("ui"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.complete(AtfReportStatus.PASS);
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.step(THEN, "the report is visually auditable", "#test-view", null,
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("<span>SETUP</span>")),
                () -> assertTrue(html.contains("<span>TEST BODY</span>")),
                () -> assertFalse(html.contains("<span>TEARDOWN</span>")),
                () -> assertTrue(html.contains("the report is visually auditable")));
    }

    @Test
    void publishesArtifactOutcomeWithoutSyntheticUiValidationChild() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-outcome-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-outcome.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.api.tests", "artifact publication", List.of("ui"));
            var teardown = scenario.startPhase(ReportUiContract.Phase.TEARDOWN.displayName());
            teardown.outcome(THEN, "the UI artifact was published", "UI audit report", AtfReportStatus.PASS);
            teardown.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("THEN the UI artifact was published")),
                () -> assertTrue(html.contains("OUTCOME TYPE")),
                () -> assertTrue(html.contains("artifact-publication")),
                () -> assertFalse(html.contains("UI validation")));
    }

    @Test
    void publishesRuntimeDiagnosticWithoutMisclassifyingItAsUiValidation() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-runtime-diagnostic-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-runtime-diagnostic.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "runtime diagnostic", List.of("ui", "diagnostic"));
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.diagnostic(AND, "known guest API/runtime branch is visible as a warning",
                    "GET /users/me → 401",
                    List.of(new AtfAssertion("Guest runtime signal", "expected anonymous bootstrap",
                            "GET /users/me → 401", true)), AtfReportStatus.WARNING);
            body.complete(AtfReportStatus.WARNING);
            scenario.complete(AtfReportStatus.WARNING);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("known guest API/runtime branch is visible as a warning")),
                () -> assertTrue(html.contains("Runtime diagnostic")),
                () -> assertTrue(html.contains("runtime-diagnostic")),
                () -> assertTrue(html.contains("Guest runtime signal")),
                () -> assertFalse(html.contains("UI validation")));
    }

    @Test
    void publishesSetupAsArtifactEvidenceWithoutSyntheticUiValidationChild() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-setup-evidence-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-setup.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "report setup", List.of("ui"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.setup(GIVEN, "the input report is available",
                    "input-report.html", List.of(new AtfAssertion(
                            "Input report", "published HTML", "input-report.html", true)),
                    AtfReportStatus.PASS);
            setup.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int setupStart = html.indexOf("<details class=\"step-detail setup-evidence atf-setup-evidence\"");
        int setupEnd = html.indexOf("</td>", setupStart);
        String setupMarkup = setupStart >= 0 && setupEnd > setupStart
                ? html.substring(setupStart, setupEnd)
                : "";
        assertAll(
                () -> assertTrue(setupStart >= 0 && setupEnd > setupStart),
                () -> assertTrue(html.contains("data-atf-evidence-type=\"report-setup\"")),
                () -> assertTrue(html.contains("<summary aria-expanded=\"false\"><span class=\"atf-disclosure-glyph\"")),
                () -> assertTrue(html.contains("Setup evidence · 1 checks")),
                () -> assertTrue(html.contains("<div class=\"body\"><table")),
                () -> assertTrue(html.contains("</table></div></details>")),
                () -> assertTrue(html.contains("class=\"atf-setup-evidence-table\"")),
                () -> assertTrue(html.contains("CHECK") && html.contains("EXPECTED")
                        && html.contains("ACTUAL") && html.contains("RESULT")),
                () -> assertTrue(html.contains("✅ PASS")),
                () -> assertTrue(html.contains("input-report.html")),
                () -> assertFalse(setupMarkup.contains("<details open")),
                () -> assertFalse(setupMarkup.contains("Request details")),
                () -> assertFalse(setupMarkup.contains("Response details")),
                () -> assertFalse(setupMarkup.contains("UI validation")),
                () -> assertFalse(setupMarkup.contains("Screen validation")));
    }

    @Test
    void keepsSetupEvidenceInsideOneClosedFrameForMultipleChecks() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-setup-multi-checks-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-setup-multi-checks.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "multiple setup checks", List.of("ui"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.setup(GIVEN, "the input report is available",
                    "input-report.html", List.of(
                            new AtfAssertion("Input report", "published HTML", "input-report.html", true),
                            new AtfAssertion("Input report is readable", "readable", "readable", true)),
                    AtfReportStatus.PASS);
            setup.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int disclosure = html.indexOf("<details class=\"step-detail setup-evidence atf-setup-evidence\"");
        int closing = html.indexOf("</table></div></details>", disclosure);
        assertAll(
                () -> assertTrue(disclosure >= 0),
                () -> assertTrue(html.contains("Setup evidence · 2 checks")),
                () -> assertTrue(closing > disclosure),
                () -> assertEquals(1, ExtentReportUiEvidenceTestSupport.count(html,
                        "step-detail setup-evidence atf-setup-evidence")));
    }

    @Test
    void projectsApiEvidenceInsideTheUiCheckpointWithClosedDisclosures() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-api-evidence-");
        Path report;
        var request = new ReportRequest("POST", "/checkout", Map.of("step", "payment"),
                Map.of("Authorization", "Bearer secret", "Content-Type", "application/json"),
                "bearer", "{\"payment\":{\"method\":\"cash-on-delivery\"},\"items\":[1,2]}");
        var response = new ReportResponse(200, "{\"message\":\"Payment was successful\",\"meta\":{\"retryable\":false}}",
                Map.of("Content-Type", "application/json", "X-Request-Id", "req-123"), Duration.ofMillis(12),
                "https://api.practicesoftwaretesting.com/payment/check");
        var execution = new ReportExecutionEvidence(response, 1, Duration.ofMillis(12), List.of(),
                List.of(new ReportAttemptRecord(1, 200, Duration.ofMillis(12), "SUCCESS", "final response")),
                request, ReportTransportProvenance.LIVE_HTTP);

        try (var session = UiExtentReportSession.openConfigured(root, "ui-api-evidence.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "API evidence in UI step", List.of("ui", "api"));
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.step(GIVEN, "the payment response is visible", "/checkout", null,
                    List.of(new AtfAssertion("Payment", "success", "success", true)), List.of(),
                    List.of(execution), AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("Request details")),
                () -> assertTrue(html.contains("Response details")),
                () -> assertTrue(html.contains("Execution evidence")),
                () -> assertTrue(html.contains("REQUEST JSON")),
                () -> assertTrue(html.contains("RESPONSE JSON")),
                () -> assertTrue(html.contains("AUTH TYPE")),
                () -> assertTrue(html.contains("AUTH PROFILE")),
                () -> assertTrue(html.contains("REQUEST HEADERS")),
                () -> assertTrue(html.contains("MANUAL REPLAY")),
                () -> assertTrue(html.contains("API 1 · METHOD")),
                () -> assertTrue(html.contains("API 1 · HTTP STATUS")),
                () -> assertTrue(html.contains("API 1 · RESPONSE HEADERS")),
                () -> assertFalse(html.contains("AUTH SOURCE")),
                () -> assertFalse(html.contains("REQUEST API 1 HEADERS")),
                () -> assertFalse(html.contains("RESPONSE API 1 HEADERS")),
                () -> assertFalse(html.contains("sec-ch-ua")),
                () -> assertFalse(html.contains("User-Agent")),
                () -> assertFalse(html.contains("Referer")),
                () -> assertTrue(html.contains("response.status")),
                () -> assertTrue(html.contains("\"payment\" : {") || html.contains("&quot;payment&quot; : {")),
                () -> assertTrue(html.contains("\"retryable\" : false")
                        || html.contains("&quot;retryable&quot; : false")),
                () -> assertFalse(html.contains("Bearer ***REDACTED***")),
                () -> assertFalse(html.contains("Bearer secret")),
                () -> assertFalse(html.contains("<details open")));
    }

    @Test
    void publishesApiEvidenceAsCausalSiblingAfterItsUiCheckpoint() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-api-trace-sibling-");
        Path report;
        var request = new ReportRequest("POST", "/checkout", Map.of("step", "payment"),
                Map.of("Content-Type", "application/json"), "none",
                "{\"payment\":{\"method\":\"cash-on-delivery\"}}");
        var response = new ReportResponse(200, "{\"message\":\"Payment was successful\"}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(12),
                "https://api.practicesoftwaretesting.com/payment/check");
        var execution = new ReportExecutionEvidence(response, 1, Duration.ofMillis(12), List.of(),
                List.of(new ReportAttemptRecord(1, 200, Duration.ofMillis(12), "SUCCESS", "final response")),
                request, ReportTransportProvenance.LIVE_HTTP);

        try (var session = UiExtentReportSession.openConfigured(root, "ui-api-trace-sibling.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "causal API trace", List.of("ui", "api"));
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.step(GIVEN, "the payment response is visible", "/checkout", null,
                    List.of(new AtfAssertion("Payment", "success", "success", true)), List.of(),
                    AtfReportStatus.PASS);
            body.apiTrace(GIVEN, "the payment response is visible", "/checkout",
                    List.of(execution), AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        String traceTitle = "<span>GIVEN the payment response is visible</span>";
        int uiStep = html.indexOf(traceTitle);
        int traceNode = html.indexOf(traceTitle, uiStep + traceTitle.length());
        int apiTrace = html.indexOf("EVIDENCE TYPE</td><td>api-trace");
        int requestJson = html.indexOf("REQUEST JSON");
        String uiProjection = uiStep >= 0 && traceNode > uiStep
                ? html.substring(uiStep, traceNode) : "";
        assertAll(
                () -> assertTrue(uiStep >= 0),
                () -> assertTrue(traceNode > uiStep),
                () -> assertTrue(apiTrace > traceNode),
                 () -> assertTrue(ExtentReportUiEvidenceTestSupport.count(
                         html, "GIVEN the payment response is visible") >= 2),
                 () -> assertTrue(html.contains("Request details")),
                 () -> assertTrue(html.contains("Response details")),
                 () -> assertTrue(html.contains("TECHNICAL TRACE")),
                 () -> assertTrue(html.contains("Execution evidence")),
                 () -> assertTrue(html.contains("AUTH PROFILE")),
                 () -> assertTrue(html.contains("REQUEST HEADERS")),
                 () -> assertTrue(html.contains("MANUAL REPLAY")),
                 () -> assertTrue(html.contains("API 1 · METHOD")),
                 () -> assertTrue(html.contains("API 1 · HTTP STATUS")),
                 () -> assertFalse(html.contains("API trace · GIVEN the payment response is visible")),
                 () -> assertFalse(html.contains("TRACE /checkout — GIVEN")),
                 () -> assertFalse(html.contains("TRACE https://api.practicesoftwaretesting.com")),
                 () -> assertFalse(html.contains("RESPONSE HEADERS")),
                 () -> assertTrue(requestJson > traceNode),
                () -> assertTrue(html.contains("EVIDENCE TYPE</td><td>api-trace")),
                () -> assertTrue(html.contains("TRACE PARENT STEP")),
                () -> assertFalse(uiProjection.contains("REQUEST JSON")),
                () -> assertFalse(html.contains("REQUEST JSON</span></summary>")));
    }

    @Test
    void rendersExecutionEvidenceForEveryExchangeInOneUiApiTrace() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-api-trace-exchanges-");
        Path report;
        var firstRequest = new ReportRequest("GET", "/products/1", Map.of(),
                Map.of("Accept", "application/json"), "none", "");
        var firstResponse = new ReportResponse(200, "{\"id\":1}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(8),
                "https://api.practicesoftwaretesting.com/products/1");
        var first = new ReportExecutionEvidence(firstResponse, 1, Duration.ofMillis(8), List.of(),
                List.of(new ReportAttemptRecord(1, 200, Duration.ofMillis(8), "SUCCESS", "first-exchange-evidence")),
                firstRequest, ReportTransportProvenance.LIVE_HTTP);
        var secondRequest = new ReportRequest("GET", "/cart", Map.of(),
                Map.of("Accept", "application/json"), "none", "");
        var secondResponse = new ReportResponse(200, "{\"quantity\":1}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(9),
                "https://api.practicesoftwaretesting.com/cart");
        var second = new ReportExecutionEvidence(secondResponse, 1, Duration.ofMillis(9), List.of(),
                List.of(new ReportAttemptRecord(1, 200, Duration.ofMillis(9), "SUCCESS", "second-exchange-evidence")),
                secondRequest, ReportTransportProvenance.LIVE_HTTP);

        try (var session = UiExtentReportSession.openConfigured(root, "ui-api-trace-exchanges.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "two causal exchanges", List.of("ui", "api"));
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.apiTrace(THEN, "cart is ready", "/checkout", List.of(first, second), AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("products/1")),
                () -> assertTrue(html.contains("/cart")),
                () -> assertTrue(html.contains("first-exchange-evidence")),
                () -> assertTrue(html.contains("second-exchange-evidence")),
                () -> assertTrue(ExtentReportUiEvidenceTestSupport.count(html,
                        "RESPONSE JSON · API") >= 2));
    }

    @Test
    void publishesTheJourneyAggregateAsOneCollapsedInspectableRequest() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-network-aggregate-");
        Path report;
        var request = new ReportRequest("GET", "/users/me", Map.of(),
                Map.of("Accept", "application/json"), "none", "");
        var response = new ReportResponse(401, "{\"message\":\"Unauthorized\"}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(4),
                "https://api.example.test/users/me");
        var evidence = new ReportExecutionEvidence(response, 1, Duration.ofMillis(4), List.of(),
                List.of(new ReportAttemptRecord(1, 401, Duration.ofMillis(4), "FAILURE", "guest branch")),
                request, ReportTransportProvenance.LIVE_HTTP);
        var step = ReportNetworkInspector.Step.from("GIVEN", "guest bootstrap is observed",
                List.of(new AtfAssertion("Guest branch", "401", "401", true)), List.of(evidence));
        var diagnostics = List.of(
                new ReportNetworkInspector.Diagnostic("response", "GET", "/users/me", 401, "HTTP 401"),
                new ReportNetworkInspector.Diagnostic("console", "GET", "/users/me", 401, "Unauthorized"));

        try (var session = UiExtentReportSession.openConfigured(root, "ui-network-aggregate.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "journey network aggregate", List.of("ui", "api"));
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.networkInspector("API network inspector — ordered UI journey",
                    List.of(evidence), List.of(step), diagnostics);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report).toLowerCase();
        assertAll(
                () -> assertTrue(html.contains("api network inspector")),
                () -> assertTrue(html.contains("authentication evidence · 1")),
                () -> assertTrue(html.contains("scenario correlation · 1")),
                () -> assertTrue(html.contains("transport counters · 1")),
                () -> assertTrue(html.contains("unique request signatures · 1")),
                () -> assertTrue(html.contains("technical diagnostics · 1")),
                () -> assertFalse(html.contains("network.aggregate.authentication")),
                () -> assertTrue(html.contains("[response, console]")),
                () -> assertFalse(html.contains("unauthorized bootstrap")));
    }
}


