package atf.ui.tests.extent.audit;

import atf.api.ui.playwright.AtfLocator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

import static atf.ui.tests.extent.support.ReportAuditChecks.count;

/**
 * Owns the DOM mechanics for one Categories-filter interaction.
 * The exploratory test remains responsible for the Gherkin intent, evidence
 * and JUnit verdict; this class only discovers and reconciles the rendered
 * filter state.
 */
public final class CategoryFilterAuditor {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;

    public CategoryFilterAuditor(UiAuditFlow auditFlow) {
        this.auditFlow = auditFlow;
        this.locators = new ReportUiLocators();
    }

    public List<CategoryOption> discover(Page page) {
        Locator categories = locators.categoryItems(page);
        List<CategoryOption> result = new ArrayList<>();
        for (int index = 0; index < categories.count(); index++) {
            Locator item = locators.categoryItem(categories, index);
            result.add(new CategoryOption(
                    index,
                    locators.categoryItemName(item).innerText().trim(),
                    count(locators.categoryItemDuration(item).innerText())));
        }
        return List.copyOf(result);
    }

    public FilterSnapshot select(Page page, CategoryOption option) {
        AtfLocator item = locators.categoryItemAction(page, option.index());
        String selector = item.selector();
        auditFlow.actions().click(item, "category filter selects " + option.name());

        Locator detail = locators.visibleCategoryDetail(page);
        auditFlow.actions().waitForVisible(locators.visibleCategoryDetailAnchor(page));
        String selectedTitle = locators.categoryTitle(detail).innerText().trim();
        Locator links = locators.categoryLinkedTests(detail);
        Locator rows = locators.categoryStatusRows(detail);
        boolean selected = item.locator().getAttribute("class").contains("active");
        List<String> linkedIds = new ArrayList<>();
        for (int index = 0; index < links.count(); index++) {
            linkedIds.add(links.nth(index).getAttribute("test-id"));
        }
        boolean linkedIdsValid = validateLinkedScenarioIds(option.declaredCount(), linkedIds).valid();
        boolean valid = selected
                && option.name().equalsIgnoreCase(selectedTitle)
                && option.declaredCount() == links.count()
                && option.declaredCount() == rows.count()
                && linkedIdsValid;
        return new FilterSnapshot(option, selectedTitle, links.count(), rows.count(), selected,
                linkedIdsValid, valid, selector);
    }

    public String selector(int zeroBasedIndex) {
        return locators.categoryItemSelector(zeroBasedIndex);
    }

    public boolean navigateToFirstLinkedScenario(Page page, CategoryOption option) {
        AtfLocator item = locators.categoryItemAction(page, option.index());
        auditFlow.actions().click(item, "category filter selects " + option.name());
        Locator detail = locators.visibleCategoryDetail(page);
        auditFlow.actions().waitForVisible(locators.visibleCategoryDetailAnchor(page));
        Locator link = locators.categoryLinkedTests(detail).first();
        if (link.count() == 0) return false;
        auditFlow.actions().click(link, "#category-view a.linked:first-of-type",
                "linked scenario opens in Tests");
        auditFlow.actions().waitForVisible(locators.testsView(page), ReportUiContract.Tab.TESTS.hash());
        return locators.testsView(page).isVisible();
    }

    /** Pure zero/one/many contract shared by browser and unit audits. */
    public static LinkValidation validateLinkedScenarioIds(int declaredCount,
                                                            List<String> linkedIds) {
        if (declaredCount < 0 || linkedIds == null || linkedIds.size() != declaredCount) {
            return new LinkValidation(false, declaredCount,
                    linkedIds == null ? 0 : linkedIds.size(), List.of("count"));
        }
        List<String> invalidIds = linkedIds.stream()
                .filter(id -> id == null || id.isBlank() || !id.matches("\\d+"))
                .toList();
        return new LinkValidation(invalidIds.isEmpty(), declaredCount, linkedIds.size(), invalidIds);
    }

    public record CategoryOption(int index, String name, int declaredCount) {
    }

    public record FilterSnapshot(CategoryOption option, String selectedTitle,
                                 int linkCount, int rowCount, boolean selected,
                                 boolean linkedIdsValid, boolean valid, String selector) {
    }

    public record LinkValidation(boolean valid, int declaredCount, int actualCount,
                                 List<String> invalidIds) {
        public LinkValidation {
            invalidIds = List.copyOf(invalidIds);
        }
    }
}
