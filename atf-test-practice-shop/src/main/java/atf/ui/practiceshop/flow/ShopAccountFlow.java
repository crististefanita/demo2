package atf.ui.practiceshop.flow;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.model.ShopCredentials;
import atf.ui.practiceshop.locators.ShopLocators;
import atf.ui.practiceshop.pages.account.ShopAccountPage;
import atf.ui.practiceshop.pages.account.ShopLoginPage;
import com.microsoft.playwright.Page;

/** Reusable authentication flow shared by each external E2E scenario. */
public final class ShopAccountFlow {
    private final ShopLocators locators;

    public ShopAccountFlow(ShopLocators locators) {
        this.locators = locators;
    }

    public void loginAndAssertAccount(Page page, PlaywrightActionSequence actions,
                                      ShopConfiguration configuration,
                                      ShopCredentials credentials) {
        ShopLoginPage login = new ShopLoginPage(page, locators, actions);
        login.open(configuration);
        login.login(credentials);
        new ShopAccountPage(page, locators, actions).assertLoaded(credentials);
    }
}


