package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.MediaTypes;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiComponentFixture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Small contract matrix: few cases, deliberately different failure boundaries. */
@AtfComponentTest
class PracticeShopApiContractVariantTest {
    private static final PracticeShopApiRequestFactory REQUESTS = new PracticeShopApiRequestFactory();

    @ParameterizedTest(name = "{0}")
    @MethodSource("variants")
    void preservesPositiveAndNegativeApiShapes(String name, PracticeShopApiHttpStatus expectedStatus,
                                               String expectedBody, ApiRequestSupplier request) {
        PracticeShopApiClient client = PracticeShopApiComponentFixture.client();

        var result = client.execute(request.create());
        var response = result.response();

        assertTrue(expectedStatus.matches(response.status()));
        assertEquals(expectedBody, response.body());
        assertTrue(response.header("content-type").contains(MediaTypes.APPLICATION_JSON));
    }

    private static Stream<Arguments> variants() {
        return Stream.of(
                Arguments.of("invalid login is unauthorized", PracticeShopApiHttpStatus.UNAUTHORIZED,
                        PracticeShopApiScenarioFixture.UNAUTHORIZED_BODY,
                        (ApiRequestSupplier) () -> REQUESTS.login(
                                PracticeShopApiScenarioFixture.invalidCredentials())),
                Arguments.of("missing product is not found", PracticeShopApiHttpStatus.NOT_FOUND,
                        PracticeShopApiScenarioFixture.NOT_FOUND_BODY,
                        (ApiRequestSupplier) () -> REQUESTS.product(
                                PracticeShopApiScenarioFixture.MISSING_PRODUCT_ID, "")),
                Arguments.of("cash payment returns a JSON confirmation", PracticeShopApiHttpStatus.OK,
                        PracticeShopApiScenarioFixture.PAYMENT_SUCCESS_BODY,
                        (ApiRequestSupplier) () -> REQUESTS.payment(PracticeShopPaymentMethod.CASH_ON_DELIVERY)));
    }

    @FunctionalInterface
    private interface ApiRequestSupplier { ApiRequest create(); }
}
