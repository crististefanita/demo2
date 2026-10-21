package atf.ui.tests.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import atf.ui.practiceshop.support.PracticeShopRuntimeDiagnostics;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/** Per-method browser lifecycle and the small composition API used by Practice Shop E2E tests. */
public final class PracticeShopE2eTestSupport {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PracticeShopE2eTestSupport.class);
    private static final String HARNESS_KEY = "e2e-harness";

    private PracticeShopE2eTestSupport() { }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(E2eExtension.class)
    public @interface WithE2eHarness { }

    /** JUnit opens and closes one harness per test method, including failed methods. */
    public static final class E2eExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
        @Override
        public void beforeEach(ExtensionContext context) {
            PracticeShopConfiguration configuration = PracticeShopConfigurationLoader.load();
            PracticeShopE2eContext browser = PracticeShopE2eContext.open(configuration);
            context.getStore(NAMESPACE).put(HARNESS_KEY, new E2eResource(
                    new E2eHarness(configuration, browser, new PracticeShopLocators(),
                            context.getRequiredTestClass().getSimpleName())));
        }

        @Override
        public void afterEach(ExtensionContext context) throws Exception {
            E2eResource resource = context.getStore(NAMESPACE).remove(HARNESS_KEY, E2eResource.class);
            if (resource != null) closeAfterTest(resource, context.getExecutionException().orElse(null));
        }

        @Override
        public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
            return parameter.getParameter().getType() == E2eHarness.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
            E2eResource resource = context.getStore(NAMESPACE).get(HARNESS_KEY, E2eResource.class);
            if (resource == null) throw new ParameterResolutionException("Practice Shop E2E harness was not opened");
            return resource.harness();
        }
    }

    /** Holds the resource after opening so teardown is idempotent even when it fails. */
    static final class E2eResource implements AutoCloseable {
        private final AutoCloseable delegate;
        private boolean closed;

        E2eResource(AutoCloseable delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        E2eHarness harness() {
            return (E2eHarness) delegate;
        }

        @Override
        public void close() throws Exception {
            if (closed) return;
            closed = true;
            delegate.close();
        }
    }

    static void closeAfterTest(E2eResource resource, Throwable testFailure) throws Exception {
        try {
            resource.close();
        } catch (Exception | Error teardownFailure) {
            if (testFailure == null) {
                if (teardownFailure instanceof Exception exception) throw exception;
                throw teardownFailure;
            }
            testFailure.addSuppressed(teardownFailure);
        }
    }

    public record E2eHarness(PracticeShopConfiguration configuration,
                             PracticeShopE2eContext context,
                             PracticeShopLocators locators,
                             String reportClass) implements AutoCloseable {
        public E2eHarness {
            Objects.requireNonNull(configuration, "configuration");
            Objects.requireNonNull(context, "context");
            Objects.requireNonNull(locators, "locators");
            if (reportClass == null || reportClass.isBlank()) {
                throw new IllegalArgumentException("reportClass is required");
            }
        }

        public void run(String title, String overview, Consumer<E2eJourney> businessFlow) {
            Objects.requireNonNull(businessFlow, "businessFlow");
            PlaywrightActionSequence actions = context.actions();
            PracticeShopE2eContext.Scenario scenario = context.scenario(reportClass, title, overview);
            E2eJourney journey = new E2eJourney(this, actions, scenario);
            scenario.execute(context.page(), actions, () -> businessFlow.accept(journey));
        }

        @Override
        public void close() {
            context.close();
        }
    }

    /** Test-facing handles; observed assertions and business flow stay in the E2E test. */
    public record E2eJourney(E2eHarness harness, PlaywrightActionSequence actions,
                             PracticeShopE2eContext.Scenario scenario) {
        public PracticeShopConfiguration configuration() { return harness.configuration(); }
        public PracticeShopE2eContext context() { return harness.context(); }
        public PracticeShopLocators locators() { return harness.locators(); }
        public Page page() { return context().page(); }
        public PracticeShopApiNetworkJournal apiNetwork() { return context().apiNetwork(); }
        public PracticeShopRuntimeDiagnostics runtimeDiagnostics() { return context().runtimeDiagnostics(); }

        /** Called only after the account page proves the authenticated browser session. */
        public void authenticatedSessionEstablished() { runtimeDiagnostics().markAuthenticated(); }

        public void checkpoint(PracticeShopPurchaseCheckpoint checkpoint) {
            scenario.checkpoint(page(), actions, checkpoint);
        }

        public void pass(String evidenceTitle, Collection<AtfAssertion> assertions) {
            scenario.pass(page(), actions, evidenceTitle, assertions);
        }

        public void completeGuestPurchase() {
            PracticeShopRuntimeDiagnostics diagnostics = runtimeDiagnostics();
            diagnostics.assertClean();
            scenario.complete(AtfReportStatus.PASS);
        }

        public void completeAuthenticatedPurchase() {
            PracticeShopRuntimeDiagnostics diagnostics = runtimeDiagnostics();
            diagnostics.assertClean();
            scenario.complete(AtfReportStatus.PASS);
        }
    }
}
