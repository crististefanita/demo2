package atf.ui.tests.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import atf.ui.practiceshop.support.PracticeShopScreenshotPolicy;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.nio.file.Path;

/** Per-method browser lifecycle and the small composition API used by Practice Shop E2E tests. */
public final class PracticeShopE2eTestSupport {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PracticeShopE2eTestSupport.class);
    private static final String HARNESS_KEY = "e2e-harness";

    private PracticeShopE2eTestSupport() { }

    public enum HarnessProfile {
        CONFIGURED,
        UI_ONLY,
        UI_API
    }

    public enum ReportFlow {
        CONFIGURED,
        SCREEN_CHECKPOINTS,
        UNIFIED_FLOW
    }

    public enum ScreenshotMode {
        CONFIGURED,
        EACH_CHECKPOINT,
        FINAL_ONLY,
        FAILURE_ONLY,
        FINAL_AND_FAILURE,
        NONE
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(E2eExtension.class)
    public @interface WithE2eHarness {
        HarnessProfile profile() default HarnessProfile.CONFIGURED;
        ReportFlow reportFlow() default ReportFlow.CONFIGURED;
        ScreenshotMode screenshots() default ScreenshotMode.CONFIGURED;
    }

    /** JUnit opens and closes one harness per test method, including failed methods. */
    public static final class E2eExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
        @Override
        public void beforeEach(ExtensionContext context) {
            PracticeShopConfiguration configuration = PracticeShopTestSupport.configurationFor(
                    context.getRequiredTestClass());
            WithE2eHarness harness = context.getRequiredTestClass().getAnnotation(WithE2eHarness.class);
            if (harness != null) configuration = configurationForHarnessProfile(configuration, harness.profile());
            PracticeShopTestSupport.AuthenticatedE2e authenticated =
                    context.getRequiredTestClass().getAnnotation(PracticeShopTestSupport.AuthenticatedE2e.class);
            PracticeShopTestSupport.AccountSetupMode accountSetup = authenticated == null
                    ? PracticeShopTestSupport.AccountSetupMode.REUSE_FIXTURE : authenticated.accountSetup();
            Path registrationProfile = authenticated != null
                    && accountSetup == PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN
                    ? PracticeShopConfigurationLoader.resolveRegistrationProfileFile(
                            authenticated.registrationProfileFile()) : null;
            ReportEvidenceProfile presentation = reportProfile(configuration, harness, accountSetup);
            PracticeShopE2eContext browser = PracticeShopE2eContext.open(configuration, presentation);
            context.getStore(NAMESPACE).put(HARNESS_KEY, new E2eResource(
                    new E2eHarness(configuration, browser, new PracticeShopLocators(),
                            context.getRequiredTestClass().getSimpleName(), accountSetup, registrationProfile,
                            context.getRoot().getUniqueId() + "::"
                                    + context.getRequiredTestClass().getName())));
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

    static PracticeShopConfiguration configurationForHarnessProfile(
            PracticeShopConfiguration configuration, HarnessProfile profile) {
        return switch (Objects.requireNonNull(profile, "profile")) {
            case CONFIGURED -> Objects.requireNonNull(configuration, "configuration");
            case UI_ONLY -> configuration.withValidationProfile(PracticeShopValidationProfile.UI_ONLY);
            case UI_API -> configuration.withValidationProfile(PracticeShopValidationProfile.UI_API);
        };
    }

    static ReportEvidenceProfile reportProfile(PracticeShopConfiguration configuration,
                                               WithE2eHarness harness) {
        ReportEvidenceProfile configured = PracticeShopScreenshotPolicy.profile(
                Objects.requireNonNull(configuration, "configuration")
                        .browser().screenshotOnFailure());
        if (harness == null) return configured;
        return configured.toBuilder()
                .flowMode(resolve(harness.reportFlow(), configured.flowMode()))
                .screenshotPolicy(resolve(harness.screenshots(), configured.screenshotPolicy()))
                .build();
    }

    private static ReportEvidenceFlowMode resolve(ReportFlow selected,
                                                  ReportEvidenceFlowMode configured) {
        return switch (Objects.requireNonNull(selected, "reportFlow")) {
            case CONFIGURED -> configured;
            case SCREEN_CHECKPOINTS -> ReportEvidenceFlowMode.SCREEN_CHECKPOINTS;
            case UNIFIED_FLOW -> ReportEvidenceFlowMode.UNIFIED_FLOW;
        };
    }

    private static ReportScreenshotPolicy resolve(ScreenshotMode selected,
                                                  ReportScreenshotPolicy configured) {
        return switch (Objects.requireNonNull(selected, "screenshots")) {
            case CONFIGURED -> configured;
            case EACH_CHECKPOINT -> ReportScreenshotPolicy.EACH_CHECKPOINT;
            case FINAL_ONLY -> ReportScreenshotPolicy.FINAL_ONLY;
            case FAILURE_ONLY -> ReportScreenshotPolicy.FAILURE_ONLY;
            case FINAL_AND_FAILURE -> ReportScreenshotPolicy.FINAL_AND_FAILURE;
            case NONE -> ReportScreenshotPolicy.NONE;
        };
    }

    static ReportEvidenceProfile reportProfile(PracticeShopConfiguration configuration,
                                               WithE2eHarness harness,
                                               PracticeShopTestSupport.AccountSetupMode accountSetup) {
        ReportEvidenceProfile profile = reportProfile(configuration, harness);
        return accountSetup == PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN
                ? profile.toBuilder().screenshotPolicy(atf.api.reporting.evidence.ReportScreenshotPolicy.NONE).build()
                : profile;
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
                             String reportClass,
                             PracticeShopTestSupport.AccountSetupMode accountSetupMode,
                             Path registrationProfileFile,
                             String accountRunScope) implements AutoCloseable {
        public E2eHarness(PracticeShopConfiguration configuration,
                          PracticeShopE2eContext context,
                          PracticeShopLocators locators, String reportClass) {
            this(configuration, context, locators, reportClass,
                    PracticeShopTestSupport.AccountSetupMode.REUSE_FIXTURE, null, "standalone");
        }

        public E2eHarness {
            Objects.requireNonNull(configuration, "configuration");
            Objects.requireNonNull(context, "context");
            Objects.requireNonNull(locators, "locators");
            if (reportClass == null || reportClass.isBlank()) {
                throw new IllegalArgumentException("reportClass is required");
            }
            Objects.requireNonNull(accountSetupMode, "accountSetupMode");
            if (accountRunScope == null || accountRunScope.isBlank()) {
                throw new IllegalArgumentException("accountRunScope is required");
            }
            if (accountSetupMode == PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN
                    && registrationProfileFile == null) {
                throw new IllegalArgumentException("register-per-run requires a local registration profile");
            }
        }

        public void run(String title, String overview, Consumer<E2eJourney> businessFlow) {
            Objects.requireNonNull(businessFlow, "businessFlow");
            PlaywrightActionSequence actions = context.actions();
            PracticeShopE2eContext.Scenario scenario = context.scenario(reportClass, title, overview);
            E2eJourney journey = new E2eJourney(this, actions, scenario);
            scenario.execute(context.page(), actions, () -> businessFlow.accept(journey));
        }

        /** Runs account acquisition in SETUP and opens TEST BODY only after it has passed. */
        public void run(String title, String overview,
                        Function<E2eSetup, PracticeShopCredentials> accountSetup,
                        BiConsumer<E2eJourney, PracticeShopCredentials> businessFlow) {
            Objects.requireNonNull(accountSetup, "accountSetup");
            Objects.requireNonNull(businessFlow, "businessFlow");
            PlaywrightActionSequence actions = context.actions();
            PracticeShopE2eContext.Scenario scenario = context.scenarioWithExplicitSetup(reportClass, title, overview);
            scenario.execute(context.page(), actions, () -> {
                E2eSetup setup = new E2eSetup(this, actions, scenario);
                PracticeShopCredentials credentials = Objects.requireNonNull(
                        accountSetup.apply(setup), "accountSetup credentials");
                if (!setup.completed()) {
                    throw new IllegalStateException("account setup must publish a passing SETUP step before TEST BODY");
                }
                businessFlow.accept(new E2eJourney(this, actions, scenario), credentials);
            });
        }

        @Override
        public void close() {
            context.close();
        }
    }

    /** Explicit setup-only view; publication intentionally excludes screenshots and raw values. */
    public static final class E2eSetup {
        private final E2eHarness harness;
        private final PlaywrightActionSequence actions;
        private final PracticeShopE2eContext.Scenario scenario;
        private boolean completed;
        private boolean failed;

        private E2eSetup(E2eHarness harness, PlaywrightActionSequence actions,
                         PracticeShopE2eContext.Scenario scenario) {
            this.harness = harness;
            this.actions = actions;
            this.scenario = scenario;
        }

        public PracticeShopConfiguration configuration() { return harness.configuration(); }
        public PracticeShopLocators locators() { return harness.locators(); }
        public Page page() { return harness.context().page(); }
        public PlaywrightActionSequence actions() { return actions; }
        public PracticeShopApiNetworkJournal apiNetwork() { return harness.context().apiNetwork(); }
        public PracticeShopTestSupport.AccountSetupMode accountSetupMode() { return harness.accountSetupMode(); }
        public Path registrationProfileFile() { return harness.registrationProfileFile(); }
        public String accountRunScope() { return harness.accountRunScope(); }
        public void checkpoint(AtfGherkinKeyword keyword, String title, Collection<AtfAssertion> assertions,
                               Collection<atf.api.reporting.model.ReportExecutionEvidence> apiEvidence) {
            if (completed || failed) throw new IllegalStateException("SETUP is already finished");
            AtfReportStatus status = assertions.stream().allMatch(AtfAssertion::passed)
                    ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
            scenario.setupCheckpoint(page(), actions, keyword, title, assertions, apiEvidence, status);
            if (status == AtfReportStatus.FAIL) {
                failed = true;
                scenario.finishSetup(AtfReportStatus.FAIL);
                throw new AssertionError("Authenticated account setup failed; TEST BODY was not started");
            }
        }

        public void complete() {
            if (completed || failed) throw new IllegalStateException("SETUP is already finished");
            scenario.finishSetup(AtfReportStatus.PASS);
            completed = true;
        }

        public void fail(String title, Collection<AtfAssertion> assertions,
                         Collection<atf.api.reporting.model.ReportExecutionEvidence> apiEvidence) {
            if (completed || failed) throw new IllegalStateException("SETUP is already finished");
            scenario.setupCheckpoint(page(), actions, AtfGherkinKeyword.GIVEN, title,
                    assertions, apiEvidence, AtfReportStatus.FAIL);
            scenario.finishSetup(AtfReportStatus.FAIL);
            failed = true;
        }

        private boolean completed() { return completed; }
    }

    /** Test-facing handles; observed assertions and business flow stay in the E2E test. */
    public record E2eJourney(E2eHarness harness, PlaywrightActionSequence actions,
                             PracticeShopE2eContext.Scenario scenario) {
        public PracticeShopConfiguration configuration() { return harness.configuration(); }
        public PracticeShopE2eContext context() { return harness.context(); }
        public PracticeShopLocators locators() { return harness.locators(); }
        public Page page() { return context().page(); }
        public PracticeShopApiNetworkJournal apiNetwork() { return context().apiNetwork(); }

        public void checkpoint(PracticeShopPurchaseCheckpoint checkpoint) {
            scenario.checkpoint(page(), actions, checkpoint);
        }

        public void pass(String evidenceTitle, Collection<AtfAssertion> assertions) {
            scenario.pass(page(), actions, evidenceTitle, assertions);
        }

        public void completePurchase() {
            scenario.complete(AtfReportStatus.PASS);
        }
    }
}
