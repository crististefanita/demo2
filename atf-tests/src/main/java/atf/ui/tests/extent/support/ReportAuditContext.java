package atf.ui.tests.extent.support;

import atf.api.console.AtfConsoleConfigurationLoader;
import atf.api.console.AtfConsoleReporter;
import atf.api.debugui.DebugUiConfiguration;
import atf.api.debugui.DebugUiServer;
import atf.api.reporting.ReportArtifactInspector;
import atf.api.reporting.ReportArtifactIdentity;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.extent.locators.ReportUiLocators;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

/** Shared read-only lifecycle for generated and existing Extent report audits. */
public final class ReportAuditContext implements AutoCloseable {
    private final ReportAuditInput input;
    private final Path inputReport;
    private final UiExtentReportSession auditSession;
    private final DebugUiServer server;
    private final PlaywrightUiSession browser;
    private final ReportUiLocators locators = new ReportUiLocators();

    private ReportAuditContext(ReportAuditInput input, UiExtentReportSession auditSession,
                               DebugUiServer server, PlaywrightUiSession browser) {
        this.input = Objects.requireNonNull(input, "input");
        this.inputReport = input.report();
        this.auditSession = auditSession;
        this.server = server;
        this.browser = browser;
    }

    public static ReportAuditContext open(UiConfiguration configuration, Path inputReport,
                                          String auditSuffix) {
        return open(configuration, ReportAuditInput.existing(inputReport), auditSuffix);
    }

    /** Opens either a freshly generated API artifact or an existing report read-only. */
    public static ReportAuditContext open(UiConfiguration configuration, ReportAuditInput input,
                                          String auditSuffix) {
        Objects.requireNonNull(configuration, "configuration");
        ReportAuditInput resolved = Objects.requireNonNull(input, "input");
        Path inputReport = resolved.report();
        if (!Files.isRegularFile(inputReport)) {
            throw new IllegalArgumentException("Extent input report does not exist: " + inputReport);
        }
        var inspection = ReportArtifactInspector.inspect(inputReport);
        boolean readableHtml = inspection.readError().isEmpty()
                && inspection.satisfies("html exists")
                && inspection.satisfies("doctype");
        if (!readableHtml) {
            throw new IllegalArgumentException("Extent input report is not readable: "
                    + inspection.violations());
        }

        String auditFileName = configuration.timestampedReportFileName(auditSuffix,
                Clock.systemDefaultZone());
        UiExtentReportSession audit = UiExtentReportSession.openConfiguredResolved(
                configuration.reportDirectory(), auditFileName,
                new AtfConsoleReporter(System.out, AtfConsoleConfigurationLoader.load().settings()));
        DebugUiServer debugUi = null;
        PlaywrightUiSession browser = null;
        try {
            debugUi = DebugUiServer.start(DebugUiConfiguration.forHtml(inputReport, 0));
            browser = PlaywrightUiSession.open(configuration.browser());
            browser.navigate(debugUi.uriFor(inputReport));
            return new ReportAuditContext(resolved, audit, debugUi, browser);
        } catch (RuntimeException failure) {
            if (browser != null) browser.close();
            if (debugUi != null) debugUi.close();
            audit.close();
            throw failure;
        }
    }

    public Path inputReport() { return inputReport; }

    /** Identity shown in UI setup evidence so the UI→API pair is auditable. */
    public ReportArtifactIdentity inputArtifactIdentity() {
        return ReportArtifactIdentity.read(inputReport);
    }

    public ReportAuditInput input() { return input; }

    public ReportAuditInputKind inputKind() { return input.kind(); }

    public Path reportPath() { return auditSession.reportPath(); }

    public UiExtentReportSession.UiScenario startScenario(String testClass, String testPackage,
                                                           String scenario, List<String> tags) {
        return auditSession.startScenario(testClass, testPackage, scenario, tags);
    }

    public PlaywrightActionSequence newActionSequence() { return browser.newActionSequence(); }

    /** Captures the current page only when the configured failure policy enables it. */
    public UiScreenValidation.Evidence captureFailure(UiConfiguration configuration, String title) {
        Objects.requireNonNull(configuration, "configuration");
        return UiScreenValidation.capture(browser.page(),
                new UiScreenValidationConfiguration(configuration.browser().screenshotOnFailure(),
                        configuration.screenValidation().fullPage()), title);
    }

    public Page openTab(String hash, PlaywrightActionSequence actions) {
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(actions, "actions");
        Page page = browser.page();
        actions.navigate(page, URI.create(server.uriFor(inputReport).toString() + hash),
                "API Extent report " + inputReport.getFileName() + " is loaded");
        actions.waitForVisible(locators.tabViewByHash(page, hash), hash);
        actions.observation("Tab visible", hash, "selected tab is visible", hash);
        return page;
    }

    @Override
    public void close() {
        browser.close();
        server.close();
        auditSession.close();
    }
}
