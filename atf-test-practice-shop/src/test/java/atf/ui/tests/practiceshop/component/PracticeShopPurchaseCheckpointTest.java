package atf.ui.tests.practiceshop.component;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopPurchaseCheckpointTest {
    @Test
    void checkpointKeepsTheGherkinAndAssertionsAsAnImmutablePageContract() {
        var checkpoint = new PracticeShopPurchaseCheckpoint(
                AtfGherkinKeyword.GIVEN, "product page is complete",
                List.of(new AtfAssertion("Product", "Combination Pliers", "Combination Pliers", true)));

        assertEquals(AtfGherkinKeyword.GIVEN, checkpoint.keyword());
        assertEquals("product page is complete", checkpoint.title());
        assertEquals(1, checkpoint.assertions().size());
        assertThrows(UnsupportedOperationException.class,
                () -> checkpoint.assertions().clear());
    }
}
