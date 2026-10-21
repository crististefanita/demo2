package atf.ui.practiceshop.flow;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.ui.playwright.AtfLocator;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.practiceshop.support.PracticeShopHomeScreenValidator;
import atf.ui.practiceshop.support.PracticeShopPageValidationResult;
import atf.ui.practiceshop.support.PracticeShopPageValidator;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import atf.ui.practiceshop.support.PracticeShopPurchaseTraceStep;
import atf.ui.practiceshop.support.PracticeShopPurchaseScreenValidator;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Immutable composition boundary shared by the semantic purchase stages.
 * It owns reporting mechanics, while the stages own business transitions.
 */
final class PracticeShopPurchaseJourneyContext {
    private final Page page;
    private final PlaywrightActionSequence actions;
    private final PracticeShopConfiguration configuration;
    private final Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver;
    private final PracticeShopApiNetworkJournal apiNetwork;
    private final PracticeShopLocators locators;
    private final PracticeShopPageValidator pageValidator;
    private final PracticeShopHomeScreenValidator homeScreenValidator;
    private final PracticeShopPurchaseScreenValidator purchaseScreenValidator;

    PracticeShopPurchaseJourneyContext(Page page, PlaywrightActionSequence actions,
                                       PracticeShopConfiguration configuration,
                                       Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver,
                                       PracticeShopApiNetworkJournal apiNetwork,
                                       PracticeShopLocators locators,
                                       PracticeShopPageValidator pageValidator,
                                       PracticeShopHomeScreenValidator homeScreenValidator,
                                       PracticeShopPurchaseScreenValidator purchaseScreenValidator) {
        this.page = Objects.requireNonNull(page, "page");
        this.actions = Objects.requireNonNull(actions, "actions");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.checkpointObserver = Objects.requireNonNull(checkpointObserver, "checkpointObserver");
        this.apiNetwork = Objects.requireNonNull(apiNetwork, "apiNetwork");
        this.locators = Objects.requireNonNull(locators, "locators");
        this.pageValidator = Objects.requireNonNull(pageValidator, "pageValidator");
        this.homeScreenValidator = Objects.requireNonNull(homeScreenValidator, "homeScreenValidator");
        this.purchaseScreenValidator = Objects.requireNonNull(purchaseScreenValidator, "purchaseScreenValidator");
    }

    Page page() { return page; }
    PlaywrightActionSequence actions() { return actions; }
    PracticeShopConfiguration configuration() { return configuration; }
    PracticeShopApiNetworkJournal apiNetwork() { return apiNetwork; }
    PracticeShopLocators locators() { return locators; }
    PracticeShopPageValidator pageValidator() { return pageValidator; }
    PracticeShopHomeScreenValidator homeScreenValidator() { return homeScreenValidator; }
    PracticeShopPurchaseScreenValidator purchaseScreenValidator() { return purchaseScreenValidator; }

    PracticeShopPurchaseCheckpoint checkpoint(AtfGherkinKeyword keyword, String title,
                                              PracticeShopPageValidationResult validation,
                                              List<AtfAssertion> additional,
                                              List<ReportExecutionEvidence> api) {
        return new PracticeShopPurchaseCheckpoint(keyword, title, validation.assertions())
                .withAdditionalAssertions(additional)
                .withApiEvidence(api);
    }

    PracticeShopPurchaseCheckpoint checkpoint(PracticeShopPurchaseTraceStep step,
                                              PracticeShopPageValidationResult validation,
                                              List<AtfAssertion> additional,
                                              List<ReportExecutionEvidence> api) {
        return checkpoint(step.keyword(), step.title(), validation, additional, api);
    }

    void publish(PracticeShopPurchaseCheckpoint checkpoint) {
        checkpointObserver.accept(checkpoint);
        if (!checkpoint.passed()) {
            throw new IllegalStateException("Page validation failed: " + checkpoint.title());
        }
    }

    void assertVisible(AtfLocator... controls) {
        for (AtfLocator control : controls) {
            actions.assertVisible(new AtfLocator(control.locator().first(), control.selector()));
        }
    }
}
