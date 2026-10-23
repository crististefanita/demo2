package atf.ui.tests.autoex.component.lifecycle;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.lifecycle.AutoExTeardown;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@AtfComponentTest
class AutoExTeardownComponentTest {
    @Test
    void inspectorFailureDoesNotPreventLaterTeardownAndRemainsTheReportedCause() {
        var events = new ArrayList<String>();
        var inspectorFailure = new IllegalStateException("network inspector failed");
        var phaseFailure = new IllegalArgumentException("phase completion failed");
        Throwable failure = null;

        failure = AutoExTeardown.attempt(failure, () -> {
            events.add("network inspector");
            throw inspectorFailure;
        });
        failure = AutoExTeardown.attempt(failure, () -> events.add("browser close"));
        failure = AutoExTeardown.attempt(failure, () -> {
            events.add("phase completion");
            throw phaseFailure;
        });
        failure = AutoExTeardown.attempt(failure, () -> events.add("scenario completion"));

        assertEquals(List.of("network inspector", "browser close", "phase completion", "scenario completion"),
                events);
        assertSame(inspectorFailure, failure);
        assertEquals(1, failure.getSuppressed().length);
        assertSame(phaseFailure, failure.getSuppressed()[0]);
    }

    @Test
    void browserCloseFailureIsKnownBeforeReportCompletionAndDoesNotSkipIt() {
        var events = new ArrayList<String>();
        var closeFailure = new IllegalStateException("browser close failed");
        Throwable failure = AutoExTeardown.attempt(null, () -> {
            events.add("browser close");
            throw closeFailure;
        });
        failure = AutoExTeardown.attempt(failure, () -> events.add("failure evidence"));
        failure = AutoExTeardown.attempt(failure, () -> events.add("phase completion"));
        failure = AutoExTeardown.attempt(failure, () -> events.add("scenario completion"));

        assertEquals(List.of("browser close", "failure evidence", "phase completion", "scenario completion"),
                events);
        assertSame(closeFailure, failure);
    }
}
