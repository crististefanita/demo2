package atf.api.testkit.lifecycle;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@AtfUnitTest
class FailureAwareCleanupTest {
    @Test
    void cleanupStagesAllRunAndKeepTheFirstFailurePrimary() {
        var primary = new AssertionError("test failed");
        var firstCleanupFailure = new IllegalStateException("inspector failed");
        var laterCleanupFailure = new IllegalArgumentException("browser close failed");
        var calls = new ArrayList<String>();

        Throwable result = FailureAwareCleanup.runAll(primary,
                () -> { calls.add("inspector"); throw firstCleanupFailure; },
                () -> calls.add("report phase"),
                () -> { calls.add("browser"); throw laterCleanupFailure; });

        assertSame(primary, result);
        assertEquals(List.of("inspector", "report phase", "browser"), calls);
        assertEquals(List.of(firstCleanupFailure, laterCleanupFailure),
                List.of(result.getSuppressed()));
    }

    @Test
    void partiallyAcquiredResourcesCloseInReverseAndKeepAcquisitionFailurePrimary() {
        var primary = new IllegalStateException("acquisition failed");
        var closeFailure = new IllegalArgumentException("browser close failed");
        var calls = new ArrayList<String>();

        Throwable result = FailureAwareCleanup.closeAfterFailure(primary,
                () -> calls.add("playwright"),
                () -> { calls.add("browser"); throw closeFailure; },
                () -> calls.add("context"));

        assertSame(primary, result);
        assertEquals(List.of("context", "browser", "playwright"), calls);
        assertEquals(List.of(closeFailure), List.of(result.getSuppressed()));
    }

    @Test
    void cleanupFailureBecomesPrimaryWhenThereWasNoEarlierFailure() {
        var cleanupFailure = new IllegalStateException("close failed");

        assertSame(cleanupFailure, FailureAwareCleanup.combine(null, cleanupFailure));
    }
}
