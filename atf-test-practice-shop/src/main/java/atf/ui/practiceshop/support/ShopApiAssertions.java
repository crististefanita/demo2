package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.api.ReportRequestSignature;
import atf.api.reporting.api.ReportQueryParameter;
import atf.api.reporting.api.ReportQueryParameters;
import atf.api.core.http.request.MediaTypes;
import atf.ui.practiceshop.api.ShopApiOperation;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.practiceshop.api.ShopApiJsonField;
import atf.ui.practiceshop.api.ShopApiQueryParameter;
import atf.ui.practiceshop.api.ShopApiRoutes;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.List;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** Pure API contract projection used by the UI/API pyramid. */
public final class ShopApiAssertions {
    private static final ShopApiHttpStatus SUCCESS_STATUS = ShopApiHttpStatus.OK;

    private ShopApiAssertions() { }

    static String productId(String responseBody, String productName) {
        JsonNode root = requiredJson(responseBody, "Catalog response");
        JsonNode data = root.path(ShopApiJsonField.DATA.wireValue());
        if (!data.isArray()) throw new IllegalStateException("Catalog response data is not an array");
        for (JsonNode product : data) {
            if (productName.equalsIgnoreCase(product.path("name").asText())) {
                String id = product.path("id").asText();
                if (!id.isBlank()) return id;
            }
        }
        throw new IllegalStateException("Catalog search did not return product: " + productName);
    }

    public static List<AtfAssertion> product(ShopApiNetworkJournal.Exchange search,
                                      ShopApiNetworkJournal.Exchange detail,
                                      String productName, String productId) {
        boolean searchSucceeded = search != null
                && SUCCESS_STATUS.matches(search.status())
                && search.isJsonResponse();
        return List.of(
                check("API precondition · catalog search",
                        ShopApiOperation.SEARCH.wireValue() + " "
                                + ShopApiRoutes.PRODUCT_SEARCH + " · " + SUCCESS_STATUS.wireValue(),
                        search == null ? "missing" : search.reportSummary(), searchSucceeded),
                check("API request query · catalog search",
                        ShopApiQueryParameter.SEARCH.wireValue() + "=" + productName,
                        search.requestBody().isBlank()
                                ? query(search.url(), ShopApiQueryParameter.SEARCH.wireValue())
                                : search.requestBody(),
                        exactText(search.requestBody(), ShopApiQueryParameter.SEARCH.wireValue(), productName)
                                || productName.equals(query(search.url(), ShopApiQueryParameter.SEARCH.wireValue()))),
                check("API response JSON · product identity", productName + " / " + productId,
                        "id=" + productId + ", name=" + productName + ", response="
                                + compact(detail.responseBody(), 280),
                        exactText(detail.responseBody(), ShopApiJsonField.ID.wireValue(), productId)
                                && exactText(detail.responseBody(), ShopApiJsonField.NAME.wireValue(),
                                productNameForId(search.responseBody(), productId))),
                check("API response headers · product detail", MediaTypes.APPLICATION_JSON,
                        detail.contentType(), detail.isJsonResponse()),
                trace("catalog search", search),
                trace("product detail", detail));
    }

    public static List<AtfAssertion> login(ShopApiNetworkJournal.Exchange login) {
        String loginResponseBody = login.responseBody();
        JsonNode loginJson = ShopJsonEvidence.parse(loginResponseBody);
        boolean readableTokenJson = loginJson != null && hasTextField(loginJson,
                ShopApiJsonField.ACCESS_TOKEN.wireValue(),
                ShopApiJsonField.ACCESS_TOKEN_CAMEL.wireValue(),
                ShopApiJsonField.TOKEN.wireValue());
        boolean validJsonTransport = SUCCESS_STATUS.matches(login.status())
                && login.isJsonResponse()
                && (readableTokenJson || positiveContentLength(login));
        return List.of(
                check("API login status", SUCCESS_STATUS.wireValue(), Integer.toString(login.status()),
                        SUCCESS_STATUS.matches(login.status())),
                check("API login request JSON", "email and password fields are sent",
                login.requestBody(), isJson(login.requestBody())
                                && field(login.requestBody(), ShopApiJsonField.EMAIL.wireValue()) != null
                                && field(login.requestBody(), ShopApiJsonField.PASSWORD.wireValue()) != null),
                check("API login response JSON", "authenticated token response or non-empty JSON transport",
                        readableTokenJson ? compact(loginResponseBody, 260)
                                : "body unavailable to Playwright transport; content-type=" + login.contentType()
                                + ", content-length=" + header(login, "content-length"),
                        validJsonTransport),
                trace("login", login));
    }

