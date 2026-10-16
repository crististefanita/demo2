package atf.ui.tests.extent.section;

import atf.ui.tests.extent.support.UiTestData;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.validate.TestsSectionValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure contract tests prove the validator is independent of browser rendering. */
class TestsSectionValidatorTest {
    private final TestsSectionValidator validator = new TestsSectionValidator();

    @Test
    void acceptsACompleteSnapshotForExpectedOwners() {
        var snapshot = new ReportAuditSnapshot.Tests(1, 2, 2, 4, 4, List.of(UiTestData.LIFECYCLE_OWNER));
        assertTrue(validator.validate(snapshot, List.of(UiTestData.LIFECYCLE_OWNER)).valid());
    }

    @Test
    void rejectsAnIncompleteSnapshotWithoutOpeningAReport() {
        var snapshot = new ReportAuditSnapshot.Tests(1, 0, 0, 0, 0, List.of(UiTestData.LIFECYCLE_OWNER));
        assertFalse(validator.validate(snapshot, List.of(UiTestData.LIFECYCLE_OWNER)).valid());
    }
}

