package atf.ui.tests.extent.audit;

import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;

/** Reusable semantic validator for the Categories screen and its linked state. */
public final class CategoryScreenValidator {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;

    public CategoryScreenValidator(UiAuditFlow auditFlow) {
        this(auditFlow, new ReportUiLocators());
    }

    public CategoryScreenValidator(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = Objects.requireNonNull(auditFlow, "auditFlow");
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    /** Selects the first discovered category and returns its dynamic screen observation. */
    public Observation inspect(Page page) {
        Locator items = locators.categoryItems(page);
        if (items.count() == 0) return Observation.empty();
        auditFlow.actions().click(locators.firstCategoryItemAction(page), "annotation filter opens");
        Locator detail = locators.visibleCategoryDetail(page);
        auditFlow.actions().waitForVisible(locators.visibleCategoryDetailAnchor(page));
        Locator links = locators.categoryLinkedTests(detail);
        String surface = locators.categorySurface(page).evaluate(
                "element => { const s=getComputedStyle(element); return s.backgroundColor + ' / ' + s.color; }")
                .toString();
        boolean styled = !surface.contains("rgb(255, 255, 255)")
                && locators.categoryContent(page).isVisible();
        return new Observation(items.count(), links.count(), surface, styled,
                locators.categoryTitle(detail).innerText().trim());
    }

    public boolean navigateToFirstLinkedScenario(Page page) {
        Locator detail = locators.visibleCategoryDetail(page);
        Locator links = locators.categoryLinkedTests(detail);
        if (links.count() == 0) return false;
        auditFlow.actions().click(locators.firstCategoryLinkAction(page),
                "linked scenario opens in Tests");
        auditFlow.actions().waitForVisible(new atf.api.ui.playwright.AtfLocator(
                locators.testsView(page), ReportUiContract.Tab.TESTS.hash()));
        return locators.testsView(page).isVisible();
    }

    public record Observation(int categoryCount, int linkedScenarioCount,
                              String filterSurface, boolean styled, String selectedTitle) {
        public static Observation empty() {
            return new Observation(0, 0, "missing", false, "");
        }
    }
}
