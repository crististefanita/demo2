package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.ShopPurchaseFlow;
import atf.ui.tests.practiceshop.support.ShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** One complete, guest-safe purchase proof; no account secret is required. */
@AtfE2eTest
@ShopE2eTestSupport.WithE2eHarness(
        profile = ShopE2eTestSupport.HarnessProfile.UI_API)
@Execution(ExecutionMode.SAME_THREAD)
@Order(3)
@DisplayName("Guest purchase flow with UI and API evidence")
public class PurchaseE2eTest {
    @Test
    @DisplayName("Configured guest can complete a demo purchase")
    void configuredGuestCanCompletePurchase(ShopE2eTestSupport.E2eHarness harness) {
        harness.run("Purchase flow",
                "the configured guest adds a product, completes billing and confirms the order", journey -> {
                    new ShopPurchaseFlow(journey.locators()).purchaseConfiguredProduct(
                            journey.page(), journey.actions(), journey.configuration(),
                            journey::checkpoint, journey.apiNetwork());
                    journey.completePurchase();
                });
    }
}
