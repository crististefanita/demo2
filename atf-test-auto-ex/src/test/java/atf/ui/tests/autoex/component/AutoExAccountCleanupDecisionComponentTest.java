package atf.ui.tests.autoex.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.account.AutoExAccountCleanupAction;
import atf.ui.autoex.support.account.AutoExAccountCleanupDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AtfComponentTest
class AutoExAccountCleanupDecisionComponentTest {
    @Test
    void cleanupIsNoOpUntilCreationIsObservedAndDeletionIsAvailableAfterward() {
        assertEquals(AutoExAccountCleanupAction.NO_OP,
                AutoExAccountCleanupDecision.resolve(false, false, false));
        assertEquals(AutoExAccountCleanupAction.CONTINUE_THEN_DELETE,
                AutoExAccountCleanupDecision.resolve(true, true, false));
        assertEquals(AutoExAccountCleanupAction.DELETE,
                AutoExAccountCleanupDecision.resolve(true, false, true));
        assertEquals(AutoExAccountCleanupAction.CONFIRMED_BUT_UNAVAILABLE,
                AutoExAccountCleanupDecision.resolve(true, false, false));
    }
}
