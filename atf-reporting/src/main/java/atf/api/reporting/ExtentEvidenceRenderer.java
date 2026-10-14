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
        if (details.isEmpty()) return;
        node.info(AtfReportLabels.responseDetails(details.size()));
        node.info(AtfReportMarkup.table("helm-evidence-table helm-trace-table",
                EvidenceTableFormatter.toExtentRows(details)));
    }

    void renderJson(ExtentTest node, List<String[]> json) {
        for (String[] item : json) {
            node.info(item[0] == null || item[0].isBlank() ? "JSON BODY" : item[0]);
            node.info(MarkupHelper.createCodeBlock(ExtentReportNodeSupport.display(item[1], settings)));
        }
    }

    void renderExecution(ExtentTest node, ApiExecutionResult execution) {
        if (execution == null) return;
        node.info(AtfReportLabels.executionEvidence(execution.attempts()));
        node.info(AtfReportMarkup.table("helm-evidence-table execution-table", new String[][]{
                {"FIELD", "VALUE"},
                {"response.status", String.valueOf(execution.response().status())},
                {"response.url", redactor.redactText(execution.response().url())},
                {"response.duration.ms", String.valueOf(execution.response().duration().toMillis())},
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
                    String.valueOf(attempt.duration().toMillis()),
                    attempt.outcome().name(),
                    redactor.redactText(attempt.reason())};
        }
        return rows;
    }
}
