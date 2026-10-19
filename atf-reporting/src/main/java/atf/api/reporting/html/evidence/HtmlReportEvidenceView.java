package atf.api.reporting.html.evidence;

import atf.api.core.config.ReportingSettings;
import atf.api.core.http.response.ApiAttemptRecord;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.duration.AtfDurationFormatter;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.reporting.evidence.EvidenceTableFormatter;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.ExtentReportNodeSupport;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;
import atf.api.reporting.model.ReportField;
import atf.api.reporting.model.ReportJsonBody;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.attribute;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.classAttribute;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.closeTag;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.openTag;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.ARIA_EXPANDED;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.ARIA_HIDDEN;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.NODE_ROLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.AttributeValue.FALSE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.AttributeValue.TRUE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.ASSERTIONS;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.DISCLOSURE_GLYPH;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.EXECUTION_EVIDENCE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.EXECUTION_TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.HELM_API_CLASSIFICATION;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.HELM_API_CLASSIFICATION_LABEL;
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
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.SE_TITLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.STEP_DETAIL;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.TECHNICAL;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.VALIDATION;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.DETAILS;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.DIV;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.PRE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.SPAN;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.SUMMARY;
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
        html.append(openTag(DETAILS, classAttribute(STEP_DETAIL, REQUEST_EVIDENCE)))
                .append(openTag(SUMMARY, attribute(ARIA_EXPANDED, FALSE)))
                .append(openTag(SPAN, classAttribute(DISCLOSURE_GLYPH), attribute(ARIA_HIDDEN, TRUE)))
                .append(closeTag(SPAN))
                .append(openTag(SPAN, classAttribute(SE_TITLE)))
                .append(escape(AtfReportLabels.requestDetails(details.size())))
                .append(closeTag(SPAN)).append(closeTag(SUMMARY))
                .append(openTag(DIV, classAttribute(REQUEST_DETAILS_CONTENT)));
        renderTable(html, classAttribute(HELM_EVIDENCE_TABLE, HELM_REQUEST_TABLE), details);
        renderJsonBodies(html, json, REQUEST_JSON_BODY);
        html.append(closeTag(DIV)).append(closeTag(DETAILS));
    }

    public void renderResponseDetails(StringBuilder html, ReportExecutionModel.Request request) {
        var details = EvidenceTableFormatter.technicalTraceFields(request.details());
        var json = request.json().stream().filter(item -> !isRequestJson(item.label())).toList();
        if (details.isEmpty() && json.isEmpty()) return;
        html.append(openTag(DETAILS, classAttribute(STEP_DETAIL, RESPONSE_EVIDENCE)))
                .append(openTag(SUMMARY, attribute(ARIA_EXPANDED, FALSE)))
                .append(openTag(SPAN, classAttribute(DISCLOSURE_GLYPH), attribute(ARIA_HIDDEN, TRUE)))
                .append(closeTag(SPAN))
                .append(openTag(SPAN, classAttribute(SE_TITLE)))
                .append(escape(AtfReportLabels.responseDetails(details.size())))
                .append(closeTag(SPAN)).append(closeTag(SUMMARY))
                .append(openTag(DIV, classAttribute(RESPONSE_DETAILS_CONTENT)))
                .append(openTag(DIV, classAttribute(HELM_API_CLASSIFICATION, TECHNICAL)))
                .append(openTag(SPAN, classAttribute(HELM_API_CLASSIFICATION_LABEL)))
                .append(AtfReportLabels.TECHNICAL_TRACE).append(closeTag(SPAN)).append(closeTag(DIV));
        renderTable(html, classAttribute(HELM_EVIDENCE_TABLE, HELM_TRACE_TABLE), details);
        renderJsonBodies(html, json, RESPONSE_JSON_BODY);
        html.append(closeTag(DIV)).append(closeTag(DETAILS));
    }

    private void renderTable(StringBuilder html, String cssClass, List<ReportField> details) {
        if (details.isEmpty()) return;
        html.append(openTag(TABLE, cssClass))
                .append(openTag(THEAD)).append(openTag(TR))
                .append(openTag(TH)).append(AtfReportLabels.FIELD).append(closeTag(TH))
                .append(openTag(TH)).append(AtfReportLabels.VALUE).append(closeTag(TH))
                .append(closeTag(TR)).append(closeTag(THEAD)).append(openTag(TBODY));
        for (ReportField detail : details) {
            html.append(openTag(TR)).append(openTag(TD)).append(escape(detail.name())).append(closeTag(TD))
                    .append(openTag(TD)).append(escape(redactor.redactField(detail.name(), detail.value())))
                    .append(closeTag(TD)).append(closeTag(TR));
        }
        html.append(closeTag(TBODY)).append(closeTag(TABLE));
    }

    private void renderJsonBodies(StringBuilder html, List<ReportJsonBody> json,
                                  AtfReportHtmlVocabulary.CssClass cssClass) {
        for (ReportJsonBody item : json) {
            html.append(openTag(DIV, classAttribute(cssClass)))
                    .append(openTag(DIV, classAttribute(HELM_SCROLL_BLOCK_TITLE)))
                    .append(escape(item.label().isBlank() ? AtfReportLabels.JSON_BODY : item.label()))
                    .append(closeTag(DIV)).append(openTag(PRE, classAttribute(JSON_CODE)))
                    .append(escape(ExtentReportNodeSupport.displayBody(
                            redactor.redactJson(item.body()), settings)))
                    .append(closeTag(PRE)).append(closeTag(DIV));
        }
    }

    private boolean isRequestJson(String label) {
        return label != null && label.trim().toUpperCase(Locale.ROOT).startsWith(AtfReportLabels.REQUEST_PREFIX);
    }

    public void renderExecution(StringBuilder html, ApiExecutionResult execution) {
        if (execution == null) return;
        html.append(openTag(DETAILS, classAttribute(STEP_DETAIL, EXECUTION_EVIDENCE)))
                .append(openTag(SUMMARY, attribute(ARIA_EXPANDED, FALSE)))
                .append(openTag(SPAN, classAttribute(DISCLOSURE_GLYPH), attribute(ARIA_HIDDEN, TRUE)))
                .append(closeTag(SPAN)).append(openTag(SPAN, classAttribute(SE_TITLE)))
                .append(escape(AtfReportLabels.executionEvidence(execution.attempts())))
                .append(closeTag(SPAN)).append(closeTag(SUMMARY))
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
            for (ApiAttemptRecord attempt : execution.attemptHistory()) {
                html.append(openTag(TR)).append(openTag(TD)).append(attempt.number()).append(closeTag(TD))
                        .append(openTag(TD)).append(attempt.statusCode()).append(closeTag(TD))
                        .append(openTag(TD)).append(AtfDurationFormatter.format(attempt.duration())).append(closeTag(TD))
                        .append(openTag(TD)).append(escape(attempt.outcome().name())).append(closeTag(TD))
                        .append(openTag(TD)).append(escape(redactor.redactText(attempt.reason())))
                        .append(closeTag(TD)).append(closeTag(TR));
            }
            html.append(closeTag(TBODY)).append(closeTag(TABLE));
        }
        html.append(closeTag(DETAILS));
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
