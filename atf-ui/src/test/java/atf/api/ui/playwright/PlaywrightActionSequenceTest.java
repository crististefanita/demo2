package atf.api.ui.playwright;

import atf.api.core.reporting.AtfReportStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }
}
