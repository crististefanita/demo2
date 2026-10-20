package atf.ui.tests.practiceshop.api.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopApiRoutesTest {
    @Test
    void dynamicRoutesUseTheSameCanonicalPathVocabularyAsTheFactory() {
        assertEquals("/products/product-1", PracticeShopApiRoutes.product("product-1"));
        assertEquals("/carts/cart-1", PracticeShopApiRoutes.cart("cart-1"));
        assertEquals("/carts/cart-1/product/quantity", PracticeShopApiRoutes.updateCartQuantity("cart-1"));
        assertEquals("/carts/cart-1/product/product-1",
                PracticeShopApiRoutes.removeCartProduct("cart-1", "product-1"));
    }

    @Test
    void dynamicRouteArgumentsFailFastInsteadOfProducingMalformedPaths() {
        assertThrows(IllegalArgumentException.class, () -> PracticeShopApiRoutes.product(" "));
        assertThrows(IllegalArgumentException.class, () -> PracticeShopApiRoutes.cart(null));
        assertThrows(IllegalArgumentException.class,
                () -> PracticeShopApiRoutes.removeCartProduct("cart-1", ""));
    }
}
