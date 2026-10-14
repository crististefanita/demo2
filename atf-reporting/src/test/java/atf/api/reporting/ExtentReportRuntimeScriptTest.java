package atf.api.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protects the ownership boundary between the publication pipeline and its runtime controllers. */
class ExtentReportRuntimeScriptTest {

    @Test
    void durationControllerOwnsAggregationAndNavigationHooks() {
        String script = ExtentReportDurationRuntime.script();

        assertAll(
                () -> assertTrue(script.contains("function aggregateClassDurations()")),
                () -> assertTrue(script.contains("data-duration-source")),
                () -> assertTrue(script.contains("window.atfActivate")),
                () -> assertTrue(script.contains("DOMContentLoaded")));
    }

    @Test
    void failureControllerOwnsDiagnosticProjectionAndDeepLink() {
        String script = ExtentReportFailureRuntime.script();

        assertAll(
                () -> assertTrue(script.contains("failureContextFor")),
                () -> assertTrue(script.contains("atf-failure-index")),
                () -> assertTrue(script.contains("Open in Tests")),
                () -> assertTrue(script.contains("renderFails=function")),
                () -> assertTrue(script.contains("phaseOrder")));
    }
}
