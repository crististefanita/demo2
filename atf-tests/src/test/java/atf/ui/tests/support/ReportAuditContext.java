package atf.ui.tests.support;

import atf.api.console.AtfConsoleConfigurationLoader;
import atf.api.console.AtfConsoleReporter;
import atf.api.debugui.DebugUiConfiguration;
import atf.api.debugui.DebugUiServer;
import atf.api.reporting.ReportArtifactInspector;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.PlaywrightUiSession;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Shared lifecycle for audits that consume an Extent HTML artifact.
 *
 * <p>The input is always served read-only. Both a generated API report and an
 * already published report use this same context; only the input path and the
 * audit output name differ.</p>
 */
public final class ReportAuditContext implements AutoCloseable {
    private final Path inputReport;
    private final UiExtentReportSession auditSession;
    private final DebugUiServer server;
    private final PlaywrightUiSession browser;

    private ReportAuditContext(Path inputReport, UiExtentReportSession auditSession,
                               DebugUiServer server, PlaywrightUiSession browser) {
        this.inputReport = inputReport;
        this.auditSession = auditSession;
        this.server = server;
        this.browser = browser;
    }

    public static ReportAuditContext open(UiConfiguration configuration, Path inputReport,
                                         String auditFileName) {
        Objects.requireNonNull(configuration, "configuration");
        Path input = Objects.requireNonNull(inputReport, "inputReport")
                .toAbsolutePath().normalize();
        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException("Extent input report does not exist: " + input);
        }
        var inspection = ReportArtifactInspector.inspect(input);
        boolean readableHtml = inspection.readError().isEmpty()
                && inspection.satisfies("html exists")
                && inspection.satisfies("doctype");
        if (!readableHtml) {
            throw new IllegalArgumentException("Extent input report is not readable: "
                    + inspection.violations());
        }

        UiExtentReportSession audit = UiExtentReportSession.openConfigured(
                configuration.reportDirectory(), auditFileName,
                new AtfConsoleReporter(System.out, AtfConsoleConfigurationLoader.load().settings()));
        DebugUiServer debugUi = null;
        PlaywrightUiSession browser = null;
        try {
            debugUi = DebugUiServer.start(DebugUiConfiguration.forHtml(input, 0));
            browser = PlaywrightUiSession.open(configuration.browser());
            browser.navigate(debugUi.uriFor(input));
            return new ReportAuditContext(input, audit, debugUi, browser);
        } catch (RuntimeException failure) {
            if (browser != null) browser.close();
            if (debugUi != null) debugUi.close();
            audit.close();
            throw failure;
        }
    }

    public Path inputReport() {
        return inputReport;
    }

    public Path reportPath() {
        return auditSession.reportPath();
    }

    public UiExtentReportSession.UiScenario startScenario(String testClass, String testPackage,
                                                          String scenario, List<String> tags) {
        return auditSession.startScenario(testClass, testPackage, scenario, tags);
    }

    public PlaywrightActionSequence newActionSequence() {
        return browser.newActionSequence();
    }

    public Page openTab(String hash, PlaywrightActionSequence actions) {
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(actions, "actions");
        Page page = browser.page();
        actions.navigate(page, URI.create(server.uriFor(inputReport).toString() + hash));
        actions.waitForVisible(page.locator(hash), hash);
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
