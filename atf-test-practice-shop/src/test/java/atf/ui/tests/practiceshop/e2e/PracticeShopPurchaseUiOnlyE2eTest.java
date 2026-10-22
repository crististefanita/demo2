package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.tests.practiceshop.support.PracticeShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** Config-driven UI-only proof of the complete guest purchase journey. */
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness(
        profile = PracticeShopE2eTestSupport.HarnessProfile.UI_ONLY)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop UI-only purchase flow")
class PracticeShopPurchaseUiOnlyE2eTest {
    @Test
    @DisplayName("Configured guest completes a purchase using UI evidence only")
    void configuredGuestCanCompletePurchaseWithUiEvidenceOnly(
            PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("UI-only purchase flow",
                "the configured guest completes product selection, billing and payment through the UI",
                journey -> {
                    new PracticeShopPurchaseFlow(journey.locators()).purchaseConfiguredProduct(
                            journey.page(), journey.actions(), journey.configuration(), journey::checkpoint);
                    journey.completeGuestPurchase();
                });
    }
}
