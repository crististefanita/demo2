package atf.ui.tests.extent.section;

import atf.ui.tests.extent.section.plan.ExtentJourney;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.ExtentSectionCheck;
import atf.ui.tests.extent.section.result.SectionAuditResult;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionAuditResultTest {
    @Test
    void preservesDeclarativeChecksAndRendererNeutralVerdict() {
        var result = new SectionAuditResult(ExtentSection.TESTS,
                Set.of(ExtentSectionCheck.STRUCTURE, ExtentSectionCheck.MOCK_RECONCILIATION),
                Set.of(ExtentSectionCheck.STRUCTURE), ExtentJourney.NONE,
                ValidationResult.of(List.of(ReportAuditChecks.check("Owners", "> 0", "1", true))),
                List.of("Tests DOM snapshot"));

        assertTrue(result.valid());
        assertEquals(Set.of(ExtentSectionCheck.STRUCTURE), result.executedChecks());
        assertEquals("Tests DOM snapshot", result.evidence().getFirst());
    }

    @Test
    void rejectsAnExecutedCheckThatWasNotDeclared() {
        assertThrows(IllegalArgumentException.class, () -> new SectionAuditResult(
                ExtentSection.FAILS,
                Set.of(ExtentSectionCheck.STRUCTURE),
                Set.of(ExtentSectionCheck.CROSS_SECTION),
                ExtentJourney.FAILURE_NAVIGATION,
                ValidationResult.empty(), List.of()));
    }
}
