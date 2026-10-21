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
        var order = PracticeShopApiOperation.resolve(
                HttpMethod.POST.value(), PracticeShopApiRoutes.GUEST_INVOICE_CREATE);

        assertEquals(PracticeShopApiOperation.PRODUCT_DETAIL, product);
        assertEquals(PracticeShopApiOperation.CART_ADD, cart);
        assertEquals(PracticeShopApiOperation.ORDER_CREATE, order);
        assertTrue(product.routeTemplate().contains("{productId}"));
        assertTrue(cart.routeTemplate().contains("{cartId}"));
    }

    @Test
    void reportTitleContainsProtocolRouteAndShortHumanIntent() {
        String title = PracticeShopApiOperation.reportTitle(
                HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK);

        assertEquals("POST /payment/check — validate payment selection", title);
    }

    @Test
    void treatsObservedQueryAsLegacySearchAliasButReportsCanonicalGet() {
        assertEquals(PracticeShopApiOperation.SEARCH,
                PracticeShopApiOperation.resolve("QUERY", PracticeShopApiRoutes.PRODUCT_SEARCH));
        assertEquals("GET /products/search — find configured product",
                PracticeShopApiOperation.reportTitle("QUERY", PracticeShopApiRoutes.PRODUCT_SEARCH));
    }

    @Test
    void labelsOrderCreationAsTheBusinessMutation() {
        assertEquals("POST /invoices/guest — create order",
                PracticeShopApiOperation.reportTitle(
                        HttpMethod.POST.value(), PracticeShopApiRoutes.GUEST_INVOICE_CREATE));
        assertEquals("POST /invoices — create order",
                PracticeShopApiOperation.reportTitle(
                        HttpMethod.POST.value(), PracticeShopApiRoutes.INVOICE_CREATE));
    }
}
