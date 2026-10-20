package atf.api.reporting.evidence;
import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.duration.AtfDurationFormatter;
import atf.api.reporting.markup.AtfReportEvidenceKeys;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.AtfReportMarkup;
import atf.api.reporting.markup.ExtentReportAssets;
import atf.api.reporting.markup.ExtentReportNodeSupport;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.model.ReportField;
import atf.api.reporting.model.ReportJsonBody;
import atf.api.reporting.model.ReportJsonBodyFormatter;
import atf.api.reporting.model.ReportCheck;
import atf.api.reporting.model.ReportScreenshot;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.security.SensitiveDataRedactor;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.util.List;

/** Renders request, response and retry evidence for transport-oriented steps. */
public final class ExtentTransportEvidenceRenderer {
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;

    ExtentTransportEvidenceRenderer(ReportingSettings settings, SensitiveDataRedactor redactor) {
        this.settings = settings;
        this.redactor = redactor;
    }

    void renderDetails(ExtentTest node, List<ReportField> details) {
        List<ReportField> traceDetails = EvidenceTableFormatter.technicalTraceFields(details);
        if (traceDetails.isEmpty()) return;
        node.info(AtfReportLabels.responseDetails(traceDetails.size()));
        node.info(AtfReportMarkup.table(AtfReportHtmlVocabulary.classList(
                        AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE,
                        AtfReportHtmlVocabulary.CssClass.HELM_TRACE_TABLE),
                EvidenceTableFormatter.toExtentRowsModel(traceDetails)));
    }

    void renderRequestDetails(ExtentTest node, List<ReportField> details,
                              List<ReportJsonBody> json) {
        List<ReportField> requestDetails = EvidenceTableFormatter.requestFields(details);
        List<ReportJsonBody> requestJson = json.stream().filter(item -> isRequestJson(item.label())).toList();
        if (requestDetails.isEmpty() && requestJson.isEmpty()) return;
        node.info(disclosureMarkup(
                AtfReportHtmlVocabulary.CssClass.REQUEST_EVIDENCE.value(),
                AtfReportLabels.requestDetails(requestDetails.size()),
                requestEvidenceMarkup(requestDetails, requestJson)));
    }

    void renderResponseDetails(ExtentTest node, List<ReportField> details,
                               List<ReportJsonBody> json) {
        List<ReportField> responseDetails = EvidenceTableFormatter.technicalTraceFields(details);
        List<ReportJsonBody> responseJson = json.stream().filter(item -> !isRequestJson(item.label())).toList();
        if (responseDetails.isEmpty() && responseJson.isEmpty()) return;
        node.info(disclosureMarkup(
                AtfReportHtmlVocabulary.CssClass.RESPONSE_EVIDENCE.value(),
                AtfReportLabels.responseDetails(responseDetails.size()),
                responseEvidenceMarkup(responseDetails, responseJson)));
    }

