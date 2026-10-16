package atf.ui.tests.extent.section;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.validate.FailsSectionValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure tests for both Fails projection branches. */
class FailsSectionValidatorTest {
    private final FailsSectionValidator validator = new FailsSectionValidator();

    @Test
    void acceptsActionableFailureRows() {
        assertTrue(validator.validate(new ReportAuditSnapshot.Failures(2, 2, 2, false)).valid());
    }

    @Test
    void acceptsAnHonestEmptyState() {
        assertTrue(validator.validate(new ReportAuditSnapshot.Failures(0, 0, 0, true)).valid());
        assertFalse(validator.validate(new ReportAuditSnapshot.Failures(0, 1, 0, true)).valid());
    }
}

