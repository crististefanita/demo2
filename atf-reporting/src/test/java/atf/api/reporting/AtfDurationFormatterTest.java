package atf.api.reporting;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtfDurationFormatterTest {
    @Test
    void preservesZeroButRoundsPositiveSubMillisecondDurationsUpToOneMillisecond() {
        assertEquals(0, AtfDurationFormatter.elapsedMillis(Duration.ZERO));
        assertEquals(1, AtfDurationFormatter.elapsedMillis(Duration.ofNanos(1)));
        assertEquals(1, AtfDurationFormatter.elapsedMillis(Duration.ofNanos(999_999)));
        assertEquals(2, AtfDurationFormatter.elapsedMillis(Duration.ofNanos(1_000_001)));
    }

    @Test
    void preservesWholeMillisecondsAndRejectsNegativeDurations() {
        assertEquals(12, AtfDurationFormatter.elapsedMillis(Duration.ofMillis(12)));
        assertThrows(IllegalArgumentException.class,
                () -> AtfDurationFormatter.elapsedMillis(Duration.ofNanos(-1)));
    }
}
