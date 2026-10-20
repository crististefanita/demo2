package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import com.microsoft.playwright.Page;

/** Account page object with semantic identity validation. */
@PracticeShopPageContract(screen = PracticeShopScreen.ACCOUNT, route = "/account",
        requiredControls = {PracticeShopControl.ACCOUNT_MARKER}, bodyMarker = "My account")
public final class PracticeShopAccountPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopAccountPage(Page page, PracticeShopLocators locators,
                                   PlaywrightActionSequence actions) {
        this.page = page; this.locators = locators; this.actions = actions;
    }

    public void assertLoaded(PracticeShopCredentials credentials) {
        actions.waitForVisible(locators.accountBody(page));
        actions.waitForText(page, locators.accountBody(page), "My account",
                "signed-in account page is displayed");
        actions.expectText(locators.accountBody(page), "My account");
    }
}


