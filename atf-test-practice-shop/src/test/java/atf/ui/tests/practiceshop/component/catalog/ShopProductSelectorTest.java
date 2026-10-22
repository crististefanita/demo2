package atf.ui.tests.practiceshop.component.catalog;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.locators.ShopProductSelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class ShopProductSelectorTest {
    @Test
    void selectsTheExactProductNameWithinTheProductCard() {
        assertEquals(
                "a[data-test^='product-']:has([data-test='product-name']:text-is('Pliers'))",
                ShopProductSelector.exactProductCard("Pliers"));
    }

    @Test
    void escapesDynamicProductNamesWithoutFallingBackToPartialText() {
        assertEquals(
                "a[data-test^='product-']:has([data-test='product-name']:text-is('Pliers\\' \\\\ Pro'))",
                ShopProductSelector.exactProductCard("Pliers' \\ Pro"));
    }

    @Test
    void rejectsBlankProductNamesBeforeBuildingASelector() {
        assertThrows(NullPointerException.class,
                () -> ShopProductSelector.exactProductCard(null));
        assertThrows(IllegalArgumentException.class,
                () -> ShopProductSelector.exactProductCard("  "));
    }
}
