package atf.api.ui.playwright;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;

import java.util.ArrayList;
import java.util.List;

/** Owns ordered UI evidence, sequence boundaries and operation timing. */
final class UiActionRecorder {
    private final List<AtfUiActionRecord> records = new ArrayList<>();
    private int nextSequence = 1;

    <T> T run(String operation, String target, String expected, Action<T> action) {
        long started = System.nanoTime();
        try {
            T observed = action.run();
            add(operation, target, expected, observed == null ? "operation completed" : observed.toString(),
                    elapsedMillis(started), AtfReportStatus.PASS);
            return observed;
        } catch (RuntimeException | AssertionError failure) {
            add(operation, target, expected, failure.getClass().getSimpleName() + ": " + failure.getMessage(),
                    elapsedMillis(started), AtfReportStatus.FAIL);
            throw failure;
        }
    }

    void observation(String operation, String target, String expected, String observed) {
        add(operation, target, expected, observed, 0, AtfReportStatus.PASS);
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
                observed, durationMillis, status));
    }

    private static long elapsedMillis(long started) {
        return Math.max(1L, (System.nanoTime() - started + 999_999L) / 1_000_000L);
    }

    @FunctionalInterface
    interface Action<T> {
        T run();
    }
}