    void renderSetupEvidence(ExtentTest node, List<ReportField> details) {
        List<ReportCheck> checks = details.stream()
                .filter(detail -> detail != null
                        && detail.name().toLowerCase(java.util.Locale.ROOT)
                                .startsWith(AtfReportEvidenceKeys.SETUP_CHECK_PREFIX))
                .map(detail -> setupCheck(detail.value()))
                .toList();
        String[][] rows = new String[checks.size() + 1][4];
        rows[0] = new String[]{AtfReportLabels.CHECK, AtfReportLabels.EXPECTED,
                AtfReportLabels.ACTUAL, AtfReportLabels.RESULT};
        for (int index = 0; index < checks.size(); index++) {
            ReportCheck check = checks.get(index);
            rows[index + 1] = new String[]{check.check(), check.expected(), check.actual(),
                    resultMarker(check.status())};
        }
        boolean failed = checks.stream().anyMatch(check -> check.status() == AtfReportStatus.FAIL);
        String label = AtfReportLabels.setupEvidence(checks.size());
        String markup = AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DETAILS,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.STEP_DETAIL,
                        AtfReportHtmlVocabulary.CssClass.SETUP_EVIDENCE,
                        AtfReportHtmlVocabulary.CssClass.ATF_SETUP_EVIDENCE),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.EVIDENCE_TYPE,
                        AtfReportEvidenceKeys.REPORT_SETUP),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.EVIDENCE_STATUS,
                        AtfReportHtmlVocabulary.statusValue(
                                failed ? AtfReportStatus.FAIL : AtfReportStatus.PASS)))
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SUMMARY,
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.ARIA_EXPANDED,
                        AtfReportHtmlVocabulary.AttributeValue.FALSE))
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SPAN,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.DISCLOSURE_GLYPH),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.ARIA_HIDDEN,
                        AtfReportHtmlVocabulary.AttributeValue.TRUE))
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SPAN)
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SPAN,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.SE_TITLE),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.TITLE, label),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.ARIA_LABEL, label))
                + label
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SPAN)
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SUMMARY)
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.BODY))
                + AtfReportMarkup.tableHtml(
                AtfReportHtmlVocabulary.CssClass.ATF_SETUP_EVIDENCE_TABLE.value(), rows)
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV)
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DETAILS);
        if (failed) {
            node.fail(markup);
        } else {
            node.info(markup);
        }
    }

    private ReportCheck setupCheck(String value) {
        String safe = value == null ? "" : value;
        String[] parts = safe.split(" \\| (?:expected=|actual=|result=)", -1);
        if (parts.length == 4) {
            return new ReportCheck(parts[0], parts[1], parts[2], parseStatus(parts[3]));
        }
        return new ReportCheck(safe.isBlank() ? AtfReportLabels.SETUP_EVIDENCE : safe,
                "", "", AtfReportStatus.INFO);
    }

    private AtfReportStatus parseStatus(String value) {
        return AtfReportStatus.fromDisplayName(value);
    }

    private String resultMarker(AtfReportStatus status) {
        return switch (status == null ? AtfReportStatus.INFO : status) {
            case PASS -> "✅ PASS";
            case FAIL -> "❌ FAIL";
            case WARNING -> "⚠️ WARNING";
            case SKIP -> "⏭️ SKIP";
            default -> "ℹ️ INFO";
        };
    }

    private String requestEvidenceMarkup(List<ReportField> details, List<ReportJsonBody> json) {
        return evidenceMarkup(AtfReportHtmlVocabulary.CssClass.HELM_REQUEST_DETAILS.value(), "",
                AtfReportHtmlVocabulary.CssClass.REQUEST_JSON_BODY.value(), details, json);
    }

    private String responseEvidenceMarkup(List<ReportField> details, List<ReportJsonBody> json) {
        return evidenceMarkup(AtfReportHtmlVocabulary.CssClass.HELM_RESPONSE_DETAILS.value(),
                AtfReportHtmlVocabulary.CssClass.TECHNICAL.value(),
                AtfReportHtmlVocabulary.CssClass.RESPONSE_JSON_BODY.value(), details, json);
    }

    private String evidenceMarkup(String rootClass, String classification, String jsonClass,
                                   List<ReportField> details, List<ReportJsonBody> json) {
        var html = new StringBuilder(AtfReportHtmlVocabulary.openTag(
                AtfReportHtmlVocabulary.Tag.DIV,
                AtfReportHtmlVocabulary.classAttribute(rootClass + "-content")));
        if (!classification.isBlank() && details.isEmpty()) {
            html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.HELM_API_CLASSIFICATION.value(),
                                    classification)))
                    .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.HELM_API_CLASSIFICATION_LABEL)))
                    .append(AtfReportLabels.TECHNICAL_TRACE)
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV));
        }
        if (!details.isEmpty()) {
            String css = classification.isBlank()
                    ? AtfReportHtmlVocabulary.classList(
                    AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE,
                    AtfReportHtmlVocabulary.CssClass.HELM_REQUEST_TABLE)
                    : AtfReportHtmlVocabulary.classList(
                    AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE,
                    AtfReportHtmlVocabulary.CssClass.HELM_TRACE_TABLE);
             html.append(AtfReportMarkup.tableHtml(css, EvidenceTableFormatter.toExtentRowsModel(details)));
        }
        for (ReportJsonBody item : json) {
            html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                            AtfReportHtmlVocabulary.classAttribute(jsonClass)))
                    .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.HELM_SCROLL_BLOCK_TITLE)))
                    .append(item.label().isBlank() ? AtfReportLabels.JSON_BODY : item.label())
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV))
                    .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.HELM_SCROLL_BLOCK_BODY)))
                    .append(MarkupHelper.createCodeBlock(ExtentReportNodeSupport.displayBody(
                             ReportJsonBodyFormatter.format(redactor.redactJson(item.body())), settings)).getMarkup())
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV));
        }
        return html.append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV)).toString();
    }

    private boolean isRequestJson(String label) {
        return label != null && label.trim().toUpperCase(java.util.Locale.ROOT)
                .startsWith(AtfReportLabels.REQUEST_JSON);
    }

    void renderExecution(ExtentTest node, ReportExecutionEvidence execution) {
        if (execution == null) return;
        var body = new StringBuilder(AtfReportHtmlVocabulary.openTag(
                AtfReportHtmlVocabulary.Tag.DIV,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.EXECUTION_EVIDENCE_BODY)))
                .append(AtfReportMarkup.table(AtfReportHtmlVocabulary.classList(
                        AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE,
                        AtfReportHtmlVocabulary.CssClass.EXECUTION_TABLE), new String[][]{
                {AtfReportLabels.FIELD, AtfReportLabels.VALUE},
                {AtfReportLabels.RESPONSE_STATUS, String.valueOf(execution.response().status())},
                {AtfReportLabels.RESPONSE_URL, redactor.redactText(execution.response().url())},
                {AtfReportLabels.RESPONSE_DURATION_MS, AtfDurationFormatter.format(execution.response().duration())},
                {AtfReportLabels.ATTEMPTS, String.valueOf(execution.attempts())},
                {AtfReportLabels.RETRY_EVENTS, String.valueOf(execution.retryEvents().size())}
        }).getMarkup());
        if (!execution.attemptHistory().isEmpty()) {
            body.append(AtfReportMarkup.table(AtfReportHtmlVocabulary.classList(
                            AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE,
                            AtfReportHtmlVocabulary.CssClass.RETRY_HISTORY_TABLE),
                    attemptRows(execution.attemptHistory())).getMarkup());
        }
        node.info(disclosureMarkup(
                AtfReportHtmlVocabulary.CssClass.EXECUTION_EVIDENCE.value(),
                AtfReportLabels.executionEvidence(execution.attempts()),
                body.append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV)).toString()));
    }

    private static String disclosureMarkup(String kind, String title, String body) {
        String safeTitle = escapeHtml(title);
        return AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DETAILS,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.STEP_DETAIL.value(), kind))
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SUMMARY,
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.ARIA_EXPANDED,
                        AtfReportHtmlVocabulary.AttributeValue.FALSE))
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SPAN,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.DISCLOSURE_GLYPH),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.ARIA_HIDDEN,
                        AtfReportHtmlVocabulary.AttributeValue.TRUE))
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SPAN)
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SPAN,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.SE_TITLE),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.TITLE, safeTitle),
                AtfReportHtmlVocabulary.attribute(
                        AtfReportHtmlVocabulary.Attribute.ARIA_LABEL, safeTitle))
                + safeTitle
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SPAN)
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SUMMARY)
                + AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.BODY))
                + body
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV)
                + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DETAILS);
    }

    void renderScreenshots(ExtentTest node, List<ReportScreenshot> screenshots) {
        for (ReportScreenshot screenshot : ExtentReportAssets.distinctScreenshotModels(screenshots)) {
            String title = screenshot.title();
            node.info(() -> AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                    AtfReportHtmlVocabulary.classAttribute(
                            AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SCREENSHOT))
                    + ExtentReportAssets.base64ScreenshotLink(
                             AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SCREENSHOT_LINK.value(), title, screenshot.base64Png())
                    + AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV));
        }
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String[][] attemptRows(List<ReportAttemptRecord> attempts) {
        String[][] rows = new String[attempts.size() + 1][5];
        rows[0] = new String[]{AtfReportLabels.ATTEMPT, AtfReportLabels.STATUS,
                AtfReportLabels.DURATION, AtfReportLabels.OUTCOME, AtfReportLabels.REASON};
        for (int index = 0; index < attempts.size(); index++) {
            ReportAttemptRecord attempt = attempts.get(index);
            rows[index + 1] = new String[]{
                    String.valueOf(attempt.number()),
                    String.valueOf(attempt.statusCode()),
                    AtfDurationFormatter.format(attempt.duration()),
                    attempt.outcome(),
                    redactor.redactText(attempt.reason())};
        }
        return rows;
    }
}
