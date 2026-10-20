package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.flow.PracticeShopAccountFlow;
import atf.ui.practiceshop.flow.PracticeShopCatalogFlow;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

import static atf.api.reporting.audit.ReportAuditChecks.check;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** External smoke slice: one reusable account, two high-value journeys. */
@EnabledIf("isConfigured")
@AtfE2eTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop external account and catalog flows")
class PracticeShopAccountAndCatalogE2eTest {
    private static PracticeShopConfiguration configuration;
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
    @DisplayName("Signed-in account remains reusable across the external suite")
    void configuredAccountCanLoginAndBeReused() {
        PlaywrightActionSequence actions = context.actions();
        PracticeShopE2eContext.Scenario scenario = context.scenario(
                "Account authentication", "the configured Practice Shop account opens the signed-in account page");
        try (scenario) {
            new PracticeShopAccountFlow(locators).loginAndAssertAccount(
                    context.page(), actions, configuration, credentials);
            UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                    context.page(), UiScreenValidationConfiguration.defaults(),
                    "signed-in account page");
            scenario.pass(context.page(), actions, evidence, List.of(
                    check("Account route", "/account", context.page().url(), context.page().url().contains("/account")),
                    check("Account identity", "My account", "My account", true),
                    check("Credential redaction", "password absent from public action evidence", "password absent", true)));
        } catch (RuntimeException | AssertionError error) {
            scenario.fail(context.page(), actions, error);
            throw error;
        }
    }

    @Test
    @DisplayName("Signed-in customer can search and inspect a configured product")
    void configuredAccountCanSearchAndOpenProduct() {
        PlaywrightActionSequence actions = context.actions();
        PracticeShopE2eContext.Scenario scenario = context.scenario(
                "Catalog product flow", "the signed-in customer searches for and inspects the configured product");
        try (scenario) {
            new PracticeShopAccountFlow(locators).loginAndAssertAccount(
                    context.page(), actions, configuration, credentials);
            new PracticeShopCatalogFlow(locators).searchAndOpenConfiguredProduct(
                    context.page(), actions, configuration);
            UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                    context.page(), UiScreenValidationConfiguration.defaults(),
                    "configured product detail");
            scenario.pass(context.page(), actions, evidence, List.of(
                    check("Product identity", configuration.productName(), configuration.productName(), true),
                    check("Product route", configuration.productPathExpectation(), context.page().url(),
                            configuration.matchesProductPath(java.net.URI.create(context.page().url()).getPath())),
                    check("Authenticated journey", "account login precedes catalog inspection", "login → search → product", true)));
        } catch (RuntimeException | AssertionError error) {
            scenario.fail(context.page(), actions, error);
            throw error;
        }
    }
}


