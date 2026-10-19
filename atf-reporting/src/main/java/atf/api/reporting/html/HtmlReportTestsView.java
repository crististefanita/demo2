package atf.api.reporting.html;

import atf.api.core.config.ReportingSettings;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.reporting.html.evidence.HtmlReportEvidenceView;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportField;
import atf.api.reporting.model.ReportExecutionModel;

import java.util.List;
import java.util.Objects;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;
import static atf.api.reporting.html.HtmlReportMarkupSupport.phaseKey;

/** Renders the Tests projection; evidence details are delegated to one view. */
public final class HtmlReportTestsView {
    private final SensitiveDataRedactor redactor;
    private final HtmlReportEvidenceView evidence;

    HtmlReportTestsView() {
        this(ReportingSettings.defaults(), new SensitiveDataRedactor());
    }

    HtmlReportTestsView(SensitiveDataRedactor redactor) {
        this(ReportingSettings.defaults(), redactor);
    }

    HtmlReportTestsView(ReportingSettings settings, SensitiveDataRedactor redactor) {
        Objects.requireNonNull(settings, "settings");
        this.redactor = Objects.requireNonNull(redactor, "redactor");
        this.evidence = new HtmlReportEvidenceView(settings, this.redactor);
    }

    String render(ReportExecutionModel snapshot) {
        List<ReportExecutionModel.Scenario> scenarios = snapshot.scenarios();
        var html = new StringBuilder();
        html.append("<section id=\"tests\" ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.TAB_PANEL))
                .append("><div ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.LAYOUT))
                .append("><aside><h2>Tests</h2>");
        for (var scenario : scenarios) {
            html.append("<a ").append(AtfReportHtmlVocabulary.classAttribute(
                            AtfReportHtmlVocabulary.CssClass.TEST_LINK.value(),
                            AtfReportHtmlVocabulary.CssClass.STATUS.value(),
                            AtfReportHtmlVocabulary.statusClass(scenario.status())))
                    .append(" href=\"#").append(scenario.id()).append("\">")
                    .append(escape(scenario.scenario())).append("<small>")
                    .append(escape(scenario.testClass())).append("</small></a>");
        }
        html.append("</aside><div ").append(AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.TEST_CONTENT)).append(">");
        for (var scenario : scenarios) {
            renderScenario(html, scenario);
        }
        return html.append("</div></div></section>").toString();
    }

    private void renderScenario(StringBuilder html, ReportExecutionModel.Scenario scenario) {
        html.append("<article id=\"").append(scenario.id()).append("\" ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.SCENARIO))
                .append("><div ").append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.SCENARIO_TITLE))
                .append("><h2>").append(escape(scenario.scenario()))
                .append("</h2><span ").append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.statusClasses(scenario.status())))
                .append(">").append(scenario.status()).append("</span></div>");
        for (var phase : scenario.phases()) {
            html.append("<section ").append(AtfReportHtmlVocabulary.classAttribute(
                            AtfReportHtmlVocabulary.CssClass.PHASE.value(),
                            AtfReportHtmlVocabulary.stateClass("phase-", phase.status())))
                    .append(" ").append(AtfReportHtmlVocabulary.attribute(
                            AtfReportHtmlVocabulary.Attribute.NODE_ROLE, "phase"))
                    .append(" ").append(AtfReportHtmlVocabulary.attribute(
                            AtfReportHtmlVocabulary.Attribute.PHASE_NAME, phaseKey(phase.name()))).append(">")
                    .append("<h3>").append(escape(phase.name())).append("<span ")
                    .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.statusClasses(phase.status()))).append(">").append(phase.status())
                    .append("</span></h3>");
            renderPhaseDetails(html, phase);
            for (var child : phase.children()) {
                renderNode(html, child);
            }
            html.append("</section>");
        }
        html.append("</article>");
    }

    private void renderPhaseDetails(StringBuilder html, ReportExecutionModel.Phase phase) {
        if (phase.details().isEmpty()) return;
        html.append("<table ").append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE.value(),
                        AtfReportHtmlVocabulary.CssClass.HELM_TRACE_TABLE.value(),
                        AtfReportHtmlVocabulary.CssClass.PHASE_DETAILS.value()))
                .append("><tbody>");
        for (ReportField detail : phase.details()) {
            html.append("<tr><th>").append(escape(detail.name())).append("</th><td>")
                    .append(escape(redactor.redactField(detail.name(), detail.value()))).append("</td></tr>");
        }
        html.append("</tbody></table>");
    }

    private void renderNode(StringBuilder html, ReportExecutionModel.Node node) {
        if (node instanceof ReportExecutionModel.Request request) {
            renderRequest(html, request);
        } else if (node instanceof ReportExecutionModel.Validation validation) {
            evidence.renderValidation(html, validation);
        } else {
            throw new IllegalStateException("Unsupported report node: " + node.getClass().getName());
        }
    }

    private void renderRequest(StringBuilder html, ReportExecutionModel.Request request) {
        html.append("<div ").append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.REQUEST))
                .append(" ").append(AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.NODE_ROLE, "request"))
                .append("><div ").append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.NODE_TITLE)).append("><span>")
                .append(escape(request.method() + " " + request.path() + " — " + request.title()))
                .append("</span><span ").append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.statusClasses(request.status())))
                .append(">").append(request.status()).append("</span></div>");
        evidence.renderRequestDetails(html, request);
        evidence.renderResponseDetails(html, request);
        evidence.renderExecution(html, request.execution());
        for (ReportExecutionModel.Node child : request.children()) {
            renderNode(html, child);
        }
        html.append("</div>");
    }
}
