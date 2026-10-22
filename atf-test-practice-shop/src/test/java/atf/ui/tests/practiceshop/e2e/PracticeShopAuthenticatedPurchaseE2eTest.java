package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.tests.practiceshop.support.PracticeShopAuthenticatedAccountTestSupport;
import atf.ui.tests.practiceshop.support.PracticeShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import atf.ui.tests.practiceshop.support.PracticeShopTestSupport;

/** One authenticated purchase proof: account lifecycle in SETUP, then the complete UI journey. */
@PracticeShopTestSupport.AuthenticatedE2e(
        accountSetup = PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN)
@PracticeShopTestSupport.AllowsRegistrationPerRun
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop authenticated purchase flow")
class PracticeShopAuthenticatedPurchaseE2eTest {
    @Test
    @DisplayName("Configured account lifecycle can complete a purchase")
    void configuredAccountCanCompletePurchase(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Authenticated purchase flow",
                "the selected account setup passes before the complete authenticated purchase begins",
                PracticeShopAuthenticatedAccountTestSupport::prepare, (journey, credentials) -> {
            new PracticeShopPurchaseFlow(journey.locators()).purchaseConfiguredProductAuthenticated(
                    journey.page(), journey.actions(), journey.configuration(),
                    journey::checkpoint, journey.apiNetwork());
            journey.completePurchase();
        });
    }
}
