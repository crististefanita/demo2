package atf.ui.practiceshop.support;

import atf.api.console.AtfConsoleReporter;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.configuration.AtfReportConfigurationLoader;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import com.microsoft.playwright.Page;

import java.nio.file.Path;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/** Owns the browser/report lifecycle for one external Practice Shop suite. */
public final class PracticeShopE2eContext implements AutoCloseable {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
    private final PracticeShopConfiguration configuration;
    private final UiExtentReportSession report;
    private final PlaywrightUiSession browser;
    private final PracticeShopApiNetworkJournal apiNetwork;
    private final PracticeShopRuntimeDiagnostics runtimeDiagnostics;

    private PracticeShopE2eContext(PracticeShopConfiguration configuration,
                                   UiExtentReportSession report, PlaywrightUiSession browser,
                                   PracticeShopApiNetworkJournal apiNetwork,
                                   PracticeShopRuntimeDiagnostics runtimeDiagnostics) {
        this.configuration = configuration; this.report = report; this.browser = browser;
        this.apiNetwork = apiNetwork; this.runtimeDiagnostics = runtimeDiagnostics;
    }

    public static PracticeShopE2eContext open(PracticeShopConfiguration configuration) {
        String file = "ui-atf-practice-shop-e2e_"
                + STAMP.withZone(Clock.systemDefaultZone().getZone()).format(Clock.systemDefaultZone().instant())
                + ".html";
        AtfConsoleSink console = AtfConsoleReporter.configuredStdout();
        var reportConfiguration = AtfReportConfigurationLoader.load();
        UiExtentReportSession report = UiExtentReportSession.openConfiguredResolved(
                configuration.uiReportDirectory(), file,
                atf.api.reporting.console.AtfConsoleSink.noOp(),
                PracticeShopReportSystemMetadata.snapshot(configuration,
                        reportConfiguration.reportingSettings()));
        try {
            PlaywrightUiSession browser = PlaywrightUiSession.open(configuration.browser(), console);
            PracticeShopApiNetworkJournal apiNetwork =
                    new PracticeShopApiNetworkJournal(configuration.validationProfile(),
                            configuration.api().evidenceBinding());
            apiNetwork.attach(browser.page());
            PracticeShopRuntimeDiagnostics runtimeDiagnostics = new PracticeShopRuntimeDiagnostics();
            runtimeDiagnostics.attach(browser.page());
            return new PracticeShopE2eContext(configuration, report, browser, apiNetwork, runtimeDiagnostics);
        } catch (RuntimeException error) {
            report.close();
            throw error;
        }
    }

    private static Path projectRoot() {
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        while (current != null) {
            if (java.nio.file.Files.isRegularFile(current.resolve("atf-reporting/pom.xml"))) return current;
            current = current.getParent();
        }
        return Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
    }

    public Page page() { return browser.page(); }
    public PracticeShopConfiguration configuration() { return configuration; }
    public PlaywrightActionSequence actions() { return browser.newActionSequence(); }
    public Path reportPath() { return report.reportPath(); }
    public PracticeShopApiNetworkJournal apiNetwork() { return apiNetwork; }
    public PracticeShopRuntimeDiagnostics runtimeDiagnostics() { return runtimeDiagnostics; }

    public Scenario scenario(String title, String overview) {
        return scenario("PracticeShopE2eTest", title, overview);
    }

    /** Starts a report scenario with the concrete test identity of the caller. */
    public Scenario scenario(String reportClass, String title, String overview) {
        UiExtentReportSession.UiScenario scenario = report.startScenario(
                reportClass, "atf.ui.tests.practiceshop",
                overview, List.of("ui", "e2e", "external", "practice-shop"));
        var setup = scenario.startPhase("SETUP");
        List<AtfAssertion> setupAssertions = setupAssertions();
        AtfReportStatus setupStatus = setupAssertions.stream().allMatch(AtfAssertion::passed)
                ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        boolean authenticated = configuration.authenticatedE2eAllowed();
        setup.setup(AtfGherkinKeyword.GIVEN,
                authenticated
                        ? "the pre-registered Practice Shop account fixture is loaded"
                        : "the typed guest checkout configuration is ready",
                authenticated ? "practice-software-testing-account.properties" : "YAML checkout inputs",
                setupAssertions, setupStatus);
        setup.complete(setupStatus);
        return new Scenario(scenario, title);
    }

