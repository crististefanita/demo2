package atf.ui.autoex.support;

import atf.api.console.junit.AtfConsoleTestContext;
import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.configuration.AtfReportConfigurationLoader;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.testkit.files.AtfTestResourceReader;
import atf.api.testkit.output.ReportOutputDirectory;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.api.ui.playwright.AtfLocator;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.config.AutoExConfigurationLoader;
import atf.ui.autoex.locator.AutoExSelectors;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Per-test composition root for one headless browser and one UI Extent artifact. */
public final class AutoExE2eRun {
    private static final String REPORT_FILE_PREFIX = "ui-atf-auto-ex-";
    private static final DateTimeFormatter REPORT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private final AutoExConfiguration configuration;
    private final PlaywrightUiSession browser;
    private final UiExtentReportSession report;
    private final UiExtentReportSession.UiScenario scenario;
    private final UiExtentReportSession.UiPhase phase;
    private final PlaywrightActionSequence actions;
    private final AutoExVignetteHandler vignetteHandler = new AutoExVignetteHandler();
    private Cleanup failureCleanup;
    private boolean finished;

    private AutoExE2eRun(AutoExConfiguration configuration, PlaywrightUiSession browser,
                         UiExtentReportSession report, UiExtentReportSession.UiScenario scenario,
                         UiExtentReportSession.UiPhase phase) {
        this.configuration = configuration;
        this.browser = browser;
        this.report = report;
        this.scenario = scenario;
        this.phase = phase;
        this.actions = browser.newActionSequence();
    }

    static AutoExE2eRun open(ExtensionContext context) {
        AutoExConfiguration configuration = AutoExConfigurationLoader.load();
        var projectRoot = AutoExProjectRoot.discover();
        var uiBase = AtfTestResourceReader.resolveContained(
                projectRoot, configuration.uiReportBaseDirectory());
        var output = ReportOutputDirectory.resolve(projectRoot, uiBase,
                context.getRequiredTestClass());
        var console = AtfConsoleTestContext.configuredSink();
        var reportConsole = AutoExConsoleBridge.reportingSink(console);
        var screenshotPolicy = AtfReportConfigurationLoader.load().screenshotPolicy();
        var evidence = ReportEvidenceProfile.forScreenshotPolicy(screenshotPolicy);
        var report = UiExtentReportSession.openConfigured(output, reportName(context), reportConsole, evidence);
        PlaywrightUiSession browser = null;
        try {
            browser = PlaywrightUiSession.open(configuration.browser(), console);
            var scenario = report.startScenario(context.getRequiredTestClass().getSimpleName(),
                    context.getRequiredTestClass().getPackageName(), context.getDisplayName(),
                    List.of("e2e", "ui", "automation-exercise"));
            var phase = scenario.startPhase("TEST BODY");
            return new AutoExE2eRun(configuration, browser, report, scenario, phase);
        } catch (RuntimeException | Error failure) {
            if (browser != null) browser.close();
            report.close();
            throw failure;
        }
    }

    private static String reportName(ExtensionContext context) {
        String method = context.getRequiredTestMethod().getName().replaceAll("[^A-Za-z0-9_-]", "-");
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return REPORT_FILE_PREFIX + method + "_" + REPORT_TIME.format(LocalDateTime.now())
                + "_" + unique + ".html";
    }

    public AutoExConfiguration configuration() { return configuration; }
    public Page page() { return browser.page(); }
    public String currentUrl() { return page().url(); }

    public void navigate(java.net.URI uri) {
        prepareUiInteraction();
        actions.navigate(page(), Objects.requireNonNull(uri, "uri"), "requested page is loaded");
    }

    public void click(String selector, String expected) {
        prepareUiInteraction();
        actions.click(locator(selector), expected);
    }

    public void clickFirst(String selector, String expected) {
        prepareUiInteraction();
        actions.click(new AtfLocator(page().locator(selector).first(), selector), expected);
    }

    public void clickLast(String selector, String expected) {
        prepareUiInteraction();
        actions.click(new AtfLocator(page().locator(selector).last(), selector), expected);
    }

    public void fill(String selector, String value, String expected) {
        prepareUiInteraction();
        actions.fill(locator(selector), value, expected);
    }

    public String text(String selector, String expected) {
        prepareUiInteraction();
        return actions.getText(locator(selector), expected);
    }

    public String textFirst(String selector, String expected) {
        prepareUiInteraction();
        return actions.getText(new AtfLocator(page().locator(selector).first(), selector + " (first match)"), expected);
    }

