package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.ShopScreen;
import atf.ui.practiceshop.locators.ShopControl;
import atf.ui.practiceshop.locators.ShopLocators;
import atf.ui.practiceshop.support.ShopPageContract;
import atf.ui.practiceshop.routes.ShopUiRoutes;
import com.microsoft.playwright.Page;

/** Cart page object: product identity and quantity are checked before checkout. */
@ShopPageContract(screen = ShopScreen.CART, route = ShopUiRoutes.CHECKOUT,
        requiredControls = {ShopControl.CART_PRODUCT_TITLE,
                ShopControl.CART_PRODUCT_QUANTITY, ShopControl.CART_PROCEED})
public final class ShopCartPage {
    private final Page page;
    private final ShopLocators locators;
    private final PlaywrightActionSequence actions;

    public ShopCartPage(Page page, ShopLocators locators,
                                PlaywrightActionSequence actions) {
        this.page = page;
        this.locators = locators;
        this.actions = actions;
    }

    public void open(ShopConfiguration configuration) {
        actions.navigate(page, configuration.uri(ShopUiRoutes.CHECKOUT), "cart page is loaded");
        actions.waitForText(page, locators.pageBody(page), "CART", "cart step is displayed");
    }

    public CartLine assertConfiguredLine(ShopConfiguration configuration) {
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