    private List<AtfAssertion> setupAssertions() {
        boolean accountConfigured = configuration.accountConfigured();
        boolean authenticated = configuration.authenticatedE2eAllowed();
        String selectedMode = authenticated ? "authenticated" : "guest";
        String expectedMode = configuration.authenticatedE2eEnabled() && accountConfigured
                ? "authenticated" : "guest";
        String profile = configuration.validationProfile().name().toLowerCase().replace('_', '-');
        return List.of(
                new AtfAssertion("Checkout mode", expectedMode + " checkout is selected",
                        selectedMode + " checkout; authenticatedE2eEnabled="
                                + configuration.authenticatedE2eEnabled()
                                + "; accountFile=" + (accountConfigured ? "available" : "missing"),
                        expectedMode.equals(selectedMode)),
                new AtfAssertion("Account setup",
                        authenticated ? "pre-registered account file is available" : "not required for guest journey",
                        authenticated
                                ? (accountConfigured ? "account file available; credentials redacted" : "account file missing")
                                : "guest journey; account file optional",
                        !authenticated || accountConfigured),
                new AtfAssertion("API evidence profile", "ui-api or ui-only", profile,
                        "ui-api".equals(profile) || "ui-only".equals(profile)));
    }

    @Override public void close() {
        Throwable failure = null;
        try {
            browser.close();
        } catch (Throwable error) {
            failure = error;
        } finally {
            try {
                report.close();
            } catch (Throwable error) {
                if (failure == null) failure = error;
                else failure.addSuppressed(error);
            }
        }
        if (failure instanceof RuntimeException runtime) throw runtime;
        if (failure instanceof Error error) throw error;
        if (failure != null) throw new IllegalStateException("Practice Shop E2E teardown failed", failure);
    }

    public final class Scenario implements AutoCloseable {
        private final UiExtentReportSession.UiScenario scenario;
        private final UiExtentReportSession.UiPhase body;
        private final String title;
        private final List<ReportNetworkInspector.Step> networkSteps = new ArrayList<>();
        private boolean complete;
        private boolean networkInspectorPublished;

        private Scenario(UiExtentReportSession.UiScenario scenario, String title) {
            this.scenario = scenario;
            this.body = scenario.startPhase("TEST BODY");
            this.title = title == null || title.isBlank() ? "completed Practice Shop scenario" : title;
        }

        /**
         * Keeps failure publication and scenario closing out of the test body.
         * Tests remain a readable business narrative; this boundary owns the
         * exceptional lifecycle and preserves the original failure.
         */
        public void execute(Page page, PlaywrightActionSequence actions, Runnable testBody) {
            try {
                testBody.run();
            } catch (RuntimeException | AssertionError failure) {
                fail(page, actions, failure);
                throw failure;
            } finally {
                close();
            }
        }

        public void checkpoint(Page page, PlaywrightActionSequence actions,
                               PracticeShopPurchaseCheckpoint checkpoint) {
            UiScreenValidation.Evidence evidence = !PracticeShopScreenshotPolicy.shouldCapture(
                    checkpoint.passed(), configuration.browser().screenshotOnFailure())
                    ? new UiScreenValidation.Evidence(checkpoint.title(), "", false)
                    : UiScreenValidation.capture(page, UiScreenValidationConfiguration.defaults(), checkpoint.title());
            PracticeShopAuditObservations.recordCheckpoint(actions, page, checkpoint, evidence);
            networkSteps.add(ReportNetworkInspector.Step.from(checkpoint.keyword().text(), checkpoint.title(),
                    checkpoint.assertions(), checkpoint.apiEvidence()));
            AtfReportStatus checkpointStatus = checkpoint.passed()
                    ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
            body.step(checkpoint.keyword(), checkpoint.title(), page.url(), evidence.base64Png(),
                    checkpoint.assertions(), PracticeShopReportUiActionAdapters.from(actions.drain()),
                    checkpointStatus);
            if (!checkpoint.apiEvidence().isEmpty()) {
                body.apiTrace(checkpoint.keyword(), checkpoint.title(), page.url(),
                        checkpoint.apiEvidence(), checkpointStatus);
            }
        }

