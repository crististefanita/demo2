package atf.api.reporting.ui.profile;

import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureProvider;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.model.ReportScreenshot;
import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.support.ReportTestScreenshots;
import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.testkit.AtfIntegrationTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Proves the typed consumer profile crosses the session boundary without a browser. */
@AtfIntegrationTest
class ReportEvidenceProfileIntegrationTest {
    @Test
    void unifiedFlowPublishesOneBusinessNodeWithNestedEvidenceAndOnlyTheFinalScreenshot()
            throws Exception {
        Path root = Files.createTempDirectory("atf-evidence-profile-");
        ReportEvidenceProfile profile = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.UNIFIED_FLOW)
                .screenshotPolicy(ReportScreenshotPolicy.FINAL_ONLY)
                .captureSource(ReportEvidenceCaptureSource.DETERMINISTIC_MOCK)
                .build();

        Path report;
        ReportEvidenceCaptureProvider provider = new ReportEvidenceCaptureProvider() {
            @Override public ReportEvidenceCaptureSource source() {
                return ReportEvidenceCaptureSource.DETERMINISTIC_MOCK;
            }
            @Override public ReportScreenshot capture(String title, String target) {
                return new ReportScreenshot(title, ReportTestScreenshots.SECOND_PIXEL_PNG);
            }
        };
        try (var session = UiExtentReportSession.openConfigured(root, "profile.html",
                AtfConsoleSink.noOp(), profile)) {
            assertEquals(profile, session.evidenceProfile());
            var scenario = session.startScenario("ProfileTest", "atf.api.reporting.ui",
                    "typed checkout", List.of("evidence", "component"));
            var body = scenario.startPhase("TEST BODY");
            var apiEvidence = paymentEvidence();
            body.step(AtfGherkinKeyword.GIVEN, "payment is ready", "#checkout",
                    ReportTestScreenshots.ONE_PIXEL_PNG,
                    List.of(new AtfAssertion("Payment form", "visible", "visible", true)),
                    List.of(new AtfUiActionRecord(1, "Get text", "#payment-title",
                            "payment heading", "Payment", 4, AtfReportStatus.PASS)),
                    List.of(apiEvidence), AtfReportStatus.PASS);
            var finalCapture = profile.capture(ReportScreenshotPolicy.CapturePoint.FINAL,
                    "confirmation is visible", "#confirmation", provider).orElseThrow();
            body.screenValidation(finalCapture.title(), "#confirmation", finalCapture.base64Png(),
                    List.of(new AtfAssertion("Confirmation", "visible", "visible", true)),
                    List.of(new AtfUiActionRecord(2, "Get text", "#confirmation",
                            "confirmation message", "Order confirmed", 5, AtfReportStatus.PASS)),
                    ReportScreenshotPolicy.CapturePoint.FINAL,
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
            report = session.reportPath();
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertEquals(1, count(html, "data-atf-evidence-type=\"unified-ui-flow\"")),
                () -> assertTrue(html.contains("UNIFIED FLOW — typed checkout")),
                () -> assertTrue(html.contains("flow.checkpoint.count")),
                () -> assertTrue(html.contains("Checkpoint 1 — GIVEN payment is ready")),
                () -> assertTrue(html.contains("Checkpoint 2 — THEN confirmation is visible")),
                () -> assertTrue(html.contains("API validation — Checkpoint 1")),
                () -> assertTrue(html.contains("Exchange 1")),
                () -> assertTrue(html.contains("Get text")),
                () -> assertTrue(html.contains("Order confirmed")),
                () -> assertEquals(1, count(html, "data-atf-screenshot=\"base64\"")),
                () -> assertTrue(html.contains("href=\"data:image/png;base64,"
                        + ReportTestScreenshots.SECOND_PIXEL_PNG + "\"")),
                () -> assertFalse(html.contains("href=\"data:image/png;base64,"
                        + ReportTestScreenshots.ONE_PIXEL_PNG + "\"")));
    }

    @Test
    void screenCheckpointsKeepTheirExistingIndependentGherkinNodes() throws Exception {
        Path root = Files.createTempDirectory("atf-evidence-checkpoints-");
        ReportEvidenceProfile profile = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS)
                .screenshotPolicy(ReportScreenshotPolicy.EACH_CHECKPOINT)
                .captureSource(ReportEvidenceCaptureSource.DETERMINISTIC_MOCK)
                .build();
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "checkpoints.html",
                AtfConsoleSink.noOp(), profile)) {
            report = session.reportPath();
            var scenario = session.startScenario("ProfileTest", "atf.api.reporting.ui",
                    "independent checkpoints", List.of("evidence", "component"));
            var body = scenario.startPhase("TEST BODY");
            body.step(AtfGherkinKeyword.GIVEN, "the page is open", "#page", null,
                    List.of(new AtfAssertion("Page", "open", "open", true)),
                    AtfReportStatus.PASS);
            body.step(AtfGherkinKeyword.THEN, "the result is visible", "#result", null,
                    List.of(new AtfAssertion("Result", "visible", "visible", true)),
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertEquals(0, count(html, "data-atf-evidence-type=\"unified-ui-flow\"")),
                () -> assertTrue(html.contains("GIVEN the page is open")),
                () -> assertTrue(html.contains("THEN the result is visible")));
    }

    private static ReportExecutionEvidence paymentEvidence() {
        var request = new ReportRequest("POST", "/checkout", Map.of(),
                Map.of("Content-Type", "application/json"), "none", "{\"items\":[1]}");
        var response = new ReportResponse(201, "{\"id\":\"order-1\"}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(8),
                "https://atf.local/checkout");
        return new ReportExecutionEvidence(response, 1, Duration.ofMillis(8), List.of(),
                List.of(new ReportAttemptRecord(1, 201, Duration.ofMillis(8),
                        "SUCCESS", "created")), request, ReportTransportProvenance.ACCEPTANCE_MOCK);
    }

    private static int count(String html, String fragment) {
        int count = 0;
        int from = 0;
        while ((from = html.indexOf(fragment, from)) >= 0) {
            count++;
            from += fragment.length();
        }
        return count;
    }
}
