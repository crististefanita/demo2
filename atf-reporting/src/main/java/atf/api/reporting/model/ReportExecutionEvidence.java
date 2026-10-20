package atf.api.reporting.model;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportRetryEvent;
import atf.api.reporting.api.ReportTraceMetadata;
import atf.api.reporting.api.ReportTransportProvenance;
import java.time.Duration;
import java.util.List;

/** Standalone transport evidence; no HTTP client, executor or core model leaks into reporting. */
public record ReportExecutionEvidence(ReportResponse response, int attempts, Duration totalDuration,
                                      List<ReportRetryEvent> retryEvents, List<ReportAttemptRecord> attemptHistory,
                                      ReportRequest request, ReportTransportProvenance provenance,
                                      ReportTraceMetadata trace) {
    public ReportExecutionEvidence {
        retryEvents=retryEvents==null?List.of():List.copyOf(retryEvents);
        attemptHistory=attemptHistory==null?List.of():List.copyOf(attemptHistory);
        totalDuration=totalDuration==null?Duration.ZERO:totalDuration;
        provenance=provenance==null?ReportTransportProvenance.UNKNOWN:provenance;
        trace=trace==null?ReportTraceMetadata.from(request):trace;
    }

    /** Compatibility constructor for all existing consumers. */
    public ReportExecutionEvidence(ReportResponse response, int attempts, Duration totalDuration,
                                   List<ReportRetryEvent> retryEvents, List<ReportAttemptRecord> attemptHistory,
                                   ReportRequest request, ReportTransportProvenance provenance) {
        this(response, attempts, totalDuration, retryEvents, attemptHistory, request, provenance,
                ReportTraceMetadata.from(request));
    }
}
