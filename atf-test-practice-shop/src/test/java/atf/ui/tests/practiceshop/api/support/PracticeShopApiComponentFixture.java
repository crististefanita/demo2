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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Deterministic component boundary; live integration remains a separate test level. */
public final class PracticeShopApiComponentFixture {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, String> JSON_HEADERS =
            Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON);

    private PracticeShopApiComponentFixture() { }

    public static PracticeShopApiClient client() {
        return PracticeShopApiClient.forTransport(
                PracticeShopApiScenarioFixture.API_ENDPOINT, transport());
    }

    /**
     * Returns a deterministic stateful cart boundary for read-back contract tests.
     * The ordinary fixture remains stateless so isolated request-variant tests do
     * not accidentally depend on execution order.
     */
    public static PracticeShopApiClient statefulCartClient() {
        return PracticeShopApiClient.forTransport(
                PracticeShopApiScenarioFixture.API_ENDPOINT, new StatefulCartTransport());
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

    private static final class StatefulCartTransport implements ApiTransport {
        private final Map<String, CartLine> carts = new HashMap<>();

        @Override
        public CompletableFuture<ApiResponse> send(ApiRequest request, String url, String auth) {
            return CompletableFuture.completedFuture(response(request, url));
        }

        private ApiResponse response(ApiRequest request, String url) {
            if (is(request, HttpMethod.POST, PracticeShopApiRoutes.CARTS)) {
                String cartId = PracticeShopApiScenarioFixture.MUTATION_CART_ID;
                carts.remove(cartId);
                return json(PracticeShopApiHttpStatus.OK,
                        "{\"id\":\"" + cartId + "\"}", url);
            }

            String cartId = cartId(request.path());
            if (cartId == null) {
                return json(PracticeShopApiHttpStatus.INTERNAL_SERVER_ERROR,
                        PracticeShopApiScenarioFixture.UNEXPECTED_ROUTE_BODY, url);
            }

            if (request.method().equals(HttpMethod.POST.value())) {
                CartLine line = line(request);
                carts.put(cartId, line);
                return json(PracticeShopApiHttpStatus.OK, mutation("item added or updated"), url);
            }
            if (request.method().equals(HttpMethod.PUT.value())) {
                CartLine line = line(request);
                carts.put(cartId, line);
                return json(PracticeShopApiHttpStatus.OK, mutation("item updated"), url);
            }
            if (request.method().equals(HttpMethod.DELETE.value())) {
                carts.remove(cartId);
                return PracticeShopApiComponentFixture.response(
                        PracticeShopApiHttpStatus.NO_CONTENT, "", Map.<String, String>of(), url);
            }
            if (request.method().equals(HttpMethod.GET.value())) {
                return json(PracticeShopApiHttpStatus.OK, cartBody(carts.get(cartId)), url);
            }
            return json(PracticeShopApiHttpStatus.INTERNAL_SERVER_ERROR,
                    PracticeShopApiScenarioFixture.UNEXPECTED_ROUTE_BODY, url);
        }

        private static String cartId(String path) {
            String prefix = PracticeShopApiRoutes.CARTS + "/";
            if (!path.startsWith(prefix)) return null;
            String remainder = path.substring(prefix.length());
            int slash = remainder.indexOf('/');
            return slash < 0 ? remainder : remainder.substring(0, slash);
        }

        private static CartLine line(ApiRequest request) {
            var body = PracticeShopJsonEvidence.parse(request.bodyText());
            return new CartLine(body.path(PracticeShopApiJsonField.PRODUCT_ID.wireValue()).asText(),
                    body.path(PracticeShopApiJsonField.QUANTITY.wireValue()).asInt());
        }

        private static String cartBody(CartLine line) {
            try {
                var root = JSON.createObjectNode();
                var items = root.putArray(PracticeShopApiJsonField.CART_ITEMS.wireValue());
                if (line != null) {
                    var item = items.addObject();
                    item.put(PracticeShopApiJsonField.PRODUCT_ID.wireValue(), line.productId());
                    item.put(PracticeShopApiJsonField.QUANTITY.wireValue(), line.quantity());
                }
                return JSON.writeValueAsString(root);
            } catch (Exception error) {
                throw new IllegalStateException("Cannot serialize stateful cart fixture", error);
            }
        }

        private static String mutation(String result) {
            return "{\"result\":\"" + result + "\"}";
        }

        private record CartLine(String productId, int quantity) { }
    }
}
