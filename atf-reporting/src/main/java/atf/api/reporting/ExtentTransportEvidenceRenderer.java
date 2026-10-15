package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.http.ApiAttemptRecord;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfDurationFormatter;
import atf.api.core.security.SensitiveDataRedactor;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.util.List;

/** Renders request, response and retry evidence for transport-oriented steps. */
final class ExtentTransportEvidenceRenderer {
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;

    ExtentTransportEvidenceRenderer(ReportingSettings settings, SensitiveDataRedactor redactor) {
        this.settings = settings;
        this.redactor = redactor;
    }

    void renderDetails(ExtentTest node, List<String[]> details) {
        List<String[]> traceDetails = EvidenceTableFormatter.technicalTraceDetails(details);
        if (traceDetails.isEmpty()) return;
        node.info(AtfReportLabels.responseDetails(traceDetails.size()));
        node.info(AtfReportMarkup.table("helm-evidence-table helm-trace-table",
                EvidenceTableFormatter.toExtentRows(traceDetails)));
    }

    void renderRequestDetails(ExtentTest node, List<String[]> details, List<String[]> json) {
        List<String[]> requestDetails = EvidenceTableFormatter.requestDetails(details);
        List<String[]> requestJson = json.stream().filter(item -> isRequestJson(item[0])).toList();
        if (requestDetails.isEmpty() && requestJson.isEmpty()) return;
        node.info(AtfReportLabels.requestDetails(requestDetails.size()));
        node.info(requestEvidenceMarkup(requestDetails, requestJson));
    }

    void renderResponseDetails(ExtentTest node, List<String[]> details, List<String[]> json) {
        List<String[]> responseDetails = EvidenceTableFormatter.technicalTraceDetails(details);
        List<String[]> responseJson = json.stream().filter(item -> !isRequestJson(item[0])).toList();
        if (responseDetails.isEmpty() && responseJson.isEmpty()) return;
        node.info(AtfReportLabels.responseDetails(responseDetails.size()));
        node.info(responseEvidenceMarkup(responseDetails, responseJson));
    }

    private String requestEvidenceMarkup(List<String[]> details, List<String[]> json) {
        return evidenceMarkup("helm-request-details", "", "request-json-body", details, json);
    }

    private String responseEvidenceMarkup(List<String[]> details, List<String[]> json) {
        return evidenceMarkup("helm-response-details", "technical", "response-json-body", details, json);
    }

    private String evidenceMarkup(String rootClass, String classification, String jsonClass,
                                  List<String[]> details, List<String[]> json) {
        var html = new StringBuilder("<div class=\"").append(rootClass).append("-content\">");
        if (!classification.isBlank() && details.isEmpty()) {
            html.append("<div class=\"helm-api-classification ").append(classification)
                    .append("\"><span class=\"helm-api-classification-label\">")
                    .append(AtfReportLabels.TECHNICAL_TRACE).append("</span></div>");
        }
        if (!details.isEmpty()) {
            String css = classification.isBlank()
                    ? "helm-evidence-table helm-request-table"
                    : "helm-evidence-table helm-trace-table";
            html.append(AtfReportMarkup.tableHtml(css, EvidenceTableFormatter.toExtentRows(details)));
        }
        for (String[] item : json) {
            html.append("<div class=\"").append(jsonClass).append("\"><div class=\"helm-scroll-block-title\">")
                    .append(item[0] == null || item[0].isBlank() ? "JSON BODY" : item[0])
                    .append("</div><div class=\"helm-scroll-block-body\">")
                    .append(MarkupHelper.createCodeBlock(ExtentReportNodeSupport.displayBody(
                            redactor.redactJson(item[1]), settings)).getMarkup())
                    .append("</div></div>");
        }
        return html.append("</div>").toString();
    }

    private boolean isRequestJson(String label) {
        return label != null && label.trim().toUpperCase(java.util.Locale.ROOT).startsWith("REQUEST");
    }

    void renderExecution(ExtentTest node, ApiExecutionResult execution) {
        if (execution == null) return;
        node.info(AtfReportLabels.executionEvidence(execution.attempts()));
        node.info(AtfReportMarkup.table("helm-evidence-table execution-table", new String[][]{
                {"FIELD", "VALUE"},
                {"response.status", String.valueOf(execution.response().status())},
                {"response.url", redactor.redactText(execution.response().url())},
                {"response.duration.ms", AtfDurationFormatter.format(execution.response().duration())},
                {"attempts", String.valueOf(execution.attempts())},
                {"retry.events", String.valueOf(execution.retryEvents().size())}
        }));
        if (!execution.attemptHistory().isEmpty()) {
            node.info(AtfReportMarkup.table("helm-evidence-table retry-history-table",
                    attemptRows(execution.attemptHistory())));
        }
    }

    void renderScreenshots(ExtentTest node, List<String[]> screenshots) {
        if (screenshots == null || screenshots.isEmpty()) return;
        for (String[] screenshot : screenshots) {
            if (screenshot == null || screenshot.length < 2 || screenshot[1].isBlank()) continue;
            String title = screenshot[0] == null || screenshot[0].isBlank()
                    ? "UI screenshot" : screenshot[0];
            String base64 = escapeHtml(screenshot[1]);
            node.info(() -> "<div class=\"atf-ui-action-screenshot\">"
                    + "<a class=\"atf-ui-action-screenshot-link\" data-atf-screenshot=\"base64\""
                    + " href=\"data:image/png;base64," + base64
                    + "\" target=\"_blank\" rel=\"noopener\" data-featherlight=\"image\" aria-label=\"Open "
                    + escapeHtml(title) + "\"><span class=\"badge badge-gradient-primary\">base64 img</span></a></div>");
        }
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String[][] attemptRows(List<ApiAttemptRecord> attempts) {
        String[][] rows = new String[attempts.size() + 1][5];
        rows[0] = new String[]{"ATTEMPT", "STATUS", "DURATION", "OUTCOME", "REASON"};
        for (int index = 0; index < attempts.size(); index++) {
            ApiAttemptRecord attempt = attempts.get(index);
            rows[index + 1] = new String[]{
                    String.valueOf(attempt.number()),
                    String.valueOf(attempt.statusCode()),
                    AtfDurationFormatter.format(attempt.duration()),
                    attempt.outcome().name(),
                    redactor.redactText(attempt.reason())};
        }
        return rows;
    }
}
