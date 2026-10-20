package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import com.microsoft.playwright.Page;

/** Login page object; credential values are never recorded as action evidence. */
@PracticeShopPageContract(screen = PracticeShopScreen.LOGIN, route = PracticeShopUiRoutes.AUTH_LOGIN,
        requiredControls = {PracticeShopControl.LOGIN_EMAIL, PracticeShopControl.LOGIN_PASSWORD,
                PracticeShopControl.LOGIN_SUBMIT})
public final class PracticeShopLoginPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopLoginPage(Page page, PracticeShopLocators locators,
                                 PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public void open(PracticeShopConfiguration configuration) {
        actions.navigate(page, configuration.uri(PracticeShopUiRoutes.AUTH_LOGIN), "login page is loaded");
        actions.waitForVisible(locators.loginEmail(page));
        actions.waitForVisible(locators.loginPassword(page));
        actions.waitForVisible(locators.loginSubmit(page));
    }

    public void login(PracticeShopCredentials credentials) {
        actions.fill(locators.loginEmail(page), credentials.email(), "email is entered securely");
        actions.fill(locators.loginPassword(page), credentials.password(), "password is entered securely");
        actions.click(locators.loginSubmit(page), "credentials are submitted");
    }
}


