package atf.ui.practiceshop.api;

import atf.api.core.http.request.ApiRequest;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pure request factory; screen APIs own the order in which requests run. */
public final class PracticeShopApiRequestFactory {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, String> JSON_HEADERS = Map.of(
            "Accept", "application/json",
            "Content-Type", "application/json");

    public ApiRequest login(PracticeShopCredentials credentials) {
        return json("POST", PracticeShopApiRoutes.LOGIN, Map.of(
                "email", credentials.email(), "password", credentials.password()), "none");
    }

    public ApiRequest currentUser(String bearerToken) {
        return get(PracticeShopApiRoutes.CURRENT_USER, bearerToken);
    }

    public ApiRequest search(String productName) {
        return query("QUERY", PracticeShopApiRoutes.PRODUCT_SEARCH,
                Map.of("q", required(productName, "product name")), "none");
    }

    public ApiRequest products(PracticeShopProductQuery filters) {
        return query("GET", PracticeShopApiRoutes.PRODUCT_COLLECTION,
                filters == null ? Map.of() : filters.asQuery(), "none");
    }

    public ApiRequest categories() {
        return get(PracticeShopApiRoutes.CATEGORIES, "");
    }

    public ApiRequest product(String productId, String bearerToken) {
        return get(PracticeShopApiRoutes.product(productId), bearerToken);
    }

    public ApiRequest createCart() {
        return json("POST", PracticeShopApiRoutes.CARTS, Map.of(), "none");
    }

    public ApiRequest addToCart(String cartId, String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("cart quantity must be positive");
        return json("POST", PracticeShopApiRoutes.cart(cartId), Map.of(
                "product_id", required(productId, "product id"), "quantity", quantity), "none");
    }

    public ApiRequest updateCartQuantity(String cartId, String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("cart quantity must be positive");
        return json("PUT", PracticeShopApiRoutes.updateCartQuantity(cartId), Map.of(
                "product_id", required(productId, "product id"), "quantity", quantity), "none");
    }

    public ApiRequest removeFromCart(String cartId, String productId) {
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), "DELETE",
                PracticeShopApiRoutes.removeCartProduct(cartId, productId), Map.of(), Map.of(),
                "none", null, "");
    }

    public ApiRequest readCart(String cartId) {
        return get(PracticeShopApiRoutes.cart(cartId), "");
    }

    public ApiRequest postcodeLookup(String country, String postcode, String houseNumber) {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("country", required(country, "country"));
        query.put("postcode", required(postcode, "postcode"));
        query.put("house_number", required(houseNumber, "house number"));
        return query("GET", PracticeShopApiRoutes.POSTCODE_LOOKUP, query, "none");
    }

    public ApiRequest payment(PracticeShopPaymentMethod method) {
        return json("POST", PracticeShopApiRoutes.PAYMENT_CHECK, Map.of(
                "payment_method", method.wireValue(), "payment_details", Map.of()), "none");
    }

    private static ApiRequest get(String path, String bearerToken) {
        Map<String, String> headers = bearerToken == null || bearerToken.isBlank()
                ? Map.of() : Map.of("Authorization", "Bearer " + bearerToken);
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), "GET", path, Map.of(), headers,
                bearerToken == null || bearerToken.isBlank() ? "none" : "bearer", null, "");
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
