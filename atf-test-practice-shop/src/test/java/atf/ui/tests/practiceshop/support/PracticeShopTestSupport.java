package atf.ui.tests.practiceshop.support;

import atf.api.core.config.ApiTransportMode;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExecutionCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Shared composition and selection boundary for Practice Shop tests. */
public final class PracticeShopTestSupport {
    private static final String AUTHENTICATED_REASON =
            "authenticated Practice Shop fixture is not configured";
    private static final String LIVE_REASON =
            "authenticated live Practice Shop API fixture is not configured";

    private PracticeShopTestSupport() { }

    /** Selects only tests that have an external account fixture available. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(AuthenticatedFixtureCondition.class)
    public @interface RequiresAuthenticatedFixture { }

    /** Selects only tests that can prove the authenticated API against HTTP. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(LiveAuthenticatedApiCondition.class)
    public @interface RequiresLiveAuthenticatedApi { }

    public static final class AuthenticatedFixtureCondition implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            return PracticeShopConfigurationLoader.load().authenticatedE2eAllowed()
                    ? ConditionEvaluationResult.enabled("authenticated fixture is configured")
                    : ConditionEvaluationResult.disabled(AUTHENTICATED_REASON);
        }
    }

    public static final class LiveAuthenticatedApiCondition implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            PracticeShopConfiguration configuration = PracticeShopConfigurationLoader.load();
            boolean enabled = configuration.authenticatedE2eAllowed()
                    && configuration.api().mode() == ApiTransportMode.LIVE;
            return enabled
                    ? ConditionEvaluationResult.enabled("authenticated live API fixture is configured")
                    : ConditionEvaluationResult.disabled(LIVE_REASON);
        }
    }
}
