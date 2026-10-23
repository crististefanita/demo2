package atf.ui.tests.extent.support.locators.parity;

import atf.ui.tests.extent.support.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Real selected-copy API evidence, never the hidden Extent template. */
public final class ApiExchangeParityLocators {
    private final ReportUiLocators common = new ReportUiLocators();
    public Locator owners(Page page) { return common.testOwners(page); }
    public Locator expand(Page page) { return common.visibleTestDetail(page).locator("span[class~='et']"); }
    public Locator requests(Page page, boolean fallback) {
        return fallback ? page.locator("section[id='tests'] [class~='request']")
                : common.visibleTestDetail(page).locator("div[data-atf-node-role='request']:not(:has(div[data-atf-node-role='request']))");
    }
    public Locator fallbackTests(Page page) { return page.locator("button[data-tab='tests']"); }
    public Locator evidence(Locator request, String kind) { return request.locator("details[class~='" + kind + "']"); }
    public Locator summary(Locator evidence) { return common.disclosureSummary(evidence); }
    public Locator rows(Locator evidence) { return evidence.locator("table > tbody > tr"); }
    public Locator documents(Locator evidence) { return evidence.locator("pre"); }
}
