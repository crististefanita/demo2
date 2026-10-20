package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Captures only the Practice Shop API traffic produced by the same UI session.
 * It is deliberately a consumer-side adapter: production modules do not depend on
 * the external API and no second browser or independent cart is created.
 */
public final class PracticeShopApiNetworkJournal {
    private final PracticeShopValidationProfile profile;
    private final PracticeShopApiTrafficCapture trafficCapture = new PracticeShopApiTrafficCapture();
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
        page.onResponse(response -> trafficCapture.capture(response).ifPresent(exchanges::add));
    }

    /** API precondition for the product-page purchase action. */
    public List<AtfAssertion> productReady(String productName) {
        return productReady(productName, null);
    }

    /** API confirmation for the login setup that precedes an authenticated UI flow. */
    public List<AtfAssertion> loginConfirmed(String displayName) {
        if (!profile.apiEvidenceRequired()) return List.of();
        Exchange login = PracticeShopApiExchangeSelector.latest(
                exchanges, "POST", PracticeShopApiRoutes.LOGIN, exchange -> exchange.status() == 200);
        Exchange currentUser = PracticeShopApiExchangeSelector.latestOptional(
                exchanges, "GET", PracticeShopApiRoutes.CURRENT_USER, exchange -> exchange.status() == 200);
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
        Exchange search = PracticeShopApiExchangeSelector.latest(
                exchanges, "QUERY", PracticeShopApiRoutes.PRODUCT_SEARCH, exchange -> exchange.status() == 200);
        productId = PracticeShopApiExchangeSelector.productIdFromPath(selectedProductPath);
        if (productId == null) {
            productId = PracticeShopApiAssertions.productId(search.responseBody(), productName);
        }
        String productPath = PracticeShopApiRoutes.product(productId);
        Exchange detail = PracticeShopApiExchangeSelector.latest(exchanges, "GET", productPath,
                exchange -> exchange.status() == 200
                        && exchange.path().equals(productPath));
        lastProductEvidence = List.of(PracticeShopApiEvidenceMapper.toExecution(search),
                PracticeShopApiEvidenceMapper.toExecution(detail));
        return PracticeShopApiAssertions.product(search, detail, productName, productId);
    }

    /** API confirmation after the UI adds the configured quantity to the cart. */
    public List<AtfAssertion> cartReady(int quantity) {
        if (!profile.apiEvidenceRequired()) return List.of();
        if (productId == null || productId.isBlank()) {
            throw new IllegalStateException("Product API precondition was not established before cart validation");
        }
        Exchange add = PracticeShopApiExchangeSelector.latest(exchanges, "POST",
                PracticeShopApiRoutes.CART_RESOURCE_PREFIX, exchange -> exchange.status() == 200
                && exchange.requestBody().contains(productId));
        Exchange read = PracticeShopApiExchangeSelector.latest(
                exchanges, "GET", add.path(), exchange -> exchange.status() == 200);
        lastCartEvidence = List.of(PracticeShopApiEvidenceMapper.toExecution(add),
                PracticeShopApiEvidenceMapper.toExecution(read));
        return PracticeShopApiAssertions.cart(add, read, productId, quantity);
    }

    /** API confirmation after the UI submits the configured payment method. */
    public List<AtfAssertion> paymentConfirmed(String paymentMethod) {
        if (!profile.apiEvidenceRequired()) return List.of();
        Exchange payment = PracticeShopApiExchangeSelector.latest(
                exchanges, "POST", PracticeShopApiRoutes.PAYMENT_CHECK, exchange -> exchange.status() == 200);
        lastPaymentEvidence = List.of(PracticeShopApiEvidenceMapper.toExecution(payment));
        return PracticeShopApiAssertions.payment(payment, paymentMethod);
    }

    public List<Exchange> exchanges() { return List.copyOf(exchanges); }
    public List<ReportExecutionEvidence> productEvidence() { return lastProductEvidence; }
    public List<ReportExecutionEvidence> loginEvidence() { return lastLoginEvidence; }
    public List<ReportExecutionEvidence> cartEvidence() { return lastCartEvidence; }
    public List<ReportExecutionEvidence> paymentEvidence() { return lastPaymentEvidence; }
    public String productId() { return productId; }

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
