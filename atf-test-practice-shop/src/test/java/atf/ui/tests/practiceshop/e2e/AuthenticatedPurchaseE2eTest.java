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

/** Reuses a registered local account, proves login, then completes an authenticated purchase. */
@ShopTestSupport.AuthenticatedE2e(
        accountSetup = ShopTestSupport.AccountSetupMode.REUSE_FIXTURE,
        accountFile = "atf-test-practice-shop/src/test/resources/"
                + "practice-software-testing-account.local.properties")
@AtfE2eTest
@ShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@Order(2)
@DisplayName("Authenticated purchase flow")
public class AuthenticatedPurchaseE2eTest {
    @Test
    @DisplayName("An existing customer signs in and completes a purchase")
    void existingAccountCanCompletePurchase(ShopE2eTestSupport.E2eHarness harness) {
        harness.run("Existing-account purchase flow",
                "a registered local account signs in before the complete purchase journey",
                ShopAuthenticatedAccountTestSupport::prepare, (journey, credentials) -> {
            var purchase = new ShopPurchaseFlow(journey.locators())
                    .purchaseConfiguredProductAuthenticated(
                            journey.page(), journey.actions(), journey.configuration(),
                            journey::checkpoint, journey.apiNetwork());
            journey.completePurchase(purchase);
        });
    }
}
