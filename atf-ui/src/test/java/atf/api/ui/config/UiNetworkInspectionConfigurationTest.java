package atf.api.ui.config;

import atf.api.testkit.AtfUnitTest;
import atf.api.ui.network.UiNetworkInspector;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class UiNetworkInspectionConfigurationTest {
    @Test
    void disabledPolicyIsFailClosed() {
        var policy = UiNetworkInspectionConfiguration.disabled();

        assertFalse(policy.enabled());
        assertFalse(policy.accepts("https://api.example.test/orders"));
    }

    @Test
    void hostAllowlistIsCaseInsensitiveAndDoesNotAcceptOtherServices() {
        var policy = UiNetworkInspectionConfiguration.forHosts(List.of("API.Example.Test"));

        assertTrue(policy.accepts("https://api.example.test/orders"));
        assertFalse(policy.accepts("https://cdn.example.test/app.js"));
        assertEquals(12_000, policy.maxBodyPreviewCharacters());
    }

    @Test
    void enabledPolicyRequiresAnExplicitHostBoundary() {
        assertThrows(IllegalArgumentException.class, () ->
                new UiNetworkInspectionConfiguration(true, Set.of(), 32, 500,
                        true, UiNetworkSettlementPolicy.disabled()));
    }

    @Test
    void settlementPolicyIsBoundedAndIndependentFromActionReplay() {
        var settlement = UiNetworkSettlementPolicy.bounded(
                Duration.ofSeconds(5), Duration.ofMillis(100));

        assertTrue(settlement.enabled());
        assertEquals(Duration.ofSeconds(5), settlement.timeout());
        assertEquals(Duration.ofMillis(100), settlement.pollInterval());
    }

    @Test
    void awaitRequiresExplicitSettlementPolicy() {
        var inspector = new UiNetworkInspector(UiNetworkInspectionConfiguration.disabled());

        assertThrows(IllegalStateException.class,
                () -> inspector.await(observation -> true, null));
    }
}
