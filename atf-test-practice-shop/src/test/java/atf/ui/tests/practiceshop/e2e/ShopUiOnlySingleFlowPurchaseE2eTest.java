package atf.ui.tests.practiceshop.e2e;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.testkit.AtfE2eTest;
import atf.api.testkit.channel.AtfTestChannel;
import atf.api.testkit.channel.TestChannel;
import atf.ui.practiceshop.flow.ShopPurchaseFlow;
import atf.ui.practiceshop.support.ShopPurchaseTraceStep;
import atf.ui.tests.practiceshop.support.ShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

/** One consolidated UI-only business proposition, action table and final screenshot. */
@AtfE2eTest
@AtfTestChannel(TestChannel.UI)
@ShopE2eTestSupport.WithE2eHarness(
        reportFlow = ShopE2eTestSupport.ReportFlow.UNIFIED_FLOW)
@Execution(ExecutionMode.SAME_THREAD)
@Order(5)
@DisplayName("Guest UI-only single-flow purchase")
public class ShopUiOnlySingleFlowPurchaseE2eTest {
    @Test
    @DisplayName("Configured guest completes one auditable UI purchase flow")
    void configuredGuestCompletesOneUiOnlyFlow(
            ShopE2eTestSupport.E2eHarness harness) {
        harness.run("UI-only purchase — one flow",
                "the configured guest completes checkout using browser actions only", journey -> {
                    var result = new ShopPurchaseFlow(journey.locators())
                            .purchaseConfiguredProduct(journey.page(), journey.actions(),
                                    journey.configuration(), journey::checkpoint);
                    journey.pass(ShopPurchaseTraceStep.ORDER_CONFIRMATION,
                            List.of(new AtfAssertion("Order confirmation",
                                    "confirmation includes a successful order and invoice",
                                    result.confirmation(),
                                    result.confirmation().contains("Thanks for your order!")
                                            && result.confirmation().contains("INV-"))));
                });
    }
}
