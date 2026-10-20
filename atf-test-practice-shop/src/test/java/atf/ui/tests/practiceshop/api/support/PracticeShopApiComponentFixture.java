package atf.ui.tests.practiceshop.api.support;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopApiJsonField;
import atf.ui.practiceshop.api.PracticeShopApiOperation;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.support.PracticeShopJsonEvidence;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Deterministic component boundary; live integration remains a separate test level. */
public final class PracticeShopApiComponentFixture {
    private static final Map<String, String> JSON_HEADERS =
            Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON);

    private PracticeShopApiComponentFixture() { }

    public static PracticeShopApiClient client() {
        return PracticeShopApiClient.forTransport(
                PracticeShopApiScenarioFixture.API_ENDPOINT, transport());
    }

    public static ApiTransport transport() {
        return (request, url, auth) -> CompletableFuture.completedFuture(
                response(request, url, auth));
    }

    private static ApiResponse response(ApiRequest request, String url, String auth) {
        if (is(request, HttpMethod.POST, PracticeShopApiRoutes.LOGIN)) {
            var requestBody = PracticeShopJsonEvidence.parse(request.bodyText());
            boolean invalid = requestBody != null
                    && PracticeShopApiScenarioFixture.INVALID_EMAIL.equals(
                    requestBody.path(PracticeShopApiJsonField.EMAIL.wireValue()).asText());
            return json(invalid ? PracticeShopApiHttpStatus.UNAUTHORIZED
                            : PracticeShopApiHttpStatus.OK,
                    invalid ? PracticeShopApiScenarioFixture.UNAUTHORIZED_BODY
                            : PracticeShopApiScenarioFixture.LOGIN_TOKEN_BODY, url);
        }
        if (is(request, HttpMethod.GET, PracticeShopApiRoutes.CURRENT_USER)) {
            boolean hasBearerHeader = request.headers().entrySet().stream()
                    .anyMatch(entry -> HttpHeader.AUTHORIZATION.value().equalsIgnoreCase(entry.getKey())
                            && entry.getValue() != null && !entry.getValue().isBlank());
            return json(hasBearerHeader ? PracticeShopApiHttpStatus.OK
                            : PracticeShopApiHttpStatus.UNAUTHORIZED,
                    hasBearerHeader ? PracticeShopApiScenarioFixture.CURRENT_USER_BODY
                            : PracticeShopApiScenarioFixture.UNAUTHORIZED_BODY, url);
        }
        if (is(request, PracticeShopApiOperation.SEARCH, PracticeShopApiRoutes.PRODUCT_SEARCH)) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.PRODUCT_SEARCH_BODY, url);
        }
        if (is(request, HttpMethod.GET, PracticeShopApiRoutes.PRODUCT_COLLECTION)) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.PRODUCT_COLLECTION_BODY, url);
        }
        if (is(request, HttpMethod.GET, PracticeShopApiRoutes.CATEGORIES)) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.CATEGORIES_BODY, url);
        }
        if (is(request, HttpMethod.GET,
                PracticeShopApiRoutes.product(PracticeShopApiScenarioFixture.OUT_OF_STOCK_PRODUCT_ID))) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.OUT_OF_STOCK_BODY, url);
        }
        if (is(request, HttpMethod.GET,
                PracticeShopApiRoutes.product(PracticeShopApiScenarioFixture.MISSING_PRODUCT_ID))) {
            return json(PracticeShopApiHttpStatus.NOT_FOUND, PracticeShopApiScenarioFixture.NOT_FOUND_BODY, url);
        }
        if (is(request, HttpMethod.GET,
                PracticeShopApiRoutes.product(PracticeShopApiScenarioFixture.PRODUCT_ID))) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.PRODUCT_BODY, url);
        }
        if (is(request, HttpMethod.POST, PracticeShopApiRoutes.CARTS)) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.CART_BODY, url);
        }
        if (is(request, HttpMethod.GET,
                PracticeShopApiRoutes.cart(PracticeShopApiScenarioFixture.CART_ID))) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.CART_ITEM_BODY, url);
        }
        if (is(request, HttpMethod.POST,
                PracticeShopApiRoutes.cart(PracticeShopApiScenarioFixture.CART_ID))) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.CART_MUTATION_BODY, url);
        }
        if (is(request, HttpMethod.PUT,
                PracticeShopApiRoutes.updateCartQuantity(PracticeShopApiScenarioFixture.MUTATION_CART_ID))) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.CART_UPDATE_BODY, url);
        }
        if (is(request, HttpMethod.DELETE,
                PracticeShopApiRoutes.removeCartProduct(PracticeShopApiScenarioFixture.MUTATION_CART_ID,
                        PracticeShopApiScenarioFixture.MUTATION_PRODUCT_ID))) {
            return response(PracticeShopApiHttpStatus.NO_CONTENT, "", Map.of(), url);
        }
        if (is(request, HttpMethod.GET, PracticeShopApiRoutes.POSTCODE_LOOKUP)) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.ADDRESS_BODY, url);
        }
        if (is(request, HttpMethod.POST, PracticeShopApiRoutes.PAYMENT_CHECK)) {
            return json(PracticeShopApiHttpStatus.OK, PracticeShopApiScenarioFixture.PAYMENT_SUCCESS_BODY, url);
        }
        return json(PracticeShopApiHttpStatus.INTERNAL_SERVER_ERROR,
                PracticeShopApiScenarioFixture.UNEXPECTED_ROUTE_BODY, url);
    }

    private static boolean is(ApiRequest request, HttpMethod method, String path) {
        return method.value().equals(request.method()) && path.equals(request.path());
    }

    private static boolean is(ApiRequest request, PracticeShopApiOperation operation, String path) {
        return operation.wireValue().equals(request.method()) && path.equals(request.path());
    }

    private static ApiResponse json(PracticeShopApiHttpStatus status, String body, String url) {
        return response(status, body, JSON_HEADERS, url);
    }

    private static ApiResponse response(PracticeShopApiHttpStatus status, String body,
                                        Map<String, String> headers, String url) {
        return new ApiResponse(status.code(), body, headers, Duration.ZERO, url);
    }
}
