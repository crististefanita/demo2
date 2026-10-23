package atf.ui.tests.autoex.component.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.api.AutoExApiAccount;
import atf.ui.autoex.api.AutoExApiRequestFactory;
import atf.ui.autoex.api.AutoExApiRoute;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Executes the consumer request catalog through the core executor and a deterministic offline transport. */
@AtfComponentTest
class AutoExApiContractComponentTest {
    private static final int FIXTURE_HTTP_STATUS = 200;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String EMAIL = "atf-autoex-contract@example.test";
    private static final String PASSWORD = "synthetic-contract-password";
    private static final AutoExApiRequestFactory REQUESTS = new AutoExApiRequestFactory();
    private static final AutoExApiAccount ACCOUNT = new AutoExApiAccount(
            "ATF Contract", EMAIL, PASSWORD, "Mr", "10", "5", "1990", "ATF", "Contract",
            "ATF", "1 Example Street", "", "United States", "10001", "NY", "New York", "5550100");

    @Test
    void allFourteenPublishedApiScenariosUseTheirExpectedHttpContractOffline() {
        List<ContractCase> cases = List.of(
                contract("API 1 products list", REQUESTS.productsList(), "GET", 200, AutoExApiRoute.PRODUCTS_LIST),
                contract("API 2 unsupported products POST", REQUESTS.postProductsList(), "POST", 405,
                        AutoExApiRoute.PRODUCTS_LIST),
                contract("API 3 brands list", REQUESTS.brandsList(), "GET", 200, AutoExApiRoute.BRANDS_LIST),
                contract("API 4 unsupported brands PUT", REQUESTS.putBrandsList(), "PUT", 405,
                        AutoExApiRoute.BRANDS_LIST),
                contract("API 5 product search", REQUESTS.searchProduct("top"), "POST", 200,
                        AutoExApiRoute.SEARCH_PRODUCT),
                contract("API 6 search parameter missing", REQUESTS.searchProduct(null), "POST", 400,
                        AutoExApiRoute.SEARCH_PRODUCT),
                contract("API 7 valid login", REQUESTS.verifyLogin(EMAIL, PASSWORD), "POST", 200,
                        AutoExApiRoute.VERIFY_LOGIN),
                contract("API 8 email missing", REQUESTS.verifyLoginWithoutEmail(PASSWORD), "POST", 400,
                        AutoExApiRoute.VERIFY_LOGIN),
                contract("API 9 unsupported login DELETE", REQUESTS.deleteMethodOnVerifyLogin(), "DELETE", 405,
                        AutoExApiRoute.VERIFY_LOGIN),
                contract("API 10 invalid login", REQUESTS.verifyLogin("missing@example.test", "wrong"), "POST", 404,
                        AutoExApiRoute.VERIFY_LOGIN),
                contract("API 11 create account", REQUESTS.createAccount(ACCOUNT), "POST", 201,
                        AutoExApiRoute.CREATE_ACCOUNT),
                contract("API 12 delete account", REQUESTS.deleteAccount(EMAIL, PASSWORD), "DELETE", 200,
                        AutoExApiRoute.DELETE_ACCOUNT),
                contract("API 13 update account", REQUESTS.updateAccount(ACCOUNT), "PUT", 200,
                        AutoExApiRoute.UPDATE_ACCOUNT),
                contract("API 14 user detail", REQUESTS.userDetail(EMAIL), "GET", 200,
                        AutoExApiRoute.USER_DETAIL_BY_EMAIL));

        for (ContractCase testCase : cases) {
            var calls = new AtomicInteger();
            ApiTransport fixture = (request, url, auth) -> {
                calls.incrementAndGet();
                assertEquals(testCase.route().path(), request.path(), testCase.name() + " path");
                assertEquals(testCase.expectedMethod(), request.method(), testCase.name() + " method");
                assertTrue(url.startsWith("https://automationexercise.invalid"), testCase.name() + " target");
                return CompletableFuture.completedFuture(new ApiResponse(FIXTURE_HTTP_STATUS,
                        responseBody(testCase), Map.of(), null, url));
            };
            ApiResponse actual = new ApiExecutor(fixture,
                    URI.create("https://automationexercise.invalid")).execute(testCase.request()).response();

            assertAll(testCase.name(),
                    () -> assertEquals(FIXTURE_HTTP_STATUS, actual.status(),
                            "fixture transport status is deliberately separate from documented responseCode"),
                    () -> assertEquals(testCase.expectedResponseCode(), responseCode(actual.body()),
                            "documented business responseCode"),
                    () -> assertTrue(actual.body().contains(expectedResponseField(testCase)),
                            "response must carry the minimum endpoint-specific shape/message"),
                    () -> assertEquals(1, calls.get(), "one scenario means one transport exchange"));
        }
        assertEquals(14, cases.size(), "all published scenarios must be represented");
    }

