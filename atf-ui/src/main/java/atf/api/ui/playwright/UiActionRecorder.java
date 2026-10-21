package atf.api.ui.playwright;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.core.ui.AtfUiEvidenceSource;
import atf.api.ui.config.UiRetryPolicy;

import java.util.ArrayList;
import java.util.List;

/** Owns ordered UI evidence, sequence boundaries and operation timing. */
final class UiActionRecorder {
    private final List<AtfUiActionRecord> records = new ArrayList<>();
    private final UiActionTrace trace;
    private UiRetryPolicy retryPolicy = UiRetryPolicy.none();
    private int nextSequence = 1;

    UiActionRecorder() {
        this(UiActionTrace.noOp());
    }

    UiActionRecorder(UiActionTrace trace) {
        this.trace = trace == null ? UiActionTrace.noOp() : trace;
    }

    void setRetryPolicy(UiRetryPolicy policy) {
        retryPolicy = policy == null ? UiRetryPolicy.none() : policy;
    }

    <T> T run(String operation, String target, String expected, Action<T> action) {
        long started = System.nanoTime();
        publish(new UiActionTrace.UiActionEvent(UiActionTrace.Kind.START,
                operation, target, expected, "", null, 0));
        int attempt = 1;
        while (true) {
            try {
                T observed = action.run();
                String value = observed == null ? "operation completed" : observed.toString();
                long duration = elapsedMillis(started);
                add(operation, target, expected, value, duration, AtfReportStatus.PASS);
                publish(new UiActionTrace.UiActionEvent(UiActionTrace.Kind.COMPLETE,
                        operation, target, expected, value, AtfReportStatus.PASS, duration));
                return observed;
            } catch (RuntimeException | AssertionError failure) {
                if (retryPolicy.allows(operation, attempt)) {
                    int nextAttempt = attempt + 1;
                    String failureObserved = failure.getClass().getSimpleName() + ": "
                            + failure.getMessage();
                    String attemptExpected = "attempt " + attempt + " failed; retrying as attempt "
                            + nextAttempt + " of " + retryPolicy.maxAttempts();
                    add("Retry attempt", target, attemptExpected, failureObserved,
                            elapsedMillis(started), AtfReportStatus.INFO);
                    publish(new UiActionTrace.UiActionEvent(UiActionTrace.Kind.OBSERVATION,
                            "Retry attempt", target, attemptExpected, failureObserved,
                            AtfReportStatus.INFO, elapsedMillis(started)));
                    String retryExpected = "attempt " + nextAttempt + " of " + retryPolicy.maxAttempts();
                    String retryObserved = "retry scheduled: " + retryPolicy.reason();
                    add("Retry", target, retryExpected, retryObserved, 0, AtfReportStatus.INFO);
                    publish(new UiActionTrace.UiActionEvent(UiActionTrace.Kind.OBSERVATION,
                            "Retry", target, retryExpected, retryObserved, AtfReportStatus.INFO, 0));
                    retryPolicy.pause();
                    attempt = nextAttempt;
                    continue;
                }
                String value = failure.getClass().getSimpleName() + ": " + failure.getMessage();
                long duration = elapsedMillis(started);
                add(operation, target, expected, value, duration, AtfReportStatus.FAIL);
                publish(new UiActionTrace.UiActionEvent(UiActionTrace.Kind.COMPLETE,
                        operation, target, expected, value, AtfReportStatus.FAIL, duration));
                throw failure;
            }
        }
    }

    void observation(String operation, String target, String expected, String observed) {
        add(operation, target, expected, observed, 0, AtfReportStatus.PASS,
                AtfUiEvidenceSource.OBSERVE);
        publish(new UiActionTrace.UiActionEvent(UiActionTrace.Kind.OBSERVATION,
                operation, target, expected, observed, AtfReportStatus.PASS, 0));
    }

    List<AtfUiActionRecord> snapshot() {
        return List.copyOf(records);
    }

    List<AtfUiActionRecord> drain() {
        List<AtfUiActionRecord> result = snapshot();
        records.clear();
        nextSequence = 1;
        return result;
    }

    private void add(String operation, String target, String expected, String observed,
                     long durationMillis, AtfReportStatus status) {
        records.add(new AtfUiActionRecord(nextSequence++, operation, target, expected,
                observed, durationMillis, status, AtfUiEvidenceSource.ACTION));
    }

    private void add(String operation, String target, String expected, String observed,
                     long durationMillis, AtfReportStatus status, AtfUiEvidenceSource source) {
        records.add(new AtfUiActionRecord(nextSequence++, operation, target, expected,
                observed, durationMillis, status, source));
    }

    private static long elapsedMillis(long started) {
        return Math.max(1L, (System.nanoTime() - started + 999_999L) / 1_000_000L);
    }

    private void publish(UiActionTrace.UiActionEvent event) {
        try {
            trace.publish(event);
        } catch (RuntimeException ignored) {
            // Diagnostic telemetry is best-effort and must never change a UI verdict.
        }
    }

    @FunctionalInterface
    interface Action<T> {
        T run();
    }
}
