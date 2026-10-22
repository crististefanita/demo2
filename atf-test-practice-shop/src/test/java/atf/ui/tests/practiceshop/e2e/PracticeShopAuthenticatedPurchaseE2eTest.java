package atf.ui.tests.practiceshop.e2e;

import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.flow.PracticeShopAccountFlow;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.support.PracticeShopPageValidator;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import atf.ui.tests.practiceshop.support.PracticeShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import atf.ui.tests.practiceshop.support.PracticeShopTestSupport;

/** One authenticated purchase proof: reusable login setup, then the complete UI journey. */
@PracticeShopTestSupport.AuthenticatedE2e
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop authenticated purchase flow")
class PracticeShopAuthenticatedPurchaseE2eTest {
    @Test
    @DisplayName("Reusable account can complete a purchase after API-backed login setup")
    void configuredAccountCanCompletePurchase(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Authenticated purchase flow",
                "the reusable account logs in, adds the configured product and completes payment", journey -> {
            var credentials = PracticeShopCredentials.load(journey.configuration().accountFile());
            new PracticeShopAccountFlow(journey.locators()).loginAndAssertAccount(
                    journey.page(), journey.actions(), journey.configuration(), credentials);
            var loginAssertions = journey.apiNetwork().loginConfirmed(journey.configuration().api(), credentials);
            journey.checkpoint(new PracticeShopPurchaseCheckpoint(AtfGherkinKeyword.GIVEN,
                            "reusable account login establishes the authenticated checkout precondition",
                            new PracticeShopPageValidator(journey.locators())
                                    .validate(journey.page(), PracticeShopAccountPage.class, "My account")
                                    .assertions())
                            .withAdditionalAssertions(loginAssertions)
                            .withApiEvidence(journey.apiNetwork().evidence(PracticeShopApiCheckpoint.LOGIN)));
            journey.authenticatedSessionEstablished();

            new PracticeShopPurchaseFlow(journey.locators()).purchaseConfiguredProductAuthenticated(
                    journey.page(), journey.actions(), journey.configuration(),
                    journey::checkpoint, journey.apiNetwork());
            journey.completeAuthenticatedPurchase();
        });
    }
}
