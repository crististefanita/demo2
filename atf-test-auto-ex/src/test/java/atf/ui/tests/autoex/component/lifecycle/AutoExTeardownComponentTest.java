package atf.ui.tests.autoex.component.lifecycle;

import atf.api.testkit.AtfComponentTest;
import atf.api.testkit.lifecycle.FailureAwareCleanup;
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

        Throwable failure = FailureAwareCleanup.runAll(null,
                () -> { events.add("network inspector"); throw inspectorFailure; },
                () -> events.add("browser close"),
                () -> { events.add("phase completion"); throw phaseFailure; },
                () -> events.add("scenario completion"));

        assertEquals(List.of("network inspector", "browser close", "phase completion", "scenario completion"),
                events);
        assertSame(inspectorFailure, failure);
        assertEquals(List.of(phaseFailure), List.of(failure.getSuppressed()));
    }

    @Test
    void browserCloseFailureDoesNotPreventFailureEvidenceOrReportCompletion() {
        var events = new ArrayList<String>();
        var closeFailure = new IllegalStateException("browser close failed");

        Throwable failure = FailureAwareCleanup.runAll(null,
                () -> { events.add("browser close"); throw closeFailure; },
                () -> events.add("failure evidence"),
                () -> events.add("phase completion"),
                () -> events.add("scenario completion"));

        assertEquals(List.of("browser close", "failure evidence", "phase completion", "scenario completion"),
                events);
        assertSame(closeFailure, failure);
    }
}
