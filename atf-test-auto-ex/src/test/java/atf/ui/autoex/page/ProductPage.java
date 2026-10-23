package atf.ui.autoex.page;

import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.AutoExE2eRun;

/** Product detail and add-to-cart actions. */
public final class ProductPage extends AutoExPage {
    public ProductPage(AutoExE2eRun run) { super(run); }

    public String details() {
        return text(AutoExSelectors.PRODUCT_INFORMATION,
                "product name, price and availability are read");
    }

    public String addConfirmation() {
        return text("#cartModal", "cart confirms that the selected product was added");
    }

    public void addQuantityToCart(int quantity) {
        fill(AutoExSelectors.QUANTITY, Integer.toString(quantity), "requested quantity is entered");
        click(AutoExSelectors.ADD_TO_CART, "product is added to the cart");
    }

    public CartPage viewCartFromConfirmation() {
        clickLast(AutoExSelectors.VIEW_CART, "cart opens from the added-product confirmation");
        return new CartPage(run);
    }
}
