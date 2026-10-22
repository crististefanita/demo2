package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.flow.PracticeShopCatalogFlow;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.pages.PracticeShopProductPage;
import atf.ui.tests.practiceshop.support.PracticeShopAuthenticatedAccountTestSupport;
import atf.ui.practiceshop.support.PracticeShopPageValidator;
import atf.ui.tests.practiceshop.support.PracticeShopE2eTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import atf.ui.tests.practiceshop.support.PracticeShopTestSupport;

import java.net.URI;
import java.util.ArrayList;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** External smoke slice: one run-created account, two high-value journeys. */
@PracticeShopTestSupport.AuthenticatedE2e(
        accountSetup = PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN)
@PracticeShopTestSupport.AllowsRegistrationPerRun
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop external account and catalog flows")
class PracticeShopAccountAndCatalogE2eTest {
    @Test
    @DisplayName("Run-created account can sign in and expose the account page")
    void registeredAccountCanLoginAndBeReused(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Account authentication",
                "a run-scoped account is registered and authenticated before account assertions",
                PracticeShopAuthenticatedAccountTestSupport::prepare, (journey, credentials) -> {
            var assertions = new PracticeShopPageValidator(journey.locators())
                    .validate(journey.page(), PracticeShopAccountPage.class)
                    .assertions();
            journey.pass("signed-in account page", assertions);
        });
    }

    @Test
    @DisplayName("Run-created account can search and inspect the configured product")
    void registeredAccountCanSearchAndOpenProduct(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Catalog product flow",
                "a run-scoped account is registered and authenticated before catalog actions",
                PracticeShopAuthenticatedAccountTestSupport::prepare, (journey, credentials) -> {
            new PracticeShopCatalogFlow(journey.locators()).searchAndOpenConfiguredProduct(
                    journey.page(), journey.actions(), journey.configuration());
            var assertions = new ArrayList<>(new PracticeShopPageValidator(journey.locators())
                    .validate(journey.page(), PracticeShopProductPage.class, journey.configuration().productName())
                    .assertions());
            String actualUrl = journey.page().url();
            assertions.add(check("Product route", journey.configuration().productPathExpectation(), actualUrl,
                    journey.configuration().matchesProductPath(URI.create(actualUrl).getPath())));
            journey.pass("configured product detail", assertions);
        });
    }
}


