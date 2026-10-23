package atf.ui.practiceshop.pages.account;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.model.ShopCredentials;
import atf.ui.practiceshop.config.ShopScreen;
import atf.ui.practiceshop.locators.ShopControl;
import atf.ui.practiceshop.locators.ShopLocators;
import atf.ui.practiceshop.support.ShopPageContract;
import atf.ui.practiceshop.routes.ShopUiRoutes;
import com.microsoft.playwright.Page;

/** Login page object; credential values are never recorded as action evidence. */
@ShopPageContract(screen = ShopScreen.LOGIN, route = ShopUiRoutes.AUTH_LOGIN,
        requiredControls = {ShopControl.LOGIN_EMAIL, ShopControl.LOGIN_PASSWORD,
                ShopControl.LOGIN_SUBMIT})
public final class ShopLoginPage {
    private final Page page;
    private final ShopLocators locators;
    private final PlaywrightActionSequence actions;

    public ShopLoginPage(Page page, ShopLocators locators,
                                 PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public void open(ShopConfiguration configuration) {
        actions.navigate(page, configuration.uri(ShopUiRoutes.AUTH_LOGIN), "login page is loaded");
        actions.waitForVisible(locators.loginEmail(page));
        actions.waitForVisible(locators.loginPassword(page));
        actions.waitForVisible(locators.loginSubmit(page));
    }

    public void login(ShopCredentials credentials) {
        actions.fill(locators.loginEmail(page), credentials.email(), "email is entered securely");
        actions.fill(locators.loginPassword(page), credentials.password(), "password is entered securely");
        actions.click(locators.loginSubmit(page), "credentials are submitted");
    }
}


