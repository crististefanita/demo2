package atf.ui.practiceshop.api;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pure request factory; screen APIs own the order in which requests run. */
public final class PracticeShopApiRequestFactory {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String NO_AUTH = PracticeShopApiAuthProfile.NONE.wireValue();
    private static final String BEARER_AUTH = PracticeShopApiAuthProfile.BEARER.wireValue();
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Map<String, String> JSON_HEADERS = Map.of(
            HttpHeader.ACCEPT.value(), MediaTypes.APPLICATION_JSON,
            HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON);

    public ApiRequest login(PracticeShopCredentials credentials) {
        return json(HttpMethod.POST.value(), PracticeShopApiRoutes.LOGIN, Map.of(
                PracticeShopApiJsonField.EMAIL.wireValue(), credentials.email(),
                PracticeShopApiJsonField.PASSWORD.wireValue(), credentials.password()),
                NO_AUTH);
    }

    public ApiRequest currentUser(String bearerToken) {
        return get(PracticeShopApiRoutes.CURRENT_USER, bearerToken);
    }

    public ApiRequest search(String productName) {
        return query(HttpMethod.GET.value(), PracticeShopApiRoutes.PRODUCT_SEARCH,
                Map.of(PracticeShopApiQueryParameter.SEARCH.wireValue(), required(productName, "product name")),
                NO_AUTH);
    }

    public ApiRequest products(PracticeShopProductQuery filters) {
        return query(HttpMethod.GET.value(), PracticeShopApiRoutes.PRODUCT_COLLECTION,
                filters == null ? Map.of() : filters.asQuery(),
                NO_AUTH);
    }

    public ApiRequest categories() {
        return get(PracticeShopApiRoutes.CATEGORIES, "");
    }

    public ApiRequest product(String productId, String bearerToken) {
        return get(PracticeShopApiRoutes.product(productId), bearerToken);
    }

    public ApiRequest createCart() {
        return json(HttpMethod.POST.value(), PracticeShopApiRoutes.CARTS, Map.of(),
                NO_AUTH);
    }

    public ApiRequest addToCart(String cartId, String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("cart quantity must be positive");
        return json(HttpMethod.POST.value(), PracticeShopApiRoutes.cart(cartId), Map.of(
                PracticeShopApiJsonField.PRODUCT_ID.wireValue(), required(productId, "product id"),
                PracticeShopApiJsonField.QUANTITY.wireValue(), quantity),
                NO_AUTH);
    }

    public ApiRequest updateCartQuantity(String cartId, String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("cart quantity must be positive");
        return json(HttpMethod.PUT.value(), PracticeShopApiRoutes.updateCartQuantity(cartId), Map.of(
                PracticeShopApiJsonField.PRODUCT_ID.wireValue(), required(productId, "product id"),
                PracticeShopApiJsonField.QUANTITY.wireValue(), quantity),
                NO_AUTH);
    }

    public ApiRequest removeFromCart(String cartId, String productId) {
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), HttpMethod.DELETE.value(),
                PracticeShopApiRoutes.removeCartProduct(cartId, productId), Map.of(), Map.of(),
                NO_AUTH, null, "");
    }

    public ApiRequest readCart(String cartId) {
        return get(PracticeShopApiRoutes.cart(cartId), "");
    }

    public ApiRequest postcodeLookup(String country, String postcode, String houseNumber) {
        Map<String, String> query = new LinkedHashMap<>();
        query.put(PracticeShopApiQueryParameter.COUNTRY.wireValue(), required(country, "country"));
        query.put(PracticeShopApiQueryParameter.POSTCODE.wireValue(), required(postcode, "postcode"));
        query.put(PracticeShopApiQueryParameter.HOUSE_NUMBER.wireValue(), required(houseNumber, "house number"));
        return query(HttpMethod.GET.value(), PracticeShopApiRoutes.POSTCODE_LOOKUP, query,
                NO_AUTH);
    }

    public ApiRequest payment(PracticeShopPaymentMethod method) {
        return json(HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK, Map.of(
                PracticeShopApiJsonField.PAYMENT_METHOD.wireValue(), method.wireValue(),
                PracticeShopApiJsonField.PAYMENT_DETAILS.wireValue(), Map.of()),
                NO_AUTH);
    }

    private static ApiRequest get(String path, String bearerToken) {
        Map<String, String> headers = bearerToken == null || bearerToken.isBlank()
                ? Map.of() : Map.of(HttpHeader.AUTHORIZATION.value(), BEARER_PREFIX + bearerToken);
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), HttpMethod.GET.value(), path, Map.of(), headers,
                bearerToken == null || bearerToken.isBlank()
                        ? NO_AUTH : BEARER_AUTH, null, "");
    }

    private static ApiRequest query(String method, String path, Map<String, String> query,
                                    String authProfile) {
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), method, path, query,
                JSON_HEADERS, authProfile, null, "");
    }

    private static ApiRequest json(String method, String path, Object body, String authProfile) {
        String bodyText;
        try {
            bodyText = JSON.writeValueAsString(body);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Cannot serialize Practice Shop API JSON body", error);
        }
        return new ApiRequest(HttpRequest.BodyPublishers.ofString(bodyText), method, path,
                Map.of(), JSON_HEADERS, authProfile, null, bodyText);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value.trim();
    }
}
