package atf.api.reporting.runtime.core;

import atf.api.testkit.AtfUnitTest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the duration runtime seams without requiring a browser or a generated report. */
@AtfUnitTest
class ExtentReportDurationRuntimeComponentTest {
    private static final List<String> FUNCTION_NAMES = List.of(
            "atfDurationValue",
            "atfFormatDuration",
            "atfNormalizeDuration",
            "normalizeTestNavigatorSeparators",
            "normalizeHeaderMetadata",
            "normalizeDurations",
            "directCardHeader",
            "directNode",
            "durationBadge",
            "nodeLabel",
            "nodePathKey",
            "nodeIdentity",
            "rememberMeasuredDurations",
            "measuredNodeDuration",
            "durationChildVisible",
            "isStructuralNode",
            "directStructuralChildren",
            "directActionDuration",
            "directTransportEvidence",
            "setNodeDuration",
            "aggregateChildDurations",
            "durationDecision",
            "aggregateNodeDuration",
            "allScenarioCards",
            "aggregateVisibleNodeDurations",
            "aggregateNodeDurations",
            "scenarioCardFor",
            "normalizeCategoryDurations",
            "normalizeOverviewDuration",
            "normalizeFailsDurations",
            "normalizeProjectedDurations",
            "markDuration",
            "publishDuration",
            "classDurationSummary",
            "publishClassDuration",
            "aggregateClassDurations",
            "scheduleDurationAggregation",
            "scheduleDurationAggregationAfterClassClick",
            "bindDurationClassClicks",
            "watchDurationNodes");

    @Test
    void facadeKeepsFourRuntimeSeamsInExecutionOrder() {
        String parsing = ExtentReportDurationRuntime.Fragment.PARSING.source();
        String aggregation = ExtentReportDurationRuntime.Fragment.AGGREGATION.source();
        String projection = ExtentReportDurationRuntime.Fragment.PROJECTION.source();
        String lifecycle = ExtentReportDurationRuntime.Fragment.LIFECYCLE.source();
        String script = ExtentReportDurationRuntime.script();

        assertAll(
                () -> assertFalse(parsing.isBlank()),
                () -> assertFalse(aggregation.isBlank()),
                () -> assertFalse(projection.isBlank()),
                () -> assertFalse(lifecycle.isBlank()),
                () -> assertEquals(
                        parsing + aggregation + projection + lifecycle,
                        script),
                () -> assertTrue(parsing.contains("function atfDurationValue(")),
                () -> assertTrue(aggregation.contains("function aggregateNodeDuration(")),
                () -> assertTrue(projection.contains("function aggregateClassDurations(")),
                () -> assertTrue(lifecycle.contains("function watchDurationNodes(")));
    }

    @Test
    void runtimeDeclaresEachDurationFunctionExactlyOnce() {
        String script = ExtentReportDurationRuntime.script();

        assertAll(FUNCTION_NAMES.stream()
                .map(name -> (org.junit.jupiter.api.function.Executable) () -> assertEquals(
                        1,
                        occurrences(script, "function " + name + "("),
                        "Expected one declaration for " + name))
                .toList());
    }

    @Test
    void boundariesKeepCrossCuttingResponsibilitiesSeparated() {
        String parsing = ExtentReportDurationRuntime.Fragment.PARSING.source();
        String aggregation = ExtentReportDurationRuntime.Fragment.AGGREGATION.source();
        String projection = ExtentReportDurationRuntime.Fragment.PROJECTION.source();
        String lifecycle = ExtentReportDurationRuntime.Fragment.LIFECYCLE.source();

        assertAll(
                () -> assertFalse(parsing.contains("MutationObserver")),
                () -> assertFalse(aggregation.contains("normalizeCategoryDurations")),
                () -> assertFalse(projection.contains("MutationObserver")),
                () -> assertFalse(lifecycle.contains("function aggregateNodeDuration(")),
                () -> assertTrue(
                        ExtentReportDurationRuntime.script().indexOf("function atfDurationValue(")
                                < ExtentReportDurationRuntime.script().indexOf("function aggregateNodeDuration(")),
                () -> assertTrue(
                        ExtentReportDurationRuntime.script().indexOf("function aggregateNodeDuration(")
                                < ExtentReportDurationRuntime.script().indexOf("function aggregateClassDurations(")),
                () -> assertTrue(
                        ExtentReportDurationRuntime.script().indexOf("function aggregateClassDurations(")
                                < ExtentReportDurationRuntime.script().indexOf("function watchDurationNodes(")));
    }

    private static int occurrences(String source, String token) {
        int count = 0;
        int from = 0;
        while ((from = source.indexOf(token, from)) >= 0) {
            count++;
            from += token.length();
        }
        return count;
    }
}
