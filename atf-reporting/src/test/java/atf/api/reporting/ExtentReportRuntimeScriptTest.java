package atf.api.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protects the ownership boundary between the publication pipeline and its runtime controllers. */
class ExtentReportRuntimeScriptTest {

    @Test
    void runtimeFacadeComposesControllersInStableOrder() {
        String html = ExtentReportRuntimeBehaviour.inject("<html><head></head><body></body></html>");

        int duration = html.indexOf("function aggregateClassDurations()");
        int failures = html.indexOf("function renderFails()");
        int causes = html.indexOf("function failureCauseChain");

        assertAll(
                () -> assertTrue(html.contains("id=\"atf-report-behaviour\"")),
                () -> assertTrue(duration >= 0),
                () -> assertTrue(failures > duration),
                () -> assertTrue(causes > failures));
    }

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

    @Test
    void failureCauseProjectionKeepsPrimaryAndSecondaryPhaseEvidence() {
        String script = ExtentReportFailureRuntime.causeChainScript();

        assertAll(
                () -> assertTrue(script.contains("failureCauseChain")),
                () -> assertTrue(script.contains("PRIMARY: ")),
                () -> assertTrue(script.contains("SECONDARY: ")),
                () -> assertTrue(script.contains("atfOriginalFailureContext")));
    }
}
