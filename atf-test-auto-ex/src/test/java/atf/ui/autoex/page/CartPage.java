package atf.ui.autoex.page;

import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.AutoExE2eRun;

/** Cart rows and checkout transition. */
public final class CartPage extends AutoExPage {
    public CartPage(AutoExE2eRun run) { super(run); }

    public void openFromHeader() { click(AutoExSelectors.CART_LINK, "cart page opens"); }

    public String productName(AutoExConfiguration.Catalog product) {
        return text(row(product) + " h4", "cart product name is read");
    }

    public String productPrice(AutoExConfiguration.Catalog product) {
        return text(row(product) + " " + AutoExSelectors.CART_PRICE, "cart unit price is read");
    }

    public String quantity(AutoExConfiguration.Catalog product) {
        return text(row(product) + " " + AutoExSelectors.CART_QUANTITY, "cart quantity is read");
    }

    public String total(AutoExConfiguration.Catalog product) {
        return text(row(product) + " " + AutoExSelectors.CART_TOTAL, "cart line total is read");
    }

    public void proceedToCheckout() { click(AutoExSelectors.CHECKOUT, "checkout begins from the cart"); }

    private static String row(AutoExConfiguration.Catalog product) {
        return AutoExSelectors.CART_PRODUCT_ROW_PREFIX + product.productId();
    }
}
