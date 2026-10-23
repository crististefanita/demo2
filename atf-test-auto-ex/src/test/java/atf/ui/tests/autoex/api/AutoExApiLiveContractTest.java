package atf.ui.tests.autoex.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.core.config.ApiSettings;
import atf.api.core.config.ApiTransportPolicy;
import atf.api.core.config.ApiTransportMode;
import atf.api.core.http.auth.AuthProfiles;
import atf.api.core.http.auth.ConfiguredApiAuthenticator;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.transport.ApiLiveTransportCatalog;
import atf.api.core.http.transport.UrlConnectionTransport;
import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.testkit.AtfConsoleMessages;
import atf.api.testkit.AtfIntegrationTest;
import atf.api.testkit.reporting.AtfReportPath;
import atf.ui.autoex.api.AutoExApiAccount;
import atf.ui.autoex.api.AutoExApiJsonField;
import atf.ui.autoex.api.AutoExApiRequestFactory;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.config.AutoExConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Opt-in live contract proof; default component gates never contact the public service. */
@AtfIntegrationTest
@EnabledIfSystemProperty(named = "atf.autoex.api.live.enabled", matches = "true")
@AtfConsoleMessages(AtfConsoleMessages.Policy.DETAILS)
@AtfReportPath
@ExtendWith({AtfConsoleTestExtension.class, AutoExApiContractReportExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
public class AutoExApiLiveContractTest {
    static final String BASE_URL_PROPERTY = "atf.autoex.api.base-url";
    static final String TIMEOUT_PROPERTY = "atf.autoex.api.timeout";
    private static final String DEFAULT_TIMEOUT = "PT20S";
    private static final int EXPECTED_HTTP_STATUS = 200;
    private static final String UNSUPPORTED_METHOD_MESSAGE = "This request method is not supported.";
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void publicApiContractsUseAUniqueAccountAndAlwaysAttemptCleanup() {
        AutoExConfiguration configuration = AutoExConfigurationLoader.load();
        ApiExecutor api = liveExecutor(configuration);
        AutoExApiRequestFactory requests = new AutoExApiRequestFactory();
        AutoExApiAccount account = account(configuration, UUID.randomUUID().toString());

        assertResponse("API 1 products list", api, requests.productsList(), 200, null,
                AutoExApiJsonField.PRODUCTS);
        assertResponse("API 2 unsupported products POST", api, requests.postProductsList(), 405,
                UNSUPPORTED_METHOD_MESSAGE);
        assertResponse("API 3 brands list", api, requests.brandsList(), 200, null,
                AutoExApiJsonField.BRANDS);
        assertResponse("API 4 unsupported brands PUT", api, requests.putBrandsList(), 405,
                UNSUPPORTED_METHOD_MESSAGE);
        assertResponse("API 5 search product", api,
                requests.searchProduct(configuration.catalog().searchTerm()), 200, null,
                AutoExApiJsonField.PRODUCTS);
        assertResponse("API 6 missing search parameter", api, requests.searchProduct(null), 400,
                "Bad request, search_product parameter is missing in POST request.");
        assertResponse("API 8 missing login email", api,
                requests.verifyLoginWithoutEmail(account.password()), 400,
                "Bad request, email or password parameter is missing in POST request.");
        assertResponse("API 9 unsupported login DELETE", api, requests.deleteMethodOnVerifyLogin(), 405,
                UNSUPPORTED_METHOD_MESSAGE);
        assertResponse("API 10 invalid login", api,
                requests.verifyLogin(account.email(), "invalid-" + account.password()), 404,
                "User not found!");

        try (AutoExApiAccountCleanup cleanup = new AutoExApiAccountCleanup(() ->
                assertResponse("API 12 cleanup", api,
                        requests.deleteAccount(account.email(), account.password()), 200, "Account deleted!"))) {
            cleanup.armBeforeCreateAttempt();
            assertResponse("API 11 create account", api, requests.createAccount(account), 201,
                    "User created!");
            assertResponse("API 7 valid login", api,
                    requests.verifyLogin(account.email(), account.password()), 200, "User exists!");
            assertResponseWithTextFields("API 14 user detail", api,
                    requests.userDetail(account.email()), 200, null,
                    Map.of(AutoExApiJsonField.USER_EMAIL.path(), account.email()));
            assertResponse("API 13 update account", api,
                    requests.updateAccount(updatedName(account)), 200, "User updated!");
        }
    }

    static ApiExecutor liveExecutor(AutoExConfiguration configuration) {
        URI baseUrl = URI.create(System.getProperty(BASE_URL_PROPERTY,
                configuration.baseUrl().toString()));
        Duration timeout = Duration.parse(System.getProperty(TIMEOUT_PROPERTY, DEFAULT_TIMEOUT));
        ApiLiveTransportCatalog catalog = ApiLiveTransportCatalog.single("autoex-live", baseUrl,
                AuthProfiles.NONE, timeout,
                (endpoint, authenticator) -> new UrlConnectionTransport(authenticator, endpoint.timeout()));
        var transport = catalog.open("autoex-live", ApiTransportPolicy.explicitLive(),
                new ConfiguredApiAuthenticator(Map.of()));
        var settings = new ApiSettings(baseUrl, AuthProfiles.NONE, timeout, Set.of(), 1);
        return new ApiExecutor(transport, settings.snapshot(), ApiTransportMode.LIVE);
    }

    private static AutoExApiAccount account(AutoExConfiguration configuration, String unique) {
        AutoExConfiguration.Registration profile = configuration.registration();
        return new AutoExApiAccount("ATF AutoEx " + unique,
                "atf-autoex+" + unique + "@example.com", "Atf-" + unique + "-Secure",
                "Mr", profile.birthDay(), profile.birthMonth(), profile.birthYear(), profile.firstName(),
                profile.lastName(), profile.company(), profile.address1(), profile.address2(),
                profile.country(), profile.postalCode(), profile.state(), profile.city(), profile.mobileNumber());
    }

    private static AutoExApiAccount updatedName(AutoExApiAccount account) {
        return new AutoExApiAccount(account.name() + " Updated", account.email(), account.password(),
                account.title(), account.birthDate(), account.birthMonth(), account.birthYear(),
                account.firstName(), account.lastName(), account.company(), account.address1(),
                account.address2(), account.country(), account.zipcode(), account.state(), account.city(),
                account.mobileNumber());
    }

    static JsonNode assertResponse(String name, ApiExecutor api, ApiRequest request, int responseCode) {
        return assertResponse(name, api, request, responseCode, null, Map.of(), new AutoExApiJsonField[0]);
    }

    static JsonNode assertResponse(String name, ApiExecutor api, ApiRequest request,
                                   int responseCode, String message) {
        return assertResponse(name, api, request, responseCode, message, Map.of(), new AutoExApiJsonField[0]);
    }

    static JsonNode assertResponse(String name, ApiExecutor api, ApiRequest request,
                                   int responseCode, String message, AutoExApiJsonField... expectedArrayFields) {
        return assertResponse(name, api, request, responseCode, message, Map.of(), expectedArrayFields);
    }

    static JsonNode assertResponseWithTextFields(String name, ApiExecutor api, ApiRequest request,
                                                 int responseCode, String message,
                                                 Map<String, String> expectedTextFields) {
        return assertResponse(name, api, request, responseCode, message, expectedTextFields,
                new AutoExApiJsonField[0]);
    }

    static JsonNode assertSearchResponse(String name, ApiExecutor api, ApiRequest request,
                                         int responseCode, String searchTerm) {
        return assertResponse(name, api, request, responseCode, null, Map.of(),
                new AutoExApiJsonField[]{AutoExApiJsonField.PRODUCTS}, searchTerm);
    }

    private static JsonNode assertResponse(String name, ApiExecutor api, ApiRequest request,
                                           int responseCode, String message,
                                           Map<String, String> expectedTextFields,
                                           AutoExApiJsonField... expectedArrayFields) {
        return assertResponse(name, api, request, responseCode, message, expectedTextFields,
                expectedArrayFields, null);
    }

    private static JsonNode assertResponse(String name, ApiExecutor api, ApiRequest request,
                                           int responseCode, String message,
                                           Map<String, String> expectedTextFields,
                                           AutoExApiJsonField[] expectedArrayFields,
                                           String expectedSearchTerm) {
        var execution = api.execute(request);
        var response = execution.response();
        JsonNode body = null;
        java.io.IOException parseFailure = null;
        try {
            body = JSON.readTree(response.body());
        } catch (java.io.IOException invalidJson) {
            parseFailure = invalidJson;
        }
        JsonNode parsedBody = body;
        var semanticAssertions = new java.util.ArrayList<AtfAssertion>();
        semanticAssertions.add(new AtfAssertion("HTTP transport status",
                Integer.toString(EXPECTED_HTTP_STATUS), Integer.toString(response.status()),
                response.status() == EXPECTED_HTTP_STATUS));
        expectedTextFields.forEach((path, expected) -> {
            JsonNode value = jsonPath(parsedBody, path);
            String actual = value == null ? "<missing>"
                    : value.isTextual() ? value.asText() : value.toString();
            semanticAssertions.add(new AtfAssertion("JSON text field: " + path, expected, actual,
                    expected.equals(actual)));
        });
        java.util.Arrays.stream(expectedArrayFields).forEach(field -> {
            JsonNode value = parsedBody == null ? null : parsedBody.get(field.path());
            String actual = value == null ? "<missing>" : value.getNodeType().name();
            semanticAssertions.add(new AtfAssertion("JSON array field: " + field.path(), "ARRAY", actual,
                    value != null && value.isArray()));
        });
        AtfAssertion searchAssertion = expectedSearchTerm == null ? null
                : AutoExApiResponseAssertions.searchResultsContain(parsedBody, expectedSearchTerm);
        if (searchAssertion != null) semanticAssertions.add(searchAssertion);
        AutoExApiContractReportExtension.publish(name, execution, responseCode, message, semanticAssertions);
        assertEquals(EXPECTED_HTTP_STATUS, response.status(), name + " transport HTTP status");
        if (parseFailure != null) throw new AssertionError(name + " response body must be valid JSON", parseFailure);
        assertEquals(responseCode, body.path(AutoExApiJsonField.RESPONSE_CODE.path()).asInt(Integer.MIN_VALUE),
                name + " JSON responseCode (transport HTTP status=" + response.status() + ")");
        if (message != null) {
            assertEquals(message, body.path(AutoExApiJsonField.MESSAGE.path()).asText(null), name + " JSON message");
        }
        for (Map.Entry<String, String> expectation : expectedTextFields.entrySet()) {
            String path = expectation.getKey();
            JsonNode value = jsonPath(body, path);
            String actual = value == null ? "<missing>"
                    : value.isTextual() ? value.asText() : value.toString();
            assertEquals(expectation.getValue(), actual, name + " JSON field " + path);
        }
        for (AutoExApiJsonField field : expectedArrayFields) {
            assertTrue(body.path(field.path()).isArray(),
                    name + " JSON field '" + field.path() + "' must be an array");
        }
        if (searchAssertion != null) {
            assertTrue(searchAssertion.passed(), name + " must return a product matching '"
                    + expectedSearchTerm + "' (actual: " + searchAssertion.actual() + ")");
        }
        return body;
    }

    private static JsonNode jsonPath(JsonNode document, String path) {
        JsonNode current = document;
        for (String segment : path.split("\\.")) {
            if (current == null) return null;
            current = current.get(segment);
        }
        return current;
    }

}
