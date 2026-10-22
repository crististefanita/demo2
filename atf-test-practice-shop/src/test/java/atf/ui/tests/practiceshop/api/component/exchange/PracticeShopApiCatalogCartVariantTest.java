package atf.ui.tests.practiceshop.api.component.exchange;

import atf.api.core.http.request.HttpMethod;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopApiJsonField;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.api.PracticeShopProductQuery;
import atf.ui.practiceshop.api.PracticeShopProductSort;
import atf.ui.practiceshop.support.PracticeShopJsonEvidence;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiComponentFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component matrix for catalog variation and cart mutation boundaries. */
@AtfComponentTest
class PracticeShopApiCatalogCartVariantTest {
    @Test
    void categorySortAndOutOfStockRemainObservableWithoutASecondE2e() {
        PracticeShopApiClient client = PracticeShopApiComponentFixture.client();
        PracticeShopApiRequestFactory requests = client.requests();

        String categoryId = PracticeShopApiScenarioFixture.CATEGORY_ID;
        String outOfStockProductId = PracticeShopApiScenarioFixture.OUT_OF_STOCK_PRODUCT_ID;
        boolean rentalOnly = PracticeShopApiScenarioFixture.FILTER_RENTAL;
        int page = PracticeShopApiScenarioFixture.FIRST_PAGE;

        var filteredExecution = client.execute(requests.products(
                new PracticeShopProductQuery(null, categoryId, rentalOnly, null,
                        PracticeShopProductSort.PRICE_ASC, page)));
        var categoriesExecution = client.execute(requests.categories());
        var outOfStockExecution = client.execute(requests.product(outOfStockProductId, ""));

        var filteredResponse = filteredExecution.response();
        var categoriesResponse = categoriesExecution.response();
        var outOfStockResponse = outOfStockExecution.response();
        var filteredBody = PracticeShopJsonEvidence.parse(filteredResponse.body());
        var categoriesBody = PracticeShopJsonEvidence.parse(categoriesResponse.body());
        var outOfStockBody = PracticeShopJsonEvidence.parse(outOfStockResponse.body());

        assertTrue(PracticeShopApiHttpStatus.OK.matches(filteredResponse.status()));
        assertNotNull(filteredBody);
        assertEquals(categoryId, filteredBody.path(PracticeShopApiJsonField.CATEGORY.wireValue()).asText());
        assertTrue(filteredBody.path(PracticeShopApiJsonField.DATA.wireValue()).isArray());
        assertTrue(PracticeShopApiHttpStatus.OK.matches(categoriesResponse.status()));
        assertNotNull(categoriesBody);
        assertTrue(categoriesBody.findValuesAsText(PracticeShopApiJsonField.ID.wireValue()).contains(categoryId));
        assertTrue(PracticeShopApiHttpStatus.OK.matches(outOfStockResponse.status()));
        assertNotNull(outOfStockBody);
        assertFalse(outOfStockBody.path(PracticeShopApiJsonField.IN_STOCK.wireValue()).asBoolean(true));
    }

    @Test
    void cartUpdateAndRemovalExposeDistinctApiContracts() {
        PracticeShopApiClient client = PracticeShopApiComponentFixture.client();
        PracticeShopApiRequestFactory requests = client.requests();

        String cartId = PracticeShopApiScenarioFixture.MUTATION_CART_ID;
        String productId = PracticeShopApiScenarioFixture.MUTATION_PRODUCT_ID;
        int expectedQuantity = PracticeShopApiScenarioFixture.UPDATED_QUANTITY;

        var updateExecution = client.execute(
                requests.updateCartQuantity(cartId, productId, expectedQuantity));
        var removeExecution = client.execute(requests.removeFromCart(cartId, productId));

        var updateResponse = updateExecution.response();
        var updateRequest = updateExecution.request();
        var removeResponse = removeExecution.response();
        var removeRequest = removeExecution.request();
        var updateRequestBody = PracticeShopJsonEvidence.parse(updateRequest.bodyText());
        var updateResponseBody = PracticeShopJsonEvidence.parse(updateResponse.body());

        assertTrue(PracticeShopApiHttpStatus.OK.matches(updateResponse.status()));
        assertEquals(HttpMethod.PUT.value(), updateRequest.method());
        assertEquals(PracticeShopApiRoutes.updateCartQuantity(cartId), updateRequest.path());
        assertNotNull(updateRequestBody);
        assertEquals(productId,
                updateRequestBody.path(PracticeShopApiJsonField.PRODUCT_ID.wireValue()).asText());
        assertEquals(expectedQuantity,
                updateRequestBody.path(PracticeShopApiJsonField.QUANTITY.wireValue()).asInt());
        assertNotNull(updateResponseBody);
        assertEquals(PracticeShopApiScenarioFixture.CART_UPDATE_RESULT,
                updateResponseBody.path(PracticeShopApiJsonField.RESULT.wireValue()).asText());

        assertTrue(PracticeShopApiHttpStatus.NO_CONTENT.matches(removeResponse.status()));
        assertEquals(HttpMethod.DELETE.value(), removeRequest.method());
        assertEquals(PracticeShopApiRoutes.removeCartProduct(cartId, productId), removeRequest.path());
        assertEquals("", removeRequest.bodyText());
    }

    @Test
    void statefulCartLifecycleKeepsReadBackContractAcrossMutations() {
        PracticeShopApiClient client = PracticeShopApiComponentFixture.statefulCartClient();
        PracticeShopApiRequestFactory requests = client.requests();
        String cartId = PracticeShopApiScenarioFixture.MUTATION_CART_ID;
        String productId = PracticeShopApiScenarioFixture.MUTATION_PRODUCT_ID;

        var created = client.execute(requests.createCart());
        var added = client.execute(requests.addToCart(cartId, productId,
                PracticeShopApiScenarioFixture.QUANTITY));
        var afterAdd = client.execute(requests.readCart(cartId));
        var updated = client.execute(requests.updateCartQuantity(cartId, productId,
                PracticeShopApiScenarioFixture.UPDATED_QUANTITY));
        var afterUpdate = client.execute(requests.readCart(cartId));
        var removed = client.execute(requests.removeFromCart(cartId, productId));
        var afterRemove = client.execute(requests.readCart(cartId));

        assertEquals(PracticeShopApiHttpStatus.OK.code(), created.response().status());
        assertEquals(PracticeShopApiHttpStatus.OK.code(), added.response().status());
        assertEquals(PracticeShopApiHttpStatus.OK.code(), afterAdd.response().status());
        assertEquals(PracticeShopApiScenarioFixture.QUANTITY,
                quantity(afterAdd.response().body()));
        assertEquals(PracticeShopApiHttpStatus.OK.code(), updated.response().status());
        assertEquals(PracticeShopApiHttpStatus.OK.code(), afterUpdate.response().status());
        assertEquals(PracticeShopApiScenarioFixture.UPDATED_QUANTITY,
                quantity(afterUpdate.response().body()));
        assertEquals(PracticeShopApiHttpStatus.NO_CONTENT.code(), removed.response().status());
        assertEquals(PracticeShopApiHttpStatus.OK.code(), afterRemove.response().status());
        assertTrue(PracticeShopJsonEvidence.parse(afterRemove.response().body())
                .path(PracticeShopApiJsonField.CART_ITEMS.wireValue()).isEmpty());
    }

    private static int quantity(String body) {
        var root = PracticeShopJsonEvidence.parse(body);
        return root.path(PracticeShopApiJsonField.CART_ITEMS.wireValue()).get(0)
                .path(PracticeShopApiJsonField.QUANTITY.wireValue()).asInt();
    }
}
