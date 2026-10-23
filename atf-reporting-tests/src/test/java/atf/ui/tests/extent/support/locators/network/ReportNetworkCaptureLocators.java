package atf.ui.tests.extent.support.locators.network;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Selector boundary for the generic Network capture evidence browser proof. */
public final class ReportNetworkCaptureLocators {
    public Locator heading(Page page, String title) {
        return page.locator("[class~='card-header'][role='button']:visible")
                .filter(new Locator.FilterOptions().setHasText(title)).first();
    }

    public Locator coverage(Page page) {
        return page.locator("summary:visible")
                .filter(new Locator.FilterOptions().setHasText("Capture coverage"));
    }

    public Locator document(Page page) {
        return page.locator("body");
    }
}
