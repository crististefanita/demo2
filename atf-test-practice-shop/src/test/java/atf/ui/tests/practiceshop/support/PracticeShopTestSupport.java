package atf.ui.tests.practiceshop.support;

import atf.api.core.config.ApiTransportMode;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
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

    /** Opens the complete E2E composition root once for one JUnit test method. */
    public static E2eHarness openE2e() {
        PracticeShopConfiguration configuration = PracticeShopConfigurationLoader.load();
        return new E2eHarness(configuration, PracticeShopE2eContext.open(configuration),
                new PracticeShopLocators());
    }

    public record E2eHarness(PracticeShopConfiguration configuration,
                             PracticeShopE2eContext context,
                             PracticeShopLocators locators) implements AutoCloseable {
        public E2eHarness {
            if (configuration == null || context == null || locators == null) {
                throw new IllegalArgumentException("Practice Shop E2E harness members are required");
            }
        }

        @Override public void close() {
            context.close();
        }
    }

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
