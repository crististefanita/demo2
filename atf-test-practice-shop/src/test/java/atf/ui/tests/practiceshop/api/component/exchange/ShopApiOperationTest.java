package atf.ui.tests.practiceshop.api.component.exchange;

import atf.api.core.http.request.HttpMethod;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.ShopApiOperation;
import atf.ui.practiceshop.api.ShopApiRoutes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopApiOperationTest {
    @Test
    void resolvesStableIntentWithoutPuttingDynamicIdsIntoTheEnum() {
        var product = ShopApiOperation.resolve(HttpMethod.GET.value(),
                ShopApiRoutes.product("product-42"));
        var cart = ShopApiOperation.resolve(HttpMethod.POST.value(),
                ShopApiRoutes.cart("cart-7"));
        var update = ShopApiOperation.resolve(HttpMethod.PUT.value(),
                ShopApiRoutes.updateCartQuantity("cart-7"));
        var remove = ShopApiOperation.resolve(HttpMethod.DELETE.value(),
                ShopApiRoutes.removeCartProduct("cart-7", "product-42"));
        var order = ShopApiOperation.resolve(
                HttpMethod.POST.value(), ShopApiRoutes.GUEST_INVOICE_CREATE);
        var registration = ShopApiOperation.resolve(
                HttpMethod.POST.value(), ShopApiRoutes.USER_REGISTER);

        assertEquals(ShopApiOperation.PRODUCT_DETAIL, product);
        assertEquals(ShopApiOperation.CART_ADD, cart);
        assertEquals(ShopApiOperation.CART_UPDATE, update);
        assertEquals(ShopApiOperation.CART_REMOVE, remove);
        assertEquals(ShopApiOperation.ORDER_CREATE, order);
        assertEquals(ShopApiOperation.USER_REGISTER, registration);
        assertTrue(product.routeTemplate().contains("{productId}"));
        assertTrue(cart.routeTemplate().contains("{cartId}"));
        assertTrue(update.routeTemplate().contains("{cartId}"));
        assertTrue(remove.routeTemplate().contains("{productId}"));
    }

    @Test
    void reportTitleContainsProtocolRouteAndShortHumanIntent() {
        String title = ShopApiOperation.reportTitle(
                HttpMethod.POST.value(), ShopApiRoutes.PAYMENT_CHECK);

        assertEquals("POST /payment/check — validate payment selection", title);
    }

    @Test
    void classifiesObservedQueryAsSearchWithoutChangingItsCapturedMethod() {
        assertEquals(ShopApiOperation.SEARCH,
                ShopApiOperation.resolve("QUERY", ShopApiRoutes.PRODUCT_SEARCH));
        assertEquals("QUERY /products/search — find configured product",
                ShopApiOperation.reportTitle("QUERY", ShopApiRoutes.PRODUCT_SEARCH));
    }

    @Test
    void labelsOrderCreationAsTheBusinessMutation() {
        assertEquals("POST /invoices/guest — create order",
                ShopApiOperation.reportTitle(
                        HttpMethod.POST.value(), ShopApiRoutes.GUEST_INVOICE_CREATE));
        assertEquals("POST /invoices — create order",
                ShopApiOperation.reportTitle(
                        HttpMethod.POST.value(), ShopApiRoutes.INVOICE_CREATE));
    }
}
