package atf.ui.practiceshop.api;

import atf.api.core.config.ApiTransportMode;
import atf.api.core.http.auth.ConfiguredApiAuthenticator;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.transport.ApiLiveTransportCatalog;
import atf.api.core.http.transport.ApiTransport;
import atf.ui.practiceshop.config.PracticeShopApiConfiguration;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.Objects;

/** Small API adapter shared by screen-specific API modules and component tests. */
public final class PracticeShopApiClient {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final PracticeShopApiRequestFactory requests;
    private final URI endpoint;
    private final ApiTransport transport;
    private final ApiExecutor executor;
    private String bearerToken = "";

    private PracticeShopApiClient(URI endpoint, ApiTransport transport,
                                  PracticeShopApiRequestFactory requests,
                                  java.time.Duration timeout) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.requests = Objects.requireNonNull(requests, "requests");
        var settings = new atf.api.core.config.ApiSettings(endpoint, "none", timeout, java.util.Set.of(), 1);
        this.executor = new ApiExecutor(transport, settings.snapshot(), ApiTransportMode.LIVE);
    }

    public static PracticeShopApiClient live(PracticeShopApiConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        if (configuration.mode() != ApiTransportMode.LIVE) {
            throw new IllegalArgumentException("Practice Shop live API client requires api.mode=live");
        }
        HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        ApiLiveTransportCatalog catalog = ApiLiveTransportCatalog.single("practice-shop",
                configuration.endpoint(), "none", configuration.timeout(), http);
        ApiTransport transport = catalog.open("practice-shop",
                atf.api.core.config.ApiTransportPolicy.explicitLive(),
                new ConfiguredApiAuthenticator(Map.of()));
        return new PracticeShopApiClient(configuration.endpoint(), transport,
                new PracticeShopApiRequestFactory(), configuration.timeout());
    }

    public static PracticeShopApiClient forTransport(URI endpoint, ApiTransport transport) {
        return new PracticeShopApiClient(endpoint, transport, new PracticeShopApiRequestFactory(),
                java.time.Duration.ofSeconds(10));
    }

    public ApiExecutionResult execute(atf.api.core.http.request.ApiRequest request) {
        return executor.execute(request);
    }

    public PracticeShopApiSession login(PracticeShopCredentials credentials) {
        ApiExecutionResult result = execute(requests.login(credentials));
        String token = result.response().status() >= 200 && result.response().status() < 300
                ? token(result.response().body()) : "";
        bearerToken = token;
        return new PracticeShopApiSession(token, result);
    }

    public ApiExecutionResult currentUser() { return execute(requests.currentUser(bearerToken)); }
    public PracticeShopApiRequestFactory requests() { return requests; }
    public URI endpoint() { return endpoint; }
    public String bearerToken() { return bearerToken; }

    private static String token(String body) {
        try {
            JsonNode root = JSON.readTree(body == null ? "" : body);
            for (String field : new String[]{"access_token", "accessToken", "token"}) {
                String value = root.path(field).asText("");
                if (!value.isBlank()) return value;
            }
            return "";
        } catch (Exception ignored) {
            return "";
        }
    }
}
