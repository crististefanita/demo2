package atf.ui.tests.extent.audit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryFilterAuditorTest {
    @Test
    void buildsStableIndexedSelectorsForDiscoveredCategories() {
        CategoryFilterAuditor auditor = new CategoryFilterAuditor(null);

        assertEquals("#category-view .test-list .test-item:nth-of-type(1)", auditor.selector(0));
        assertEquals("#category-view .test-list .test-item:nth-of-type(7)", auditor.selector(6));
    }

    @Test
    void rejectsAnInvalidCategoryIndexBeforeBuildingASelector() {
        CategoryFilterAuditor auditor = new CategoryFilterAuditor(null);

        assertThrows(IllegalArgumentException.class, () -> auditor.selector(-1));
    }

    @Test
    void categoryLinkContractCoversZeroOneAndMany() {
        assertTrue(CategoryFilterAuditor.validateLinkedScenarioIds(0, List.of()).valid());
        assertTrue(CategoryFilterAuditor.validateLinkedScenarioIds(1, List.of("12")).valid());
        assertTrue(CategoryFilterAuditor.validateLinkedScenarioIds(3, List.of("12", "13", "14")).valid());
    }

    @Test
    void categoryLinkContractRejectsWrongCardinalityAndMalformedIds() {
        assertFalse(CategoryFilterAuditor.validateLinkedScenarioIds(1, List.of()).valid());
        assertFalse(CategoryFilterAuditor.validateLinkedScenarioIds(2, List.of("12", "bad")).valid());
        assertFalse(CategoryFilterAuditor.validateLinkedScenarioIds(-1, List.of()).valid());
    }
}
