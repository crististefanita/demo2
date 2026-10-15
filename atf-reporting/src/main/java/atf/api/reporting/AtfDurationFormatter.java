package atf.api.reporting;

import java.time.Duration;
import java.util.Objects;

/** Formats measured durations without truncating a positive sub-millisecond interval to zero. */
final class AtfDurationFormatter {
    private AtfDurationFormatter() {
    }

    static long elapsedMillis(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration must not be negative");
        }
        if (duration.isZero()) {
            return 0;
        }
        return Math.max(1L, (duration.toNanos() + 999_999L) / 1_000_000L);
    }
}
