package atf.api.core.ui;

import atf.api.core.reporting.AtfReportStatus;

import java.util.Objects;

/** Immutable, renderer-neutral evidence for one consecutive UI action. */
public record AtfUiActionRecord(
        int sequence,
        String operation,
        String target,
        String expected,
        String observed,
        long durationMillis,
        AtfReportStatus status) {

    public AtfUiActionRecord {
        if (sequence < 1) throw new IllegalArgumentException("sequence must be positive");
        operation = required(operation, "operation");
        target = required(target, "target");
        expected = normalize(expected);
        observed = normalize(observed);
        if (durationMillis < 0) throw new IllegalArgumentException("durationMillis must not be negative");
        status = Objects.requireNonNull(status, "status");
    }

    public String durationText() {
        // Playwright action tables intentionally keep the reference format in
        // milliseconds even for slower operations (for example 547 ms).
        return durationMillis + " ms";
    }

    /** Canonical text label retained for non-HTML consumers. */
    public String resultLabel() {
        return switch (status) {
            case PASS -> "Pass";
            case WARNING -> "Warning";
            case FAIL -> "Fail";
            case SKIP -> "Skip";
            case INFO -> "Info";
        };
    }

    /** Compact visual marker used by the HTML action-result cell. */
    public String resultEmoji() {
        return switch (status) {
            case PASS -> "✅";
            case WARNING -> "⚠️";
            case FAIL -> "❌";
            case SKIP -> "⏭️";
            case INFO -> "ℹ️";
        };
    }

    private static String required(String value, String name) {
        String normalized = normalize(value);
        if (normalized.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return normalized;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
