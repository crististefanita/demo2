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
import java.util.Locale;
import java.util.Objects;

/**
 * Renderer scoped to one report file. It deliberately owns no static mutable
 * state, so parallel executions cannot leak nodes or verdicts into each other.
 */
public final class HtmlReportSession implements AtfReportPorts.Session {
    public static final List<String> TAB_ORDER = List.of("Tests", "Category", "Fails", "Overview");

    private final Path reportPath;
    private final ReportingSettings settings;
    private final HtmlReportSemanticDocument document = new HtmlReportSemanticDocument();
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
        renderTests(html);
        renderCategory(html);
        renderFails(html);
        renderOverview(html);
        html.append("</main><script>").append(Css.SCRIPT).append("</script></body></html>");
        return html.toString();
    }

    private void renderTests(StringBuilder html) {
        html.append("<section id=\"tests\" class=\"tab-panel\"><div class=\"layout\"><aside><h2>Tests</h2>");
        for (HtmlReportSemanticDocument.ScenarioModel scenario : document.scenarios()) {
            html.append("<a class=\"test-link status-").append(scenario.status().name().toLowerCase())
                    .append("\" href=\"#").append(scenario.id).append("\">")
                    .append(escape(scenario.scenario)).append("<small>")
                    .append(escape(scenario.testClass)).append("</small></a>");
        }
        html.append("</aside><div class=\"test-content\">");
        for (HtmlReportSemanticDocument.ScenarioModel scenario : document.scenarios()) renderScenario(html, scenario);
        html.append("</div></div></section>");
    }

    private void renderScenario(StringBuilder html, HtmlReportSemanticDocument.ScenarioModel scenario) {
        html.append("<article id=\"").append(scenario.id).append("\" class=\"scenario\">")
                .append("<div class=\"scenario-title\"><h2>").append(escape(scenario.scenario))
                .append("</h2><span class=\"status status-").append(scenario.status().name().toLowerCase())
                .append("\">").append(scenario.status()).append("</span></div>");
        for (HtmlReportSemanticDocument.PhaseModel phase : scenario.phases) {
            html.append("<section class=\"phase phase-").append(phase.status.name().toLowerCase())
                    .append("\" data-atf-node-role=\"phase\" data-atf-phase-name=\"")
                    .append(escape(phaseKey(phase.name))).append("\">")
                    .append("<h3>").append(escape(phase.name)).append("<span class=\"status status-")
                    .append(phase.status.name().toLowerCase()).append("\">").append(phase.status).append("</span></h3>");
            for (HtmlReportSemanticDocument.NodeModel child : phase.children) renderNode(html, child);
            html.append("</section>");
        }
        html.append("</article>");
    }

    private void renderCategory(StringBuilder html) {
        html.append("<section id=\"category\" class=\"tab-panel\"><h2>Category</h2><div class=\"tag-list\">");
        document.scenarios().stream().flatMap(s -> s.tags.stream()).distinct().sorted()
                .forEach(tag -> html.append("<span class=\"tag\">").append(escape(tag)).append("</span>"));
        html.append("</div></section>");
    }

    private void renderFails(StringBuilder html) {
        html.append("<section id=\"fails\" class=\"tab-panel\"><h2>Fails</h2><div class=\"fail-list\">");
        boolean any = false;
        for (HtmlReportSemanticDocument.ScenarioModel scenario : document.scenarios()) {
            for (HtmlReportSemanticDocument.PhaseModel phase : scenario.phases) {
                for (HtmlReportSemanticDocument.NodeModel child : phase.children) {
                    if (child instanceof HtmlReportSemanticDocument.ValidationModel validation) {
                        for (AtfAssertion row : validation.assertions) {
                            if (row.resultStatus() == AtfReportStatus.FAIL || (!row.passed() && validation.status == AtfReportStatus.FAIL)) {
                                any = true;
                                html.append("<a class=\"fail-row\" href=\"#").append(scenario.id).append("\">")
                                        .append("<span class=\"status status-fail\">Fail</span><strong>")
                                        .append(escape(row.name())).append("</strong><span>")
                                        .append(escape(row.actual())).append("</span></a>");
                            }
                        }
                    }
                }
            }
        }
        if (!any) html.append("<p class=\"empty\">No failed assertions.</p>");
        html.append("</div></section>");
    }

    private void renderOverview(StringBuilder html) {
        var scenarios = document.scenarios();
        long passed = scenarios.stream().filter(s -> s.status() == AtfReportStatus.PASS).count();
        long failed = scenarios.stream().filter(s -> s.status() == AtfReportStatus.FAIL).count();
        long skipped = scenarios.stream().filter(s -> s.status() == AtfReportStatus.SKIP).count();
        html.append("<section id=\"overview\" class=\"tab-panel\"><h2>Overview</h2><div class=\"summary\">")
                .append(metric("Tests", scenarios.size())).append(metric("Passed", passed))
                .append(metric("Failed", failed)).append(metric("Skipped", skipped)).append("</div>")
                .append("<p class=\"contract\">Overview is the default view; its tab remains last in the navigation contract.</p></section>");
    }

    private static String metric(String label, long value) {
        return "<div class=\"metric\"><span>" + escape(label) + "</span><strong>" + value + "</strong></div>";
    }

    private static String phaseKey(String name) {
        return (name == null ? "" : name.trim()).toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Report session is closed");
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private void renderNode(StringBuilder html, HtmlReportSemanticDocument.NodeModel node) {
        if (node instanceof HtmlReportSemanticDocument.RequestModel request) {
            renderRequest(html, request);
        } else if (node instanceof HtmlReportSemanticDocument.ValidationModel validation) {
            renderValidation(html, validation);
        } else {
            throw new IllegalStateException("Unsupported report node: " + node.getClass().getName());
        }
    }

    private void renderRequest(StringBuilder html, HtmlReportSemanticDocument.RequestModel request) {
        html.append("<div class=\"request\" data-atf-node-role=\"request\"><div class=\"node-title\"><span>")
                .append(escape(request.method + " " + request.path + " — " + request.title))
                .append("</span><span class=\"status status-").append(request.status.name().toLowerCase())
                .append("\">").append(request.status).append("</span></div>");
        if (!request.details.isEmpty()) {
            html.append("<details class=\"step-detail response-evidence\"><summary aria-expanded=\"false\"><span class=\"atf-disclosure-glyph\" aria-hidden=\"true\"></span><span class=\"se-title\">")
                    .append(escape(AtfReportLabels.responseDetails(request.details.size())))
                    .append("</span></summary><div class=\"helm-api-classification technical\"><span class=\"helm-api-classification-label\">")
                    .append(AtfReportLabels.TECHNICAL_TRACE)
                    .append("</span></div><table class=\"helm-evidence-table helm-trace-table\">");
            for (String[] detail : request.details) {
                html.append("<tr><th>").append(escape(detail[0])).append("</th><td>")
                        .append(escape(detail[1])).append("</td></tr>");
            }
            html.append("</table></details>");
        }
        for (String[] item : request.json) {
            html.append("<details class=\"step-detail json-evidence\"><summary aria-expanded=\"false\"><span class=\"atf-disclosure-glyph\" aria-hidden=\"true\"></span><span class=\"se-title\">")
                    .append(escape(item[0])).append("</span></summary><pre class=\"json-code\">")
                    .append(escape(item[1])).append("</pre></details>");
        }
        html.append("</div>");
    }

    private void renderValidation(StringBuilder html, HtmlReportSemanticDocument.ValidationModel validation) {
        html.append("<div class=\"validation\" data-atf-node-role=\"validation\"><div class=\"node-title\"><span>")
                .append(escape(validation.title)).append("</span><span class=\"status status-")
                .append(validation.status.name().toLowerCase()).append("\">").append(validation.status)
                .append("</span></div><table class=\"assertions\"><thead><tr><th>ASSERTION</th><th>EXPECTED</th><th>ACTUAL</th><th>RESULT</th></tr></thead><tbody>");
        for (AtfAssertion row : validation.assertions) {
            html.append("<tr><td>").append(escape(row.name())).append("</td><td>")
                    .append(escape(row.expected())).append("</td><td>").append(escape(row.actual()))
                    .append("</td><td><span class=\"status status-")
                    .append(row.resultStatus().name().toLowerCase()).append("\">")
                    .append(row.resultStatus()).append("</span></td></tr>");
        }
        html.append("</tbody></table></div>");
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
