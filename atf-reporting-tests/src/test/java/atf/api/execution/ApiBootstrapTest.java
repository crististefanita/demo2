package atf.api.execution;
import atf.api.testkit.AtfComponentTest;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.api.core.config.AtfConfiguration;
import atf.api.core.config.ReportingSettings;
import atf.api.core.config.ApiSettings;
import atf.api.core.config.ApiTransportMode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest

class ApiBootstrapTest {
    private static final ApiTransport TRANSPORT = (request, url, auth) ->
            CompletableFuture.completedFuture(new ApiResponse(200, "{}", Map.of(), Duration.ZERO, url));

    @Test
    void acceptanceIsTheTypedDefaultAndKeepsRuntimeSnapshot() {
        var runtime = new AtfConfiguration(
                new ApiSettings(URI.create("https://acceptance.example"), "mock"),
                new ReportingSettings(96)).snapshot();

        var module = ApiBootstrap.acceptance(runtime, TRANSPORT);

        assertEquals(ApiTransportMode.ACCEPTANCE, ApiTransportBinding.acceptance(TRANSPORT).mode());
        assertEquals(runtime.api(), module.settings());
        assertEquals(200, module.executor().execute(ApiRequest.of("GET", "/health"))
                .response().status());
    }

    @Test
    void liveRequiresAnExplicitOptIn() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiTransportBinding(ApiTransportMode.LIVE, TRANSPORT, false));
        assertEquals(ApiTransportMode.LIVE, ApiTransportBinding.live(TRANSPORT).mode());
    }

    @Test
    void modeParsingDefaultsToAcceptanceAndRejectsUnknownValues() {
        assertEquals(ApiTransportMode.ACCEPTANCE, ApiTransportMode.parse(""));
        assertEquals(ApiTransportMode.LIVE, ApiTransportMode.parse(" live "));
        assertThrows(IllegalArgumentException.class, () -> ApiTransportMode.parse("shadow"));
    }
}
