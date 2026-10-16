package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfConsoleEvent;
import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.core.ui.AtfUiActionRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static atf.api.reporting.HtmlReportMarkupSupport.escape;

/**
 * Renderer scoped to one report file. It deliberately owns no static mutable
 * state, so parallel executions cannot leak nodes or verdicts into each other.
 */
public final class HtmlReportSession implements AtfReportPorts.Session {
    public static final List<String> TAB_ORDER = List.of("Tests", "Category", "Fails", "Overview");

    private final Path reportPath;
    private final ReportingSettings settings;
    private final HtmlReportSemanticDocument document = new HtmlReportSemanticDocument();
    private final AtfReportSemanticRecorder semantic = new AtfReportSemanticRecorder();
    private final HtmlReportTestsView testsView;
    private final HtmlReportCategoryView categoryView = new HtmlReportCategoryView();
    private final HtmlReportFailsView failsView = new HtmlReportFailsView();
    private final HtmlReportOverviewView overviewView = new HtmlReportOverviewView();
    private final AtfConsoleSink console;
    private boolean closed;

    public HtmlReportSession(Path reportPath) {
        this(reportPath, ReportingSettings.defaults());
    }

    public HtmlReportSession(Path reportPath, ReportingSettings settings) {
        this(reportPath, settings, AtfConsoleSink.noOp());
    }

    public HtmlReportSession(Path reportPath, ReportingSettings settings, AtfConsoleSink console) {
        this.reportPath = Objects.requireNonNull(reportPath, "reportPath");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.testsView = new HtmlReportTestsView(settings, new SensitiveDataRedactor());
        this.console = console == null ? AtfConsoleSink.noOp() : console;
    }

    @Override
    public Path reportPath() {
        return reportPath;
    }

