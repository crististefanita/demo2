package atf.ui.tests.autoex.component;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.AutoExEvidenceMode;
import atf.ui.autoex.support.AutoExEvidenceProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Guards AutoEx flow composition and screenshot-policy mapping without a live site or browser. */
@AtfComponentTest
class AutoExEvidenceProfileComponentTest {
    @Test
    void evidenceModesSelectTheExpectedFlowAndScreenshotPolicy() {
        var configured = AutoExEvidenceProfile.forRun(AutoExEvidenceMode.Mode.CONFIGURED,
                ReportScreenshotPolicy.FINAL_AND_FAILURE);
        var checkpointScreenshots = AutoExEvidenceProfile.forRun(
                AutoExEvidenceMode.Mode.CHECKPOINT_SCREENSHOTS, ReportScreenshotPolicy.NONE);
        var finalOnly = AutoExEvidenceProfile.forRun(
                AutoExEvidenceMode.Mode.FINAL_SCREENSHOT_ONLY, ReportScreenshotPolicy.EACH_CHECKPOINT);

        assertAll(
                () -> assertEquals(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS,
                        configured.flowMode()),
                () -> assertEquals(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS,
                        checkpointScreenshots.flowMode()),
                () -> assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, finalOnly.flowMode()),
                () -> assertEquals(ReportScreenshotPolicy.FINAL_AND_FAILURE,
                        configured.screenshotPolicy()),
                () -> assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT,
                        checkpointScreenshots.screenshotPolicy()),
                () -> assertEquals(ReportScreenshotPolicy.FINAL_ONLY,
                        finalOnly.screenshotPolicy()),
                () -> assertEquals(ReportEvidenceCaptureSource.BROWSER,
                        finalOnly.captureSource()));
    }
}
