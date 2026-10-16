package atf.api.execution;

import atf.api.core.config.AtfRuntimeSnapshot;

import java.util.Objects;

/**
 * Typed composition root for one isolated API execution.
 *
 * <p>The default caller path is {@link ApiTransportBinding#acceptance}; live
 * HTTP is available only through the explicit {@link ApiTransportBinding#live}
 * factory. No string flag or mutable global can silently change a test from a
 * mock-backed acceptance run to a real network call.</p>
 */
public final class ApiBootstrap {
    private ApiBootstrap() { }

    public static ApiModule compose(AtfRuntimeSnapshot runtime,
                                    ApiTransportBinding binding) {
        Objects.requireNonNull(runtime, "runtime");
        Objects.requireNonNull(binding, "binding");
        return ApiModuleFactory.fromTransport(binding.transport(), runtime);
    }

    public static ApiModule acceptance(AtfRuntimeSnapshot runtime,
                                       atf.api.core.http.ApiTransport transport) {
        return compose(runtime, ApiTransportBinding.acceptance(transport));
    }

    public static ApiModule live(AtfRuntimeSnapshot runtime,
                                 atf.api.core.http.ApiTransport transport) {
        return compose(runtime, ApiTransportBinding.live(transport));
    }
}
