package atf.api.ui.playwright;
import atf.api.testkit.AtfComponentTest;

import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.core.ui.AtfUiEvidenceSource;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest

class PlaywrightActionSequenceTest {
    @Test
    void drainsConsecutiveObservationsInOrderAndStartsANewSequence() {
        var actions = new PlaywrightActionSequence();
        actions.observation("Get text", "role=heading[name=Quote]", "heading is visible", "Quote");
        actions.observation("Focus", "role=tab[name=Checklist]", "tab is focused", "focus completed");

        var first = actions.drain();
        var second = actions.drain();

        assertEquals(2, first.size());
        assertEquals(1, first.get(0).sequence());
        assertEquals("Get text", first.get(0).operation());
        assertEquals("Focus", first.get(1).operation());
        assertEquals(0, second.size());
        assertTrue(first.stream().allMatch(action -> action.status() == AtfReportStatus.PASS));
        assertTrue(first.stream().allMatch(action -> action.source() == AtfUiEvidenceSource.OBSERVE));
    }

    @Test
    void rejectsBlankLocatorTargetsBeforeAnElementActionCanBeRecorded() {
        assertThrows(IllegalArgumentException.class,
                () -> PlaywrightActionSequence.requireLocatorTarget(null));
        assertThrows(IllegalArgumentException.class,
                () -> PlaywrightActionSequence.requireLocatorTarget("  "));
        assertEquals("button[data-filter-value='clear']",
                PlaywrightActionSequence.requireLocatorTarget(" button[data-filter-value='clear'] "));
    }

    @Test
    void rejectsNonEmptyProseBecauseActionsMustExposeTheEffectiveLocator() {
        assertThrows(IllegalArgumentException.class,
                () -> PlaywrightActionSequence.requireLocatorTarget("selected owner detail"));
        assertEquals("details:visible > summary",
                PlaywrightActionSequence.requireLocatorTarget(" details:visible > summary "));
    }

    @Test
    void typedLocatorCarriesTheValidatedSelectorForElementBoundOperations() {
        assertThrows(NullPointerException.class,
                () -> new AtfLocator(null, "#test-view"));
        assertThrows(IllegalArgumentException.class,
                () -> PlaywrightActionSequence.requireLocatorTarget("selected owner detail"));
    }

    @Test
    void fillRecordsSafeEvidenceWithoutExposingTheEnteredValue() {
        var entered = new ArrayList<String>();
        var locator = (com.microsoft.playwright.Locator) Proxy.newProxyInstance(
                com.microsoft.playwright.Locator.class.getClassLoader(),
                new Class<?>[]{com.microsoft.playwright.Locator.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "count" -> 1;
                    case "fill" -> {
                        entered.add((String) arguments[0]);
                        yield null;
                    }
                    case "toString" -> "#secret-field";
                    default -> throw new UnsupportedOperationException(method.getName());
                });

        var actions = new PlaywrightActionSequence();
        actions.fill(new AtfLocator(locator, "#secret-field"), "sensitive-value", "field is filled");

        var record = actions.snapshot().getFirst();
        assertEquals("Fill", record.operation());
        assertEquals("value entered", record.observed());
        assertEquals("field is filled", record.expected());
        assertEquals(List.of("sensitive-value"), entered);
        assertTrue(record.observed().contains("value"));
    }

    @Test
    void nativeOptionAndValueAssertionsRemainTraceable() {
        var selected = new ArrayList<String>();
        var locator = (com.microsoft.playwright.Locator) Proxy.newProxyInstance(
                com.microsoft.playwright.Locator.class.getClassLoader(),
                new Class<?>[]{com.microsoft.playwright.Locator.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "count" -> 1;
                    case "selectOption" -> {
                        selected.add(((com.microsoft.playwright.options.SelectOption) arguments[0]).label);
                        yield List.of("cash-on-delivery");
                    }
                    case "inputValue" -> "2";
                    case "toString" -> "select[data-test='payment-method']";
                    default -> throw new UnsupportedOperationException(method.getName());
                });

        var actions = new PlaywrightActionSequence();
        actions.selectOption(new AtfLocator(locator, "select[data-test='payment-method']"),
                "Cash on Delivery", "demo payment method is selected");
        actions.expectValue(new AtfLocator(locator, "input[data-test='product-quantity']"), "2");

        assertEquals(2, actions.snapshot().size());
        assertEquals("Select option", actions.snapshot().get(0).operation());
        assertEquals("option selected", actions.snapshot().get(0).observed());
        assertEquals("Expect value", actions.snapshot().get(1).operation());
        assertEquals("2", actions.snapshot().get(1).observed());
        assertTrue(selected.getFirst().contains("Cash on Delivery"));
    }

    @Test
    void publishesUiTraceThroughAnInjectedPortWithoutTakingConsoleOwnership() {
        var events = new java.util.ArrayList<UiActionTrace.UiActionEvent>();
        var actions = new PlaywrightActionSequence(events::add);

        actions.observation("Click", "#nav-test", "Tests tab is visible", "Tests tab is visible");

        assertEquals(1, events.size());
        assertEquals(UiActionTrace.Kind.OBSERVATION, events.get(0).kind());
        assertEquals("Click", events.get(0).operation());
        assertEquals("#nav-test", events.get(0).target());
        assertEquals(AtfReportStatus.PASS, events.get(0).status());
    }
}
