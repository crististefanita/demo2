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

    public enum BrowserMode {
        HEADED,
        HEADLESS
    }

    /**
     * Declares a directly runnable authenticated E2E composition root.
     * Defaults intentionally point to the ignored local fixture and headed
     * browser so an IntelliJ run is useful without VM options.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(AuthenticatedE2eCondition.class)
    public @interface AuthenticatedE2e {
        String accountFile() default
                "atf-test-practice-shop/src/test/resources/practice-software-testing-account.local.properties";

        BrowserMode browserMode() default BrowserMode.HEADED;

        int zoomPercent() default 70;
    }

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

    public static final class AuthenticatedE2eCondition implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            try {
                PracticeShopConfiguration configuration = configurationFor(context.getRequiredTestClass());
                return configuration.authenticatedE2eAllowed()
                        ? ConditionEvaluationResult.enabled("authenticated IntelliJ E2E profile is configured")
                        : ConditionEvaluationResult.disabled(AUTHENTICATED_REASON);
            } catch (RuntimeException error) {
                return ConditionEvaluationResult.disabled(
                        AUTHENTICATED_REASON + ": " + error.getMessage());
            }
        }
    }

    /** Applies the class-level launch profile without mutating global system properties. */
    public static PracticeShopConfiguration configurationFor(Class<?> testClass) {
        PracticeShopConfiguration base = PracticeShopConfigurationLoader.load();
        AuthenticatedE2e profile = testClass.getAnnotation(AuthenticatedE2e.class);
        if (profile == null) return base;
        return base.forAuthenticatedE2e(
                PracticeShopConfigurationLoader.resolveAccountFile(profile.accountFile()),
                profile.browserMode() == BrowserMode.HEADLESS,
                profile.zoomPercent());
    }
}
