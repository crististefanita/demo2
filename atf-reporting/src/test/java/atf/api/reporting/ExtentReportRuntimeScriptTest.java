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
                () -> assertTrue(script.contains("function directStructuralChildren(card)")),
                () -> assertTrue(script.contains("function directCardHeader(card)"),
                        "duration badges must be read from the node's own header, never from a descendant clone"),
                () -> assertTrue(script.contains("function directNode(card)"),
                        "each projected card must retain the measurement on its own Extent node"),
                () -> assertTrue(script.contains("function nodeIdentity(card)"),
                        "projected cards must resolve their measured duration through the stable Extent node id"),
                () -> assertTrue(script.contains("function nodePathKey(card)"),
                        "projected cards without a preserved id must resolve through their structural title path"),
                () -> assertTrue(script.contains("atfMeasuredDurationsByNodeId=Object.create(null)"),
                        "source-tree measurements must survive vendor-generated detail clones"),
                () -> assertTrue(script.contains("atfMeasuredDurationsByPath=Object.create(null)"),
                        "structural-path measurements must survive id rewriting by the vendor renderer"),
                () -> assertTrue(script.contains("function rememberMeasuredDurations()"),
                        "original vendor durations must be captured before structural aggregation mutates containers"),
                () -> assertTrue(script.contains("#test-view .test-list-item .test-contents .card > .card-header"),
                        "the measurement map must be populated only from the immutable source tree"),
                () -> assertTrue(script.contains("data-atf-measured-duration-ms"),
                        "the original measurement must also travel with cloned badges when Extent clones markup"),
                () -> assertTrue(script.contains("node.getAttribute('data-atf-measured-duration-ms')"),
                        "cloned Extent nodes must prefer their own immutable measured duration"),
                () -> assertTrue(script.contains("#test-view .test-list-item .test-contents .card > .card-header"),
                        "measurement capture must read each card's own direct header, never a descendant header"),
                () -> assertTrue(script.contains("function measuredNodeDuration(card)"),
                        "leaf nodes must retain their own measured duration across repeated clone passes"),
                () -> assertTrue(script.contains("function(id){rememberMeasuredDurations();atfBaseActivate(id)"),
                        "original durations must be captured before the vendor activation mutates the tree"),
                () -> assertTrue(script.contains("function isStructuralNode(card)"),
                        "duration aggregation must remain correct while a projected clone is being assigned semantic roles"),
                () -> assertTrue(script.contains("function directActionDuration(card)")),
                () -> assertTrue(script.contains("function directTransportEvidence(card)"),
                        "HTTP request nodes must retain their own measured exchange duration when they contain validation children"),
                () -> assertTrue(script.contains("function aggregateNodeDuration(card)")),
                () -> assertTrue(script.contains("function aggregateNodeDurations()")),
                () -> assertTrue(script.contains("function aggregateVisibleNodeDurations()"),
                        "the visible detail clone must be recalculated independently of the hidden source tree"),
                () -> assertTrue(script.contains("function allScenarioCards()"),
                        "the source scenario tree and the visible detail clone must share duration aggregation"),
                () -> assertTrue(script.contains("function setNodeDuration(card,value,source)")),
                () -> assertTrue(script.contains("container=children.length>0||actions.present"),
                        "leaf badges must remain measured values; only structural containers may be rewritten"),
                () -> assertTrue(script.contains("container||source==='measured'"),
                        "a projected leaf may be corrected from its source measurement without becoming aggregated"),
                () -> assertTrue(script.contains("atfFormatDuration(value)"),
                        "aggregated values must use the common display formatter"),
                () -> assertTrue(script.contains("Math.max(childTotal,ownAction)"),
                        "a vendor duration already inclusive of its child must not be added twice"),
                () -> assertTrue(script.contains("filter(durationChildVisible)"),
                        "filtered-out scenarios must not contribute to the visible class total"),
                () -> assertTrue(script.contains("data-duration-source',source")),
                () -> assertTrue(script.contains("data-duration-source")),
                () -> assertTrue(script.contains("window.atfActivate")),
                () -> assertTrue(script.contains("setTimeout(aggregateClassDurations,150)"),
                        "a deep-linked Tests view must be recalculated after asynchronous DOM activation"),
                () -> assertTrue(script.contains("setTimeout(aggregateClassDurations,500)"),
                        "class rendering may complete after the initial Extent activation callback"),
                () -> assertTrue(script.contains("function watchDurationNodes()"),
                        "late scenario insertion must trigger duration recalculation"),
                () -> assertTrue(script.contains("function bindDurationClassClicks()"),
                        "switching between API test classes must trigger a fresh class-total calculation"),
                () -> assertTrue(script.contains("function scheduleDurationAggregationAfterClassClick()"),
                        "class selection must be rechecked after the vendor finishes rendering the detail"),
                () -> assertTrue(script.contains("scheduleDurationAggregation()"),
                        "the mutation observer must coalesce recalculation requests"),
                () -> assertTrue(script.contains("testItems();aggregateNodeDurations()"),
                        "late aggregation must assign scenario identities before walking the tree"),
                () -> assertTrue(script.contains("DOMContentLoaded")));
    }

    @Test
    void filterRecalculationReusesTheStructuralDurationController() {
        String html = ExtentReportRuntimeBehaviour.inject("<html><head></head><body></body></html>");

        assertTrue(html.contains("refreshFilterSelection();aggregateClassDurations();captureDomContract();"),
                "changing a Tests filter must recalculate the visible structural totals");
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

    @Test
    void teardownThenActionsReuseTheValidationDisclosureContract() {
        String html = ExtentReportRuntimeBehaviour.inject("<html><head></head><body></body></html>");

        assertAll(
                () -> assertTrue(html.contains("/^(GIVEN|WHEN|THEN|AND|BUT)\\s+/.test(upper)"),
                        "THEN and all other Gherkin outcome nodes must share the validation card role"),
                () -> assertTrue(html.contains("data-atf-node-role=\"validation\"]"),
                        "the validation styling and disclosure selectors must apply to the THEN card"),
                () -> assertTrue(html.contains("assignNodeRoles();aggregateClassDurations();normalizeEvidenceDisclosures()"),
                        "opening a scenario must recalculate the visible cloned phase tree"),
                () -> assertTrue(html.contains("function bindNodeCollapse()")
                                && html.contains("data-atf-node-open"),
                        "the THEN card must receive the same plus/minus interaction contract as UI validation"));
    }
}
