package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;

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
    private final HtmlReportTestsView testsView = new HtmlReportTestsView();
    private final HtmlReportCategoryView categoryView = new HtmlReportCategoryView();
    private final HtmlReportFailsView failsView = new HtmlReportFailsView();
    private final HtmlReportOverviewView overviewView = new HtmlReportOverviewView();
    private boolean closed;

    public HtmlReportSession(Path reportPath) {
        this(reportPath, ReportingSettings.defaults());
    }

    public HtmlReportSession(Path reportPath, ReportingSettings settings) {
        this.reportPath = Objects.requireNonNull(reportPath, "reportPath");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    @Override
    public Path reportPath() {
        return reportPath;
    }

    @Override
    public AtfReportPorts.Scenario startScenario(String testClass, String scenario, Collection<String> tags) {
        ensureOpen();
        var model = document.startScenario(testClass, scenario, tags);
        return new ScenarioScope(model);
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
        var scenarios = document.scenarios();
        html.append(testsView.render(scenarios));
        html.append(categoryView.render(scenarios));
        html.append(failsView.render(scenarios));
        html.append(overviewView.render(scenarios));
        html.append("</main><script>").append(Css.SCRIPT).append("</script></body></html>");
        return html.toString();
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Report session is closed");
    }

    private final class ScenarioScope implements AtfReportPorts.Scenario {
        private final HtmlReportSemanticDocument.ScenarioModel model;
        private boolean closed;

        private ScenarioScope(HtmlReportSemanticDocument.ScenarioModel model) { this.model = model; }
        @Override public String id() { return model.id; }
        @Override public AtfReportPorts.Phase startPhase(String name) {
            ensureOpen();
            var phase = new HtmlReportSemanticDocument.PhaseModel(name);
            model.phases.add(phase);
            return new PhaseScope(model, phase);
        }
        @Override public void complete(AtfReportStatus status) { model.status = status; }
        @Override public boolean hasWarnings() { return model.status == AtfReportStatus.WARNING || model.phases.stream().anyMatch(p -> p.status == AtfReportStatus.WARNING); }
        @Override public void close() { closed = true; }
        private void ensureOpen() { if (closed) throw new IllegalStateException("Scenario is closed"); }
    }

    private final class PhaseScope implements AtfReportPorts.Phase {
        private final HtmlReportSemanticDocument.ScenarioModel scenario;
        private final HtmlReportSemanticDocument.PhaseModel model;
        private boolean closed;
        private PhaseScope(HtmlReportSemanticDocument.ScenarioModel scenario, HtmlReportSemanticDocument.PhaseModel model) { this.scenario = scenario; this.model = model; }
        @Override public AtfReportPorts.Request startRequest(String title, String method, String path) {
            ensureOpen();
            var request = new HtmlReportSemanticDocument.RequestModel(title, method, path);
            model.children.add(request);
            return new RequestScope(model, request);
        }
        @Override public void complete(AtfReportStatus status) { model.status = status; }
        @Override public void close() { closed = true; }
        private void ensureOpen() { if (closed) throw new IllegalStateException("Phase is closed"); }
    }

    private final class RequestScope implements AtfReportPorts.Request {
        private final HtmlReportSemanticDocument.PhaseModel phase;
        private final HtmlReportSemanticDocument.RequestModel model;
        private boolean closed;
        private RequestScope(HtmlReportSemanticDocument.PhaseModel phase, HtmlReportSemanticDocument.RequestModel model) { this.phase = phase; this.model = model; }
        @Override public void detail(String name, String value) { model.details.add(new String[]{name, value}); }
        @Override public void json(String label, String json) { model.json.add(new String[]{label, json}); }
        @Override public AtfReportPorts.Validation startValidation(String title) {
            ensureOpen();
            var validation = new HtmlReportSemanticDocument.ValidationModel(title);
            phase.children.add(validation);
            return new ValidationScope(validation);
        }
        @Override public void complete(AtfReportStatus status) { model.status = status; }
        @Override public void close() { closed = true; }
    }

    private final class ValidationScope implements AtfReportPorts.Validation {
        private final HtmlReportSemanticDocument.ValidationModel model;
        private boolean closed;
        private ValidationScope(HtmlReportSemanticDocument.ValidationModel model) { this.model = model; }
        @Override public void assertions(Collection<AtfAssertion> assertions) { model.assertions = assertions == null ? List.of() : List.copyOf(assertions); }
        @Override public void complete(AtfReportStatus status) { model.status = status; }
        @Override public void close() { closed = true; }
    }

    private static final class Css {
        private static final String SCRIPT = "const tabs=[...document.querySelectorAll('.tab')];const panels=[...document.querySelectorAll('.tab-panel')];function activate(id){tabs.forEach(t=>t.classList.toggle('active',t.dataset.tab===id));panels.forEach(p=>p.classList.toggle('active',p.id===id));history.replaceState(null,'','#'+id)}tabs.forEach(t=>t.addEventListener('click',()=>activate(t.dataset.tab)));activate('overview');document.querySelectorAll('.fail-row').forEach(a=>a.addEventListener('click',()=>activate('tests')));";
    }
}
