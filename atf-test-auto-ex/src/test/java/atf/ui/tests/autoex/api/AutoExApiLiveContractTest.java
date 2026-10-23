package atf.ui.tests.autoex.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import atf.api.core.config.ApiSettings;
import atf.api.core.config.ApiTransportPolicy;
import atf.api.core.config.ApiTransportMode;
import atf.api.core.http.auth.AuthProfiles;
import atf.api.core.http.auth.ConfiguredApiAuthenticator;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.transport.ApiLiveTransportCatalog;
import atf.api.testkit.AtfIntegrationTest;
import atf.ui.autoex.api.AutoExApiAccount;
import atf.ui.autoex.api.AutoExApiRequestFactory;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.config.AutoExConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Opt-in live contract proof; default component gates never contact the public service. */
@AtfIntegrationTest
@EnabledIfSystemProperty(named = "atf.autoex.api.live.enabled", matches = "true")
public class AutoExApiLiveContractTest {
    private static final String BASE_URL_PROPERTY = "atf.autoex.api.base-url";
    private static final String TIMEOUT_PROPERTY = "atf.autoex.api.timeout";
    private static final String DEFAULT_TIMEOUT = "PT20S";
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void publicApiContractsUseAUniqueAccountAndAlwaysAttemptCleanup() {
        AutoExConfiguration configuration = AutoExConfigurationLoader.load();
        URI baseUrl = URI.create(System.getProperty(BASE_URL_PROPERTY,
                configuration.baseUrl().toString()));
        Duration timeout = Duration.parse(System.getProperty(TIMEOUT_PROPERTY, DEFAULT_TIMEOUT));
        ApiExecutor api = liveExecutor(baseUrl, timeout);
        AutoExApiRequestFactory requests = new AutoExApiRequestFactory();
        AutoExApiAccount account = account(configuration, UUID.randomUUID().toString());

        assertResponseCode("API 1 products list", api, requests.productsList(), 200);
        assertResponseCode("API 2 unsupported products POST", api, requests.postProductsList(), 405);
        assertResponseCode("API 3 brands list", api, requests.brandsList(), 200);
        assertResponseCode("API 4 unsupported brands PUT", api, requests.putBrandsList(), 405);
        assertResponseCode("API 5 search product", api,
                requests.searchProduct(configuration.catalog().searchTerm()), 200);
        assertResponseCode("API 6 missing search parameter", api, requests.searchProduct(null), 400);
        assertResponseCode("API 8 missing login email", api,
                requests.verifyLoginWithoutEmail(account.password()), 400);
        assertResponseCode("API 9 unsupported login DELETE", api, requests.deleteMethodOnVerifyLogin(), 405);
        assertResponseCode("API 10 invalid login", api,
                requests.verifyLogin(account.email(), "invalid-" + account.password()), 404);

        try (AccountCleanup cleanup = new AccountCleanup(api,
                requests.deleteAccount(account.email(), account.password()))) {
            cleanup.armBeforeCreateAttempt();
            assertResponseCode("API 11 create account", api, requests.createAccount(account), 201);
            assertResponseCode("API 7 valid login", api,
                    requests.verifyLogin(account.email(), account.password()), 200);
            var detail = assertResponseCode("API 14 user detail", api,
                    requests.userDetail(account.email()), 200);
            assertTrue(detail.path("user").toString().contains(account.email()),
                    "detail response must identify the synthetic account used by this scenario");
            assertResponseCode("API 13 update account", api,
                    requests.updateAccount(updatedName(account)), 200);
        }
    }

    private static ApiExecutor liveExecutor(URI baseUrl, Duration timeout) {
        ApiLiveTransportCatalog catalog = ApiLiveTransportCatalog.single("autoex-live", baseUrl,
                AuthProfiles.NONE, timeout, HttpClient.newBuilder().connectTimeout(timeout).build());
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

    private static JsonNode assertResponseCode(String name, ApiExecutor api, ApiRequest request, int expected) {
        var response = api.execute(request).response();
        assertTrue(response.status() >= 200 && response.status() < 500,
                name + " returned unexpected transport HTTP status " + response.status());
        final JsonNode body;
        try {
            body = JSON.readTree(response.body());
        } catch (java.io.IOException invalidJson) {
            throw new AssertionError(name + " response body must be valid JSON", invalidJson);
        }
        assertEquals(expected, body.path("responseCode").asInt(Integer.MIN_VALUE),
                name + " JSON responseCode (transport HTTP status=" + response.status() + ")");
        return body;
    }

    private static final class AccountCleanup implements AutoCloseable {
        private final ApiExecutor api;
        private final ApiRequest cleanupRequest;
        private boolean armed;

        private AccountCleanup(ApiExecutor api, ApiRequest cleanupRequest) {
            this.api = api;
            this.cleanupRequest = cleanupRequest;
        }

        void armBeforeCreateAttempt() { armed = true; }

        @Override
        public void close() {
            if (!armed) return;
            assertResponseCode("API 12 cleanup", api, cleanupRequest, 200);
            armed = false;
        }
    }
}
