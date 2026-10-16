package atf.ui.tests.extent.plan;

import atf.ui.tests.extent.section.plan.ExtentSection;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the explicit bridge from historical rule names to section modules. */
class UiReportAuditRuleAliasTest {
    @Test
    void sectionRulesHaveCanonicalAliases() {
        assertEquals(Set.of(ExtentSection.OVERVIEW),
                UiReportAuditRule.OVERVIEW.canonicalSections());
        assertEquals(Set.of(ExtentSection.TESTS),
                UiReportAuditRule.REQUEST_CONTRACT.canonicalSections());
        assertEquals(Set.of(ExtentSection.FAILS),
                UiReportAuditRule.FAILURE_FLOW.canonicalSections());
        assertTrue(UiReportAuditRule.STYLE.isCanonicalAlias());
    }

    @Test
    void compatibilityOnlyRulesRemainExplicitlyVisible() {
        assertFalse(UiReportAuditRule.SETUP.isCanonicalAlias());
        assertFalse(UiReportAuditRule.FAILURE_DETAILS.isCanonicalAlias());
    }
}
