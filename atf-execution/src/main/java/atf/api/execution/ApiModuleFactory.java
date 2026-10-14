package atf.api.execution;

import atf.api.core.config.ApiSettings;
import atf.api.core.http.ApiExecutor;
import atf.api.core.http.ApiTransport;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/** Creates an isolated API module without exposing construction details to scenarios. */
public final class ApiModuleFactory {
    private ApiModuleFactory() { }

    public static ApiModule fromTransport(ApiTransport transport, ApiSettings settings) {
        Objects.requireNonNull(transport, "transport");
        Objects.requireNonNull(settings, "settings");
        var snapshot = settings.snapshot();
        return new ApiModule(snapshot, new ApiExecutor(transport, snapshot));
    }

    public static ApiModule fromTransport(ApiTransport transport, URI baseUri) {
        return fromTransport(transport, new ApiSettings(baseUri, "default"));
    }

    public static ApiModule fromTransport(ApiTransport transport, URI baseUri,
                                          Duration timeout, Set<Integer> retryStatuses,
                                          int maxAttempts) {
        return fromTransport(transport,
                new ApiSettings(baseUri, "default", timeout, retryStatuses, maxAttempts));
    }
}
