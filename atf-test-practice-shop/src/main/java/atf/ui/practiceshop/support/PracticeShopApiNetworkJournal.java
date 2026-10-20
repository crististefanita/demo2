package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Captures only the Practice Shop API traffic produced by the same UI session.
 * It is deliberately a test-side adapter: production modules do not depend on
 * the external API and no second browser or independent cart is created.
 */
public final class PracticeShopApiNetworkJournal {
    private static final String API_HOST = "api.practicesoftwaretesting.com";
    private static final Pattern SENSITIVE_JSON_VALUE = Pattern.compile(
            "(?i)(\\\"(?:email|password|access_token|refresh_token|accessToken|refreshToken|token|authorization|cookie)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")");

    private final PracticeShopValidationProfile profile;
    private final List<Exchange> exchanges = new ArrayList<>();
    private List<ReportExecutionEvidence> lastProductEvidence = List.of();
    private List<ReportExecutionEvidence> lastCartEvidence = List.of();
    private List<ReportExecutionEvidence> lastPaymentEvidence = List.of();
    private List<ReportExecutionEvidence> lastLoginEvidence = List.of();
    private String productId;

    public PracticeShopApiNetworkJournal(PracticeShopValidationProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    public void attach(Page page) {
        Objects.requireNonNull(page, "page");
        if (!profile.apiEvidenceRequired()) return;
        page.onResponse(this::capture);
    }

    /** API precondition for the product-page purchase action. */
    public List<AtfAssertion> productReady(String productName) {
        return productReady(productName, null);
    }

    /** API confirmation for the login setup that precedes an authenticated UI flow. */
    public List<AtfAssertion> loginConfirmed(String displayName) {
        if (!profile.apiEvidenceRequired()) return List.of();
        Exchange login = latest("POST", "/users/login", exchange -> exchange.status() == 200);
        Exchange currentUser = latestOptional("GET", "/users/me", exchange -> exchange.status() == 200);
        lastLoginEvidence = currentUser == null
                ? List.of(PracticeShopApiEvidenceMapper.toExecution(login))
                : List.of(PracticeShopApiEvidenceMapper.toExecution(login),
                        PracticeShopApiEvidenceMapper.toExecution(currentUser));
        return PracticeShopApiAssertions.login(login, currentUser, displayName);
    }

    /**
     * API precondition for the product selected by the UI. The route is the
     * source of truth when the external catalog rotates IDs or contains
     * several cards with the same display name.
     */
    public List<AtfAssertion> productReady(String productName, String selectedProductPath) {
        if (!profile.apiEvidenceRequired()) return List.of();
        Exchange search = latest("QUERY", "/products/search", exchange -> exchange.status() == 200);
        productId = productIdFromPath(selectedProductPath);
        if (productId == null) {
            productId = PracticeShopApiAssertions.productId(search.responseBody(), productName);
        }
        Exchange detail = latest("GET", "/products/" + productId,
                exchange -> exchange.status() == 200
                        && exchange.path().equals("/products/" + productId));
        lastProductEvidence = List.of(PracticeShopApiEvidenceMapper.toExecution(search),
                PracticeShopApiEvidenceMapper.toExecution(detail));
        return PracticeShopApiAssertions.product(search, detail, productName, productId);
    }

    private static String productIdFromPath(String selectedProductPath) {
        if (selectedProductPath == null || selectedProductPath.isBlank()) return null;
        String normalized = selectedProductPath.trim();
        int separator = normalized.lastIndexOf('/');
        if (separator < 0 || separator == normalized.length() - 1) return null;
        String id = normalized.substring(separator + 1).split("[?#]", 2)[0].trim();
        return id.isBlank() ? null : id;
    }

    /** API confirmation after the UI adds the configured quantity to the cart. */
    public List<AtfAssertion> cartReady(int quantity) {
        if (!profile.apiEvidenceRequired()) return List.of();
        if (productId == null || productId.isBlank()) {
            throw new IllegalStateException("Product API precondition was not established before cart validation");
        }
        Exchange add = latest("POST", "/carts/", exchange -> exchange.status() == 200
                && exchange.requestBody().contains(productId));
        Exchange read = latest("GET", add.path(), exchange -> exchange.status() == 200);
        lastCartEvidence = List.of(PracticeShopApiEvidenceMapper.toExecution(add),
                PracticeShopApiEvidenceMapper.toExecution(read));
        return PracticeShopApiAssertions.cart(add, read, productId, quantity);
    }

    /** API confirmation after the UI submits the configured payment method. */
    public List<AtfAssertion> paymentConfirmed(String paymentMethod) {
        if (!profile.apiEvidenceRequired()) return List.of();
        Exchange payment = latest("POST", "/payment/check", exchange -> exchange.status() == 200);
        lastPaymentEvidence = List.of(PracticeShopApiEvidenceMapper.toExecution(payment));
        return PracticeShopApiAssertions.payment(payment, paymentMethod);
    }

    public List<Exchange> exchanges() { return List.copyOf(exchanges); }
    public List<ReportExecutionEvidence> productEvidence() { return lastProductEvidence; }
    public List<ReportExecutionEvidence> loginEvidence() { return lastLoginEvidence; }
    public List<ReportExecutionEvidence> cartEvidence() { return lastCartEvidence; }
    public List<ReportExecutionEvidence> paymentEvidence() { return lastPaymentEvidence; }
    public String productId() { return productId; }

    private void capture(Response response) {
        if (!isPracticeShopApi(response.request().url())) return;
        var request = response.request();
        String requestBody = redact(request.postData());
        String responseBody;
        try {
            responseBody = redact(new String(response.body()));
        } catch (RuntimeException unavailable) {
            // Chromium may release a compressed CORS payload before CDP can
            // read it. Keep this explicit in evidence; status/content-length
            // and the authenticated /users/me response remain verifiable.
            responseBody = "<body unavailable: Chromium released response payload>";
        }
        exchanges.add(new Exchange(request.method(), path(request.url()), request.url(), response.status(),
                requestBody, responseBody, request.headers(), response.headers()));
    }

    private static boolean isPracticeShopApi(String url) {
        try { return API_HOST.equals(java.net.URI.create(url).getHost()); }
        catch (RuntimeException ignored) { return false; }
    }

    private Exchange latest(String method, String path, Predicate<Exchange> extra) {
        Exchange exchange = latestOptional(method, path, extra);
        if (exchange != null) return exchange;
        throw new IllegalStateException("Missing API exchange: " + method + " " + path
                + "; captured=" + exchanges.stream().map(Exchange::summary).toList());
    }

    private Exchange latestOptional(String method, String path, Predicate<Exchange> extra) {
        for (int index = exchanges.size() - 1; index >= 0; index--) {
            Exchange exchange = exchanges.get(index);
            if (method.equalsIgnoreCase(exchange.method())
                    && exchange.path().contains(path) && extra.test(exchange)) return exchange;
        }
        return null;
    }

    private static String path(String url) {
        java.net.URI uri = java.net.URI.create(url);
        return uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
    }

    private static String redact(String value) {
        return value == null ? "" : SENSITIVE_JSON_VALUE.matcher(value).replaceAll("$1<redacted>$2");
    }

    public record Exchange(String method, String path, String url, int status,
                           String requestBody, String responseBody,
                           java.util.Map<String, String> requestHeaders,
                           java.util.Map<String, String> responseHeaders) {
        public Exchange {
            requestHeaders = requestHeaders == null ? java.util.Map.of() : java.util.Map.copyOf(requestHeaders);
            responseHeaders = responseHeaders == null ? java.util.Map.of() : java.util.Map.copyOf(responseHeaders);
            requestBody = requestBody == null ? "" : requestBody;
            responseBody = responseBody == null ? "" : responseBody;
        }

        public String contentType() {
            return responseHeaders.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase("content-type"))
                    .map(java.util.Map.Entry::getValue).findFirst().orElse("");
        }

        public boolean isJsonResponse() { return contentType().toLowerCase().contains("application/json"); }
        public String authType() {
            if (requestHeaders.keySet().stream().anyMatch(key -> key.equalsIgnoreCase("authorization"))) {
                String value = requestHeaders.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase("authorization"))
                        .map(Map.Entry::getValue).findFirst().orElse("").toLowerCase();
                if (value.startsWith("bearer")) return "bearer";
                if (value.startsWith("basic")) return "basic";
                return "authorization-header";
            }
            if (requestHeaders.keySet().stream().anyMatch(key -> key.equalsIgnoreCase("x-api-key"))) return "api-key";
            if (requestHeaders.keySet().stream().anyMatch(key -> key.equalsIgnoreCase("cookie"))) return "cookie";
            return "none";
        }
        public String summary() { return method + " " + path + " → " + status + " " + contentType(); }
    }
}
