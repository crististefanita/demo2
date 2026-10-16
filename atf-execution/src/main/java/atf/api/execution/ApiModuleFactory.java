package atf.api.execution;

import atf.api.core.config.ApiSettings;
import atf.api.core.config.ApiBootstrapConfiguration;
import atf.api.core.config.ApiTransportMode;
import atf.api.core.config.AtfRuntimeSnapshot;
import atf.api.core.http.ApiExecutor;
import atf.api.core.http.ApiTransport;
import atf.api.core.reporting.AtfConsoleSink;

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

    /** Composes one module from the fully typed bootstrap contract. */
    public static ApiModule fromTransport(ApiTransport transport,
                                          ApiBootstrapConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        return fromTransport(transport, configuration.settings());
    }

    /** Selects the already-created transport only after the typed policy is validated. */
    public static ApiModule bootstrap(ApiBootstrapConfiguration configuration,
                                      ApiTransport acceptanceTransport,
                                      ApiTransport liveTransport) {
        Objects.requireNonNull(configuration, "configuration");
        ApiTransport selected = configuration.transportPolicy().mode() == ApiTransportMode.ACCEPTANCE
                ? Objects.requireNonNull(acceptanceTransport, "acceptanceTransport")
                : Objects.requireNonNull(liveTransport, "liveTransport");
        return fromTransport(selected, configuration);
    }

    /** Composes execution from the immutable runtime boundary. */
    public static ApiModule fromTransport(ApiTransport transport, AtfRuntimeSnapshot runtime) {
        Objects.requireNonNull(transport, "transport");
        Objects.requireNonNull(runtime, "runtime");
        return new ApiModule(runtime.api(), new ApiExecutor(transport, runtime.api()));
    }

    public static ApiModule fromTransport(ApiTransport transport, URI baseUri) {
        return fromTransport(transport, new ApiSettings(baseUri, "default"));
    }

    /** Composes an API module with an optional renderer-neutral console observer. */
    public static ApiModule fromTransport(ApiTransport transport, URI baseUri,
                                          AtfConsoleSink console) {
        Objects.requireNonNull(transport, "transport");
        var settings = new ApiSettings(Objects.requireNonNull(baseUri, "baseUri"), "default");
        var snapshot = settings.snapshot();
        return new ApiModule(snapshot, new ApiExecutor(transport, snapshot, console));
    }

    public static ApiModule fromTransport(ApiTransport transport, URI baseUri,
                                          Duration timeout, Set<Integer> retryStatuses,
                                          int maxAttempts) {
        return fromTransport(transport,
                new ApiSettings(baseUri, "default", timeout, retryStatuses, maxAttempts));
    }
}
