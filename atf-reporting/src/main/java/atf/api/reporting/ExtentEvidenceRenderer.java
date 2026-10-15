package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.http.ApiAttemptRecord;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.security.SensitiveDataRedactor;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.util.Collection;
import java.util.List;

/** Renders request-bound evidence without owning Extent tree lifecycle. */
final class ExtentEvidenceRenderer {
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;

    ExtentEvidenceRenderer(ReportingSettings settings, SensitiveDataRedactor redactor) {
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
                    .append(MarkupHelper.createCodeBlock(ExtentReportNodeSupport.display(item[1], settings)).getMarkup())
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
                {"response.duration.ms", String.valueOf(AtfDurationFormatter.elapsedMillis(execution.response().duration()))},
                {"attempts", String.valueOf(execution.attempts())},
                {"retry.events", String.valueOf(execution.retryEvents().size())}
        }));
        if (!execution.attemptHistory().isEmpty()) {
            node.info(AtfReportMarkup.table("helm-evidence-table retry-history-table",
                    attemptRows(execution.attemptHistory())));
        }
    }

    void renderAssertions(ExtentTest node, Collection<AtfAssertion> assertions) {
        var table = new String[assertions.size() + 1][4];
        table[0] = new String[]{AtfReportLabels.ASSERTION, AtfReportLabels.EXPECTED,
                AtfReportLabels.ACTUAL, AtfReportLabels.RESULT};
        int index = 0;
        for (var row : assertions) {
            table[++index] = new String[]{
                    redactor.redactText(row.name()),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.expected()), settings),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.actual()), settings),
                    ExtentReportNodeSupport.resultMarker(row.resultStatus())};
        }
        node.info(AtfReportMarkup.table("helm-evidence-table helm-validation-table", table));
    }

    private String[][] attemptRows(List<ApiAttemptRecord> attempts) {
        String[][] rows = new String[attempts.size() + 1][5];
        rows[0] = new String[]{"ATTEMPT", "STATUS", "DURATION (MS)", "OUTCOME", "REASON"};
        for (int index = 0; index < attempts.size(); index++) {
            ApiAttemptRecord attempt = attempts.get(index);
            rows[index + 1] = new String[]{
                    String.valueOf(attempt.number()),
                    String.valueOf(attempt.statusCode()),
                    String.valueOf(AtfDurationFormatter.elapsedMillis(attempt.duration())),
                    attempt.outcome().name(),
                    redactor.redactText(attempt.reason())};
        }
        return rows;
    }
}
