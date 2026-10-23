package atf.ui.tests.autoex.component;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.testkit.AtfComponentTest;
import atf.api.testkit.evidence.AtfUiEvidenceFlow;
import atf.api.testkit.evidence.AtfUiScreenshotPolicy;
import atf.ui.autoex.support.AutoExEvidenceProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Guards AutoEx flow composition and screenshot-policy mapping without a live site or browser. */
@AtfComponentTest
class AutoExEvidenceProfileComponentTest {
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
}
