package atf.ui.practiceshop.flow;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.practiceshop.support.PracticeShopHomeScreenValidator;
import atf.ui.practiceshop.support.PracticeShopPageValidator;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import atf.ui.practiceshop.support.PracticeShopPurchaseScreenValidator;
import com.microsoft.playwright.Page;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Public facade for one complete Practice Shop purchase journey.
 * Catalog and checkout transitions are delegated to semantic stages so callers
 * keep the same API while each business boundary can be tested independently.
 */
public final class PracticeShopPurchaseFlow {
    private final PracticeShopLocators locators;
    private final PracticeShopPageValidator pageValidator;
    private final PracticeShopHomeScreenValidator homeScreenValidator;
    private final PracticeShopPurchaseScreenValidator purchaseScreenValidator;
    private final PracticeShopCatalogPurchaseStage catalogStage = new PracticeShopCatalogPurchaseStage();
    private final PracticeShopCheckoutPurchaseStage checkoutStage = new PracticeShopCheckoutPurchaseStage();

    public PracticeShopPurchaseFlow(PracticeShopLocators locators) {
        this.locators = Objects.requireNonNull(locators, "locators");
        this.pageValidator = new PracticeShopPageValidator(locators);
        this.homeScreenValidator = new PracticeShopHomeScreenValidator(locators);
        this.purchaseScreenValidator = new PracticeShopPurchaseScreenValidator(locators);
    }

    public PurchaseResult purchaseConfiguredProduct(Page page, PlaywrightActionSequence actions,
                                                    PracticeShopConfiguration configuration) {
        return purchaseConfiguredProduct(page, actions, configuration, ignored -> { });
    }

    /** Runs the journey while exposing immutable page checkpoints to any evidence sink. */
    public PurchaseResult purchaseConfiguredProduct(Page page, PlaywrightActionSequence actions,
                                                    PracticeShopConfiguration configuration,
                                                    Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver) {
        return execute(page, actions, configuration, checkpointObserver,
                new PracticeShopApiNetworkJournal(
                        atf.ui.practiceshop.config.PracticeShopValidationProfile.UI_ONLY), false);
    }

    /** Same journey after an authenticated setup; the sign-in checkout step is validated explicitly. */
    public PurchaseResult purchaseConfiguredProductAuthenticated(Page page, PlaywrightActionSequence actions,
                                                                  PracticeShopConfiguration configuration,
                                                                  Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver,
                                                                  PracticeShopApiNetworkJournal apiNetwork) {
        return execute(page, actions, configuration, checkpointObserver, apiNetwork, true);
    }

    /** Backward-compatible API-aware guest journey used by the existing E2E proof. */
    public PurchaseResult purchaseConfiguredProduct(Page page, PlaywrightActionSequence actions,
                                                    PracticeShopConfiguration configuration,
                                                    Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver,
                                                    PracticeShopApiNetworkJournal apiNetwork) {
        return execute(page, actions, configuration, checkpointObserver, apiNetwork, false);
    }

    private PurchaseResult execute(Page page, PlaywrightActionSequence actions,
                                   PracticeShopConfiguration configuration,
                                   Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver,
                                   PracticeShopApiNetworkJournal apiNetwork,
                                   boolean authenticated) {
        var context = new PracticeShopPurchaseJourneyContext(page, actions, configuration,
                checkpointObserver, apiNetwork, locators, pageValidator,
                homeScreenValidator, purchaseScreenValidator);
        catalogStage.prepareCart(context);
        return checkoutStage.complete(context, authenticated);
    }

    public record PurchaseResult(String productName, String quantity,
                                 String paymentMethod, String confirmation) {
    }
}
