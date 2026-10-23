package atf.ui.autoex.api;

import atf.api.core.http.auth.AuthProfiles;
import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.HttpMethod;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Pure form/query request construction for the consumer's documented API contract. */
public final class AutoExApiRequestFactory {
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    public ApiRequest productsList() { return get(AutoExApiRoute.PRODUCTS_LIST); }
    public ApiRequest postProductsList() { return form(HttpMethod.POST, AutoExApiRoute.PRODUCTS_LIST, Map.of()); }
    public ApiRequest brandsList() { return get(AutoExApiRoute.BRANDS_LIST); }
    public ApiRequest putBrandsList() { return form(HttpMethod.PUT, AutoExApiRoute.BRANDS_LIST, Map.of()); }

    public ApiRequest searchProduct(String term) {
        return form(HttpMethod.POST, AutoExApiRoute.SEARCH_PRODUCT,
                term == null ? Map.of() : Map.of("search_product", term));
    }

    public ApiRequest verifyLogin(String email, String password) {
        return form(HttpMethod.POST, AutoExApiRoute.VERIFY_LOGIN,
                fields("email", email, "password", password));
    }

    public ApiRequest verifyLoginWithoutEmail(String password) {
        return form(HttpMethod.POST, AutoExApiRoute.VERIFY_LOGIN, fields("password", password));
    }

    public ApiRequest deleteMethodOnVerifyLogin() {
        return form(HttpMethod.DELETE, AutoExApiRoute.VERIFY_LOGIN, Map.of());
    }

    public ApiRequest createAccount(AutoExApiAccount account) {
        return form(HttpMethod.POST, AutoExApiRoute.CREATE_ACCOUNT,
                Objects.requireNonNull(account, "account").formFields());
    }

    public ApiRequest deleteAccount(String email, String password) {
        return form(HttpMethod.DELETE, AutoExApiRoute.DELETE_ACCOUNT,
                fields("email", email, "password", password));
    }

    public ApiRequest updateAccount(AutoExApiAccount account) {
        return form(HttpMethod.PUT, AutoExApiRoute.UPDATE_ACCOUNT,
                Objects.requireNonNull(account, "account").formFields());
    }

    public ApiRequest userDetail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), HttpMethod.GET.value(),
                AutoExApiRoute.USER_DETAIL_BY_EMAIL.path(), Map.of("email", email.trim()), Map.of(),
                AuthProfiles.NONE, null, "");
    }

    private static ApiRequest get(AutoExApiRoute route) {
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), HttpMethod.GET.value(),
                route.path(), Map.of(), Map.of(), AuthProfiles.NONE, null, "");
    }

    private static ApiRequest form(HttpMethod method, AutoExApiRoute route, Map<String, String> fields) {
        Map<String, String> safeFields = fields == null ? Map.of() : fields;
        String body = safeFields.entrySet().stream()
                .map(field -> encode(field.getKey()) + "=" + encode(field.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
        Map<String, String> headers = !body.isEmpty()
                ? Map.of(HttpHeader.CONTENT_TYPE.value(), FORM_CONTENT_TYPE) : Map.of();
        return new ApiRequest(body.isEmpty() ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body),
                method.value(), route.path(), Map.of(), headers, AuthProfiles.NONE, null, body);
    }

    private static Map<String, String> fields(String... entries) {
        if (entries.length % 2 != 0) throw new IllegalArgumentException("field pairs are incomplete");
        Map<String, String> fields = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            String value = entries[index + 1];
            if (value != null) fields.put(entries[index], value);
        }
        return fields;
    }

    private static String encode(String value) {
        return URLEncoder.encode(Objects.requireNonNull(value, "form value"), StandardCharsets.UTF_8);
    }
}
