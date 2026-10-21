package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.Objects;

/** Component validation slices for the non-home screens of the purchase journey. */
public final class PracticeShopPurchaseScreenValidator {
    private final PracticeShopComponentAssertions components;

    public PracticeShopPurchaseScreenValidator(PracticeShopLocators locators) {
        this.components = new PracticeShopComponentAssertions(Objects.requireNonNull(locators, "locators"));
    }

    public List<AtfAssertion> product(Page page) {
        return concat(
                components.rendered(page, PracticeShopControl.PRODUCT_NAME, PracticeShopControl.PRODUCT_PRICE,
                        PracticeShopControl.PRODUCT_DESCRIPTION, PracticeShopControl.PRODUCT_QUANTITY,
                        PracticeShopControl.ADD_TO_CART),
                components.interactive(page, PracticeShopControl.PRODUCT_QUANTITY, PracticeShopControl.ADD_TO_CART));
    }

    public List<AtfAssertion> cart(Page page) {
        return concat(
                components.rendered(page, PracticeShopControl.CART_PRODUCT_TITLE,
                        PracticeShopControl.CART_PRODUCT_QUANTITY, PracticeShopControl.CART_PROCEED),
                components.interactive(page, PracticeShopControl.CART_PRODUCT_QUANTITY,
                        PracticeShopControl.CART_PROCEED));
    }

    public List<AtfAssertion> authenticatedCheckout(Page page) {
        return concat(
                List.of(components.rendered(page, PracticeShopControl.AUTHENTICATED_PROCEED)),
                List.of(components.interactive(page, PracticeShopControl.AUTHENTICATED_PROCEED)));
    }

    public List<AtfAssertion> guestCheckout(Page page) {
        return concat(
                components.rendered(page, PracticeShopControl.GUEST_TAB, PracticeShopControl.GUEST_EMAIL,
                        PracticeShopControl.GUEST_FIRST_NAME, PracticeShopControl.GUEST_LAST_NAME,
                        PracticeShopControl.GUEST_SUBMIT),
                components.interactive(page, PracticeShopControl.GUEST_EMAIL, PracticeShopControl.GUEST_FIRST_NAME,
                        PracticeShopControl.GUEST_LAST_NAME, PracticeShopControl.GUEST_SUBMIT));
    }

    public List<AtfAssertion> billing(Page page) {
        return concat(
                components.rendered(page, PracticeShopControl.COUNTRY, PracticeShopControl.POSTAL_CODE,
                        PracticeShopControl.HOUSE_NUMBER, PracticeShopControl.STREET, PracticeShopControl.CITY,
                        PracticeShopControl.STATE, PracticeShopControl.BILLING_PROCEED),
                components.interactive(page, PracticeShopControl.COUNTRY, PracticeShopControl.POSTAL_CODE,
                        PracticeShopControl.HOUSE_NUMBER, PracticeShopControl.STREET, PracticeShopControl.CITY,
                        PracticeShopControl.STATE, PracticeShopControl.BILLING_PROCEED));
    }

    public List<AtfAssertion> payment(Page page) {
        return concat(
                components.rendered(page, PracticeShopControl.PAYMENT_METHOD, PracticeShopControl.PAYMENT_CHECK),
                components.interactive(page, PracticeShopControl.PAYMENT_METHOD, PracticeShopControl.PAYMENT_CHECK));
    }

    public List<AtfAssertion> paymentChecked(Page page) {
        return List.of(components.rendered(page, PracticeShopControl.PAYMENT_SUCCESS));
    }

    public List<AtfAssertion> orderConfirmation(Page page) {
        return List.of(components.rendered(page, PracticeShopControl.ORDER_CONFIRMATION));
    }

    @SafeVarargs
    private static List<AtfAssertion> concat(List<AtfAssertion>... groups) {
        return java.util.Arrays.stream(groups).flatMap(List::stream).toList();
    }
}
