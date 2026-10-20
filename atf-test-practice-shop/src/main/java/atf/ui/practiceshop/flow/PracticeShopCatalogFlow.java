package atf.ui.practiceshop.flow;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.pages.PracticeShopHomePage;
import atf.ui.practiceshop.pages.PracticeShopProductPage;
import com.microsoft.playwright.Page;

/** Catalog flow: deterministic search followed by product contract checks. */
public final class PracticeShopCatalogFlow {
    private final PracticeShopLocators locators;

    public PracticeShopCatalogFlow(PracticeShopLocators locators) {
        this.locators = locators;
    }

    public void searchAndOpenConfiguredProduct(Page page, PlaywrightActionSequence actions,
                                                PracticeShopConfiguration configuration) {
        PracticeShopHomePage home = new PracticeShopHomePage(page, locators, actions);
        home.open(configuration);
        home.search(configuration.productName());
        atf.api.ui.playwright.AtfLocator configuredProduct = configuration.usesDynamicProductPath()
                ? locators.productLinkFirst(page, configuration.productName())
                : locators.productLinkByPath(page, configuration.productPath());
        boolean configuredPathFound = configuration.usesDynamicProductPath()
                || configuredProduct.locator().count() > 0;
        atf.api.ui.playwright.AtfLocator product = configuredProduct;
        String selectedPath = product.locator().getAttribute("href");
        if (!configuredPathFound || !configuration.matchesProductPath(selectedPath)) {
            throw new IllegalStateException("Configured productPath '" + configuration.productPath()
                    + "' was not found for product '" + configuration.productName()
                    + "'; selected route='" + selectedPath + "'");
        }
        actions.waitForVisible(product);
        actions.click(product, "matching product opens from search results");
        new PracticeShopProductPage(page, locators, actions).assertLoaded(configuration);
    }
}


