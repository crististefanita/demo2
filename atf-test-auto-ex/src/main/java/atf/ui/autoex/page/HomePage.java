package atf.ui.autoex.page;

import atf.ui.autoex.locator.AutoExRoutes;
import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.journey.AutoExPurchaseSession;

/** Store entry and its primary navigation. */
public final class HomePage extends AutoExPage {
    public HomePage(AutoExPurchaseSession run) { super(run); }

    public String open() {
        run.navigate(AutoExRoutes.HOME.resolve(run.configuration().baseUrl()));
        new PrivacyChoicesPage(run).rejectOptionalProcessingIfPrompted();
        return run.textFirst(AutoExSelectors.STORE_HEADING, "Automation Exercise home page is visible");
    }

    public void openProducts() {
        click(AutoExSelectors.PRODUCTS_LINK, "product catalog opens");
    }
}
