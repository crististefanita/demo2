package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.locators.PracticeShopProductSelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopProductSelectorTest {
    @Test
    void selectsTheExactProductNameWithinTheProductCard() {
        assertEquals(
                "a[data-test^='product-']:has([data-test='product-name']:text-is('Pliers'))",
                PracticeShopProductSelector.exactProductCard("Pliers"));
    }

    @Test
    void escapesDynamicProductNamesWithoutFallingBackToPartialText() {
        assertEquals(
                "a[data-test^='product-']:has([data-test='product-name']:text-is('Pliers\\' \\\\ Pro'))",
                PracticeShopProductSelector.exactProductCard("Pliers' \\ Pro"));
    }

    @Test
    void rejectsBlankProductNamesBeforeBuildingASelector() {
        assertThrows(NullPointerException.class,
                () -> PracticeShopProductSelector.exactProductCard(null));
        assertThrows(IllegalArgumentException.class,
                () -> PracticeShopProductSelector.exactProductCard("  "));
    }
}
