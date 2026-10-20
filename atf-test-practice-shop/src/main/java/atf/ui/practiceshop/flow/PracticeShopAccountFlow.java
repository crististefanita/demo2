package atf.ui.practiceshop.flow;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.pages.PracticeShopLoginPage;
import com.microsoft.playwright.Page;

/** Reusable authentication flow shared by each external E2E scenario. */
public final class PracticeShopAccountFlow {
    private final PracticeShopLocators locators;

    public PracticeShopAccountFlow(PracticeShopLocators locators) {
        this.locators = locators;
    }

    public void loginAndAssertAccount(Page page, PlaywrightActionSequence actions,
                                      PracticeShopConfiguration configuration,
                                      PracticeShopCredentials credentials) {
        PracticeShopLoginPage login = new PracticeShopLoginPage(page, locators, actions);
        login.open(configuration);
        login.login(credentials);
        new PracticeShopAccountPage(page, locators, actions).assertLoaded(credentials);
    }
}


