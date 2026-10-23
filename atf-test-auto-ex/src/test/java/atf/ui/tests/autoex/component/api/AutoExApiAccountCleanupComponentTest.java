package atf.ui.tests.autoex.component.api;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.autoex.api.AutoExApiAccountCleanup;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Proves cleanup is armed before a potentially partial create and cannot replace its primary failure. */
@AtfComponentTest
class AutoExApiAccountCleanupComponentTest {
    @Test
    void cleanupRunsAfterCreateFailureAndItsFailureIsSuppressed() {
        AtomicInteger cleanupCalls = new AtomicInteger();
        var primaryFailure = new IllegalStateException("account create response was malformed");
        var cleanupFailure = new AssertionError("account deletion failed");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            try (var cleanup = new AutoExApiAccountCleanup(() -> {
                cleanupCalls.incrementAndGet();
                throw cleanupFailure;
            })) {
                cleanup.armBeforeCreateAttempt();
                throw primaryFailure;
            }
        });

        assertEquals(1, cleanupCalls.get());
        assertSame(primaryFailure, thrown, "the create failure remains the primary failure");
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(cleanupFailure, thrown.getSuppressed()[0]);
    }

    @Test
    void cleanupDoesNotRunBeforeACreateAttemptIsArmed() {
        AtomicInteger cleanupCalls = new AtomicInteger();

        try (var cleanup = new AutoExApiAccountCleanup(cleanupCalls::incrementAndGet)) {
            // No create attempt was made, so there is no account to remove.
        }

        assertEquals(0, cleanupCalls.get());
    }
}
