package atf.ui.autoex.support;

import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@AtfComponentTest
class AutoExFailureCombinerComponentTest {
    @Test
    void cleanupFailureIsSuppressedWithoutReplacingTheOriginalTestFailure() {
        var testFailure = new AssertionError("purchase assertion failed");
        var cleanupFailure = new IllegalStateException("account cleanup failed");

        Throwable combined = AutoExFailureCombiner.combine(testFailure, cleanupFailure);

        assertSame(testFailure, combined);
        assertEquals(1, combined.getSuppressed().length);
        assertSame(cleanupFailure, combined.getSuppressed()[0]);
    }

    @Test
    void cleanupFailureIsPrimaryWhenNoTestFailureExists() {
        var cleanupFailure = new IllegalStateException("account cleanup failed");

        assertSame(cleanupFailure, AutoExFailureCombiner.combine(null, cleanupFailure));
    }
}
