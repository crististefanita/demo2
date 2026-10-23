package atf.ui.tests.practiceshop.api.unit.reporting;

import atf.api.testkit.AtfUnitTest;
import atf.ui.practiceshop.support.api.ShopApiBodyEvidence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
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
