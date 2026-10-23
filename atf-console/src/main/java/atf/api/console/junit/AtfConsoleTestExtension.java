package atf.api.console.junit;
import atf.api.console.AtfConsoleConfigurationLoader;
import atf.api.console.ConsoleSettings;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.testkit.AtfConsoleMessages;
import atf.api.testkit.console.AtfConsoleMessagePolicyResolver;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.api.TestFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Adds the configured ATF console lifecycle to directly executed JUnit tests. */
public final class AtfConsoleTestExtension implements BeforeEachCallback, TestWatcher {
    private final ConcurrentMap<String, AtfConsoleTestLifecycle> activeTests =
            new ConcurrentHashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        if (context.getRequiredTestMethod().isAnnotationPresent(TestFactory.class)) {
            // A factory is a dynamic-test container, not an executable test result.
            // Each child opts into a correctly scoped lifecycle via AtfConsoleTestContext.dynamicTest(...).
            return;
        }
        ConsoleSettings settings = effectiveSettings(context.getRequiredTestClass());
        var lifecycle = AtfConsoleTestLifecycle.configured(
                context.getRequiredTestClass().getSimpleName(),
                context.getRequiredTestMethod().getName(), settings);
        lifecycle.start();
        activeTests.put(context.getUniqueId(), lifecycle);
        AtfConsoleTestContext.install(lifecycle);
    }

    static ConsoleSettings effectiveSettings(Class<?> testClass) {
        ConsoleSettings configured = AtfConsoleConfigurationLoader.load().settings();
        return applyPolicy(configured, AtfConsoleMessagePolicyResolver.resolve(testClass));
    }

    static ConsoleSettings applyPolicy(ConsoleSettings configured, AtfConsoleMessages.Policy policy) {
        return switch (policy) {
            case INHERIT -> configured;
            case DETAILS -> new ConsoleSettings(configured.enabled(), true);
            case SUMMARY -> new ConsoleSettings(configured.enabled(), false);
            case OFF -> ConsoleSettings.disabled();
        };
    }

    @Override public void testSuccessful(ExtensionContext context) { complete(context, AtfReportStatus.PASS); }
    @Override public void testFailed(ExtensionContext context, Throwable cause) { complete(context, AtfReportStatus.FAIL); }
    @Override public void testAborted(ExtensionContext context, Throwable cause) { complete(context, AtfReportStatus.SKIP); }

    private void complete(ExtensionContext context, AtfReportStatus status) {
        var lifecycle = activeTests.remove(context.getUniqueId());
        if (lifecycle != null) lifecycle.complete(status);
        AtfConsoleTestContext.clear();
    }
}
