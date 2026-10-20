package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopScreenshotPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component contract for the Practice Shop checkpoint screenshot policy. */
@AtfComponentTest
class PracticeShopScreenshotPolicyTest {
    @Test
    void capturesPassedCheckpointOnlyWhenPolicyRequestsIt() {
        assertTrue(PracticeShopScreenshotPolicy.shouldCapture(true, true));
        assertFalse(PracticeShopScreenshotPolicy.shouldCapture(true, false));
    }

    @Test
    void capturesFailedCheckpointRegardlessOfConfiguredPassPolicy() {
        assertTrue(PracticeShopScreenshotPolicy.shouldCapture(false, true));
        assertTrue(PracticeShopScreenshotPolicy.shouldCapture(false, false));
    }
}
