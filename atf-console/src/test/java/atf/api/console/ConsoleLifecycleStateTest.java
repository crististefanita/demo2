package atf.api.console;

import atf.api.core.reporting.AtfConsoleEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleLifecycleStateTest {
    @Test
    void entersBeforeChildrenAndLeavesBeforeCompletion() {
        var state = new ConsoleLifecycleState();

        assertAll(
                () -> assertEquals(0, state.depthFor(AtfConsoleEvent.Type.START)),
                () -> assertEquals(1, state.depthFor(AtfConsoleEvent.Type.START)),
                () -> assertEquals(2, state.depthFor(AtfConsoleEvent.Type.DETAIL)),
                () -> assertEquals(1, state.depthFor(AtfConsoleEvent.Type.COMPLETE)),
                () -> assertEquals(0, state.depthFor(AtfConsoleEvent.Type.COMPLETE)),
                () -> assertEquals(0, state.currentDepth()));
    }

    @Test
    void completionCannotDriveIndentationBelowZero() {
        var state = new ConsoleLifecycleState();

        assertEquals(0, state.depthFor(AtfConsoleEvent.Type.COMPLETE));
        assertEquals(0, state.currentDepth());
    }
}
