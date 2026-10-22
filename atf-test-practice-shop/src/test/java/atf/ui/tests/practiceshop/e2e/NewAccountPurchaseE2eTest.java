package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.ShopPurchaseFlow;
import atf.ui.tests.practiceshop.support.ShopAuthenticatedAccountTestSupport;
import atf.ui.tests.practiceshop.support.ShopE2eTestSupport;
import atf.ui.tests.practiceshop.support.ShopTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** Creates a synthetic account, proves login, then completes one authenticated purchase. */
@ShopTestSupport.AuthenticatedE2e(
        accountSetup = ShopTestSupport.AccountSetupMode.REGISTER_PER_RUN,
        registrationProfile = ShopTestSupport.RegistrationProfileMode.SYNTHETIC)
@ShopTestSupport.AllowsRegistrationPerRun
@AtfE2eTest
@ShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@Order(1)
@DisplayName("New account authenticated purchase")
public class NewAccountPurchaseE2eTest {
    @Test
    @DisplayName("A newly registered and signed-in customer completes a purchase")
    void newAccountCanCompletePurchase(ShopE2eTestSupport.E2eHarness harness) {
        harness.run("New-account purchase flow",
                "a synthetic account is registered and signed in before the complete purchase journey",
                ShopAuthenticatedAccountTestSupport::prepare, (journey, credentials) -> {
                    var purchase = new ShopPurchaseFlow(journey.locators())
                            .purchaseConfiguredProductAuthenticated(
                            journey.page(), journey.actions(), journey.configuration(),
                            journey::checkpoint, journey.apiNetwork());
                    journey.completePurchase(purchase);
                });
    }
}
