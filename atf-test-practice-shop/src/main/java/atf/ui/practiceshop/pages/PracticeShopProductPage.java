package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import com.microsoft.playwright.Page;

/** Product detail page object. */
@PracticeShopPageContract(screen = PracticeShopScreen.PRODUCT, route = "/product/*",
        requiredControls = {PracticeShopControl.PRODUCT_NAME, PracticeShopControl.PRODUCT_PRICE,
                PracticeShopControl.PRODUCT_DESCRIPTION, PracticeShopControl.PRODUCT_QUANTITY,
                PracticeShopControl.ADD_TO_CART})
public final class PracticeShopProductPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopProductPage(Page page, PracticeShopLocators locators,
                                   PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public String assertLoaded(PracticeShopConfiguration configuration) {
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

    public void addConfiguredQuantity(PracticeShopConfiguration configuration) {
        actions.fill(locators.productQuantity(page), Integer.toString(configuration.checkout().quantity()),
                "configured product quantity is set");
        actions.click(locators.addToCart(page), "configured product is added to the cart");
        actions.waitForText(page, locators.pageBody(page), "Product added to shopping cart",
                "cart addition is acknowledged by the shop");
    }
}


