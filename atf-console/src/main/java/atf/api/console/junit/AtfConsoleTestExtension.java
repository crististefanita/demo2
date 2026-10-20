package atf.api.console.junit;
import atf.api.core.reporting.contract.AtfReportStatus;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Adds the configured ATF console lifecycle to directly executed JUnit tests. */
public final class AtfConsoleTestExtension implements BeforeEachCallback, TestWatcher {
    private final ConcurrentMap<String, AtfConsoleTestLifecycle> activeTests =
            new ConcurrentHashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        var lifecycle = AtfConsoleTestLifecycle.configured(
                context.getRequiredTestClass().getSimpleName(),
                context.getRequiredTestMethod().getName());
        lifecycle.start();
        activeTests.put(context.getUniqueId(), lifecycle);
        AtfConsoleTestContext.install(lifecycle);
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
