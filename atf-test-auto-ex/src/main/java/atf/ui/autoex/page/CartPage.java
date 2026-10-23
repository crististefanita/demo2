package atf.ui.autoex.page;

import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.checkout.AutoExOrderLine;
import atf.ui.autoex.support.journey.AutoExPurchaseSession;

/** Cart rows and checkout transition. */
public final class CartPage extends AutoExPage {
    public CartPage(AutoExPurchaseSession run) { super(run); }

    public void openFromHeader() { click(AutoExSelectors.CART_LINK, "cart page opens"); }

    public AutoExOrderLine orderLine(AutoExConfiguration.Catalog product) {
        return new AutoExOrderLine(productName(product), productPrice(product), quantity(product), total(product));
    }

    public String productName(AutoExConfiguration.Catalog product) {
        return text(row(product) + " " + AutoExSelectors.CART_PRODUCT_NAME, "cart product name is read");
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

    public String removeProductAndReadEmptyCart(AutoExConfiguration.Catalog product) {
        click(row(product) + " " + AutoExSelectors.CART_REMOVE, "selected product is removed from the cart");
        run.waitForText(AutoExSelectors.EMPTY_CART, "Cart is empty!",
                "cart confirms that its last product was removed");
        return text(AutoExSelectors.EMPTY_CART, "empty-cart confirmation is read");
    }

    private static String row(AutoExConfiguration.Catalog product) {
        return AutoExSelectors.CART_PRODUCT_ROW_PREFIX + product.productId();
    }
}