    public String value(String selector, String expected) {
        prepareUiInteraction();
        return actions.expectValue(locator(selector), expected);
    }

    public void select(String selector, String label, String expected) {
        prepareUiInteraction();
        actions.selectOption(locator(selector), label, expected);
    }

    public boolean visible(String selector) {
        prepareUiInteraction();
        return page().locator(selector).isVisible();
    }

    public int count(String selector) {
        prepareUiInteraction();
        return page().locator(selector).count();
    }

    public String firstAttribute(String selector, String attribute) {
        prepareUiInteraction();
        return page().locator(selector).first().getAttribute(attribute);
    }

    public void checkpoint(AtfGherkinKeyword keyword, String title, List<AtfAssertion> assertions) {
        prepareUiInteraction();
        boolean passed = assertions.stream().allMatch(AtfAssertion::passed);
        var screenshot = UiScreenValidation.capture(page(), UiScreenValidationConfiguration.defaults(), title,
                List.of(page().locator(AutoExSelectors.SENSITIVE_FIELDS)));
        phase.step(keyword, title, currentUrl(), screenshot.base64Png(), assertions,
                ReportModelAdapters.uiActions(actions.drain()),
                passed ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
        if (!passed) throw new AssertionError("UI checkpoint failed: " + title);
    }

    public void onFailureCleanup(Cleanup cleanup) {
        failureCleanup = Objects.requireNonNull(cleanup, "cleanup");
    }

    public void clearFailureCleanup() { failureCleanup = null; }

    void finish(Throwable testFailure) throws Exception {
        if (finished) return;
        finished = true;
        Throwable terminalFailure = testFailure;
        boolean cleanupWasPending = failureCleanup != null;
        if (failureCleanup != null) {
            try { failureCleanup.run(); }
            catch (Exception | Error cleanupFailure) {
                terminalFailure = combine(terminalFailure, cleanupFailure);
            }
            failureCleanup = null;
        }
        if (testFailure == null && cleanupWasPending && terminalFailure == null) {
            terminalFailure = new AssertionError(
                    "E2E completed without explicitly deleting its disposable account");
        }

        if (terminalFailure != null) publishFailure(terminalFailure);
        AtfReportStatus status = terminalFailure == null ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        try { phase.complete(status); }
        catch (RuntimeException | Error reportFailure) { terminalFailure = combine(terminalFailure, reportFailure); }
        try { scenario.complete(status); }
        catch (RuntimeException | Error reportFailure) { terminalFailure = combine(terminalFailure, reportFailure); }
        try { browser.close(); }
        catch (RuntimeException | Error closeFailure) { terminalFailure = combine(terminalFailure, closeFailure); }
        try { report.close(); }
        catch (RuntimeException | Error closeFailure) { terminalFailure = combine(terminalFailure, closeFailure); }

        if (testFailure == null && terminalFailure != null) {
            if (terminalFailure instanceof Exception exception) throw exception;
            if (terminalFailure instanceof Error error) throw error;
            throw new IllegalStateException("Automation Exercise E2E teardown failed", terminalFailure);
        }
    }

    private void publishFailure(Throwable failure) {
        String screenshot = "";
        try {
            screenshot = UiScreenValidation.capture(page(), UiScreenValidationConfiguration.defaults(),
                    "Failure state — sensitive fields masked",
                    List.of(page().locator(AutoExSelectors.SENSITIVE_FIELDS))).base64Png();
        } catch (RuntimeException | Error captureFailure) {
            failure.addSuppressed(captureFailure);
        }
        try {
            phase.failureEvidence("Automation Exercise UI E2E failure", currentUrl(),
                    ReportModelAdapters.uiActions(actions.drain()), failure, screenshot);
        } catch (RuntimeException | Error reportFailure) {
            failure.addSuppressed(reportFailure);
        }
    }

    public boolean dismissGoogleVignetteIfPresent() {
        return vignetteHandler.dismissIfPresent(page(), actions);
    }

    private void prepareUiInteraction() {
        dismissGoogleVignetteIfPresent();
    }

    private static Throwable combine(Throwable primary, Throwable secondary) {
        if (primary == null) return secondary;
        if (primary != secondary) primary.addSuppressed(secondary);
        return primary;
    }

    private AtfLocator locator(String selector) { return new AtfLocator(page().locator(selector), selector); }

    @FunctionalInterface
    public interface Cleanup { void run() throws Exception; }
}
