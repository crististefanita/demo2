package atf.api.reporting.ui.audit.result;
import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.audit.ReportAuditChecks;
import atf.api.reporting.audit.ValidationResult;

import atf.api.reporting.ui.audit.plan.ExtentJourney;
import atf.api.reporting.ui.audit.plan.ExtentSection;
import atf.api.reporting.ui.audit.plan.ExtentSectionCheck;
import atf.api.reporting.ui.audit.result.SectionAuditResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
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
