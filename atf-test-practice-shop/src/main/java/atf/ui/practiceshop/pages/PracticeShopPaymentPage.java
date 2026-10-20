package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import com.microsoft.playwright.Page;

/** Payment page object; the demo payment method is configuration, not a test literal. */
@PracticeShopPageContract(screen = PracticeShopScreen.PAYMENT, route = "/checkout",
        requiredControls = {PracticeShopControl.PAYMENT_METHOD, PracticeShopControl.FINISH})
public final class PracticeShopPaymentPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopPaymentPage(Page page, PracticeShopLocators locators,
                                   PlaywrightActionSequence actions) {
        this.page = page;
        this.locators = locators;
        this.actions = actions;
    }

    public void chooseMethod(String paymentMethod) {
        actions.waitForVisible(locators.paymentMethod(page));
        actions.selectOption(locators.paymentMethod(page), paymentMethod,
                "configured demo payment method is selected");
    }

    public String confirm() {
        actions.click(locators.finish(page), "payment is confirmed");
        actions.waitForText(page, locators.paymentSuccess(page), "Payment was successful",
                "payment success confirmation is displayed");
        return actions.expectText(locators.paymentSuccess(page), "Payment was successful");
    }
}
