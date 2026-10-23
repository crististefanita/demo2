package atf.ui.tests.extent.support.locators.disclosure;

import atf.ui.tests.extent.support.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Vendor controls and the semantic state they must govern. */
public final class ReportBulkDisclosureLocators {
    private final ReportUiLocators common = new ReportUiLocators();
    public Locator owners(Page page) { return common.testOwners(page); }
    public Locator detail(Page page) { return common.visibleTestDetail(page); }
    public Locator expand(Page page) { return detail(page).locator("span[class~='et']"); }
    public Locator collapse(Page page) { return detail(page).locator("span[class~='ct']"); }
    public Locator cards(Page page) {
        return detail(page).locator("div[data-atf-scenario-open],div[data-atf-node-role]");
    }
    public Locator header(Locator card) { return common.requestNodeHeader(card); }
    public Locator disclosures(Page page) { return detail(page).locator("details"); }
}
