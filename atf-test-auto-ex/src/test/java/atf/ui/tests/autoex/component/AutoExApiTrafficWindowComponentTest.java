package atf.ui.tests.autoex.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.AutoExApiTrafficWindow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AtfComponentTest
class AutoExApiTrafficWindowComponentTest {
    @Test
    void setupExchangeCannotSatisfyCausalBrowserTrafficEvidence() {
        var window = new AutoExApiTrafficWindow();
        List<String> observations = new java.util.ArrayList<>(List.of("explicit catalog API precondition"));

        window.markBaseline(observations);
        observations.add("UI search triggered exchange");
        observations.add("checkout triggered exchange");

        assertEquals(List.of("UI search triggered exchange", "checkout triggered exchange"),
                window.causalSince(observations));
    }

    @Test
    void configuredJourneyWithNoSubsequentExchangeHasNoCausalApiEvidence() {
        var window = new AutoExApiTrafficWindow();
        List<String> observations = List.of("explicit catalog API precondition");

        window.markBaseline(observations);

        assertEquals(List.of(), window.causalSince(observations));
    }
}
