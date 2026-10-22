package atf.ui.tests.practiceshop.component.catalog;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.routes.ShopUiRoutes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class ShopUiRoutesTest {
    @Test
    void buildsOnlyRelativeUiRoutes() {
        assertEquals("/product/1", ShopUiRoutes.product("1"));
        assertEquals("/categories/pliers", ShopUiRoutes.category("pliers"));
        assertEquals("/checkout", ShopUiRoutes.CHECKOUT);
    }

    @Test
    void rejectsMissingDynamicRouteSegments() {
        assertThrows(IllegalArgumentException.class, () -> ShopUiRoutes.product(" "));
        assertThrows(IllegalArgumentException.class, () -> ShopUiRoutes.category(null));
    }
}
