package atf.ui.tests.practiceshop.support.lifecycle;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Applies the Shop external-site opt-in before the browser/report harness is opened. */
public final class ShopE2eActivationExtension implements ExecutionCondition {
    private static final String ENABLE_PROPERTY = "atf.practice.shop.e2e.enabled";
    private static final String INTELLIJ_RUNNER_PROPERTY = "idea.test.cyclic.buffer.size";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String override = System.getProperty(ENABLE_PROPERTY);
        boolean ideRunnerDetected = override == null
                && System.getProperty(INTELLIJ_RUNNER_PROPERTY) != null;
        var decision = ShopE2eActivationPolicy.evaluate(override, ideRunnerDetected);
        return decision.enabled()
                ? ConditionEvaluationResult.enabled(decision.reason())
                : ConditionEvaluationResult.disabled(decision.reason());
    }
}
