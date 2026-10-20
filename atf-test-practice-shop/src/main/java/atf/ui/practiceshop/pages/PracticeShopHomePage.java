package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import com.microsoft.playwright.Page;

/** Catalog landing page object. */
@PracticeShopPageContract(screen = PracticeShopScreen.HOME, route = "/",
        requiredControls = {PracticeShopControl.HOME_NAVIGATION, PracticeShopControl.HOME_BRAND,
                PracticeShopControl.HOME_BANNER, PracticeShopControl.HOME_SORT,
                PracticeShopControl.HOME_PRICE_RANGE, PracticeShopControl.SEARCH_QUERY,
                PracticeShopControl.HOME_SEARCH_RESET, PracticeShopControl.HOME_SEARCH_SUBMIT,
                PracticeShopControl.HOME_CATEGORY_FILTERS, PracticeShopControl.HOME_BRAND_FILTERS,
                PracticeShopControl.HOME_ECO_FILTER, PracticeShopControl.HOME_PRODUCT_GRID,
                PracticeShopControl.HOME_PRODUCT_NAMES, PracticeShopControl.HOME_PRODUCT_IMAGES,
                PracticeShopControl.HOME_PAGINATION, PracticeShopControl.HOME_FOOTER})
public final class PracticeShopHomePage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopHomePage(Page page, PracticeShopLocators locators,
                                PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public void open(PracticeShopConfiguration configuration) {
        actions.navigate(page, configuration.uri("/"), "catalog home is loaded");
        actions.waitForVisible(locators.searchQuery(page));
        actions.waitForVisible(locators.homeProductGridFirst(page));
    }

    public void search(String productName) {
        var searchQuery = locators.searchQuery(page);
        actions.fill(searchQuery, productName, "product search term is entered");
        actions.pressKey(page, "Enter", searchQuery, "catalog search is submitted");
    }
}


