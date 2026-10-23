package atf.api.reporting.api.evidence;

import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.evidence.ReportEvidenceTable;
import atf.api.reporting.evidence.EvidenceTableFormatter;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.security.SensitiveDataRedactor;
import java.util.List;

/** Shared transport timing vocabulary for Extent and fallback. */
public final class ReportExecutionSummary {
    private ReportExecutionSummary() { }
    public static ReportEvidenceTable table(ReportExecutionEvidence execution, SensitiveDataRedactor redactor) {
        return ReportEvidenceTable.of(List.of(AtfReportLabels.FIELD, AtfReportLabels.VALUE), List.of(
                row(AtfReportLabels.RESPONSE_STATUS, execution.response().statusLabel()),
                row(AtfReportLabels.RESPONSE_URL, redactor.redactText(execution.response().url())),
                row(AtfReportLabels.RESPONSE_DURATION_MS, ReportTransportTiming.display(execution.response().duration())),
                row(AtfReportLabels.TOTAL_DURATION_MS, ReportTransportTiming.display(execution.totalDuration())),
                row(AtfReportLabels.ATTEMPTS, String.valueOf(execution.attempts())),
                row(AtfReportLabels.RETRY_EVENTS, String.valueOf(execution.retryEvents().size())),
                List.of("TIMING SCOPE", "Response and total: transport measurements; node badge: reporting lifecycle")
        ));
    }

    private static List<String> row(String key, String value) {
        return List.of(EvidenceTableFormatter.displayName(key), value);
    }
}
