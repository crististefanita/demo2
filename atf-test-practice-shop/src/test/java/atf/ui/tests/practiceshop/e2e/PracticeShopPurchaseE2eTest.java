package atf.ui.tests.practiceshop.e2e;

import atf.api.testkit.AtfE2eTest;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopE2eContext;
import atf.ui.practiceshop.support.PracticeShopRuntimeDiagnostics;
import atf.ui.tests.practiceshop.support.PracticeShopTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** One complete, guest-safe purchase proof; no account secret is required. */
@AtfE2eTest
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Practice Shop complete purchase flow")
class PracticeShopPurchaseE2eTest {
    private PracticeShopConfiguration configuration;
    private PracticeShopE2eContext context;
    private PracticeShopTestSupport.E2eHarness harness;
    private final PracticeShopLocators locators = new PracticeShopLocators();

    @BeforeEach
    void open() {
        harness = PracticeShopTestSupport.openE2e();
        configuration = harness.configuration();
        context = harness.context();
    }

    @AfterEach
    void close() {
        if (harness != null) harness.close();
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
            var guestDiagnostics = context.runtimeDiagnostics().structuredDiagnostics().stream()
                    .filter(PracticeShopRuntimeDiagnostics::isExpectedGuestSignal)
                    .toList();
            if (!guestDiagnostics.isEmpty()) {
                String warningTarget = context.runtimeDiagnostics().expectedGuestWarningTarget();
                scenario.warningDiagnostics("known guest API/runtime branch is visible as a warning",
                        warningTarget, PracticeShopRuntimeDiagnostics.toNetworkDiagnostics(guestDiagnostics));
            }
            scenario.complete(guestWarnings.isEmpty() ? AtfReportStatus.PASS : AtfReportStatus.WARNING);
        });
    }
}
