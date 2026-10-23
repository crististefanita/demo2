package atf.ui.autoex.api;

import atf.api.core.config.ApiSettings;
import atf.api.core.config.ApiTransportMode;
import atf.api.core.config.ApiTransportPolicy;
import atf.api.core.http.auth.AuthProfiles;
import atf.api.core.http.auth.ConfiguredApiAuthenticator;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.transport.ApiLiveTransportCatalog;
import atf.api.core.http.transport.UrlConnectionTransport;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/** Explicit, single-attempt transport shared by live API contracts and real customer setup. */
public final class AutoExLiveApi {
    private static final String PROFILE = "autoex-live";

    private AutoExLiveApi() { }

    public static ApiExecutor open(URI baseUrl, Duration timeout) {
        var catalog = ApiLiveTransportCatalog.single(PROFILE, baseUrl, AuthProfiles.NONE, timeout,
                (endpoint, authenticator) -> new UrlConnectionTransport(authenticator, endpoint.timeout()));
        var transport = catalog.open(PROFILE, ApiTransportPolicy.explicitLive(),
                new ConfiguredApiAuthenticator(Map.of()));
        var settings = new ApiSettings(baseUrl, AuthProfiles.NONE, timeout, Set.of(), 1);
        return new ApiExecutor(transport, settings.snapshot(), ApiTransportMode.LIVE);
    }
}
