package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** One complete, guest-safe purchase proof; no account secret is required. */
@AtfE2eTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop complete purchase flow")
class PracticeShopPurchaseE2eTest {
    private PracticeShopConfiguration configuration;
    private PracticeShopE2eContext context;
    private final PracticeShopLocators locators = new PracticeShopLocators();

    @BeforeAll
    void open() {
        configuration = PracticeShopConfigurationLoader.load();
        context = PracticeShopE2eContext.open(configuration);
    }

    @AfterAll
    void close() {
        if (context != null) context.close();
    }

    @Test
    @DisplayName("Configured guest can complete a demo purchase")
    void configuredGuestCanCompletePurchase() {
        PlaywrightActionSequence actions = context.actions();
        PracticeShopE2eContext.Scenario scenario = context.scenario(
                "PracticeShopPurchaseE2eTest",
                "Purchase flow", "the configured guest adds a product, completes billing and confirms payment");
        scenario.execute(context.page(), actions, () -> {
            new PracticeShopPurchaseFlow(locators).purchaseConfiguredProduct(
                    context.page(), actions, configuration,
                    checkpoint -> scenario.checkpoint(context.page(), actions, checkpoint),
                    context.apiNetwork());
            var guestWarnings = context.runtimeDiagnostics().expectedGuestWarnings();
            context.runtimeDiagnostics().assertClean();
            if (!guestWarnings.isEmpty()) {
                scenario.warning("known guest API/runtime branch is visible as a warning",
                        context.runtimeDiagnostics().expectedGuestWarningTarget(), guestWarnings.stream()
                                .map(message -> new AtfAssertion("Guest runtime signal", "expected guest branch",
                                        message, true)).toList());
            }
            scenario.complete(guestWarnings.isEmpty() ? AtfReportStatus.PASS : AtfReportStatus.WARNING);
        });
    }
}
