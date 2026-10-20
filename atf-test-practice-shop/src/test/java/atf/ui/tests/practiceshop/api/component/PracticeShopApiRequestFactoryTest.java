package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiAuthProfile;
import atf.ui.practiceshop.api.PracticeShopApiJsonField;
import atf.ui.practiceshop.api.PracticeShopApiOperation;
import atf.ui.practiceshop.api.PracticeShopApiQueryParameter;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.practiceshop.api.PracticeShopProductQuery;
import atf.ui.practiceshop.api.PracticeShopProductSort;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.support.PracticeShopJsonEvidence;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiRequestFactoryTest {
    private final PracticeShopApiRequestFactory requests = new PracticeShopApiRequestFactory();

    @Test
    void loginIsAJsonWriteWithNoAuthenticationPrecondition() {
        PracticeShopCredentials credentials = PracticeShopApiScenarioFixture.validCredentials();
        ApiRequest request = requests.login(credentials);

        assertEquals(HttpMethod.POST.value(), request.method());
        assertEquals(PracticeShopApiRoutes.LOGIN, request.path());
        assertEquals(PracticeShopApiAuthProfile.NONE.wireValue(), request.authProfile());
        assertEquals(MediaTypes.APPLICATION_JSON, request.headers().get(HttpHeader.CONTENT_TYPE.value()));
        var body = PracticeShopJsonEvidence.parse(request.bodyText());
        assertNotNull(body);
        assertEquals(PracticeShopApiScenarioFixture.VALID_EMAIL,
                body.path(PracticeShopApiJsonField.EMAIL.wireValue()).asText());
        assertEquals(PracticeShopApiScenarioFixture.VALID_PASSWORD,
                body.path(PracticeShopApiJsonField.PASSWORD.wireValue()).asText());
    }

    @ParameterizedTest
    @EnumSource(PracticeShopPaymentMethod.class)
    void paymentVariantsRemainJsonAndUseTheConfiguredWireValue(PracticeShopPaymentMethod method) {
        ApiRequest request = requests.payment(method);

        assertEquals(HttpMethod.POST.value(), request.method());
        assertEquals(PracticeShopApiRoutes.PAYMENT_CHECK, request.path());
        assertEquals(MediaTypes.APPLICATION_JSON, request.headers().get(HttpHeader.CONTENT_TYPE.value()));
        var body = PracticeShopJsonEvidence.parse(request.bodyText());
        assertNotNull(body);
        assertEquals(method.wireValue(),
                body.path(PracticeShopApiJsonField.PAYMENT_METHOD.wireValue()).asText());
    }

    @Test
    void authenticatedScreenRequestsCarryBearerButAnonymousRequestsDoNot() {
        ApiRequest anonymous = requests.currentUser("");
        ApiRequest authenticated = requests.currentUser(PracticeShopApiScenarioFixture.AUTH_TOKEN);

        assertEquals(Map.of(), anonymous.headers());
        assertEquals("Bearer " + PracticeShopApiScenarioFixture.AUTH_TOKEN,
                authenticated.headers().get(HttpHeader.AUTHORIZATION.value()));
        assertEquals(PracticeShopApiAuthProfile.BEARER.wireValue(), authenticated.authProfile());
    }

    @Test
    void invalidQuantityFailsBeforeAWriteCanReachTheCart() {
        assertThrows(IllegalArgumentException.class,
                () -> requests.addToCart(PracticeShopApiScenarioFixture.MUTATION_CART_ID,
                        PracticeShopApiScenarioFixture.MUTATION_PRODUCT_ID, 0));
    }

    @Test
    void queryVariantsAreKeptTypedAndEncodedByTheCoreUrlBuilder() {
        ApiRequest request = requests.postcodeLookup(PracticeShopApiScenarioFixture.COUNTRY,
                PracticeShopApiScenarioFixture.POSTCODE, PracticeShopApiScenarioFixture.HOUSE_NUMBER);

        assertEquals(HttpMethod.GET.value(), request.method());
        assertEquals(PracticeShopApiScenarioFixture.COUNTRY,
                request.query().get(PracticeShopApiQueryParameter.COUNTRY.wireValue()));
        assertEquals(PracticeShopApiScenarioFixture.POSTCODE,
                request.query().get(PracticeShopApiQueryParameter.POSTCODE.wireValue()));
        assertEquals(PracticeShopApiScenarioFixture.HOUSE_NUMBER,
                request.query().get(PracticeShopApiQueryParameter.HOUSE_NUMBER.wireValue()));
    }

    @Test
    void productSearchUsesTheRealHttpGetVerbAndKeepsSearchAsBusinessIntent() {
        ApiRequest request = requests.search(PracticeShopApiScenarioFixture.PRODUCT_NAME);

        assertEquals(HttpMethod.GET.value(), request.method());
        assertEquals(HttpMethod.GET.value(), PracticeShopApiOperation.SEARCH.wireValue());
        assertEquals(PracticeShopApiRoutes.PRODUCT_SEARCH, request.path());
    }

    @Test
    void catalogFiltersAreTypedAndKeepTheDocumentedQueryVocabulary() {
        ApiRequest request = requests.products(new PracticeShopProductQuery(
                PracticeShopApiScenarioFixture.FILTER_BRAND_ID, PracticeShopApiScenarioFixture.CATEGORY_ID,
                PracticeShopApiScenarioFixture.FILTER_RENTAL, PracticeShopApiScenarioFixture.FILTER_PRICE_RANGE,
                PracticeShopProductSort.PRICE_ASC, PracticeShopApiScenarioFixture.FILTER_PAGE));

        assertEquals(HttpMethod.GET.value(), request.method());
        assertEquals(PracticeShopApiRoutes.PRODUCT_COLLECTION, request.path());
        assertEquals(PracticeShopApiScenarioFixture.FILTER_BRAND_ID,
                request.query().get(PracticeShopApiQueryParameter.BRAND.wireValue()));
        assertEquals(PracticeShopApiScenarioFixture.CATEGORY_ID,
                request.query().get(PracticeShopApiQueryParameter.CATEGORY.wireValue()));
        assertEquals(Boolean.toString(PracticeShopApiScenarioFixture.FILTER_RENTAL),
                request.query().get(PracticeShopApiQueryParameter.RENTAL.wireValue()));
        assertEquals(PracticeShopApiScenarioFixture.FILTER_PRICE_RANGE,
                request.query().get(PracticeShopApiQueryParameter.PRICE_RANGE.wireValue()));
        assertEquals(PracticeShopProductSort.PRICE_ASC.wireValue(),
                request.query().get(PracticeShopApiQueryParameter.SORT.wireValue()));
        assertEquals(Integer.toString(PracticeShopApiScenarioFixture.FILTER_PAGE),
                request.query().get(PracticeShopApiQueryParameter.PAGE.wireValue()));
    }

    @Test
    void cartUpdateIsPutJsonAndRemoveHasAnExplicitProductRoute() {
        String cartId = PracticeShopApiScenarioFixture.MUTATION_CART_ID;
        String productId = PracticeShopApiScenarioFixture.MUTATION_PRODUCT_ID;
        int expectedQuantity = PracticeShopApiScenarioFixture.UPDATED_QUANTITY;

        ApiRequest update = requests.updateCartQuantity(cartId, productId, expectedQuantity);
        ApiRequest remove = requests.removeFromCart(cartId, productId);

        assertEquals(HttpMethod.PUT.value(), update.method());
        assertEquals(PracticeShopApiRoutes.updateCartQuantity(cartId), update.path());
        assertEquals(MediaTypes.APPLICATION_JSON, update.headers().get(HttpHeader.CONTENT_TYPE.value()));
        var updateBody = PracticeShopJsonEvidence.parse(update.bodyText());
        assertNotNull(updateBody);
        assertEquals(productId,
                updateBody.path(PracticeShopApiJsonField.PRODUCT_ID.wireValue()).asText());
        assertEquals(expectedQuantity,
                updateBody.path(PracticeShopApiJsonField.QUANTITY.wireValue()).asInt());
        assertEquals(HttpMethod.DELETE.value(), remove.method());
        assertEquals(PracticeShopApiRoutes.removeCartProduct(cartId, productId), remove.path());
        assertEquals("", remove.bodyText());
    }
}
