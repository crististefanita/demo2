package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.flow.PracticeShopAccountFlow;
import atf.ui.practiceshop.flow.PracticeShopCatalogFlow;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.pages.PracticeShopProductPage;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
import atf.ui.practiceshop.support.PracticeShopPageValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import atf.ui.tests.practiceshop.support.PracticeShopTestSupport;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** External smoke slice: one reusable account, two high-value journeys. */
@PracticeShopTestSupport.RequiresAuthenticatedFixture
@AtfE2eTest
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop external account and catalog flows")
class PracticeShopAccountAndCatalogE2eTest {
    private PracticeShopConfiguration configuration;
    private PracticeShopCredentials credentials;
    private PracticeShopE2eContext context;
    private PracticeShopTestSupport.E2eHarness harness;
    private final PracticeShopLocators locators = new PracticeShopLocators();

    @BeforeEach
    void open() {
        harness = PracticeShopTestSupport.openE2e();
        configuration = harness.configuration();
        credentials = PracticeShopCredentials.load(configuration.accountFile());
        context = harness.context();
    }

    @AfterEach
    void close() { if (harness != null) harness.close(); }

    @Test
    @DisplayName("Signed-in account remains reusable across the external suite")
    void configuredAccountCanLoginAndBeReused() {
        PlaywrightActionSequence actions = context.actions();
        PracticeShopE2eContext.Scenario scenario = context.scenario(
                "PracticeShopAccountAndCatalogE2eTest",
                "Account authentication", "the configured Practice Shop account opens the signed-in account page");
        scenario.execute(context.page(), actions, () -> {
            new PracticeShopAccountFlow(locators).loginAndAssertAccount(
                    context.page(), actions, configuration, credentials);
            context.runtimeDiagnostics().markAuthenticatedSetup();
            var assertions = new PracticeShopPageValidator(locators)
                    .validate(context.page(), PracticeShopAccountPage.class)
                    .assertions();
            scenario.pass(context.page(), actions, "signed-in account page", assertions);
        });
    }

    @Test
    @DisplayName("Signed-in customer can search and inspect a configured product")
    void configuredAccountCanSearchAndOpenProduct() {
        PlaywrightActionSequence actions = context.actions();
        PracticeShopE2eContext.Scenario scenario = context.scenario(
                "PracticeShopAccountAndCatalogE2eTest",
                "Catalog product flow", "the signed-in customer searches for and inspects the configured product");
        scenario.execute(context.page(), actions, () -> {
            new PracticeShopAccountFlow(locators).loginAndAssertAccount(
                    context.page(), actions, configuration, credentials);
            context.runtimeDiagnostics().markAuthenticatedSetup();
            new PracticeShopCatalogFlow(locators).searchAndOpenConfiguredProduct(
                    context.page(), actions, configuration);
            var assertions = new java.util.ArrayList<>(new PracticeShopPageValidator(locators)
                    .validate(context.page(), PracticeShopProductPage.class, configuration.productName())
                    .assertions());
            assertions.add(check("Product route", configuration.productPathExpectation(), context.page().url(),
                    configuration.matchesProductPath(java.net.URI.create(context.page().url()).getPath())));
            scenario.pass(context.page(), actions, "configured product detail", assertions);
        });
    }
}


