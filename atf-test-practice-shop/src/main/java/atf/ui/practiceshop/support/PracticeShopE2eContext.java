package atf.ui.practiceshop.support;

import atf.api.console.AtfConsoleReporter;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.configuration.AtfReportConfigurationLoader;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.model.ReportScreenshot;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.support.evidence.PracticeShopBrowserCaptureProvider;
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
    private static final String NETWORK_INSPECTOR_SUMMARY_TITLE =
            "API network inspector — journey summary";
    private final PracticeShopConfiguration configuration;
    private final UiExtentReportSession report;
    private final PlaywrightUiSession browser;
    private final PracticeShopBrowserCaptureProvider browserCaptureProvider;
    private final PracticeShopApiNetworkJournal apiNetwork;

    private PracticeShopE2eContext(PracticeShopConfiguration configuration,
                                   UiExtentReportSession report, PlaywrightUiSession browser,
                                   PracticeShopBrowserCaptureProvider browserCaptureProvider,
                                   PracticeShopApiNetworkJournal apiNetwork) {
        this.configuration = configuration; this.report = report; this.browser = browser;
        this.browserCaptureProvider = browserCaptureProvider;
        this.apiNetwork = apiNetwork;
    }

    public static PracticeShopE2eContext open(PracticeShopConfiguration configuration) {
        return open(configuration, PracticeShopScreenshotPolicy.profile(
                configuration.reporting().screenshotPolicy()));
    }

    public static PracticeShopE2eContext open(PracticeShopConfiguration configuration,
                                               ReportEvidenceProfile evidenceProfile) {
        ReportEvidenceProfile yamlGovernedProfile = java.util.Objects.requireNonNull(
                        evidenceProfile, "evidenceProfile").toBuilder()
                .screenshotPolicy(configuration.reporting().screenshotPolicy())
                .build();
        String file = "ui-atf-practice-shop-e2e_"
                + STAMP.withZone(Clock.systemDefaultZone().getZone()).format(Clock.systemDefaultZone().instant())
                + ".html";
        AtfConsoleSink console = AtfConsoleReporter.configuredStdout();
        var reportConfiguration = AtfReportConfigurationLoader.load();
        UiExtentReportSession report = UiExtentReportSession.openConfiguredResolved(
                configuration.uiReportDirectory(), file,
                atf.api.reporting.console.AtfConsoleSink.noOp(),
                yamlGovernedProfile,
                PracticeShopReportSystemMetadata.snapshot(configuration,
                        reportConfiguration.reportingSettings(), yamlGovernedProfile));
        try {
            PlaywrightUiSession browser = PlaywrightUiSession.open(configuration.browser(), console);
            PracticeShopBrowserCaptureProvider browserCaptureProvider =
                    new PracticeShopBrowserCaptureProvider(browser.page(),
                            UiScreenValidationConfiguration.defaults());
            PracticeShopApiNetworkJournal apiNetwork =
                    new PracticeShopApiNetworkJournal(configuration.validationProfile(),
                            configuration.api().evidenceBinding());
            apiNetwork.attach(browser.networkInspector());
            return new PracticeShopE2eContext(configuration, report, browser, browserCaptureProvider,
                    apiNetwork);
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
    public ReportScreenshot captureCheckpointScreenshot(String title,
            ReportScreenshotPolicy.CapturePoint capturePoint) {
        return report.evidenceProfile().capture(capturePoint, title, page().url(), browserCaptureProvider)
                .orElse(null);
    }

    /** Captures the visible browser viewport directly, independent of terminal screenshot policy. */
    public ReportScreenshot captureBrowserViewport(String title) {
        return browserCaptureProvider.capture(title, page().url());
    }

    public Scenario scenario(String title, String overview) {
        return scenario("PracticeShopE2eTest", title, overview);
    }

    /** Starts a report scenario with the concrete test identity of the caller. */
    public Scenario scenario(String reportClass, String title, String overview) {
        UiExtentReportSession.UiScenario scenario = startReportScenario(reportClass, overview);
        var setup = scenario.startPhase("SETUP");
        List<AtfAssertion> setupAssertions = setupAssertions(configuration);
        AtfReportStatus setupStatus = setupAssertions.stream().allMatch(AtfAssertion::passed)
                ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        boolean authenticated = configuration.authenticatedE2eAllowed();
        setup.setup(AtfGherkinKeyword.GIVEN,
                authenticated
                        ? "the pre-registered Practice Shop account fixture is loaded"
                        : "the typed guest checkout configuration is ready",
                authenticated ? configuration.accountPolicy().evidenceTarget() : "YAML checkout inputs",
                setupAssertions, setupStatus);
        setup.complete(setupStatus);
        return new Scenario(scenario, title, null);
    }

    /** Opens SETUP and defers TEST BODY until the caller proves account setup succeeded. */
    public Scenario scenarioWithExplicitSetup(String reportClass, String title, String overview) {
        UiExtentReportSession.UiScenario scenario = startReportScenario(reportClass, overview);
        return new Scenario(scenario, title, scenario.startPhase("SETUP"));
    }

    private UiExtentReportSession.UiScenario startReportScenario(String reportClass, String overview) {
        return report.startScenario(reportClass, "atf.ui.tests.practiceshop",
                overview, List.of("ui", "e2e", "external", "practice-shop"));
    }

    static List<AtfAssertion> setupAssertions(PracticeShopConfiguration configuration) {
        boolean accountFixtureRequired = configuration.authenticatedE2eEnabled();
        boolean accountConfigured = accountFixtureRequired && configuration.accountConfigured();
        boolean authenticated = configuration.authenticatedE2eAllowed();
        String selectedMode = authenticated ? "authenticated" : "guest";
        String expectedMode = configuration.authenticatedE2eEnabled() && accountConfigured
                ? "authenticated" : "guest";
        String profile = configuration.validationProfile().name().toLowerCase().replace('_', '-');
        var accountPolicy = configuration.accountPolicy();
        boolean policyAccepted = !accountFixtureRequired || accountPolicy.accepts(configuration.accountFile());
        String observedPolicy = !accountFixtureRequired
                ? "not required for guest journey"
                : policyAccepted ? accountPolicy.displayName() + "; credentials redacted"
                : "fixture path outside module-owned src/test/resources";
        String modeEvidence = authenticated ? "authenticated checkout; account fixture available"
                : "guest checkout; account fixture not required";
        return List.of(
                new AtfAssertion("Checkout mode", expectedMode + " checkout is selected",
                        modeEvidence,
                        expectedMode.equals(selectedMode)),
                new AtfAssertion("Account setup",
                        authenticated ? "pre-registered account file is available" : "not required for guest journey",
                        authenticated
                                ? (accountConfigured ? "account file available; credentials redacted" : "account file missing")
                                : "not required for guest journey",
                        !authenticated || accountConfigured),
                new AtfAssertion("Account fixture policy",
                        accountFixtureRequired ? accountPolicy.expectedEvidence() : "not required for guest journey",
                        observedPolicy,
                        policyAccepted),
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
        private final UiExtentReportSession.UiPhase setupPhase;
        private UiExtentReportSession.UiPhase body;
        private final String title;
        private final List<ReportNetworkInspector.Step> networkSteps = new ArrayList<>();
        private boolean complete;
        private boolean networkInspectorPublished;
        private boolean setupStepPublished;
        private boolean setupFinished;

        private Scenario(UiExtentReportSession.UiScenario scenario, String title,
                         UiExtentReportSession.UiPhase setupPhase) {
            this.scenario = scenario;
            this.setupPhase = setupPhase;
            this.body = setupPhase == null ? scenario.startPhase("TEST BODY") : null;
            this.title = title == null || title.isBlank() ? "completed Practice Shop scenario" : title;
        }

        /** Publishes setup evidence and captures after every non-empty Playwright action table. */
        public void setupCheckpoint(Page page, PlaywrightActionSequence actions, AtfGherkinKeyword keyword,
                                    String stepTitle,
                                    Collection<AtfAssertion> assertions,
                                    Collection<ReportExecutionEvidence> apiEvidence,
                                    AtfReportStatus status) {
            setupCheckpoint(page, actions, keyword, stepTitle, assertions, apiEvidence, null, status);
        }

        public void setupCheckpoint(Page page, PlaywrightActionSequence actions, AtfGherkinKeyword keyword,
                                    String stepTitle,
                                    Collection<AtfAssertion> assertions,
                                    Collection<ReportExecutionEvidence> apiEvidence,
                                    ReportScreenshot suppliedScreenshot,
                                    AtfReportStatus status) {
            if (setupPhase == null || setupFinished) {
                throw new IllegalStateException("Explicit authenticated setup is not open");
            }
            ReportScreenshotPolicy.CapturePoint point = status == AtfReportStatus.PASS
                    ? ReportScreenshotPolicy.CapturePoint.CHECKPOINT
                    : ReportScreenshotPolicy.CapturePoint.FAILURE;
            ReportScreenshot screenshot = suppliedScreenshot != null ? suppliedScreenshot
                    : actions.snapshot().isEmpty() ? null : captureCheckpointScreenshot(stepTitle, point);
            setupPhase.setup(keyword, stepTitle, page.url(), assertions,
                    PracticeShopReportUiActionAdapters.from(actions.drain()), apiEvidence, screenshot, status);
            setupStepPublished = true;
        }

        /** The business body does not exist in the report until setup has passed. */
        public void finishSetup(AtfReportStatus status) {
            if (setupPhase == null || setupFinished) {
                throw new IllegalStateException("Explicit authenticated setup is not open");
            }
            if (!setupStepPublished) {
                throw new IllegalStateException("SETUP must publish at least one account step before it can finish");
            }
            setupPhase.complete(status);
            setupFinished = true;
            if (status == AtfReportStatus.PASS) {
                body = scenario.startPhase("TEST BODY");
            } else {
                scenario.complete(status);
                complete = true;
            }
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
            if (body == null) throw new IllegalStateException("TEST BODY cannot start before setup passes");
            var capturePoint = checkpoint.passed()
                    ? ReportScreenshotPolicy.CapturePoint.CHECKPOINT
                    : ReportScreenshotPolicy.CapturePoint.FAILURE;
            String payload = actions.snapshot().isEmpty()
                    ? "" : capture(page, checkpoint.title(), capturePoint);
            UiScreenValidation.Evidence evidence = new UiScreenValidation.Evidence(
                    checkpoint.title(), payload, !payload.isBlank());
            PracticeShopAuditObservations.recordCheckpoint(actions, page, checkpoint, evidence);
            networkSteps.add(ReportNetworkInspector.Step.from(checkpoint.keyword().text(), checkpoint.title(),
                    checkpoint.assertions(), checkpoint.apiEvidence()));
            AtfReportStatus checkpointStatus = checkpoint.passed()
                    ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
            body.step(checkpoint.keyword(), checkpoint.title(), page.url(), evidence.base64Png(),
                    checkpoint.assertions(), PracticeShopReportUiActionAdapters.from(actions.drain()),
                    checkpoint.apiEvidence(), capturePoint, checkpointStatus);
        }

        /** Final screen contract for the optional authenticated catalog scenarios. */
        public void pass(Page page, PlaywrightActionSequence actions,
                         String evidenceTitle,
                         Collection<AtfAssertion> assertions) {
            if (body == null) throw new IllegalStateException("TEST BODY cannot start before setup passes");
            String payload = capture(page, evidenceTitle, ReportScreenshotPolicy.CapturePoint.FINAL);
            UiScreenValidation.Evidence evidence = new UiScreenValidation.Evidence(
                    evidenceTitle, payload, !payload.isBlank());
            var actionEvidence = PracticeShopReportUiActionAdapters.from(actions.drain());
            if (evidence.hasScreenshotPayload()) {
                PracticeShopAuditObservations.requireScreenshotForScreenValidation(evidence);
                PracticeShopAuditObservations.recordScreenContract(actions, page, title,
                        assertions, evidence, true);
                body.screenValidation(title, page.url(), evidence.base64Png(),
                        assertions, actionEvidence,
                        ReportScreenshotPolicy.CapturePoint.FINAL, AtfReportStatus.PASS);
            } else {
                body.step(AtfGherkinKeyword.THEN, title, page.url(), "", assertions,
                        actionEvidence, List.of(), ReportScreenshotPolicy.CapturePoint.FINAL,
                        AtfReportStatus.PASS);
            }
            publishNetworkInspector();
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
            complete = true;
        }

        public void complete() {
            complete(AtfReportStatus.PASS);
        }

        public void complete(AtfReportStatus status) {
            if (body == null) throw new IllegalStateException("TEST BODY cannot start before setup passes");
            publishNetworkInspector();
            body.complete(status);
            scenario.complete(status);
            complete = true;
        }

        public void fail(Page page, PlaywrightActionSequence actions, Throwable failure) {
            if (complete) return;
            if (body == null && setupPhase != null && !setupFinished) {
                try {
                    if (!setupStepPublished) {
                        AtfAssertion failed = new AtfAssertion("Authenticated account setup",
                                "registration/login setup passes before TEST BODY",
                                "setup failed; business flow was not started", false);
                        setupPhase.step(AtfGherkinKeyword.GIVEN,
                                "authenticated account setup completes before the purchase flow",
                                page.url(), "", List.of(failed),
                                PracticeShopReportUiActionAdapters.from(actions.drain()),
                                setupEvidence(), ReportScreenshotPolicy.CapturePoint.FAILURE,
                                AtfReportStatus.FAIL);
                        setupStepPublished = true;
                    }
                    setupPhase.complete(AtfReportStatus.FAIL);
                    setupFinished = true;
                    scenario.complete(AtfReportStatus.FAIL);
                } catch (IllegalStateException alreadyClosed) {
                    // Preserve the original setup failure if report publication already closed the phase.
                } finally {
                    complete = true;
                }
                return;
            }
            try {
                String screenshot = "";
                try {
                    screenshot = capture(page, "external E2E failure",
                            ReportScreenshotPolicy.CapturePoint.FAILURE);
                } catch (RuntimeException captureFailure) {
                    failure.addSuppressed(captureFailure);
                }
                body.failureEvidence("external E2E failure", page.url(),
                        PracticeShopReportUiActionAdapters.from(actions.drain()), failure, screenshot);
                publishNetworkInspector();
                body.complete(AtfReportStatus.FAIL);
                scenario.complete(AtfReportStatus.FAIL);
            } catch (IllegalStateException alreadyClosed) {
                // A renderer may close the scenario while publishing a partial failure.
                // Preserve the browser/test exception rather than masking it.
            } finally {
                complete = true;
            }
        }

        private String capture(Page page, String title,
                               ReportScreenshotPolicy.CapturePoint point) {
            return report.evidenceProfile().capture(point, title, page.url(),
                            browserCaptureProvider)
                    .map(ReportScreenshot::base64Png).orElse("");
        }

        @Override public void close() {
            if (!complete) {
                if (body == null) {
                    fail(page(), actions(), new IllegalStateException("authenticated setup did not complete"));
                } else {
                    try { complete(AtfReportStatus.FAIL); }
                    finally { complete = true; }
                }
            }
        }

        private List<ReportExecutionEvidence> setupEvidence() {
            List<ReportExecutionEvidence> evidence = new ArrayList<>();
            evidence.addAll(apiNetwork.evidence(atf.ui.practiceshop.config.PracticeShopApiCheckpoint.REGISTRATION));
            evidence.addAll(apiNetwork.evidence(atf.ui.practiceshop.config.PracticeShopApiCheckpoint.LOGIN));
            return List.copyOf(evidence);
        }

        private void publishNetworkInspector() {
            if (networkInspectorPublished || !configuration.validationProfile().apiEvidenceRequired()) return;
            body.networkInspector(NETWORK_INSPECTOR_SUMMARY_TITLE, apiNetwork.inspectorEvidence(),
                    networkSteps);
            networkInspectorPublished = true;
        }

    }
}