        /** Compatibility path for the optional authenticated catalog scenarios. */
        public void pass(Page page, PlaywrightActionSequence actions,
                         UiScreenValidation.Evidence evidence,
                         Collection<AtfAssertion> assertions) {
            PracticeShopAuditObservations.recordScreenContract(actions, page, title,
                    assertions, evidence, true);
            body.screenValidation(title, page.url(), evidence.base64Png(),
                    assertions, PracticeShopReportUiActionAdapters.from(actions.drain()),
                    AtfReportStatus.PASS);
            publishNetworkInspector();
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
            complete = true;
        }

        public void complete() {
            complete(AtfReportStatus.PASS);
        }

        public void complete(AtfReportStatus status) {
            publishNetworkInspector();
            body.complete(status);
            scenario.complete(status);
            complete = true;
        }

        public void warning(String title, String target, Collection<AtfAssertion> assertions) {
            body.diagnostic(AtfGherkinKeyword.AND, title, target, assertions,
                    AtfReportStatus.WARNING);
        }

        /**
         * Publishes one assertion per deduplicated runtime signature. The
         * aggregate remains the canonical source for count/sources, so a
         * response plus console observation is not rendered as two identical
         * warning rows.
         */
        public void warningDiagnostics(String title, String target,
                                       Collection<ReportNetworkInspector.Diagnostic> diagnostics) {
            var aggregate = ReportNetworkInspector.aggregate(List.of(), List.of(), diagnostics);
            var assertions = aggregate.diagnostics().stream()
                    .map(diagnostic -> new AtfAssertion(
                            "Runtime diagnostic",
                            diagnostic.method() + " " + diagnostic.route() + " → " + diagnostic.status()
                                    + " observed",
                            "count=" + diagnostic.count() + "; sources=" + diagnostic.sources(),
                            true))
                    .toList();
            warning(title, target, assertions);
        }

        public void fail(Page page, PlaywrightActionSequence actions, Throwable failure) {
            if (complete) return;
            try {
                publishNetworkInspector();
                body.failureEvidence("external E2E failure", page.url(),
                        PracticeShopReportUiActionAdapters.from(actions.drain()), failure);
                body.complete(AtfReportStatus.FAIL);
                scenario.complete(AtfReportStatus.FAIL);
            } catch (IllegalStateException alreadyClosed) {
                // A renderer may close the scenario while publishing a partial failure.
                // Preserve the browser/test exception rather than masking it.
            } finally {
                complete = true;
            }
        }

        @Override public void close() {
            if (!complete) {
                try {
                    complete(AtfReportStatus.FAIL);
                }
                finally { complete = true; }
            }
        }

        private void publishNetworkInspector() {
            if (networkInspectorPublished || !configuration.validationProfile().apiEvidenceRequired()) return;
            body.networkInspector("API network inspector — ordered UI journey", apiNetwork.selectedEvidence(),
                    networkSteps, runtimeDiagnostics.structuredDiagnostics().stream()
                            .map(diagnostic -> new ReportNetworkInspector.Diagnostic(
                                    diagnostic.source(), diagnostic.method(), diagnostic.route(),
                                    diagnostic.status(), diagnostic.message()))
                            .toList());
            networkInspectorPublished = true;
        }
    }
}


