package atf.ui.tests.autoex.component;

import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.journey.AutoExPurchaseTraceStep;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class AutoExPurchaseTraceStepTest {
    @Test
    void catalogOwnsEveryReportedPurchaseCheckpointExactlyOnce() {
        var steps = Arrays.stream(AutoExPurchaseTraceStep.values()).toList();
        var titles = steps.stream().map(AutoExPurchaseTraceStep::title).toList();

        assertEquals(13, steps.size());
        assertEquals(steps.size(), titles.stream().distinct().count());
        assertTrue(steps.stream().allMatch(step -> step.keyword() != null
                && step.title() != null && !step.title().isBlank()));
        assertEquals(AtfGherkinKeyword.GIVEN,
                AutoExPurchaseTraceStep.CATALOG_API_PRECONDITION.keyword());
        assertEquals(AtfGherkinKeyword.THEN,
                AutoExPurchaseTraceStep.TERMINAL_STATE.keyword());
    }
}
