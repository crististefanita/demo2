package atf.ui.tests.extent.section;

import atf.api.core.reporting.AtfAssertion;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.ExtentSectionCheck;
import atf.ui.tests.extent.section.validate.SectionAuditCheckPolicy;
import atf.ui.tests.extent.support.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionAuditCheckPolicyTest {
    @Test
    void executesOnlyChecksDeclaredForEvidence() {
        ValidationResult full = ValidationResult.of(List.of(
                new AtfAssertion("Evidence tables", "visible", "visible", true),
                new AtfAssertion("Request JSON", "JSON", "{}", true),
                new AtfAssertion("Response JSON", "JSON", "{}", true)));

        ValidationResult selected = SectionAuditCheckPolicy.select(ExtentSection.EVIDENCE,
                EnumSet.of(ExtentSectionCheck.JSON_CONTRACT), full);

        assertTrue(selected.valid());
        assertEquals(List.of("Request JSON", "Response JSON"),
                selected.assertions().stream().map(AtfAssertion::name).toList());
    }

    @Test
    void failedAssertionsRemainFailedAfterCheckProjection() {
        ValidationResult full = ValidationResult.of(List.of(
                new AtfAssertion("Inline screenshot", "canonical link", "missing", false)));

        ValidationResult selected = SectionAuditCheckPolicy.select(ExtentSection.SCREEN_VALIDATION,
                EnumSet.of(ExtentSectionCheck.SCREENSHOT_LINK), full);

        assertTrue(!selected.valid());
        assertEquals(1, selected.assertions().size());
    }

    @Test
    void unselectedFailedCheckDoesNotPoisonTheSelectedContract() {
        ValidationResult full = ValidationResult.of(List.of(
                new AtfAssertion("Category surface", "styled", "unstyled", false),
                new AtfAssertion("Request JSON", "JSON", "{}", true)));

        ValidationResult selected = SectionAuditCheckPolicy.select(ExtentSection.EVIDENCE,
                EnumSet.of(ExtentSectionCheck.JSON_CONTRACT), full);

        assertTrue(selected.valid());
        assertEquals(List.of("Request JSON"),
                selected.assertions().stream().map(AtfAssertion::name).toList());
    }
}
