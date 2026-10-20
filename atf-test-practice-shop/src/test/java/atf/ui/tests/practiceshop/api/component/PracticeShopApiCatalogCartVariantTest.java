package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.api.PracticeShopProductQuery;
import atf.ui.practiceshop.api.PracticeShopProductSort;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component matrix for catalog variation and cart mutation boundaries. */
@AtfComponentTest
class PracticeShopApiCatalogCartVariantTest {
    private final PracticeShopApiRequestFactory requests = new PracticeShopApiRequestFactory();

    @Test
    void categorySortAndOutOfStockRemainObservableWithoutASecondE2e() throws Exception {
        PracticeShopApiClient client = PracticeShopApiClient.forTransport(
                URI.create("https://api.practicesoftwaretesting.com"), transport());

        var filtered = client.execute(client.requests().products(
                new PracticeShopProductQuery(null, "category-1", false, null,
                        PracticeShopProductSort.PRICE_ASC, 1)));
        var categories = client.execute(client.requests().categories());
        var outOfStock = client.execute(client.requests().product("out-of-stock", ""));

        assertEquals(200, filtered.response().status());
        assertTrue(filtered.response().body().contains("category-1"));
        assertEquals(200, categories.response().status());
        assertTrue(categories.response().body().contains("category-1"));
        assertEquals(200, outOfStock.response().status());
        assertFalse(com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(outOfStock.response().body()).path("in_stock").asBoolean(true));
    }

    @Test
    void cartQuantityUpdateAndProductRemovalPreserveMutationContracts() {
        PracticeShopApiClient client = PracticeShopApiClient.forTransport(
                URI.create("https://api.practicesoftwaretesting.com"), transport());

        var updated = client.execute(requests.updateCartQuantity("cart-1", "product-1", 3));
        var removed = client.execute(requests.removeFromCart("cart-1", "product-1"));

        assertEquals(200, updated.response().status());
        assertEquals("PUT", updated.request().method());
        assertTrue(updated.request().bodyText().contains("\"quantity\":3"));
        assertEquals(204, removed.response().status());
        assertEquals("DELETE", removed.request().method());
    }

    private static ApiTransport transport() {
        return (request, url, auth) -> {
            int status = 200;
            String body;
            if (request.path().equals(PracticeShopApiRoutes.PRODUCT_COLLECTION)) {
                body = "{\"data\":[{\"id\":\"oos\",\"name\":\"Out of stock\",\"in_stock\":false}],\"category\":\"category-1\"}";
            } else if (request.path().equals(PracticeShopApiRoutes.CATEGORIES)) {
                body = "[{\"id\":\"category-1\",\"name\":\"Hand Tools\"}]";
            } else if (request.path().equals(PracticeShopApiRoutes.product("out-of-stock"))) {
                body = "{\"id\":\"out-of-stock\",\"name\":\"Out of stock\",\"in_stock\":false}";
            } else if (request.method().equals("PUT") && request.path().endsWith("/product/quantity")) {
                body = "{\"result\":\"item updated\"}";
            } else if (request.method().equals("DELETE") && request.path().contains("/product/")) {
                status = 204;
                body = "";
            } else {
                status = 500;
                body = "{\"message\":\"unexpected route\"}";
            }
            return CompletableFuture.completedFuture(new ApiResponse(status, body,
                    Map.of("content-type", "application/json"), Duration.ZERO, url));
        };
    }
}
