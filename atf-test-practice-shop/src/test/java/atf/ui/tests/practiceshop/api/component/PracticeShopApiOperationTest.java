package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.request.HttpMethod;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiOperation;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiOperationTest {
    @Test
    void resolvesStableIntentWithoutPuttingDynamicIdsIntoTheEnum() {
        var product = PracticeShopApiOperation.resolve(HttpMethod.GET.value(),
                PracticeShopApiRoutes.product("product-42"));
        var cart = PracticeShopApiOperation.resolve(HttpMethod.POST.value(),
                PracticeShopApiRoutes.cart("cart-7"));

        assertEquals(PracticeShopApiOperation.PRODUCT_DETAIL, product);
        assertEquals(PracticeShopApiOperation.CART_ADD, cart);
        assertTrue(product.routeTemplate().contains("{productId}"));
        assertTrue(cart.routeTemplate().contains("{cartId}"));
    }

    @Test
    void reportTitleContainsProtocolRouteAndShortHumanIntent() {
        String title = PracticeShopApiOperation.reportTitle(
                HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK);

        assertEquals("POST /payment/check — validate payment selection", title);
    }
}
