package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopBillingAddress;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import com.microsoft.playwright.Page;

/** Billing address page object with no raw selectors outside the locator owner. */
@PracticeShopPageContract(screen = PracticeShopScreen.BILLING, route = "/checkout",
        requiredControls = {PracticeShopControl.COUNTRY, PracticeShopControl.POSTAL_CODE,
                PracticeShopControl.HOUSE_NUMBER, PracticeShopControl.STREET, PracticeShopControl.CITY,
                PracticeShopControl.STATE, PracticeShopControl.BILLING_PROCEED})
public final class PracticeShopBillingPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopBillingPage(Page page, PracticeShopLocators locators,
                                   PlaywrightActionSequence actions) {
        this.page = page;
        this.locators = locators;
        this.actions = actions;
    }

    public void fill(PracticeShopBillingAddress address) {
        actions.waitForVisible(locators.country(page));
        actions.selectOption(locators.country(page), address.country(), "billing country is selected");
        actions.fill(locators.postalCode(page), address.postalCode(), "billing postal code is entered");
        actions.fill(locators.houseNumber(page), address.houseNumber(), "billing house number is entered");
        actions.fill(locators.street(page), address.street(), "billing street is entered");
        actions.fill(locators.city(page), address.city(), "billing city is entered");
        actions.fill(locators.state(page), address.state(), "billing state is entered");
    }

    public void proceedToPayment() {
        actions.click(locators.billingProceed(page), "billing address proceeds to payment");
    }
}
