package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.tests.practiceshop.support.PracticeShopAuthenticatedAccountTestSupport;
import atf.ui.tests.practiceshop.support.PracticeShopE2eTestSupport;
import atf.ui.tests.practiceshop.support.PracticeShopTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** Creates a fresh synthetic account, proves login, then completes an authenticated purchase. */
@PracticeShopTestSupport.AuthenticatedE2e(
        accountSetup = PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN,
        registrationProfile = PracticeShopTestSupport.RegistrationProfileMode.SYNTHETIC)
@PracticeShopTestSupport.AllowsRegistrationPerRun
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Authenticated purchase flow")
class AuthenticatedPurchaseE2eTest {
    @Test
    @DisplayName("A newly registered and signed-in customer completes a purchase")
    void registeredAccountCanCompletePurchase(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Authenticated purchase flow",
                "a synthetic account is registered and signed in before the complete purchase journey",
                PracticeShopAuthenticatedAccountTestSupport::prepare, (journey, credentials) -> {
            var purchase = new PracticeShopPurchaseFlow(journey.locators())
                    .purchaseConfiguredProductAuthenticated(
                            journey.page(), journey.actions(), journey.configuration(),
                            journey::checkpoint, journey.apiNetwork());
            journey.completePurchase(purchase);
        });
    }
}
