package atf.ui.tests.practiceshop.component.lifecycle;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.practiceshop.support.lifecycle.ShopE2eActivationPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopE2eActivationPolicyComponentTest {
    @Test
    void ordinaryRunnerWithoutExplicitOptInRemainsDisabled() {
        var decision = ShopE2eActivationPolicy.evaluate(null, false);

        assertFalse(decision.enabled());
        assertTrue(decision.reason().contains("atf.practice.shop.e2e.enabled=true"));
    }

    @Test
    void directIntellijRunRemainsEnabled() {
        var decision = ShopE2eActivationPolicy.evaluate(null, true);

        assertTrue(decision.enabled());
        assertEquals("enabled by IntelliJ runner detection heuristic", decision.reason());
    }

    @Test
    void explicitJvmOverrideWinsOverIntellijDetection() {
        var enabled = ShopE2eActivationPolicy.evaluate("true", false);
        var disabled = ShopE2eActivationPolicy.evaluate("false", true);

        assertTrue(enabled.enabled());
        assertFalse(disabled.enabled());
        assertTrue(disabled.reason().contains("enabled=false"));
    }

    @Test
    void malformedOverrideFailsClosedWithActionableReason() {
        var decision = ShopE2eActivationPolicy.evaluate("yes", true);

        assertFalse(decision.enabled());
        assertTrue(decision.reason().contains("exactly true or false"));
    }
}
