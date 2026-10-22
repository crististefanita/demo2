package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.ShopScreen;
import atf.ui.practiceshop.locators.ShopControl;
import atf.ui.practiceshop.locators.ShopLocators;
import atf.ui.practiceshop.support.ShopPageContract;
import atf.ui.practiceshop.routes.ShopUiRoutes;
import com.microsoft.playwright.Page;

/** Product detail page object. */
@ShopPageContract(screen = ShopScreen.PRODUCT, route = ShopUiRoutes.PRODUCT_PATTERN,
        requiredControls = {ShopControl.PRODUCT_NAME, ShopControl.PRODUCT_PRICE,
                ShopControl.PRODUCT_DESCRIPTION, ShopControl.PRODUCT_QUANTITY,
                ShopControl.ADD_TO_CART})
public final class ShopProductPage {
    private final Page page;
    private final ShopLocators locators;
    private final PlaywrightActionSequence actions;

    public ShopProductPage(Page page, ShopLocators locators,
                                   PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public String assertLoaded(ShopConfiguration configuration) {
        actions.waitForVisible(locators.productName(page));
        String productName = actions.expectText(locators.productName(page), configuration.productName());
        actions.assertVisible(locators.productPrice(page));
        actions.assertVisible(locators.productDescription(page));
        actions.assertVisible(locators.productQuantity(page));
        actions.waitForEnabled(page, locators.productQuantity(page),
                "product quantity is enabled after product data is ready");
        actions.assertVisible(locators.addToCart(page));
        return productName;
    }

    public void addConfiguredQuantity(ShopConfiguration configuration) {
        actions.fill(locators.productQuantity(page), Integer.toString(configuration.checkout().quantity()),
                "configured product quantity is set");
        actions.click(locators.addToCart(page), "configured product is added to the cart");
        actions.waitForText(page, locators.pageBody(page), "Product added to shopping cart",
                "cart addition is acknowledged by the shop");
    }
}


