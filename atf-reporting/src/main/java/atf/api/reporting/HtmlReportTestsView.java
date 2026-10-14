package atf.api.reporting;

import atf.api.core.http.ApiAttemptRecord;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.security.SensitiveDataRedactor;

import java.util.List;

import static atf.api.reporting.HtmlReportMarkupSupport.escape;
import static atf.api.reporting.HtmlReportMarkupSupport.phaseKey;

/** Renders only the Tests projection of the fallback semantic document. */
final class HtmlReportTestsView {
    private final SensitiveDataRedactor redactor;

    HtmlReportTestsView() {
        this(new SensitiveDataRedactor());
    }

    HtmlReportTestsView(SensitiveDataRedactor redactor) {
        this.redactor = redactor;
    }

    String render(List<HtmlReportSemanticDocument.ScenarioModel> scenarios) {
        var html = new StringBuilder();
        html.append("<section id=\"tests\" class=\"tab-panel\"><div class=\"layout\"><aside><h2>Tests</h2>");
        for (var scenario : scenarios) {
            html.append("<a class=\"test-link status-").append(scenario.status().name().toLowerCase())
                    .append("\" href=\"#").append(scenario.id).append("\">")
                    .append(escape(scenario.scenario)).append("<small>")
                    .append(escape(scenario.testClass)).append("</small></a>");
        }
        html.append("</aside><div class=\"test-content\">");
        for (var scenario : scenarios) {
            renderScenario(html, scenario);
        }
        return html.append("</div></div></section>").toString();
    }

    private void renderScenario(StringBuilder html, HtmlReportSemanticDocument.ScenarioModel scenario) {
        html.append("<article id=\"").append(scenario.id).append("\" class=\"scenario\">")
                .append("<div class=\"scenario-title\"><h2>").append(escape(scenario.scenario))
                .append("</h2><span class=\"status status-").append(scenario.status().name().toLowerCase())
                .append("\">").append(scenario.status()).append("</span></div>");
        for (var phase : scenario.phases) {
            html.append("<section class=\"phase phase-").append(phase.status.name().toLowerCase())
                    .append("\" data-atf-node-role=\"phase\" data-atf-phase-name=\"")
                    .append(escape(phaseKey(phase.name))).append("\">")
                    .append("<h3>").append(escape(phase.name)).append("<span class=\"status status-")
                    .append(phase.status.name().toLowerCase()).append("\">").append(phase.status).append("</span></h3>");
            renderPhaseDetails(html, phase);
            for (var child : phase.children) {
                renderNode(html, child);
            }
            html.append("</section>");
        }
        html.append("</article>");
    }

    private void renderPhaseDetails(StringBuilder html, HtmlReportSemanticDocument.PhaseModel phase) {
        if (phase.details.isEmpty()) return;
        html.append("<table class=\"helm-evidence-table helm-trace-table phase-details\"><tbody>");
        for (String[] detail : phase.details) {
            html.append("<tr><th>").append(escape(detail[0])).append("</th><td>")
                    .append(escape(redactor.redactField(detail[0], detail[1]))).append("</td></tr>");
        }
        html.append("</tbody></table>");
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
                        .append(escape(redactor.redactField(detail[0], detail[1]))).append("</td></tr>");
            }
            html.append("</table></details>");
        }
        for (String[] item : request.json) {
            html.append("<details class=\"step-detail json-evidence\"><summary aria-expanded=\"false\"><span class=\"atf-disclosure-glyph\" aria-hidden=\"true\"></span><span class=\"se-title\">")
                    .append(escape(item[0])).append("</span></summary><pre class=\"json-code\">")
                    .append(escape(redactor.redactJson(item[1]))).append("</pre></details>");
        }
        renderExecution(html, request.execution);
        html.append("</div>");
    }

    private void renderExecution(StringBuilder html, ApiExecutionResult execution) {
        if (execution == null) {
            return;
        }
        html.append("<details class=\"step-detail execution-evidence\"><summary aria-expanded=\"false\">")
                .append("<span class=\"atf-disclosure-glyph\" aria-hidden=\"true\"></span>")
                .append("<span class=\"se-title\">Execution evidence · ")
                .append(execution.attempts()).append(" attempts</span></summary>")
                .append("<table class=\"helm-evidence-table execution-table\"><thead><tr><th>FIELD</th><th>VALUE</th></tr></thead><tbody>");
        row(html, "response.status", String.valueOf(execution.response().status()));
        row(html, "response.url", redactor.redactText(execution.response().url()));
        row(html, "response.duration.ms", String.valueOf(execution.response().duration().toMillis()));
        row(html, "attempts", String.valueOf(execution.attempts()));
        row(html, "retry.events", String.valueOf(execution.retryEvents().size()));
        html.append("</tbody></table>");
        if (!execution.attemptHistory().isEmpty()) {
            html.append("<table class=\"helm-evidence-table retry-history-table\"><thead><tr>")
                    .append("<th>ATTEMPT</th><th>STATUS</th><th>DURATION (MS)</th><th>OUTCOME</th><th>REASON</th>")
                    .append("</tr></thead><tbody>");
            for (ApiAttemptRecord attempt : execution.attemptHistory()) {
                html.append("<tr><td>").append(attempt.number()).append("</td><td>")
                        .append(attempt.statusCode()).append("</td><td>")
                        .append(attempt.duration().toMillis()).append("</td><td>")
                        .append(escape(attempt.outcome().name())).append("</td><td>")
                        .append(escape(redactor.redactText(attempt.reason()))).append("</td></tr>");
            }
            html.append("</tbody></table>");
        }
        html.append("</details>");
    }

    private static void row(StringBuilder html, String field, String value) {
        html.append("<tr><th>").append(escape(field)).append("</th><td>")
                .append(escape(value == null ? "" : value)).append("</td></tr>");
    }

    private void renderValidation(StringBuilder html, HtmlReportSemanticDocument.ValidationModel validation) {
        html.append("<div class=\"validation\" data-atf-node-role=\"validation\"><div class=\"node-title\"><span>")
                .append(escape(validation.title)).append("</span><span class=\"status status-")
                .append(validation.status.name().toLowerCase()).append("\">").append(validation.status)
                .append("</span></div><table class=\"assertions\"><thead><tr><th>ASSERTION</th><th>EXPECTED</th><th>ACTUAL</th><th>RESULT</th></tr></thead><tbody>");
        for (AtfAssertion row : validation.assertions) {
            html.append("<tr><td>").append(escape(redactor.redactText(row.name()))).append("</td><td>")
                    .append(escape(redactor.redactField(row.name(), row.expected()))).append("</td><td>")
                    .append(escape(redactor.redactField(row.name(), row.actual())))
                    .append("</td><td><span class=\"status status-")
                    .append(row.resultStatus().name().toLowerCase()).append("\">")
                    .append(row.resultStatus()).append("</span></td></tr>");
        }
        html.append("</tbody></table></div>");
    }
}