    public static List<AtfAssertion> cart(ShopApiNetworkJournal.Exchange add,
                                   ShopApiNetworkJournal.Exchange read,
                                   String productId, int quantity) {
        String addRequestBody = add.requestBody();
        String readResponseBody = read.responseBody();
        JsonNode addJson = ShopJsonEvidence.parse(addRequestBody);
        JsonNode readJson = ShopJsonEvidence.parse(readResponseBody);
        return List.of(
                check("API request JSON · cart add", "product_id=" + productId + ", quantity=" + quantity,
                        addRequestBody, exactText(addJson, ShopApiJsonField.PRODUCT_ID.wireValue(), productId)
                                && exactInt(addJson, ShopApiJsonField.QUANTITY.wireValue(), quantity)),
                check("API response · cart add",
                        SUCCESS_STATUS.wireValue() + " application/json", add.summary(),
                        SUCCESS_STATUS.matches(add.status()) && add.isJsonResponse()),
                check("API response JSON · cart read", "cart_items contains " + productId
                                + " with quantity " + quantity,
                        "product_id=" + productId + ", quantity=" + quantity + ", response="
                                + compact(readResponseBody, 280),
                        cartItemMatches(readJson, productId, quantity)),
                trace("cart add", add),
                trace("cart read", read));
    }

    public static List<AtfAssertion> payment(ShopApiNetworkJournal.Exchange payment,
                                      String paymentMethod) {
        String expectedMethod = paymentMethod.toLowerCase().replace(' ', '-');
        String requestBody = payment.requestBody();
        String responseBody = payment.responseBody();
        JsonNode request = ShopJsonEvidence.parse(requestBody);
        JsonNode response = ShopJsonEvidence.parse(responseBody);
        return List.of(
                check("API request JSON · payment", "payment_method=" + expectedMethod,
                        requestBody, exactText(request, ShopApiJsonField.PAYMENT_METHOD.wireValue(),
                                expectedMethod)),
                check("API response JSON · payment", "Payment was successful",
                        responseBody, exactText(response, ShopApiJsonField.MESSAGE.wireValue(),
                                "Payment was successful")),
                check("API response headers · payment", MediaTypes.APPLICATION_JSON, payment.contentType(),
                        payment.isJsonResponse()),
                trace("payment", payment));
    }

    public static List<AtfAssertion> order(ShopApiNetworkJournal.Exchange order,
                                           String uiConfirmation) {
        JsonNode response = order == null ? null : ShopJsonEvidence.parse(order.responseBody());
        String invoice = response == null ? "" : response.path(
                ShopApiJsonField.INVOICE_NUMBER.wireValue()).asText("");
        JsonNode total = response == null ? null : response.get(ShopApiJsonField.TOTAL.wireValue());
        boolean positiveTotal = total != null && total.isNumber() && total.decimalValue().signum() > 0;
        return List.of(
                check("API response · order creation", "2xx application/json",
                        order == null ? "missing" : order.summary(), order != null
                                && ShopApiHttpStatus.isSuccess(order.status()) && order.isJsonResponse()),
                check("API response JSON · order identity", "order id and invoice number are present",
                        response == null ? order == null ? "missing" : compact(order.responseBody(), 240)
                                : "id=" + response.path(ShopApiJsonField.ID.wireValue()).asText("")
                                + ", invoice=" + invoice,
                        response != null
                                && !response.path(ShopApiJsonField.ID.wireValue()).asText("").isBlank()
                                && !invoice.isBlank()),
                check("UI/API order correlation", "invoice number from order creation request is shown in confirmation",
                        "invoice=" + invoice + "; UI=" + compact(uiConfirmation, 220),
                        !invoice.isBlank() && uiConfirmation != null && uiConfirmation.contains(invoice)),
                check("API response JSON · order total", "total is greater than zero",
                        total == null ? "missing" : total.asText(), positiveTotal),
                trace("order creation", order));
    }