    /** Returns the same renderer-neutral semantic population used by Extent. */
    public AtfReportSemanticSnapshot semanticSnapshot() {
        return semantic.snapshot();
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
        var model = document.startScenario(testClass, scenario, tags);
        String name = (testClass == null ? "API tests" : testClass) + " :: "
                + (scenario == null ? "Scenario" : scenario);
        console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.SCENARIO, name));
        return new ScenarioScope(model, name,
                semantic.startScenario(testClass, testPackage, scenario, tags));
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
        html.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
                .append("<title>Configurable API Automation</title>")
                .append("<style>").append(AtfReportTheme.fallbackCss()).append("</style></head><body>");
        html.append("<header><span class=\"brand\">ATF</span><span class=\"title\">Configurable API Automation</span></header>");
        html.append("<nav class=\"tabs\">");
        for (String tab : TAB_ORDER) {
            String id = tab.toLowerCase();
            html.append("<button class=\"tab\" data-tab=\"").append(id).append("\">").append(escape(tab)).append("</button>");
        }
        html.append("</nav><main>");
        var snapshot = document.snapshot();
        html.append(testsView.render(snapshot));
        html.append(categoryView.render(snapshot));
        html.append(failsView.render(snapshot));
        html.append(overviewView.render(snapshot));
        html.append("</main><script>").append(Css.SCRIPT).append("</script></body></html>");
        return html.toString();
    }

    /** Fails before publication if the renderer and the shared semantic model diverge. */
    private void assertSemanticPopulationAligned() {
        int renderedScenarios = document.scenarios().size();
        int recordedScenarios = semantic.snapshot().scenarioCount();
        if (renderedScenarios != recordedScenarios) {
            throw new IllegalStateException("Fallback semantic snapshot drift: rendered scenarios="
                    + renderedScenarios + ", recorded scenarios=" + recordedScenarios);
        }
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Report session is closed");
    }

    private final class ScenarioScope implements AtfReportPorts.Scenario {
        private final HtmlReportSemanticDocument.ScenarioModel model;
        private final String consoleName;
        private final AtfReportSemanticRecorder.ScenarioHandle semantic;
        private final long startedAt = System.nanoTime();
        private boolean closed;

        private ScenarioScope(HtmlReportSemanticDocument.ScenarioModel model, String consoleName,
                              AtfReportSemanticRecorder.ScenarioHandle semantic) {
            this.model = model;
            this.consoleName = consoleName;
            this.semantic = semantic;
        }
        @Override public String id() { return model.id; }
        @Override public AtfReportPorts.Phase startPhase(String name) {
            ensureOpen();
            var phase = new HtmlReportSemanticDocument.PhaseModel(name);
            model.phases.add(phase);
            String phaseName = name == null ? "" : name;
            console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.PHASE, phaseName));
            return new PhaseScope(model, phase, phaseName, semantic.startPhase(phaseName));
        }
        @Override public void complete(AtfReportStatus status) {
            model.status = status;
            semantic.complete(status);
            console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.SCENARIO,
                    consoleName, status, elapsedMillis(startedAt)));
        }
        @Override public boolean hasWarnings() { return model.status == AtfReportStatus.WARNING || model.phases.stream().anyMatch(p -> p.status == AtfReportStatus.WARNING); }
        @Override public void close() { closed = true; }
        private void ensureOpen() { if (closed) throw new IllegalStateException("Scenario is closed"); }
    }

    private final class PhaseScope implements AtfReportPorts.Phase {
        private final HtmlReportSemanticDocument.ScenarioModel scenario;
        private final HtmlReportSemanticDocument.PhaseModel model;
        private final String consoleName;
        private final AtfReportSemanticRecorder.PhaseHandle semantic;
        private final long startedAt = System.nanoTime();
        private boolean closed;
        private PhaseScope(HtmlReportSemanticDocument.ScenarioModel scenario,
                           HtmlReportSemanticDocument.PhaseModel model, String consoleName,
                           AtfReportSemanticRecorder.PhaseHandle semantic) {
            this.scenario = scenario;
            this.model = model;
            this.consoleName = consoleName;
            this.semantic = semantic;
        }
        @Override public AtfReportPorts.Request startRequest(String title, String method, String path) {
            ensureOpen();
            var request = new HtmlReportSemanticDocument.RequestModel(title, method, path);
            model.children.add(request);
            String requestName = ExtentReportNodeSupport.renderRequestTitle(title, method, path);
            console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.REQUEST,
                    requestName));
            return new RequestScope(request, requestName,
                    semantic.startRequest(title, method, path));
        }
        @Override public void detail(String name, String value) {
            model.details.add(new String[]{name, value});
            semantic.detail(name);
        }
        @Override public void complete(AtfReportStatus status) {
            model.status = status;
            semantic.complete(status);
            console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.PHASE,
                    consoleName, status, elapsedMillis(startedAt)));
        }
        @Override public void close() { closed = true; }
        private void ensureOpen() { if (closed) throw new IllegalStateException("Phase is closed"); }
    }

    private final class RequestScope implements AtfReportPorts.Request {
        private final HtmlReportSemanticDocument.RequestModel model;
        private final String consoleName;
        private final AtfReportSemanticRecorder.RequestHandle semantic;
        private final long startedAt = System.nanoTime();
        private boolean closed;
        private RequestScope(HtmlReportSemanticDocument.RequestModel model, String consoleName,
                             AtfReportSemanticRecorder.RequestHandle semantic) {
            this.model = model;
            this.consoleName = consoleName;
            this.semantic = semantic;
        }
        @Override public void detail(String name, String value) { model.details.add(new String[]{name, value}); semantic.detail(name); }
        @Override public void json(String label, String json) { model.json.add(new String[]{label, json}); semantic.json(label); }
        @Override public void execution(ApiExecutionResult result) { model.execution = result; semantic.execution(); }
        @Override public void screenshot(String title, String base64Png) { semantic.screenshot(); }
        @Override public void uiActions(Collection<AtfUiActionRecord> actions) { semantic.uiActions(actions); }
        @Override public AtfReportPorts.Validation startValidation(String title) {
            ensureOpen();
            var validation = new HtmlReportSemanticDocument.ValidationModel(title);
            model.children.add(validation);
            String validationName = title == null ? "Validation" : title;
            console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.VALIDATION, validationName));
            return new ValidationScope(validation, validationName,
                    semantic.startValidation(validationName));
        }
        @Override public void complete(AtfReportStatus status) {
            model.status = status;
            semantic.complete(status);
            console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                    consoleName, status, elapsedMillis(startedAt)));
        }
        @Override public void close() { closed = true; }
    }

    private final class ValidationScope implements AtfReportPorts.Validation {
        private final HtmlReportSemanticDocument.ValidationModel model;
        private final String consoleName;
        private final AtfReportSemanticRecorder.ValidationHandle semantic;
        private final long startedAt = System.nanoTime();
        private boolean closed;
        private ValidationScope(HtmlReportSemanticDocument.ValidationModel model, String consoleName,
                                AtfReportSemanticRecorder.ValidationHandle semantic) {
            this.model = model;
            this.consoleName = consoleName;
            this.semantic = semantic;
        }
        @Override public void assertions(Collection<AtfAssertion> assertions) { model.assertions = assertions == null ? List.of() : List.copyOf(assertions); semantic.assertions(assertions); }
        @Override public void uiActions(Collection<AtfUiActionRecord> actions) { semantic.uiActions(actions); }
        @Override public void screenshot(String title, String base64Png) { semantic.screenshot(); }
        @Override public void mediaScreenshot(String title, String base64Png) { semantic.screenshot(); }
        @Override public void complete(AtfReportStatus status) {
            model.status = status;
            semantic.complete(status);
            console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.VALIDATION,
                    consoleName, status, elapsedMillis(startedAt)));
        }
        @Override public void close() { closed = true; }
    }

    private static final class Css {
        private static final String SCRIPT = "const tabs=[...document.querySelectorAll('.tab')];const panels=[...document.querySelectorAll('.tab-panel')];function activate(id){tabs.forEach(t=>t.classList.toggle('active',t.dataset.tab===id));panels.forEach(p=>p.classList.toggle('active',p.id===id));history.replaceState(null,'','#'+id)}tabs.forEach(t=>t.addEventListener('click',()=>activate(t.dataset.tab)));activate('overview');document.querySelectorAll('.fail-row').forEach(a=>a.addEventListener('click',()=>activate('tests')));";
    }

    private static long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }
}
