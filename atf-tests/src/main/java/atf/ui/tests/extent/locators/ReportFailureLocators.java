package atf.ui.tests.extent.locators;

import atf.api.ui.playwright.AtfLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Selectors and replay actions for the report failure view. */
final class ReportFailureLocators {
    private static final String FAILS_VIEW = "#atf-fails-view";
    private static final String FAILURE_ITEMS = "#atf-failure-list > .test-item";

    Locator names(Locator fails) { return fails.locator("#atf-failure-list .name"); }
    Locator items(Locator fails) { return fails.locator(FAILURE_ITEMS); }
    Locator listSurface(Locator fails) { return fails.locator(".test-list"); }
    Locator detailSurface(Locator fails) { return fails.locator(".test-content"); }
    Locator openLinks(Locator fails) { return fails.locator("a.atf-failure-open"); }
    Locator detail(Locator fails) { return fails.locator("#atf-failure-detail"); }
    Locator causeValues(Locator fails) {
        return fails.locator(".atf-failure-index-cause .atf-failure-value");
    }
    Locator emptyState(Locator fails) {
        return fails.locator("#atf-failure-list .atf-fails-empty");
    }
    AtfLocator itemAction(Page page, int zeroBasedIndex) {
        int index = requireIndex(zeroBasedIndex);
        return new AtfLocator(items(page.locator(FAILS_VIEW)).nth(index),
                FAILS_VIEW + " " + FAILURE_ITEMS + ":nth-of-type(" + (index + 1) + ")");
    }
    AtfLocator firstOpenAction(Page page) {
        return new AtfLocator(openLinks(page.locator(FAILS_VIEW)).first(),
                FAILS_VIEW + " a.atf-failure-open:first-of-type");
    }
    Locator primaryDetails(Locator fails) {
        return fails.getByText("PRIMARY:", new Locator.GetByTextOptions().setExact(false));
    }

    private static int requireIndex(int zeroBasedIndex) {
        if (zeroBasedIndex < 0) throw new IllegalArgumentException("zeroBasedIndex must be >= 0");
        return zeroBasedIndex;
    }
}
