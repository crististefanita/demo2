package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import com.microsoft.playwright.Page;

/** Payment page object; the demo payment method is configuration, not a test literal. */
@PracticeShopPageContract(screen = PracticeShopScreen.PAYMENT, route = PracticeShopUiRoutes.CHECKOUT,
        requiredControls = {PracticeShopControl.PAYMENT_METHOD, PracticeShopControl.PAYMENT_CHECK})
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

    public String chooseMethod(String paymentMethod) {
        actions.waitForVisible(locators.paymentMethod(page));
        actions.selectOption(locators.paymentMethod(page), paymentMethod,
                "configured demo payment method is selected");
        return actions.getText(locators.selectedPaymentMethod(page),
                "selected payment method is observed");
    }

    public String checkPayment() {
        actions.click(locators.paymentCheck(page), "configured payment method is checked");
        actions.waitForText(page, locators.paymentSuccess(page), "Payment was successful",
                "payment success confirmation is displayed");
        return actions.expectText(locators.paymentSuccess(page), "Payment was successful");
    }
}
