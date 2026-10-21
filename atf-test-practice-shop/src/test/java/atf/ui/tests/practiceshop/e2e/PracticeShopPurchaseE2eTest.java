package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.tests.practiceshop.support.PracticeShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** One complete, guest-safe purchase proof; no account secret is required. */
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop complete purchase flow")
class PracticeShopPurchaseE2eTest {
    @Test
    @DisplayName("Configured guest can complete a demo purchase")
    void configuredGuestCanCompletePurchase(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Purchase flow",
                "the configured guest adds a product, completes billing and confirms payment", journey -> {
                    new PracticeShopPurchaseFlow(journey.locators()).purchaseConfiguredProduct(
                            journey.page(), journey.actions(), journey.configuration(),
                            journey::checkpoint, journey.apiNetwork());
                    journey.completeGuestPurchase();
                });
    }
}