    private static AtfAssertion trace(String label, ShopApiNetworkJournal.Exchange exchange) {
        boolean present = exchange != null && !exchange.signature().isBlank();
        String observed = exchange == null ? "missing" : "sequence="
                + (exchange.sequence() > 0 ? exchange.sequence() : "n/a")
                + "; phase=" + exchange.phase()
                + "; signature=" + ReportRequestSignature.displaySignature(exchange.signature());
        return check("API trace · " + label,
                "request signature + phase + capture order", observed, present);
    }

    private static boolean isJson(String body) { return ShopJsonEvidence.isJson(body); }

    private static JsonNode requiredJson(String body, String label) {
        JsonNode node = ShopJsonEvidence.parse(body);
        if (node == null) throw new IllegalStateException(label + " is not usable JSON");
        return node;
    }

    private static JsonNode field(String body, String name) {
        JsonNode root = ShopJsonEvidence.parse(body);
        return root == null ? null : root.get(name);
    }

    private static boolean hasTextField(JsonNode root, String... names) {
        for (String name : names) {
            JsonNode value = root.get(name);
            if (value != null && value.isTextual() && !value.asText().isBlank()) return true;
        }
        return false;
    }

    private static boolean exactText(String body, String path, String expected) {
        return exactText(ShopJsonEvidence.parse(body), path, expected);
    }

    private static boolean exactText(JsonNode root, String path, String expected) {
        JsonNode value = root == null ? null : root.at("/" + path.replace(".", "/"));
        return value != null && value.isTextual() && expected.equals(value.asText());
    }

    private static boolean exactInt(JsonNode root, String path, int expected) {
        JsonNode value = root == null ? null : root.at("/" + path.replace(".", "/"));
        return value != null && value.isIntegralNumber() && expected == value.intValue();
    }

    private static boolean cartItemMatches(JsonNode root, String productId, int quantity) {
        if (root == null || !root.path(ShopApiJsonField.CART_ITEMS.wireValue()).isArray()) return false;
        for (JsonNode item : root.path(ShopApiJsonField.CART_ITEMS.wireValue())) {
            if (exactText(item, ShopApiJsonField.PRODUCT_ID.wireValue(), productId)
                    && exactInt(item, ShopApiJsonField.QUANTITY.wireValue(), quantity)) return true;
        }
        return false;
    }

    private static String productNameForId(String body, String productId) {
        JsonNode root = ShopJsonEvidence.parse(body);
        JsonNode data = root == null ? null
                : root.path(ShopApiJsonField.DATA.wireValue());
        if (data != null && data.isArray()) {
            for (JsonNode product : data) {
                if (productId.equals(product.path(ShopApiJsonField.ID.wireValue()).asText())) {
                    return product.path(ShopApiJsonField.NAME.wireValue()).asText("");
                }
            }
        }
        return "";
    }

    private static String query(String url, String name) {
        return ReportQueryParameters.fromUri(URI.create(url)).values().stream()
                .filter(parameter -> parameter.name().equals(name))
                .map(ReportQueryParameter::value)
                .findFirst().orElse("");
    }

    private static boolean positiveContentLength(ShopApiNetworkJournal.Exchange exchange) {
        try { return Long.parseLong(header(exchange, "content-length")) > 0; }
        catch (NumberFormatException ignored) { return false; }
    }

    private static String header(ShopApiNetworkJournal.Exchange exchange, String name) {
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
