package atf.ui.tests.practiceshop.e2e;

import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.testkit.AtfE2eTest;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.flow.PracticeShopAccountFlow;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
import atf.ui.practiceshop.support.PracticeShopPageValidator;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** One authenticated purchase proof: reusable login setup, then the complete UI journey. */
@EnabledIf("isConfigured")
@AtfE2eTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop authenticated purchase flow")
class PracticeShopAuthenticatedPurchaseE2eTest {
    private PracticeShopConfiguration configuration;
    private PracticeShopCredentials credentials;
    private PracticeShopE2eContext context;
    private final PracticeShopLocators locators = new PracticeShopLocators();

    static boolean isConfigured() { return PracticeShopConfigurationLoader.load().accountConfigured(); }

    @BeforeAll
    void open() {
        configuration = PracticeShopConfigurationLoader.load();
        credentials = PracticeShopCredentials.load(configuration.accountFile());
        context = PracticeShopE2eContext.open(configuration);
    }

    @AfterAll
    void close() { if (context != null) context.close(); }

    @Test
    @DisplayName("Reusable account can complete a purchase after API-backed login setup")
    void configuredAccountCanCompletePurchase() {
        PlaywrightActionSequence actions = context.actions();
        PracticeShopE2eContext.Scenario scenario = context.scenario(
                "PracticeShopAuthenticatedPurchaseE2eTest",
                "Authenticated purchase flow",
                "the reusable account logs in, adds the configured product and completes payment");
        try (scenario) {
            new PracticeShopAccountFlow(locators).loginAndAssertAccount(
                    context.page(), actions, configuration, credentials);
            var loginAssertions = context.apiNetwork().loginConfirmed(credentials.displayName());
            scenario.checkpoint(context.page(), actions,
                    new PracticeShopPurchaseCheckpoint(AtfGherkinKeyword.GIVEN,
                            "reusable account login establishes the authenticated checkout precondition",
                            new PracticeShopPageValidator(locators)
                                    .validate(context.page(), PracticeShopAccountPage.class, "My account")
                                    .assertions())
                            .withAdditionalAssertions(loginAssertions)
                            .withApiEvidence(context.apiNetwork().loginEvidence()));

            new PracticeShopPurchaseFlow(locators).purchaseConfiguredProductAuthenticated(
                    context.page(), actions, configuration,
                    checkpoint -> scenario.checkpoint(context.page(), actions, checkpoint),
                    context.apiNetwork());
            context.runtimeDiagnostics().assertClean();
            scenario.complete();
        } catch (RuntimeException | AssertionError error) {
            scenario.fail(context.page(), actions, error);
            throw error;
        }
    }
}
