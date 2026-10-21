package atf.ui.practiceshop.api;

import atf.api.core.config.ApiTransportMode;
import atf.api.core.http.auth.ConfiguredApiAuthenticator;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.transport.ApiLiveTransportCatalog;
import atf.api.core.http.transport.ApiTransport;
import atf.api.core.http.transport.UrlConnectionTransport;
import atf.ui.practiceshop.config.PracticeShopApiConfiguration;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/** Small API adapter shared by screen-specific API modules and component tests. */
public final class PracticeShopApiClient implements AutoCloseable {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final PracticeShopApiRequestFactory requests;
    private final URI endpoint;
    private final ApiTransport transport;
    private final ApiExecutor executor;
    private String bearerToken = "";
    private boolean closed;

    private PracticeShopApiClient(URI endpoint, ApiTransport transport,
                                  PracticeShopApiRequestFactory requests,
                                  java.time.Duration timeout,
                                  ApiTransportMode transportMode) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.requests = Objects.requireNonNull(requests, "requests");
        var settings = new atf.api.core.config.ApiSettings(endpoint, PracticeShopApiAuthProfile.NONE.wireValue(),
                timeout, java.util.Set.of(), 1);
        // The factory is intentionally transport-agnostic; the explicit mode keeps
        // report provenance aligned with the transport selected by the composition root.
        this.executor = new ApiExecutor(transport, settings.snapshot(),
                Objects.requireNonNull(transportMode, "transportMode"));
    }

    public static PracticeShopApiClient live(PracticeShopApiConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        if (configuration.mode() != ApiTransportMode.LIVE) {
            throw new IllegalArgumentException("Practice Shop live API client requires api.mode=live");
        }
        ApiLiveTransportCatalog catalog = ApiLiveTransportCatalog.single("practice-shop",
                configuration.endpoint(), PracticeShopApiAuthProfile.NONE.wireValue(), configuration.timeout(),
                (endpoint, authenticator) -> new UrlConnectionTransport(authenticator, endpoint.timeout()));
        ApiTransport transport = catalog.open("practice-shop",
                atf.api.core.config.ApiTransportPolicy.explicitLive(),
                new ConfiguredApiAuthenticator(Map.of()));
        return new PracticeShopApiClient(configuration.endpoint(), transport,
                new PracticeShopApiRequestFactory(), configuration.timeout(), ApiTransportMode.LIVE);
    }

    public static PracticeShopApiClient forTransport(URI endpoint, ApiTransport transport) {
        return forTransport(endpoint, transport, ApiTransportMode.ACCEPTANCE);
    }

    /**
     * Exposes provenance selection for deterministic contract tests and custom transports.
     * The normal production entry points remain {@link #live(PracticeShopApiConfiguration)}
     * and {@link #forTransport(URI, ApiTransport)}.
     */
    public static PracticeShopApiClient forTransport(URI endpoint, ApiTransport transport,
                                                     ApiTransportMode transportMode) {
        return new PracticeShopApiClient(endpoint, transport, new PracticeShopApiRequestFactory(),
                java.time.Duration.ofSeconds(10), transportMode);
    }

    public ApiExecutionResult execute(atf.api.core.http.request.ApiRequest request) {
        ensureOpen();
        return executor.execute(request);
    }

    public PracticeShopApiSession login(PracticeShopCredentials credentials) {
        ensureOpen();
        ApiExecutionResult result = execute(requests.login(credentials));
        var response = result.response();
        String token = PracticeShopApiHttpStatus.isSuccess(response.status())
                ? token(response.body()) : "";
        bearerToken = token;
        return new PracticeShopApiSession(token, result);
    }

    public PracticeShopApiRequestFactory requests() { return requests; }
    public URI endpoint() { return endpoint; }
    public String bearerToken() { return bearerToken; }

    /**
     * Closes the logical API session owned by this client. The current HTTP
     * transport closes each URL connection per exchange, so cleanup here is
     * deliberately about session state: no bearer token can survive the test
     * boundary and later network use fails fast.
     */
    @Override
    public void close() {
        bearerToken = "";
        closed = true;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Practice Shop API client is closed");
    }

    private static String token(String body) {
        try {
            JsonNode root = JSON.readTree(body == null ? "" : body);
            if (root == null || !root.isObject()) {
                throw new IllegalStateException("Practice Shop login response is not a JSON object");
            }
            for (PracticeShopApiJsonField field : new PracticeShopApiJsonField[]{
                    PracticeShopApiJsonField.ACCESS_TOKEN,
                    PracticeShopApiJsonField.ACCESS_TOKEN_CAMEL,
                    PracticeShopApiJsonField.TOKEN}) {
                String value = root.path(field.wireValue()).asText("");
                if (!value.isBlank()) return value;
            }
            throw new IllegalStateException("Practice Shop login response does not contain an access token");
        } catch (com.fasterxml.jackson.core.JsonProcessingException error) {
            throw new IllegalStateException("Practice Shop login response is malformed JSON", error);
        }
    }
}
