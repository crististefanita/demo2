package atf.ui.tests.practiceshop.component;

import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopPurchaseTraceStep;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AtfComponentTest
class PracticeShopPurchaseTraceStepTest {
    @Test
    void guestPurchaseUsesOneContextOneActionSevenContinuationsAndOneOutcome() {
        var keywords = Arrays.stream(PracticeShopPurchaseTraceStep.values())
                .filter(step -> step != PracticeShopPurchaseTraceStep.AUTHENTICATED_CHECKOUT)
                .map(PracticeShopPurchaseTraceStep::keyword)
                .toList();

        assertEquals(List.of(
                AtfGherkinKeyword.GIVEN,
                AtfGherkinKeyword.AND,
                AtfGherkinKeyword.AND,
                AtfGherkinKeyword.AND,
                AtfGherkinKeyword.WHEN,
                AtfGherkinKeyword.AND,
                AtfGherkinKeyword.AND,
                AtfGherkinKeyword.AND,
                AtfGherkinKeyword.AND,
                AtfGherkinKeyword.THEN), keywords);
    }
}
