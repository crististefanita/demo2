package atf.api.reporting.html;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.console.AtfConsoleEvent;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.semantic.AtfReportSemanticRecorder;
import atf.api.reporting.semantic.AtfReportSemanticSnapshot;
import atf.api.reporting.theme.AtfReportTheme;
import atf.api.reporting.html.script.HtmlReportNavigationScript;
import atf.api.reporting.html.script.HtmlReportScript;
import atf.api.reporting.html.scope.HtmlReportScenarioScope;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.ui.model.ReportDocumentModel;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.security.SensitiveDataRedactor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/**
 * Renderer scoped to one report file. It deliberately owns no static mutable
 * state, so parallel executions cannot leak nodes or verdicts into each other.
 */
public final class HtmlReportSession implements AtfReportPorts.Session {
    private static final String HTML_DOCTYPE = "<!doctype html>";
    private static final String HTML_OPEN = "<html lang=\"en\"><head>";
    private static final String HTML_META = "<meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">";
    private static final String HTML_TITLE = "<title>" + AtfReportLabels.REPORT_TITLE + "</title>";
    private static final String HTML_STYLE_OPEN = "<style>";
    private static final String HTML_STYLE_CLOSE = "</style>";
    private static final String HTML_BODY_OPEN = "</head><body>";
    private static final String HTML_HEADER = "<header><span class=\"brand\">ATF</span><span class=\"title\">"
            + AtfReportLabels.REPORT_TITLE + "</span></header>";
    private static final String HTML_MAIN_OPEN = "</nav><main>";
    private static final String HTML_MAIN_CLOSE = "</main>";
    private static final String HTML_SCRIPT_OPEN = "<script>";
    private static final String HTML_SCRIPT_CLOSE = "</script></body></html>";

    public static final ReportDocumentModel DOCUMENT_MODEL = ReportDocumentModel.fallback();

    private final Path reportPath;
    private final ReportingSettings settings;
    private final HtmlReportSemanticDocument document = new HtmlReportSemanticDocument();
    private final AtfReportSemanticRecorder semantic = new AtfReportSemanticRecorder();
    private final HtmlReportTestsView testsView;
    private final HtmlReportCategoryView categoryView = new HtmlReportCategoryView();
    private final HtmlReportFailsView failsView = new HtmlReportFailsView();
    private final HtmlReportOverviewView overviewView = new HtmlReportOverviewView();
    private final AtfConsoleSink console;
    private final HtmlReportScript navigationScript;
    private boolean closed;

    public HtmlReportSession(Path reportPath) {
        this(reportPath, ReportingSettings.defaults());
    }

    public HtmlReportSession(Path reportPath, ReportingSettings settings) {
        this(reportPath, settings, AtfConsoleSink.noOp());
    }

    public HtmlReportSession(Path reportPath, ReportingSettings settings, AtfConsoleSink console) {
        this(reportPath, settings, console, HtmlReportNavigationScript.standard());
    }

    /**
     * Creates a session with an explicit browser asset port.
     *
     * <p>The overload is public so an alternate report host can replace
     * navigation without subclassing or copying the lifecycle renderer.</p>
     */
    public HtmlReportSession(Path reportPath, ReportingSettings settings, AtfConsoleSink console,
                             HtmlReportScript navigationScript) {
        this.reportPath = Objects.requireNonNull(reportPath, "reportPath");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.testsView = new HtmlReportTestsView(settings, new SensitiveDataRedactor());
        this.console = console == null ? AtfConsoleSink.noOp() : console;
        this.navigationScript = Objects.requireNonNull(navigationScript, "navigationScript");
    }

    @Override
    public Path reportPath() {
        return reportPath;
    }

    /** Returns the same renderer-neutral semantic population used by Extent. */
    public AtfReportSemanticSnapshot semanticSnapshot() {
        return AtfReportSemanticSnapshot.from(document.snapshot());
    }

    /** Exposes the fallback execution tree for component-level parity checks. */
    public atf.api.reporting.model.ReportExecutionModel executionModel() {
        return document.snapshot();
    }

    @Override
    public AtfReportPorts.Scenario startScenario(String testClass, String scenario, Collection<String> tags) {
        return startScenario(testClass, null, scenario, tags);
    }

    /** Package-aware overload kept in parity with the Extent projection. */
    @Override
    public AtfReportPorts.Scenario startScenario(String testClass, String testPackage,
                                                 String scenario, Collection<String> tags) {
        ensureOpen();
        var model = document.startScenario(testClass, testPackage, scenario, tags);
        String name = (testClass == null ? "API tests" : testClass) + " :: "
                + (scenario == null ? "Scenario" : scenario);
        console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.SCENARIO, name));
        return new HtmlReportScenarioScope(model, name,
                semantic.startScenario(testClass, testPackage, scenario, tags), console);
    }

    @Override
    public void flush() {
        ensureOpen();
        try {
            if (reportPath.getParent() != null) Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, render(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot write report " + reportPath, error);
        }
    }

    @Override
    public void close() {
        if (!closed) {
            flush();
            closed = true;
        }
    }

    public String render() {
        assertSemanticPopulationAligned();
        var html = new StringBuilder(32_000);
        html.append(String.join("", HTML_DOCTYPE, HTML_OPEN, HTML_META, HTML_TITLE,
                HTML_STYLE_OPEN, AtfReportTheme.fallbackCss(), HTML_STYLE_CLOSE, HTML_BODY_OPEN));
        html.append(HTML_HEADER);
        html.append("<nav ").append(AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.TABS)).append(">");
        for (var tabModel : DOCUMENT_MODEL.tabs()) {
            var tab = tabModel.tab();
            html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.BUTTON,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.TAB),
                            AtfReportHtmlVocabulary.attribute(
                                    AtfReportHtmlVocabulary.Attribute.FALLBACK_TAB, tab.value())))
                    .append(escape(tab.label()))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.BUTTON));
        }
        html.append(HTML_MAIN_OPEN);
        var snapshot = document.snapshot();
        html.append(testsView.render(snapshot));
        html.append(categoryView.render(snapshot));
        html.append(failsView.render(snapshot));
        html.append(overviewView.render(snapshot));
        html.append(HTML_MAIN_CLOSE).append(HTML_SCRIPT_OPEN)
                .append(navigationScript.source()).append(HTML_SCRIPT_CLOSE);
        return html.toString();
    }

    /** Fails before publication if the renderer and the shared semantic model diverge. */
    private void assertSemanticPopulationAligned() {
        var fallbackSnapshot = AtfReportSemanticSnapshot.from(document.snapshot());
        var recordedSnapshot = semantic.snapshot();
        if (!fallbackSnapshot.equals(recordedSnapshot)) {
            throw new IllegalStateException("Fallback semantic snapshot drift between execution model and recorder");
        }
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Report session is closed");
    }

    private static long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }
}
