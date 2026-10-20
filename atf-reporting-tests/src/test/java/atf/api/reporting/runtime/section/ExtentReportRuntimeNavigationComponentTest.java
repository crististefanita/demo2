package atf.api.reporting.runtime.section;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportRuntimeNavigationComponentTest {
    private static final List<String> FUNCTIONS = List.of(
            "normalizeCodeBlocks", "normalizeDisclosureState", "captureDomContract",
            "openScenarioCard", "openDeepLinkCard", "openScenarioAuditPath",
            "collapseDirectScenarioCards", "collapseScenarioCards",
            "toggleScenario", "initializeScenarioCard", "bindScenarioCollapse",
            "watchScenarioCards", "phaseDirectBody", "applyPhaseBodyState",
            "bindPhaseCollapse", "nodePanel", "bindNodeCollapse", "activateFromHash",
            "normalizeNavigationLinks");

    @Test
    void navigationFacadePublishesOneStableDeclarationPerFunction() {
        String script = ExtentReportRuntimeNavigation.script();

        assertAll(
                () -> assertEquals(1, count(script, "window.atfOpenTest=function")),
                () -> assertEquals(1, count(script, "window.atfActivate=function")),
                () -> assertEquals(1, count(script, "document.addEventListener('DOMContentLoaded'")),
                () -> assertEquals(1, count(script, "window.addEventListener('load'")),
                () -> FUNCTIONS.forEach(function -> assertEquals(1,
                        count(script, "function " + function + "("),
                        () -> "Navigation function must have one declaration: " + function)));
    }

    @Test
    void navigationResponsibilitiesRemainSeparatedByComponent() {
        String dom = ExtentReportRuntimeNavigationDom.script();
        String disclosure = ExtentReportRuntimeNavigationDisclosure.script();
        String controller = ExtentReportRuntimeNavigationController.script();

        assertAll(
                () -> assertTrue(dom.contains("function captureDomContract(")),
                () -> assertTrue(dom.contains("window.__atfDomContract")),
                () -> assertTrue(disclosure.contains("function bindScenarioCollapse(")),
                () -> assertTrue(disclosure.contains("function bindNodeCollapse(")),
                () -> assertTrue(disclosure.contains("function nodePanel(")),
                () -> assertTrue(controller.contains("window.atfOpenTest=function")),
                () -> assertTrue(controller.contains("function activateFromHash(")),
                () -> assertTrue(controller.contains("screen-validation")),
                () -> assertTrue(disclosure.contains("function openScenarioAuditPath(")),
                () -> assertTrue(controller.contains("function normalizeNavigationLinks(")),
                () -> assertFalse(dom.contains("window.atfActivate=function")),
                () -> assertFalse(disclosure.contains("window.atfActivate=function")));
    }

    private static int count(String source, String token) {
        int count = 0;
        int start = 0;
        while ((start = source.indexOf(token, start)) >= 0) {
            count++;
            start += token.length();
        }
        return count;
    }
}
