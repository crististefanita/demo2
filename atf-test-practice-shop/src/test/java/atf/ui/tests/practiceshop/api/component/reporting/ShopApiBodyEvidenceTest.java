package atf.ui.tests.practiceshop.api.component.reporting;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.ShopApiBodyEvidence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopApiBodyEvidenceTest {
    @Test
    void unavailableBrowserBodyContainsTypedBoundedDiagnostic() {
        String evidence = ShopApiBodyEvidence.unavailable(
                "Chromium response body", new IllegalStateException("payload released"));

        assertTrue(ShopApiBodyEvidence.isUnavailable(evidence));
        assertTrue(evidence.contains("IllegalStateException"));
        assertTrue(evidence.contains("payload released"));
        assertFalse(evidence.isBlank());
    }
}
