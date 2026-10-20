package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import com.microsoft.playwright.Page;

/** Final payment confirmation boundary, kept separate from payment input. */
@PracticeShopPageContract(screen = PracticeShopScreen.CONFIRMATION, route = PracticeShopUiRoutes.CHECKOUT,
        requiredControls = {PracticeShopControl.PAYMENT_SUCCESS}, bodyMarker = "Payment was successful")
public final class PracticeShopConfirmationPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopConfirmationPage(Page page, PracticeShopLocators locators,
                                        PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public String assertLoaded() {
        actions.waitForText(page, locators.paymentSuccess(page), "Payment was successful",
                "payment confirmation is displayed");
        return actions.expectText(locators.paymentSuccess(page), "Payment was successful");
    }
}