    @Test
    void requestsUseFormFieldsAndQueryParametersWithoutLeakingCredentialValuesInAccountModel() {
        ApiRequest login = REQUESTS.verifyLogin(EMAIL, PASSWORD);
        ApiRequest detail = REQUESTS.userDetail(EMAIL);
        ApiRequest delete = REQUESTS.deleteAccount(EMAIL, PASSWORD);
        Map<String, String> loginFields = decodeForm(login.bodyText());
        Map<String, String> deleteFields = decodeForm(delete.bodyText());

        assertAll(
                () -> assertEquals("POST", login.method()),
                () -> assertEquals(EMAIL, loginFields.get("email")),
                () -> assertEquals(PASSWORD, loginFields.get("password")),
                () -> assertEquals("DELETE", delete.method()),
                () -> assertEquals(EMAIL, deleteFields.get("email")),
                () -> assertEquals(PASSWORD, deleteFields.get("password")),
                () -> assertEquals("application/x-www-form-urlencoded",
                        delete.headers().get("Content-Type")),
                () -> assertEquals(Map.of("email", EMAIL), detail.query()),
                () -> assertEquals(AutoExApiRoute.USER_DETAIL_BY_EMAIL.path(), detail.path()),
                () -> assertFalse(ACCOUNT.toString().contains(PASSWORD),
                        "typed account diagnostics must redact its password"),
                () -> assertTrue(ACCOUNT.formFields().keySet().containsAll(SetOfRequiredAccountFields.VALUE)));
    }

    @Test
    void accountMutationsCarryTheCompletePublishedFormVocabularyAndSearchIsEncoded() {
        ApiRequest create = REQUESTS.createAccount(ACCOUNT);
        ApiRequest update = REQUESTS.updateAccount(ACCOUNT);
        ApiRequest search = REQUESTS.searchProduct("top & tools");
        Map<String, String> createFields = decodeForm(create.bodyText());
        Map<String, String> updateFields = decodeForm(update.bodyText());

        assertAll(
                () -> assertEquals("POST", create.method()),
                () -> assertEquals("PUT", update.method()),
                () -> assertEquals(SetOfRequiredAccountFields.VALUE, createFields.keySet()),
                () -> assertEquals(SetOfRequiredAccountFields.VALUE, updateFields.keySet()),
                () -> assertEquals("top & tools", decodeForm(search.bodyText()).get("search_product")),
                () -> assertTrue(search.bodyText().contains("top+%26+tools")),
                () -> assertTrue(createFields.entrySet().stream()
                                .filter(field -> !field.getKey().equals("address2"))
                                .noneMatch(field -> field.getValue().isBlank()),
                        "the deterministic account fixture must satisfy every required field"));
    }

    private static ContractCase contract(String name, ApiRequest request, String method, int responseCode,
                                         AutoExApiRoute route) {
        return new ContractCase(name, request, method, responseCode, route);
    }

    private static String responseBody(ContractCase testCase) {
        return switch (testCase.expectedResponseCode()) {
            case 200 -> "{\"responseCode\":200,\"message\":\"" + expectedResponseField(testCase)
                    + "\",\"products\":[],\"brands\":[],\"searched_products\":[],\"user\":{}}";
            case 201 -> "{\"responseCode\":201,\"message\":\"User created!\"}";
            case 400 -> "{\"responseCode\":400,\"message\":\"Bad request\"}";
            case 404 -> "{\"responseCode\":404,\"message\":\"User not found!\"}";
            case 405 -> "{\"responseCode\":405,\"message\":\"This request method is not supported.\"}";
            default -> throw new IllegalArgumentException(
                    "No fixture response for responseCode " + testCase.expectedResponseCode());
        };
    }

    private static String expectedResponseField(ContractCase testCase) {
        if (testCase.expectedResponseCode() == 405) return "This request method is not supported.";
        if (testCase.expectedResponseCode() == 400) return "Bad request";
        if (testCase.expectedResponseCode() == 404) return "User not found!";
        if (testCase.expectedResponseCode() == 201) return "User created!";
        return switch (testCase.route()) {
            case PRODUCTS_LIST -> "products";
            case BRANDS_LIST -> "brands";
            case SEARCH_PRODUCT -> "searched_products";
            case VERIFY_LOGIN -> "User exists!";
            case CREATE_ACCOUNT -> "User created!";
            case DELETE_ACCOUNT -> "Account deleted!";
            case UPDATE_ACCOUNT -> "User updated!";
            case USER_DETAIL_BY_EMAIL -> "user";
        };
    }

    private static int responseCode(String responseBody) {
        try {
            JsonNode document = JSON.readTree(responseBody);
            return document.path("responseCode").asInt(Integer.MIN_VALUE);
        } catch (java.io.IOException invalidJson) {
            throw new AssertionError("fixture response must be valid JSON", invalidJson);
        }
    }

    private static Map<String, String> decodeForm(String body) {
        Map<String, String> values = new LinkedHashMap<>();
        if (body == null || body.isBlank()) return values;
        for (String pair : body.split("&")) {
            String[] field = pair.split("=", 2);
            values.put(URLDecoder.decode(field[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(field.length == 1 ? "" : field[1], StandardCharsets.UTF_8));
        }
        return values;
    }

    private record ContractCase(String name, ApiRequest request, String expectedMethod, int expectedResponseCode,
                                AutoExApiRoute route) { }

    private static final class SetOfRequiredAccountFields {
        private static final java.util.Set<String> VALUE = java.util.Set.of("name", "email", "password",
                "title", "birth_date", "birth_month", "birth_year", "firstname", "lastname",
                "company", "address1", "address2", "country", "zipcode", "state", "city",
                "mobile_number");
    }
}
