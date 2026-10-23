package atf.ui.tests.autoex.component.lifecycle;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.lifecycle.AutoExE2eInitialization;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@AtfComponentTest
class AutoExE2eInitializationComponentTest {
    @Test
    void failureAfterBrowserAcquisitionClosesOnlyTheBrowser() {
        var events = new ArrayList<String>();
        var failure = new IllegalStateException("scenario creation failed");

        var actual = assertThrows(IllegalStateException.class, () -> create(
                events,
                ignored -> { throw failure; },
                ignored -> fail("scenario must not be created"),
                ignored -> fail("phase must not be created")));

        assertSame(failure, actual);
        assertEquals(List.of("browser", "scenario", "close browser"), events);
    }

    @Test
    void failureAfterScenarioAcquisitionMarksScenarioFailedThenClosesBrowser() {
        var events = new ArrayList<String>();
        var failure = new IllegalArgumentException("phase creation failed");

        var actual = assertThrows(IllegalArgumentException.class, () -> create(
                events,
                ignored -> "scenario",
                ignored -> { throw failure; },
                ignored -> fail("run must not be created")));

        assertSame(failure, actual);
        assertEquals(List.of("browser", "scenario", "phase", "scenario FAIL", "close browser"), events);
    }

    @Test
    void failureAfterPhaseAcquisitionMarksPhaseAndScenarioFailedThenClosesBrowser() {
        var events = new ArrayList<String>();
        var failure = new IllegalStateException("run creation failed");

        var actual = assertThrows(IllegalStateException.class, () -> create(
                events,
                ignored -> "scenario",
                ignored -> "phase",
                ignored -> { throw failure; }));

        assertSame(failure, actual);
        assertEquals(List.of("browser", "scenario", "phase", "run", "phase FAIL", "scenario FAIL", "close browser"),
                events);
    }

    @Test
    void rollbackFailuresAreSuppressedWithoutReplacingTheSetupFailure() {
        var events = new ArrayList<String>();
        var setupFailure = new IllegalStateException("phase creation failed");
        var phaseFailure = new IllegalArgumentException("phase report close failed");
        var browserFailure = new UnsupportedOperationException("browser close failed");

        var actual = assertThrows(IllegalStateException.class, () -> AutoExE2eInitialization.create(
                () -> { events.add("browser"); return "browser"; },
                ignored -> { events.add("scenario"); return "scenario"; },
                ignored -> { events.add("phase"); return "phase"; },
                (browser, scenario, phase) -> { throw setupFailure; },
                ignored -> { events.add("phase FAIL"); throw phaseFailure; },
                ignored -> events.add("scenario FAIL"),
                ignored -> { events.add("close browser"); throw browserFailure; }));

        assertSame(setupFailure, actual);
        assertEquals(List.of("browser", "scenario", "phase", "phase FAIL", "scenario FAIL", "close browser"), events);
        assertEquals(List.of(phaseFailure, browserFailure), List.of(actual.getSuppressed()));
    }

    @Test
    void nullBrowserIsRejectedBeforeAnyDependentResourceIsCreated() {
        var events = new ArrayList<String>();

        var failure = assertThrows(NullPointerException.class, () -> AutoExE2eInitialization.create(
                () -> { events.add("browser"); return null; },
                ignored -> { events.add("scenario"); return "scenario"; },
                ignored -> { events.add("phase"); return "phase"; },
                (browser, scenario, phase) -> "run",
                ignored -> events.add("phase FAIL"),
                ignored -> events.add("scenario FAIL"),
                ignored -> events.add("close browser")));

        assertEquals("browserFactory returned null", failure.getMessage());
        assertEquals(List.of("browser"), events);
    }

    @Test
    void nullScenarioRollsBackTheAcquiredBrowser() {
        var events = new ArrayList<String>();

        var failure = assertThrows(NullPointerException.class, () -> AutoExE2eInitialization.create(
                () -> { events.add("browser"); return "browser"; },
                ignored -> { events.add("scenario"); return null; },
                ignored -> { events.add("phase"); return "phase"; },
                (browser, scenario, phase) -> "run",
                ignored -> events.add("phase FAIL"),
                ignored -> events.add("scenario FAIL"),
                ignored -> events.add("close browser")));

        assertEquals("scenarioFactory returned null", failure.getMessage());
        assertEquals(List.of("browser", "scenario", "close browser"), events);
    }

    @Test
    void nullPhaseFailsScenarioAndClosesTheAcquiredBrowser() {
        var events = new ArrayList<String>();

        var failure = assertThrows(NullPointerException.class, () -> AutoExE2eInitialization.create(
                () -> { events.add("browser"); return "browser"; },
                ignored -> { events.add("scenario"); return "scenario"; },
                ignored -> { events.add("phase"); return null; },
                (browser, scenario, phase) -> "run",
                ignored -> events.add("phase FAIL"),
                ignored -> events.add("scenario FAIL"),
                ignored -> events.add("close browser")));

        assertEquals("phaseFactory returned null", failure.getMessage());
        assertEquals(List.of("browser", "scenario", "phase", "scenario FAIL", "close browser"), events);
    }

    @Test
    void nullRunFailsAvailableReportNodesAndClosesTheBrowser() {
        var events = new ArrayList<String>();

        var failure = assertThrows(NullPointerException.class, () -> AutoExE2eInitialization.create(
                () -> { events.add("browser"); return "browser"; },
                ignored -> { events.add("scenario"); return "scenario"; },
                ignored -> { events.add("phase"); return "phase"; },
                (browser, scenario, phase) -> { events.add("run"); return null; },
                ignored -> events.add("phase FAIL"),
                ignored -> events.add("scenario FAIL"),
                ignored -> events.add("close browser")));

        assertEquals("runFactory returned null", failure.getMessage());
        assertEquals(List.of("browser", "scenario", "phase", "run", "phase FAIL", "scenario FAIL",
                "close browser"), events);
    }

    private static void create(List<String> events,
                               java.util.function.Function<String, String> scenarioFactory,
                               java.util.function.Function<String, String> phaseFactory,
                               java.util.function.Function<String, String> runFactory) {
        AutoExE2eInitialization.create(
                () -> { events.add("browser"); return "browser"; },
                browser -> { events.add("scenario"); return scenarioFactory.apply(browser); },
                scenario -> { events.add("phase"); return phaseFactory.apply(scenario); },
                (browser, scenario, phase) -> { events.add("run"); return runFactory.apply(phase); },
                ignored -> events.add("phase FAIL"),
                ignored -> events.add("scenario FAIL"),
                ignored -> events.add("close browser"));
    }
}
