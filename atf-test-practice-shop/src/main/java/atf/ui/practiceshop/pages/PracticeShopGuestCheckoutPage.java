package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopGuest;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import com.microsoft.playwright.Page;

/** Guest identity page object; entered values are intentionally redacted by the action facade. */
@PracticeShopPageContract(screen = PracticeShopScreen.GUEST_CHECKOUT, route = "/checkout",
        requiredControls = {PracticeShopControl.GUEST_EMAIL, PracticeShopControl.GUEST_FIRST_NAME,
                PracticeShopControl.GUEST_LAST_NAME, PracticeShopControl.GUEST_SUBMIT})
public final class PracticeShopGuestCheckoutPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopGuestCheckoutPage(Page page, PracticeShopLocators locators,
                                         PlaywrightActionSequence actions) {
        this.page = page;
        this.locators = locators;
        this.actions = actions;
    }

    public void continueAsGuest(PracticeShopGuest guest) {
        fillIdentity(guest);
        submitIdentity();
    }

    /** Fills the form without submitting it, allowing value validation at this boundary. */
    public void fillIdentity(PracticeShopGuest guest) {
        actions.click(locators.guestTab(page), "guest checkout is selected");
        actions.fill(locators.guestEmail(page), guest.email(), "guest email is entered securely");
        actions.fill(locators.guestFirstName(page), guest.firstName(), "guest first name is entered");
        actions.fill(locators.guestLastName(page), guest.lastName(), "guest last name is entered");
    }

    /** Submits the already validated identity and waits for the checkout transition. */
    public void submitIdentity() {
        actions.click(locators.guestSubmit(page), "guest checkout identity is submitted");
        actions.waitForText(page, locators.pageBody(page), "Continuing as guest",
                "guest checkout identity is displayed");
    }

    public void proceedToBilling() {
        actions.click(locators.guestProceed(page), "guest checkout proceeds to billing");
    }
}
