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
        var order = ShopApiOperation.resolve(
                HttpMethod.POST.value(), ShopApiRoutes.GUEST_INVOICE_CREATE);

        assertEquals(ShopApiOperation.PRODUCT_DETAIL, product);
        assertEquals(ShopApiOperation.CART_ADD, cart);
        assertEquals(ShopApiOperation.ORDER_CREATE, order);
        assertTrue(product.routeTemplate().contains("{productId}"));
        assertTrue(cart.routeTemplate().contains("{cartId}"));
    }

    @Test
    void reportTitleContainsProtocolRouteAndShortHumanIntent() {
        String title = ShopApiOperation.reportTitle(
                HttpMethod.POST.value(), ShopApiRoutes.PAYMENT_CHECK);

        assertEquals("POST /payment/check — validate payment selection", title);
    }

    @Test
    void treatsObservedQueryAsLegacySearchAliasButReportsCanonicalGet() {
        assertEquals(ShopApiOperation.SEARCH,
                ShopApiOperation.resolve("QUERY", ShopApiRoutes.PRODUCT_SEARCH));
        assertEquals("GET /products/search — find configured product",
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
