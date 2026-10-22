package atf.ui.tests.practiceshop.component.catalog;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopUiRoutesTest {
    @Test
    void buildsOnlyRelativeUiRoutes() {
        assertEquals("/product/1", PracticeShopUiRoutes.product("1"));
        assertEquals("/categories/pliers", PracticeShopUiRoutes.category("pliers"));
        assertEquals("/checkout", PracticeShopUiRoutes.CHECKOUT);
    }

    @Test
    void rejectsMissingDynamicRouteSegments() {
        assertThrows(IllegalArgumentException.class, () -> PracticeShopUiRoutes.product(" "));
        assertThrows(IllegalArgumentException.class, () -> PracticeShopUiRoutes.category(null));
    }
}
