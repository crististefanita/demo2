package atf.ui.tests.practiceshop.component.purchase;

import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.ShopPurchaseTraceStep;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AtfComponentTest
class ShopPurchaseTraceStepTest {
    @Test
    void guestPurchaseUsesOneContextOneActionSevenContinuationsAndOneOutcome() {
        var keywords = Arrays.stream(ShopPurchaseTraceStep.values())
                .filter(step -> step != ShopPurchaseTraceStep.AUTHENTICATED_CHECKOUT)
                .map(ShopPurchaseTraceStep::keyword)
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
