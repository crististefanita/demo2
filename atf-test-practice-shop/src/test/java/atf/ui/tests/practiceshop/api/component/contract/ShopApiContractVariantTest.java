package atf.ui.tests.practiceshop.api.component.contract;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.MediaTypes;
import atf.ui.practiceshop.api.ShopApiClient;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.practiceshop.api.ShopApiRequestFactory;
import atf.ui.practiceshop.api.ShopPaymentMethod;
import atf.ui.tests.practiceshop.api.support.ShopApiScenarioFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiComponentFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiCallComponentTest;
import atf.ui.tests.practiceshop.api.support.ShopApiCallAuditContextTestSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Small contract matrix: few cases, deliberately different failure boundaries. */
@ShopApiCallComponentTest
class ShopApiContractVariantTest {
    private static final ShopApiRequestFactory REQUESTS = new ShopApiRequestFactory();

    @ParameterizedTest(name = "{0}")
    @MethodSource("variants")
    void preservesPositiveAndNegativeApiShapes(String name, ShopApiHttpStatus expectedStatus,
                                               String expectedBody, ApiRequestSupplier request) {
        ShopApiClient client = ShopApiComponentFixture.client();

        ApiRequest apiRequest = request.create();
        ShopApiCallAuditContextTestSupport.expectNext(name, expectedStatus);
        var result = client.execute(apiRequest);
        var response = result.response();

        assertTrue(expectedStatus.matches(response.status()));
        assertEquals(expectedBody, response.body());
        assertTrue(response.header("content-type").contains(MediaTypes.APPLICATION_JSON));
    }

    private static Stream<Arguments> variants() {
        return Stream.of(
                Arguments.of("invalid login is unauthorized", ShopApiHttpStatus.UNAUTHORIZED,
                        ShopApiScenarioFixture.UNAUTHORIZED_BODY,
                        (ApiRequestSupplier) () -> REQUESTS.login(
                                ShopApiScenarioFixture.invalidCredentials())),
                Arguments.of("missing product is not found", ShopApiHttpStatus.NOT_FOUND,
                        ShopApiScenarioFixture.NOT_FOUND_BODY,
                        (ApiRequestSupplier) () -> REQUESTS.product(
                                ShopApiScenarioFixture.MISSING_PRODUCT_ID, "")),
                Arguments.of("cash payment returns a JSON confirmation", ShopApiHttpStatus.OK,
                        ShopApiScenarioFixture.PAYMENT_SUCCESS_BODY,
                        (ApiRequestSupplier) () -> REQUESTS.payment(ShopPaymentMethod.CASH_ON_DELIVERY)));
    }

    @FunctionalInterface
    private interface ApiRequestSupplier { ApiRequest create(); }
}
