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
        requiredControls = {PracticeShopControl.ORDER_CONFIRMATION}, bodyMarker = "Thanks for your order!")
public final class PracticeShopConfirmationPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopConfirmationPage(Page page, PracticeShopLocators locators,
                                        PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public String confirmOrder() {
        actions.click(locators.confirmOrder(page), "demo order is submitted");
        actions.waitForText(page, locators.orderConfirmation(page), "Thanks for your order!",
                "completed order confirmation is displayed");
        return actions.expectText(locators.orderConfirmation(page), "Thanks for your order!");
    }
}
