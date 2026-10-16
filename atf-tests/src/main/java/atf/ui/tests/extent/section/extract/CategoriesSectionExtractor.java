package atf.ui.tests.extent.section.extract;

import atf.api.ui.playwright.AtfLocator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Browser adapter for category/filter facts; it never asserts the contract. */
public final class CategoriesSectionExtractor {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;

    public CategoriesSectionExtractor(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = Objects.requireNonNull(auditFlow, "auditFlow");
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    public ReportAuditSnapshot.Annotations extract(Page page) {
        Locator view = locators.categoryView(page);
        Locator items = locators.categoryItems(page);
        if (items.count() == 0) return new ReportAuditSnapshot.Annotations(0, 0, "missing", false, List.of());
        List<Integer> cardinalities = new ArrayList<>();
        for (int index = 0; index < items.count(); index++) {
            auditFlow.actions().click(locators.categoryItemAction(page, index), "annotation filter opens");
            auditFlow.actions().waitForVisible(locators.visibleCategoryDetailAnchor(page));
            cardinalities.add(locators.categoryLinkedTests(locators.categoryContentDetail(page)).count());
        }
        String surface = String.valueOf(locators.categoryList(view).evaluate(
                "element => { const s=getComputedStyle(element); return s.backgroundColor + ' / ' + s.color; }"));
        boolean styled = !surface.contains("rgb(255, 255, 255)") && locators.categoryContent(view).isVisible();
        return new ReportAuditSnapshot.Annotations(items.count(), cardinalities.stream()
                .mapToInt(Integer::intValue).sum(), surface, styled, cardinalities);
    }

    public boolean navigateToFirstLinkedScenario(Page page) {
        Locator detail = locators.visibleCategoryDetail(page);
        Locator links = locators.categoryLinkedTests(detail);
        if (links.count() == 0) return false;
        auditFlow.actions().click(locators.firstCategoryLinkAction(page), "linked scenario opens in Tests");
        auditFlow.actions().waitForVisible(new AtfLocator(locators.testsView(page),
                ReportUiContract.Tab.TESTS.hash()));
        return locators.testsView(page).isVisible();
    }
}
