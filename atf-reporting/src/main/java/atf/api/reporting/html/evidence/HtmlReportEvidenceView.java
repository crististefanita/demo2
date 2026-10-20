package atf.api.reporting.html.evidence;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.duration.AtfDurationFormatter;
import atf.api.reporting.security.SensitiveDataRedactor;
import atf.api.reporting.evidence.EvidenceTableFormatter;
import atf.api.reporting.evidence.markup.ReportEvidenceMarkup;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.AtfReportMarkup;
import atf.api.reporting.evidence.markup.ReportEvidenceJsonFormatter;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;
import atf.api.reporting.model.ReportField;
import atf.api.reporting.model.ReportJsonBody;
import atf.api.reporting.api.ReportModelAdapters;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.attribute;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.classAttribute;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.closeTag;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.openTag;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.NODE_ROLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.ASSERTIONS;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.EXECUTION_EVIDENCE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.EXECUTION_TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.HELM_REQUEST_TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.HELM_SCROLL_BLOCK_TITLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.HELM_TRACE_TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.JSON_CODE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.NODE_TITLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.REQUEST_EVIDENCE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.REQUEST_DETAILS_CONTENT;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.REQUEST_JSON_BODY;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.RESPONSE_EVIDENCE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.RESPONSE_DETAILS_CONTENT;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.RESPONSE_JSON_BODY;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.RETRY_HISTORY_TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.VALIDATION;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.DIV;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.PRE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.SPAN;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.TBODY;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.TD;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.TH;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.THEAD;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.TR;

/** Renders request, response, execution and validation evidence for fallback HTML. */
public final class HtmlReportEvidenceView {
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;

