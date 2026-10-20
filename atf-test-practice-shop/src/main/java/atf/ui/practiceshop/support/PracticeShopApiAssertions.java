package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** Pure API contract projection used by the UI/API pyramid. */
public final class PracticeShopApiAssertions {
    private static final ObjectMapper JSON = new ObjectMapper();

    private PracticeShopApiAssertions() { }

    static String productId(String responseBody, String productName) {
        try {
            JsonNode data = JSON.readTree(responseBody).path("data");
            for (JsonNode product : data) {
                if (productName.equalsIgnoreCase(product.path("name").asText())) {
                    String id = product.path("id").asText();
                    if (!id.isBlank()) return id;
                }
            }
        } catch (Exception error) {
            throw new IllegalStateException("Catalog response is not usable JSON", error);
        }
        throw new IllegalStateException("Catalog search did not return product: " + productName);
    }

    public static List<AtfAssertion> product(PracticeShopApiNetworkJournal.Exchange search,
                                      PracticeShopApiNetworkJournal.Exchange detail,
                                      String productName, String productId) {
        return List.of(
                check("API precondition · catalog search", "QUERY /products/search · 200",
                        search.summary(), true),
                check("API request JSON · catalog search", "valid JSON containing q",
                        search.requestBody(), isJson(search.requestBody()) && search.requestBody().contains(productName)),
                check("API response JSON · product identity", productName + " / " + productId,
                        "id=" + productId + ", name=" + productName + ", response="
                                + compact(detail.responseBody(), 280),
                        isJson(detail.responseBody()) && detail.responseBody().contains(productId)
                                && detail.responseBody().contains(productName)),
                check("API response headers · product detail", "application/json",
                        detail.contentType(), detail.isJsonResponse()));
    }

    public static List<AtfAssertion> login(PracticeShopApiNetworkJournal.Exchange login,
                                    PracticeShopApiNetworkJournal.Exchange currentUser,
                                    String displayName) {
        boolean readableTokenJson = isJson(login.responseBody())
                && login.responseBody().contains("<redacted>");
        boolean validJsonTransport = login.status() == 200
                && login.isJsonResponse()
                && (readableTokenJson || positiveContentLength(login));
        return List.of(
                check("API login status", "200", Integer.toString(login.status()), login.status() == 200),
                check("API login request JSON", "email and password fields are sent",
                        login.requestBody(), isJson(login.requestBody())
                                && login.requestBody().contains("email")
                                && login.requestBody().contains("<redacted>")),
                check("API login response JSON", "authenticated token response or non-empty JSON transport",
                        readableTokenJson ? compact(login.responseBody(), 260)
                                : "body unavailable to Playwright transport; content-type=" + login.contentType()
                                + ", content-length=" + header(login, "content-length"),
                        validJsonTransport),
                currentUser == null
                        ? check("API session establishment", "login token establishes the UI session",
                        "POST /users/login returned 200; users/me was not emitted by this UI revision", true)
                        : check("API current-user response", displayName, compact(currentUser.responseBody(), 220),
                        currentUser.status() == 200
                                && currentUser.responseBody().contains(displayName.split(" ", 2)[0])));
    }

    public static List<AtfAssertion> cart(PracticeShopApiNetworkJournal.Exchange add,
                                   PracticeShopApiNetworkJournal.Exchange read,
                                   String productId, int quantity) {
        String expectedQuantity = "\"quantity\":" + quantity;
        return List.of(
                check("API request JSON · cart add", "product_id=" + productId + ", quantity=" + quantity,
                        add.requestBody(), isJson(add.requestBody()) && add.requestBody().contains(productId)
                                && add.requestBody().contains(expectedQuantity)),
                check("API response · cart add", "200 application/json", add.summary(),
                        add.status() == 200 && add.isJsonResponse()),
                check("API response JSON · cart read", "cart_items contains " + productId
                                + " with quantity " + quantity,
                        "product_id=" + productId + ", quantity=" + quantity + ", response="
                                + compact(read.responseBody(), 280),
                        isJson(read.responseBody()) && read.responseBody().contains(productId)
                                && read.responseBody().contains(expectedQuantity)));
    }

    public static List<AtfAssertion> payment(PracticeShopApiNetworkJournal.Exchange payment,
                                      String paymentMethod) {
        String expectedMethod = paymentMethod.toLowerCase().replace(' ', '-');
        return List.of(
                check("API request JSON · payment", "payment_method=" + expectedMethod,
                        payment.requestBody(), isJson(payment.requestBody())
                                && payment.requestBody().contains(expectedMethod)),
                check("API response JSON · payment", "Payment was successful",
                        payment.responseBody(), isJson(payment.responseBody())
                                && payment.responseBody().contains("Payment was successful")),
                check("API response headers · payment", "application/json", payment.contentType(),
                        payment.isJsonResponse()));
    }

    private static boolean isJson(String body) {
        if (body == null || body.isBlank()) return false;
        try { JSON.readTree(body); return true; }
        catch (Exception ignored) { return false; }
    }

    private static boolean positiveContentLength(PracticeShopApiNetworkJournal.Exchange exchange) {
        try { return Long.parseLong(header(exchange, "content-length")) > 0; }
        catch (NumberFormatException ignored) { return false; }
    }

    private static String header(PracticeShopApiNetworkJournal.Exchange exchange, String name) {
        return exchange.responseHeaders().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(java.util.Map.Entry::getValue)
                .findFirst().orElse("unknown");
    }

    private static String compact(String value, int limit) {
        String normalized = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= limit ? normalized : normalized.substring(0, limit) + "…";
    }
}
