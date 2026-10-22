package atf.ui.tests.practiceshop.api.component.reporting;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopApiBodyEvidence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiBodyEvidenceTest {
    @Test
    void unavailableBrowserBodyContainsTypedBoundedDiagnostic() {
        String evidence = PracticeShopApiBodyEvidence.unavailable(
                "Chromium response body", new IllegalStateException("payload released"));

        assertTrue(PracticeShopApiBodyEvidence.isUnavailable(evidence));
        assertTrue(evidence.contains("IllegalStateException"));
        assertTrue(evidence.contains("payload released"));
        assertFalse(evidence.isBlank());
    }
}
