package atf.ui.tests.practiceshop.e2e;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.tests.practiceshop.support.PracticeShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

/** One consolidated UI-only business proposition, action table and final screenshot. */
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness(
        profile = PracticeShopE2eTestSupport.HarnessProfile.UI_ONLY,
        reportFlow = PracticeShopE2eTestSupport.ReportFlow.UNIFIED_FLOW,
        screenshots = PracticeShopE2eTestSupport.ScreenshotMode.FINAL_ONLY)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop UI-only single-flow purchase")
class PracticeShopPurchaseUiOnlySingleFlowE2eTest {
    @Test
    @DisplayName("Configured guest completes one auditable UI purchase flow")
    void configuredGuestCompletesOneUiOnlyFlow(
            PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("UI-only purchase — one flow",
                "the configured guest completes checkout using browser actions only", journey -> {
                    var result = new PracticeShopPurchaseFlow(journey.locators())
                            .purchaseConfiguredProduct(journey.page(), journey.actions(),
                                    journey.configuration(), journey::checkpoint);
                    journey.pass("final order confirmation",
                            List.of(new AtfAssertion("Order confirmation",
                                    "confirmation includes a successful order and invoice",
                                    result.confirmation(),
                                    result.confirmation().contains("Thanks for your order!")
                                            && result.confirmation().contains("INV-"))));
                });
    }
}
