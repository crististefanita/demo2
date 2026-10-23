package atf.ui.tests.autoex.component.lifecycle;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.lifecycle.AutoExE2eActivationPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class AutoExE2eActivationPolicyComponentTest {
    @Test
    void explicitJvmFalseOverridesYamlAndIntellijDetection() {
        var decision = AutoExE2eActivationPolicy.evaluate("false", true, true);

        assertFalse(decision.enabled());
        assertEquals("disabled by explicit JVM property atf.autoex.e2e.enabled=false", decision.reason());
    }

    @Test
    void explicitJvmTrueNamesItsActivationSource() {
        var decision = AutoExE2eActivationPolicy.evaluate("true", false, false);

        assertTrue(decision.enabled());
        assertEquals("enabled by JVM property atf.autoex.e2e.enabled=true", decision.reason());
    }

    @Test
    void yamlAndIntellijHeuristicAreDistinguishable() {
        var yamlDecision = AutoExE2eActivationPolicy.evaluate(null, true, true);
        var ideDecision = AutoExE2eActivationPolicy.evaluate(null, false, true);

        assertEquals("enabled by AutoEx YAML configuration", yamlDecision.reason());
        assertEquals("enabled by IntelliJ runner detection heuristic", ideDecision.reason());
    }

    @Test
    void ordinaryRunnerWithoutOptInRemainsDisabled() {
        var decision = AutoExE2eActivationPolicy.evaluate(null, false, false);

        assertFalse(decision.enabled());
        assertTrue(decision.reason().contains("atf.autoex.e2e.enabled=true"));
    }
}
