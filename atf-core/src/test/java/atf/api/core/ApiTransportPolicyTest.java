package atf.api.core;

import atf.api.core.config.ApiTransportMode;
import atf.api.core.config.ApiTransportPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiTransportPolicyTest {
    @Test
    void acceptanceIsTheSafeTypedDefault() {
        assertEquals(ApiTransportMode.ACCEPTANCE, ApiTransportMode.parse(null));
        assertEquals(ApiTransportMode.ACCEPTANCE, ApiTransportPolicy.acceptance().mode());
    }

    @Test
    void liveRequiresAnExplicitOptIn() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiTransportPolicy(ApiTransportMode.LIVE, false));
        assertEquals(ApiTransportMode.LIVE, ApiTransportPolicy.explicitLive().mode());
    }
}
