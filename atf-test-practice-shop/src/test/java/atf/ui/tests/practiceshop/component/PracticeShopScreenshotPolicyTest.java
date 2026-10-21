package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.ui.practiceshop.support.PracticeShopScreenshotPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Component contract for the Practice Shop checkpoint screenshot policy. */
@AtfComponentTest
class PracticeShopScreenshotPolicyTest {
    @Test
    void mapsTheLegacySwitchToOneBrowserProfile() {
        var allCheckpoints = PracticeShopScreenshotPolicy.profile(true);
        var failuresOnly = PracticeShopScreenshotPolicy.profile(false);

        assertEquals(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS, allCheckpoints.flowMode());
        assertEquals(ReportEvidenceCaptureSource.BROWSER, failuresOnly.captureSource());
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT, allCheckpoints.screenshotPolicy());
        assertEquals(ReportScreenshotPolicy.FAILURE_ONLY, failuresOnly.screenshotPolicy());
    }
}
