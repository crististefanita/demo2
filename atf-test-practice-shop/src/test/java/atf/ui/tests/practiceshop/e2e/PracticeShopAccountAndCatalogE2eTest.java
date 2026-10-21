package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.flow.PracticeShopAccountFlow;
import atf.ui.practiceshop.flow.PracticeShopCatalogFlow;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.pages.PracticeShopProductPage;
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

/** External smoke slice: one reusable account, two high-value journeys. */
@PracticeShopTestSupport.RequiresAuthenticatedFixture
@AtfE2eTest
@PracticeShopE2eTestSupport.WithE2eHarness
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop external account and catalog flows")
class PracticeShopAccountAndCatalogE2eTest {
    @Test
    @DisplayName("Signed-in account remains reusable across the external suite")
    void configuredAccountCanLoginAndBeReused(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Account authentication",
                "the configured Practice Shop account opens the signed-in account page", journey -> {
            var credentials = PracticeShopCredentials.load(journey.configuration().accountFile());
            new PracticeShopAccountFlow(journey.locators()).loginAndAssertAccount(
                    journey.page(), journey.actions(), journey.configuration(), credentials);
            journey.authenticatedSessionEstablished();
            var assertions = new PracticeShopPageValidator(journey.locators())
                    .validate(journey.page(), PracticeShopAccountPage.class)
                    .assertions();
            journey.pass("signed-in account page", assertions);
        });
    }

    @Test
    @DisplayName("Signed-in customer can search and inspect a configured product")
    void configuredAccountCanSearchAndOpenProduct(PracticeShopE2eTestSupport.E2eHarness harness) {
        harness.run("Catalog product flow",
                "the signed-in customer searches for and inspects the configured product", journey -> {
            var credentials = PracticeShopCredentials.load(journey.configuration().accountFile());
            new PracticeShopAccountFlow(journey.locators()).loginAndAssertAccount(
                    journey.page(), journey.actions(), journey.configuration(), credentials);
            journey.authenticatedSessionEstablished();
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


