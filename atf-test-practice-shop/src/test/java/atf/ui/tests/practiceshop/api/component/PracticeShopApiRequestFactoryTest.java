package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.request.ApiRequest;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.practiceshop.api.PracticeShopProductQuery;
import atf.ui.practiceshop.api.PracticeShopProductSort;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiRequestFactoryTest {
    private final PracticeShopApiRequestFactory requests = new PracticeShopApiRequestFactory();

    @Test
    void loginIsAJsonWriteWithNoAuthenticationPrecondition() {
        ApiRequest request = requests.login(new PracticeShopCredentials(
                "shop@example.test", "secret", "ATF", "Shop"));

        assertEquals("POST", request.method());
        assertEquals(PracticeShopApiRoutes.LOGIN, request.path());
        assertEquals("none", request.authProfile());
        assertEquals("application/json", request.headers().get("Content-Type"));
        assertTrue(request.bodyText().contains("\"email\":\"shop@example.test\""));
        assertTrue(request.bodyText().contains("\"password\":\"secret\""));
    }

    @ParameterizedTest
    @EnumSource(PracticeShopPaymentMethod.class)
    void paymentVariantsRemainJsonAndUseTheConfiguredWireValue(PracticeShopPaymentMethod method) {
        ApiRequest request = requests.payment(method);

        assertEquals("POST", request.method());
        assertEquals(PracticeShopApiRoutes.PAYMENT_CHECK, request.path());
        assertEquals(method.wireValue(),
                request.bodyText().replaceAll(".*\\\"payment_method\\\":\\\"([^\\\"]+)\\\".*", "$1"));
        assertEquals("application/json", request.headers().get("Content-Type"));
    }

    @Test
    void authenticatedScreenRequestsCarryBearerButAnonymousRequestsDoNot() {
        ApiRequest anonymous = requests.currentUser("");
        ApiRequest authenticated = requests.currentUser("token-value");

        assertEquals(Map.of(), anonymous.headers());
        assertEquals("Bearer token-value", authenticated.headers().get("Authorization"));
        assertEquals("bearer", authenticated.authProfile());
    }

    @Test
    void invalidQuantityFailsBeforeAWriteCanReachTheCart() {
        assertThrows(IllegalArgumentException.class,
                () -> requests.addToCart("cart-1", "product-1", 0));
    }

    @Test
    void queryVariantsAreKeptTypedAndEncodedByTheCoreUrlBuilder() {
        ApiRequest request = requests.postcodeLookup("RO", "010101", "10");

        assertEquals("GET", request.method());
        assertEquals("RO", request.query().get("country"));
        assertEquals("010101", request.query().get("postcode"));
        assertEquals("10", request.query().get("house_number"));
    }

    @Test
    void catalogFiltersAreTypedAndKeepTheDocumentedQueryVocabulary() {
        ApiRequest request = requests.products(new PracticeShopProductQuery(
                "brand-1", "category-1", false, "price,10,30",
                PracticeShopProductSort.PRICE_ASC, 2));

        assertEquals("GET", request.method());
        assertEquals(PracticeShopApiRoutes.PRODUCT_COLLECTION, request.path());
        assertEquals("brand-1", request.query().get("by_brand"));
        assertEquals("category-1", request.query().get("by_category"));
        assertEquals("false", request.query().get("is_rental"));
        assertEquals("price,10,30", request.query().get("between"));
        assertEquals("price,asc", request.query().get("sort"));
        assertEquals("2", request.query().get("page"));
    }

    @Test
    void cartUpdateIsPutJsonAndRemoveHasAnExplicitProductRoute() {
        ApiRequest update = requests.updateCartQuantity("cart-1", "product-1", 3);
        ApiRequest remove = requests.removeFromCart("cart-1", "product-1");

        assertEquals("PUT", update.method());
        assertEquals(PracticeShopApiRoutes.updateCartQuantity("cart-1"), update.path());
        assertEquals("application/json", update.headers().get("Content-Type"));
        assertTrue(update.bodyText().contains("\"product_id\":\"product-1\""));
        assertTrue(update.bodyText().contains("\"quantity\":3"));
        assertEquals("DELETE", remove.method());
        assertEquals(PracticeShopApiRoutes.removeCartProduct("cart-1", "product-1"), remove.path());
        assertEquals("", remove.bodyText());
    }
}
