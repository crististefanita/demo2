package atf.api.ui.playwright;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.ui.AtfUiEvidenceSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