    public HtmlReportEvidenceView(ReportingSettings settings, SensitiveDataRedactor redactor) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.redactor = Objects.requireNonNull(redactor, "redactor");
    }

    public void renderRequestDetails(StringBuilder html, ReportExecutionModel.Request request) {
        var details = EvidenceTableFormatter.requestFields(request.details());
        var json = request.json().stream().filter(item -> isRequestJson(item.label())).toList();
        if (details.isEmpty() && json.isEmpty()) return;
        html.append(ReportEvidenceMarkup.disclosureStart(
                        REQUEST_EVIDENCE.value(), AtfReportLabels.requestDetails(details.size())))
                .append(openTag(DIV, classAttribute(REQUEST_DETAILS_CONTENT)));
        renderTable(html, AtfReportHtmlVocabulary.classList(HELM_EVIDENCE_TABLE, HELM_REQUEST_TABLE), details);
        renderJsonBodies(html, json, REQUEST_JSON_BODY);
        html.append(closeTag(DIV)).append(ReportEvidenceMarkup.disclosureEnd());
    }

    public void renderResponseDetails(StringBuilder html, ReportExecutionModel.Request request) {
        var details = EvidenceTableFormatter.technicalTraceFields(request.details());
        var json = request.json().stream().filter(item -> !isRequestJson(item.label())).toList();
        if (details.isEmpty() && json.isEmpty()) return;
        html.append(ReportEvidenceMarkup.disclosureStart(
                        RESPONSE_EVIDENCE.value(), AtfReportLabels.responseDetails(details.size())))
                .append(openTag(DIV, classAttribute(RESPONSE_DETAILS_CONTENT)));
        renderTable(html, AtfReportHtmlVocabulary.classList(HELM_EVIDENCE_TABLE, HELM_TRACE_TABLE), details);
        renderJsonBodies(html, json, RESPONSE_JSON_BODY);
        html.append(closeTag(DIV)).append(ReportEvidenceMarkup.disclosureEnd());
    }

    private void renderTable(StringBuilder html, String cssClass, List<ReportField> details) {
        if (details.isEmpty()) return;
        html.append(AtfReportMarkup.tableHtml(cssClass, EvidenceTableFormatter.toExtentRowsModel(
                details.stream()
                        .map(detail -> new ReportField(detail.name(),
                                redactor.redactField(detail.name(), detail.value())))
                        .toList())));
    }

    private void renderJsonBodies(StringBuilder html, List<ReportJsonBody> json,
                                  AtfReportHtmlVocabulary.CssClass cssClass) {
        for (ReportJsonBody item : json) {
            html.append(openTag(DIV, classAttribute(cssClass)))
                    .append(openTag(DIV, classAttribute(HELM_SCROLL_BLOCK_TITLE)))
                    .append(escape(ReportEvidenceJsonFormatter.label(item)))
                    .append(closeTag(DIV)).append(openTag(PRE, classAttribute(JSON_CODE)))
                    .append(escape(ReportEvidenceJsonFormatter.displayBody(item, redactor, settings)))
                    .append(closeTag(PRE)).append(closeTag(DIV));
        }
    }

    private boolean isRequestJson(String label) {
        return label != null && label.trim().toUpperCase(Locale.ROOT).startsWith(AtfReportLabels.REQUEST_PREFIX);
    }

    public void renderExecution(StringBuilder html, ReportExecutionEvidence execution) {
        if (execution == null) return;
        html.append(ReportEvidenceMarkup.disclosureStart(
                        EXECUTION_EVIDENCE.value(), AtfReportLabels.executionEvidence(execution.attempts())))
                .append(openTag(TABLE, classAttribute(HELM_EVIDENCE_TABLE, EXECUTION_TABLE)))
                .append(openTag(THEAD)).append(openTag(TR))
                .append(openTag(TH)).append(AtfReportLabels.FIELD).append(closeTag(TH))
                .append(openTag(TH)).append(AtfReportLabels.VALUE).append(closeTag(TH))
                .append(closeTag(TR)).append(closeTag(THEAD)).append(openTag(TBODY));
        row(html, AtfReportLabels.RESPONSE_STATUS, String.valueOf(execution.response().status()));
        row(html, AtfReportLabels.RESPONSE_URL, redactor.redactText(execution.response().url()));
        row(html, AtfReportLabels.RESPONSE_DURATION_MS,
                AtfDurationFormatter.format(execution.response().duration()));
        row(html, AtfReportLabels.ATTEMPTS, String.valueOf(execution.attempts()));
        row(html, AtfReportLabels.RETRY_EVENTS, String.valueOf(execution.retryEvents().size()));
        html.append(closeTag(TBODY)).append(closeTag(TABLE));
        if (!execution.attemptHistory().isEmpty()) {
            html.append(openTag(TABLE, classAttribute(HELM_EVIDENCE_TABLE, RETRY_HISTORY_TABLE)))
                    .append(openTag(THEAD)).append(openTag(TR))
                    .append(openTag(TH)).append(AtfReportLabels.ATTEMPT).append(closeTag(TH))
                    .append(openTag(TH)).append(AtfReportLabels.STATUS).append(closeTag(TH))
                    .append(openTag(TH)).append(AtfReportLabels.DURATION).append(closeTag(TH))
                    .append(openTag(TH)).append(AtfReportLabels.OUTCOME).append(closeTag(TH))
                    .append(openTag(TH)).append(AtfReportLabels.REASON).append(closeTag(TH))
                    .append(closeTag(TR)).append(closeTag(THEAD)).append(openTag(TBODY));
            for (ReportAttemptRecord attempt : execution.attemptHistory()) {
                html.append(openTag(TR)).append(openTag(TD)).append(attempt.number()).append(closeTag(TD))
                        .append(openTag(TD)).append(attempt.statusCode()).append(closeTag(TD))
                        .append(openTag(TD)).append(AtfDurationFormatter.format(attempt.duration())).append(closeTag(TD))
                        .append(openTag(TD)).append(escape(attempt.outcome())).append(closeTag(TD))
                        .append(openTag(TD)).append(escape(redactor.redactText(attempt.reason())))
                        .append(closeTag(TD)).append(closeTag(TR));
            }
            html.append(closeTag(TBODY)).append(closeTag(TABLE));
        }
        html.append(ReportEvidenceMarkup.disclosureEnd());
    }

    /** Test/composition seam for legacy execution snapshots; typed production callers stay unchanged. */
    public void renderExecution(StringBuilder html, Object execution) {
        renderExecution(html, ReportModelAdapters.execution(execution));
    }

    private static void row(StringBuilder html, String field, String value) {
        html.append(openTag(TR)).append(openTag(TH)).append(escape(field)).append(closeTag(TH))
                .append(openTag(TD)).append(escape(value == null ? "" : value)).append(closeTag(TD))
                .append(closeTag(TR));
    }

    public void renderValidation(StringBuilder html, ReportExecutionModel.Validation validation) {
        html.append(openTag(DIV, classAttribute(VALIDATION), attribute(NODE_ROLE, "validation")))
                .append(openTag(DIV, classAttribute(NODE_TITLE)))
                .append(openTag(SPAN)).append(escape(validation.title())).append(closeTag(SPAN))
                .append(openTag(SPAN, classAttribute(AtfReportHtmlVocabulary.statusClasses(validation.status()))))
                .append(validation.status()).append(closeTag(SPAN)).append(closeTag(DIV))
                .append(openTag(TABLE, classAttribute(ASSERTIONS)))
                .append(openTag(THEAD)).append(openTag(TR))
                .append(openTag(TH)).append(AtfReportLabels.ASSERTION).append(closeTag(TH))
                .append(openTag(TH)).append(AtfReportLabels.EXPECTED).append(closeTag(TH))
                .append(openTag(TH)).append(AtfReportLabels.ACTUAL).append(closeTag(TH))
                .append(openTag(TH)).append(AtfReportLabels.RESULT).append(closeTag(TH))
                .append(closeTag(TR)).append(closeTag(THEAD)).append(openTag(TBODY));
        for (AtfAssertion row : validation.assertions()) {
            html.append(openTag(TR)).append(openTag(TD)).append(escape(redactor.redactText(row.name())))
                    .append(closeTag(TD)).append(openTag(TD))
                    .append(escape(redactor.redactField(row.name(), row.expected()))).append(closeTag(TD))
                    .append(openTag(TD)).append(escape(redactor.redactField(row.name(), row.actual())))
                    .append(closeTag(TD)).append(openTag(TD))
                    .append(openTag(SPAN, classAttribute(AtfReportHtmlVocabulary.statusClasses(row.resultStatus()))))
                    .append(row.resultStatus()).append(closeTag(SPAN)).append(closeTag(TD)).append(closeTag(TR));
        }
        html.append(closeTag(TBODY)).append(closeTag(TABLE)).append(closeTag(DIV));
    }
}
