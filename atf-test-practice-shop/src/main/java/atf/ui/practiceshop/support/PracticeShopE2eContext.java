package atf.ui.practiceshop.support;

import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.UiExtentReportSession;
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
        UiExtentReportSession report = UiExtentReportSession.openConfiguredResolved(
                projectRoot().resolve("artifacts/reports/ui"),
                file, event -> { });
        try {
            PlaywrightUiSession browser = PlaywrightUiSession.open(configuration.browser(),
                    AtfConsoleSink.noOp());
            PracticeShopApiNetworkJournal apiNetwork =
                    new PracticeShopApiNetworkJournal(configuration.validationProfile());
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
        setup.setup(AtfGherkinKeyword.GIVEN,
                "the typed guest checkout configuration is ready",
                "YAML checkout inputs",
                List.of(new AtfAssertion("Guest checkout", "valid and account-independent",
                        configuration.accountConfigured() ? "optional account available" : "guest flow without account",
                        true),
                        new AtfAssertion("API evidence profile", "ui-api or ui-only",
                                configuration.validationProfile().name().toLowerCase(), true),
                        new AtfAssertion("API evidence disclosures", "request, response and execution details are attached per checkpoint",
                                configuration.validationProfile().apiEvidenceRequired()
                                        ? "enabled per API exchange" : "disabled by profile",
                                true)),
                AtfReportStatus.PASS);
        setup.complete(AtfReportStatus.PASS);
        return new Scenario(scenario, title);
    }

    @Override public void close() { browser.close(); report.close(); }

    public final class Scenario implements AutoCloseable {
        private final UiExtentReportSession.UiScenario scenario;
        private final UiExtentReportSession.UiPhase body;
        private final String title;
        private boolean complete;

        private Scenario(UiExtentReportSession.UiScenario scenario, String title) {
            this.scenario = scenario;
            this.body = scenario.startPhase("TEST BODY");
            this.title = title == null || title.isBlank() ? "completed Practice Shop scenario" : title;
        }

        public void checkpoint(Page page, PlaywrightActionSequence actions,
                               PracticeShopPurchaseCheckpoint checkpoint) {
            UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                    page, UiScreenValidationConfiguration.defaults(), checkpoint.title());
            body.step(checkpoint.keyword(), checkpoint.title(), page.url(), evidence.base64Png(),
                    checkpoint.assertions(), PracticeShopReportUiActionAdapters.from(actions.drain()),
                    checkpoint.apiEvidence(),
                    checkpoint.passed() ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
        }

        /** Compatibility path for the optional authenticated catalog scenarios. */
        public void pass(Page page, PlaywrightActionSequence actions,
                         UiScreenValidation.Evidence evidence,
                         Collection<AtfAssertion> assertions) {
            body.screenValidation(title, page.url(), evidence.base64Png(),
                    assertions, PracticeShopReportUiActionAdapters.from(actions.drain()),
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
            complete = true;
        }

        public void complete() {
            complete(AtfReportStatus.PASS);
        }

        public void complete(AtfReportStatus status) {
            body.complete(status);
            scenario.complete(status);
            complete = true;
        }

        public void warning(String title, String target, Collection<AtfAssertion> assertions) {
            body.step(AtfGherkinKeyword.AND, title, target, "", assertions, List.of(),
                    AtfReportStatus.WARNING);
        }

        public void fail(Page page, PlaywrightActionSequence actions, Throwable failure) {
            if (complete) return;
            try {
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
                    body.complete(AtfReportStatus.FAIL);
                    scenario.complete(AtfReportStatus.FAIL);
                }
                finally { complete = true; }
            }
        }
    }
}


