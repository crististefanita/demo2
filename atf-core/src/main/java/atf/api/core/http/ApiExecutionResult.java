package atf.api.core.http;

import java.time.Duration;
import java.util.List;

public record ApiExecutionResult(ApiResponse response, int attempts, Duration totalDuration,
                                 List<RetryEvent> retryEvents, List<ApiAttemptRecord> attemptHistory,
                                 ApiRequest request) {
    public ApiExecutionResult {
        retryEvents = retryEvents == null ? List.of() : List.copyOf(retryEvents);
        attemptHistory = attemptHistory == null ? List.of() : List.copyOf(attemptHistory);
    }

    /** Compatibility constructor for callers that only need retry metadata. */
    public ApiExecutionResult(ApiResponse response, int attempts, Duration totalDuration, List<RetryEvent> retryEvents) {
        this(response, attempts, totalDuration, retryEvents, List.of(), null);
    }

    /** Compatibility constructor for callers that already provide attempt history. */
    public ApiExecutionResult(ApiResponse response, int attempts, Duration totalDuration,
                              List<RetryEvent> retryEvents, List<ApiAttemptRecord> attemptHistory) {
        this(response, attempts, totalDuration, retryEvents, attemptHistory, null);
    }
}
