package atf.api.reporting.model;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportRetryEvent;
import atf.api.reporting.api.ReportTransportProvenance;
import java.time.Duration;
import java.util.List;

/** Standalone transport evidence; no HTTP client, executor or core model leaks into reporting. */
public record ReportExecutionEvidence(ReportResponse response, int attempts, Duration totalDuration,
                                      List<ReportRetryEvent> retryEvents, List<ReportAttemptRecord> attemptHistory,
                                      ReportRequest request, ReportTransportProvenance provenance) {
    public ReportExecutionEvidence {
        retryEvents=retryEvents==null?List.of():List.copyOf(retryEvents);
        attemptHistory=attemptHistory==null?List.of():List.copyOf(attemptHistory);
        totalDuration=totalDuration==null?Duration.ZERO:totalDuration;
        provenance=provenance==null?ReportTransportProvenance.UNKNOWN:provenance;
    }
}
