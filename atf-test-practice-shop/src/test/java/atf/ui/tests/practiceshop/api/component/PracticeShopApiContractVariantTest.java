package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Small contract matrix: few cases, deliberately different failure boundaries. */
@AtfComponentTest
class PracticeShopApiContractVariantTest {
    private static final PracticeShopApiRequestFactory REQUESTS = new PracticeShopApiRequestFactory();

    @ParameterizedTest(name = "{0}")
    @MethodSource("variants")
    void preservesPositiveAndNegativeApiShapes(String name, int expectedStatus,
                                               String expectedBody, ApiRequestSupplier request) {
        PracticeShopApiClient client = PracticeShopApiClient.forTransport(
                URI.create("https://api.practicesoftwaretesting.com"), transport());

        var result = client.execute(request.create());

        assertEquals(expectedStatus, result.response().status());
        assertEquals(expectedBody, result.response().body());
        assertTrue(result.response().header("content-type").contains("application/json"));
    }

    private static Stream<Arguments> variants() {
        return Stream.of(
                Arguments.of("invalid login is unauthorized", 401, "{\"message\":\"Unauthorized\"}",
                        (ApiRequestSupplier) () -> REQUESTS.login(new atf.ui.practiceshop.config.PracticeShopCredentials(
                                "invalid@example.test", "invalid", "ATF", "Invalid"))),
                Arguments.of("anonymous current user is rejected", 401, "{\"message\":\"Unauthorized\"}",
                        (ApiRequestSupplier) () -> REQUESTS.currentUser("")),
                Arguments.of("missing product is not found", 404, "{\"message\":\"Not found\"}",
                        (ApiRequestSupplier) () -> REQUESTS.product("missing-product", "")),
                Arguments.of("cash payment returns a JSON confirmation", 200,
                        "{\"message\":\"Payment was successful\"}",
                        (ApiRequestSupplier) () -> REQUESTS.payment(atf.ui.practiceshop.api.PracticeShopPaymentMethod.CASH_ON_DELIVERY)));
    }

    private static ApiTransport transport() {
        return (request, url, auth) -> {
            int status;
            String body;
            if (request.path().equals(PracticeShopApiRoutes.LOGIN)) {
                status = 401; body = "{\"message\":\"Unauthorized\"}";
            } else if (request.path().equals(PracticeShopApiRoutes.CURRENT_USER)) {
                status = 401; body = "{\"message\":\"Unauthorized\"}";
            } else if (request.path().equals(PracticeShopApiRoutes.product("missing-product"))) {
                status = 404; body = "{\"message\":\"Not found\"}";
            } else if (request.path().equals(PracticeShopApiRoutes.PAYMENT_CHECK)) {
                status = 200; body = "{\"message\":\"Payment was successful\"}";
            } else {
                status = 500; body = "{\"message\":\"unexpected route\"}";
            }
            return CompletableFuture.completedFuture(new ApiResponse(status, body,
                    Map.of("content-type", "application/json"), Duration.ZERO, url));
        };
    }

    @FunctionalInterface
    private interface ApiRequestSupplier { atf.api.core.http.request.ApiRequest create(); }
}
