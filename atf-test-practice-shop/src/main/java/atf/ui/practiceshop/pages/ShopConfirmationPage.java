package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.ShopScreen;
import atf.ui.practiceshop.locators.ShopControl;
import atf.ui.practiceshop.locators.ShopLocators;
import atf.ui.practiceshop.support.ShopPageContract;
import atf.ui.practiceshop.routes.ShopUiRoutes;
import com.microsoft.playwright.Page;

/** Final payment confirmation boundary, kept separate from payment input. */
@ShopPageContract(screen = ShopScreen.CONFIRMATION, route = ShopUiRoutes.CHECKOUT,
        requiredControls = {ShopControl.ORDER_CONFIRMATION}, bodyMarker = "Thanks for your order!")
public final class ShopConfirmationPage {
    private final Page page;
    private final ShopLocators locators;
    private final PlaywrightActionSequence actions;

    public ShopConfirmationPage(Page page, ShopLocators locators,
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
