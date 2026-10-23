package atf.ui.tests.autoex.component.journey;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.account.AutoExAccountDeletionProgress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pure business safety decisions: no browser, fake advert, injected hash or simulated shop. */
@AtfComponentTest
class AutoExAccountDeletionSafetyComponentTest {
    @ParameterizedTest
    @CsvSource({
            "true, 0, false, true",
            "true, 1, false, false",
            "true, 2, false, false",
            "true, 0, true, false",
            "true, 1, true, false",
            "false, 0, false, false",
            "false, 1, false, false",
            "false, 0, true, false"
    })
    void onlyAnAdvertCancelledBeforeAnyDeletionRequestAllowsNavigationToResume(
            boolean advertDismissed, int startedRequests, boolean destinationReached, boolean allowed) {
        var progress = new AutoExAccountDeletionProgress(advertDismissed, startedRequests, destinationReached);

        assertEquals(allowed, progress.mayResumeNavigation());
    }

    @Test
    void anInvalidRequestCountCannotAuthorizeAnotherDeletion() {
        assertThrows(IllegalArgumentException.class, () -> new AutoExAccountDeletionProgress(true, -1, false));
    }
}
