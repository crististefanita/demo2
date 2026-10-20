package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import com.microsoft.playwright.Page;

/** Cart page object: product identity and quantity are checked before checkout. */
@PracticeShopPageContract(screen = PracticeShopScreen.CART, route = PracticeShopUiRoutes.CHECKOUT,
        requiredControls = {PracticeShopControl.CART_PRODUCT_TITLE,
                PracticeShopControl.CART_PRODUCT_QUANTITY, PracticeShopControl.CART_PROCEED})
public final class PracticeShopCartPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopCartPage(Page page, PracticeShopLocators locators,
                                PlaywrightActionSequence actions) {
        this.page = page;
        this.locators = locators;
        this.actions = actions;
    }

    public void open(PracticeShopConfiguration configuration) {
        actions.navigate(page, configuration.uri(PracticeShopUiRoutes.CHECKOUT), "cart page is loaded");
        actions.waitForText(page, locators.pageBody(page), "CART", "cart step is displayed");
    }

    public CartLine assertConfiguredLine(PracticeShopConfiguration configuration) {
        String productName = actions.expectText(locators.cartProductTitle(page), configuration.productName());
        String quantity = actions.expectValue(locators.cartProductQuantity(page),
                Integer.toString(configuration.checkout().quantity()));
        return new CartLine(productName, quantity);
    }

    public void proceed() {
        actions.click(locators.cartProceed(page), "cart proceeds to checkout identity");
    }

    public record CartLine(String productName, String quantity) {
    }
}
