package atf.ui.autoex.support;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Guards checkpoint layout independently from the live site and browser. */
@AtfComponentTest
class AutoExEvidenceProfileComponentTest {
    @Test
    void everyEvidenceVariantKeepsSeparateStepsAndSelectsItsScreenshotPolicy() {
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
                () -> assertEquals(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS, finalOnly.flowMode()),
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
