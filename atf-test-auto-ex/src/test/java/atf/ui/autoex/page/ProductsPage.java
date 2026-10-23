package atf.ui.autoex.page;

import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.AutoExE2eRun;

/** Product search and catalog selection behavior. */
public final class ProductsPage extends AutoExPage {
    public ProductsPage(AutoExE2eRun run) { super(run); }

    public void search(String query) {
        fill(AutoExSelectors.SEARCH_INPUT, query, "search query is entered");
        click(AutoExSelectors.SEARCH_BUTTON, "catalog search is submitted");
    }

    public String firstProductName() {
        return text(AutoExSelectors.SEARCH_RESULT_PRODUCT_NAME, "first matching product name is read");
    }

    public ProductPage openFirstProduct() {
        clickFirst(AutoExSelectors.PRODUCT_DETAILS_LINK, "first matching product details open");
        return new ProductPage(run);
    }
}
