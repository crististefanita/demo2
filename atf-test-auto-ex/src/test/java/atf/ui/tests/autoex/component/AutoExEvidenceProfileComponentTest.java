package atf.ui.tests.autoex.component;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.testkit.AtfComponentTest;
import atf.api.testkit.evidence.AtfUiEvidence;
import atf.api.testkit.evidence.AtfUiEvidenceFlow;
import atf.api.testkit.evidence.AtfUiScreenshotPolicy;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.core.ui.AtfUiEvidenceSource;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.autoex.support.AutoExScreenAuditObservations;
import atf.ui.autoex.support.AutoExEvidenceProfile;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiFinalScreenshotE2eTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards AutoEx flow composition and screenshot-policy mapping without a live site or browser. */
@AtfComponentTest
class AutoExEvidenceProfileComponentTest {
    private static final String CONFIRMATION_URL = "https://example.test/confirmation";

    @Test
    void annotationOverridesOnlyDeclaredAxesAndConfiguredAxesRemainConfigured() {
        var configured = AutoExEvidenceProfile.forRun(AtfUiEvidenceFlow.CONFIGURED,
                AtfUiScreenshotPolicy.CONFIGURED, ReportScreenshotPolicy.FINAL_AND_FAILURE);
        var unifiedWithConfiguredScreenshots = AutoExEvidenceProfile.forRun(
                AtfUiEvidenceFlow.UNIFIED_FLOW, AtfUiScreenshotPolicy.CONFIGURED,
                ReportScreenshotPolicy.NONE);
        var finalOnly = AutoExEvidenceProfile.forRun(AtfUiEvidenceFlow.UNIFIED_FLOW,
                AtfUiScreenshotPolicy.FINAL_ONLY, ReportScreenshotPolicy.EACH_CHECKPOINT);

        assertAll(
                () -> assertEquals(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS,
                        configured.flowMode()),
                () -> assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW,
                        unifiedWithConfiguredScreenshots.flowMode()),
                () -> assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, finalOnly.flowMode()),
                () -> assertEquals(ReportScreenshotPolicy.FINAL_AND_FAILURE,
                        configured.screenshotPolicy()),
                () -> assertEquals(ReportScreenshotPolicy.NONE,
                        unifiedWithConfiguredScreenshots.screenshotPolicy()),
                () -> assertEquals(ReportScreenshotPolicy.FINAL_ONLY,
                        finalOnly.screenshotPolicy()),
                () -> assertEquals(ReportEvidenceCaptureSource.BROWSER,
                        finalOnly.captureSource()));
    }

    @Test
    void singleFinalScreenshotFlowAlsoCapturesFailureEvidence() {
        AtfUiEvidence annotation = AutoExPurchaseUiFinalScreenshotE2eTest.class
                .getAnnotation(AtfUiEvidence.class);

        assertEquals(AtfUiScreenshotPolicy.FINAL_AND_FAILURE, annotation.screenshots());
        var profile = AutoExEvidenceProfile.forRun(annotation.flow(), annotation.screenshots(),
                ReportScreenshotPolicy.NONE);
        assertAll(
                () -> assertTrue(profile.captures(ReportScreenshotPolicy.CapturePoint.FINAL)),
                () -> assertTrue(profile.captures(ReportScreenshotPolicy.CapturePoint.FAILURE)),
                () -> assertFalse(profile.captures(ReportScreenshotPolicy.CapturePoint.CHECKPOINT)));
    }

    @Test
    void terminalScreenAuditIsRecordedOnceBeforeTheActionSequenceIsDrained() {
        PlaywrightActionSequence actions = new PlaywrightActionSequence();
        actions.observation("Prior UI evidence", "#checkout", "ready", "ready");

        AutoExScreenAuditObservations.recordTerminalScreenContract(actions,
                "purchase confirmation is displayed", CONFIRMATION_URL,
                List.of(new AtfAssertion("Confirmation", "visible", "visible", true)),
                true, true);
        var evidence = actions.drain();

        assertEquals(2, evidence.size());
        assertEquals("Prior UI evidence", evidence.get(0).operation());
        assertEquals(AtfUiEvidenceSource.OBSERVE, evidence.get(1).source());
        assertEquals("Audit screen contract", evidence.get(1).operation());
        assertTrue(evidence.get(1).observed().contains("screenshot=captured"));
        assertTrue(actions.snapshot().isEmpty());
    }
}
