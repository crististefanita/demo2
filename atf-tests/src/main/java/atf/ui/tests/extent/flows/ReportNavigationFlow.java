package atf.ui.tests.extent.flows;

import atf.api.ui.playwright.AtfLocator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** End-to-end semantic flow for Tests filters, Categories and Overview. */
public final class ReportNavigationFlow {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;

    public ReportNavigationFlow(UiAuditFlow auditFlow) {
        this(auditFlow, new ReportUiLocators());
    }

    public ReportNavigationFlow(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = auditFlow;
        this.locators = locators;
    }

    public NavigationSnapshot traverse() {
        Page tests = auditFlow.openTab(ReportUiContract.Tab.TESTS.hash());
        ReportAuditSnapshot.Tests baseline = inspectTests(tests);

        AtfLocator statusToggle = locators.testStatusTriggerAction(tests);
        auditFlow.actions().click(statusToggle, "status filter opens");
        AtfLocator passFilter = locators.statusFilterAction(tests, "pass");
        auditFlow.actions().waitForVisible(passFilter);
        auditFlow.actions().click(passFilter, "Pass filter selects");
        int passScenarioCount = visibleScenarioCount(tests);
        boolean passSelected = "true".equals(passFilter.locator().getAttribute("aria-pressed"));
        auditFlow.actions().observation("Status filter result", passFilter.selector(),
                "visible scenarios are reconciled", passScenarioCount + " visible scenarios");

        AtfLocator tagToggle = locators.testTagTriggerAction(tests);
        Locator tagFilters = locators.tagFilters(tests);
        int tagCount = tagFilters.count();
        int tagSelectionSteps = 0;
        if (tagCount > 0) {
            ensureMenuOpen(tagToggle, locators.testTagMenu(tests),
                    locators.tagFilterAction(tests, 0));
            auditFlow.actions().click(locators.tagFilterAction(tests, 0),
                    "first dynamic tag selects");
            tagSelectionSteps++;
        }
        if (tagCount > 1) {
            ensureMenuOpen(tagToggle, locators.testTagMenu(tests),
                    locators.tagFilterAction(tests, 1));
            auditFlow.actions().click(locators.tagFilterAction(tests, 1),
                    "second dynamic tag replaces selection");
            tagSelectionSteps++;
        }
        int combinedScenarioCount = visibleScenarioCount(tests);
        int activeTagCount = activeFilterCount(locators.tagFilters(tests));
        boolean tagCombinationValid = tagCount == 0 ? activeTagCount == 0 : activeTagCount == 1;
        auditFlow.actions().observation("Combined tag filter result", ReportUiContract.TAG_FILTER_MENU,
                "the last selected tag remains represented while Pass stays combined",
                activeTagCount + " active after " + tagSelectionSteps + " selections / " + tagCount + " available");

        clearStatusAndTags(tests);
        int clearedScenarioCount = scenarioIdCount(tests);
        boolean clearRestoresBaseline = clearedScenarioCount == baseline.scenarioCount();
        auditFlow.actions().observation("Clear filter result", locators.clearStatusFilterAction(tests).selector(),
                "Tests returns to the unfiltered baseline",
                clearedScenarioCount + " scenarios; baseline=" + baseline.scenarioCount());

        Page categories = auditFlow.openTab(ReportUiContract.Tab.CATEGORIES.hash());
        CategorySnapshot category = inspectFirstCategory(categories);
        Page overview = auditFlow.openTab(ReportUiContract.Tab.OVERVIEW.hash());
        ReportAuditSnapshot.Overview overviewSnapshot =
                new atf.ui.tests.extent.section.extract.OverviewSectionExtractor(locators)
                        .inspect(overview, true);
        return new NavigationSnapshot(overview, baseline, passSelected, passScenarioCount,
                combinedScenarioCount, activeTagCount, tagSelectionSteps, tagCombinationValid,
                clearedScenarioCount, clearRestoresBaseline, category, overviewSnapshot);
    }

    private void clearStatusAndTags(Page page) {
        auditFlow.actions().pressKey(page, "Escape", ReportUiContract.BODY_SELECTOR,
                "all transient filter menus are closed before Clear");
        AtfLocator statusToggle = locators.testStatusTriggerAction(page);
        AtfLocator clear = locators.clearStatusFilterAction(page);
        ensureMenuOpen(statusToggle, locators.testStatusMenu(page), clear);
        auditFlow.actions().click(clear, "status filter clears");

        AtfLocator tagToggle = locators.testTagTriggerAction(page);
        Locator tagMenu = locators.testTagMenu(page);
        Locator tagFilters = locators.tagFilters(page);
        ensureMenuOpen(tagToggle, tagMenu, tagFilters.count() > 0
                ? locators.tagFilterAction(page, 0) : null);
        Locator selected = locators.activeTagFilters(page);
        while (selected.count() > 0) {
            auditFlow.actions().click(locators.activeTagFilterAction(page), "one tag filter clears");
            selected = locators.activeTagFilters(page);
            if (selected.count() > 0) {
                ensureMenuOpen(tagToggle, tagMenu, locators.activeTagFilterAction(page));
            }
        }
    }

    private void ensureMenuOpen(AtfLocator toggle, Locator menu, AtfLocator requiredItem) {
        if (!menu.isVisible()) auditFlow.actions().click(toggle, "filter menu opens");
        if (requiredItem != null && requiredItem.locator().count() > 0) {
            auditFlow.actions().waitForVisible(requiredItem);
        }
    }

    public ReportAuditSnapshot.Tests inspectTests(Page page) {
        Locator owners = locators.testOwners(page);
        if (owners.count() > 0) auditFlow.actions().click(locators.firstTestOwnerAction(page),
                "first test owner opens");
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int index = 0; index < owners.count(); index++) {
            names.add(locators.testOwnerName(locators.testOwnerItem(page, index)).innerText().trim());
        }
        return new ReportAuditSnapshot.Tests(owners.count(), scenarioIdCount(page),
                0, 0, 0, names);
    }

    public CategorySnapshot inspectFirstCategory(Page page) {
        Locator items = locators.categoryItems(page);
        if (items.count() == 0) return new CategorySnapshot(false, 0, 0, 0);
        Locator first = locators.categoryItem(items, 0);
        auditFlow.actions().click(locators.firstCategoryItemAction(page),
                "first category filter selects");
        AtfLocator detailAnchor = locators.visibleCategoryDetailAnchor(page);
        auditFlow.actions().waitForVisible(detailAnchor);
        Locator detail = locators.visibleCategoryDetail(page);
        int links = locators.categoryLinkedTests(detail).count();
        int rows = locators.categoryStatusRows(detail).count();
        int declared = ReportAuditChecks.count(locators.categoryItemDuration(first).textContent());
        String className = first.getAttribute("class");
        boolean valid = className != null && className.contains("active")
                && declared == links && declared == rows;
        return new CategorySnapshot(valid, declared, links, rows);
    }

    private int visibleScenarioCount(Page page) {
        Locator scenarios = locators.scenarioCards(page);
        int visible = 0;
        for (int index = 0; index < scenarios.count(); index++) {
            if (scenarios.nth(index).isVisible()) visible++;
        }
        return visible;
    }

    private int scenarioIdCount(Page page) {
        Locator scenarios = locators.scenarioCards(page);
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int index = 0; index < scenarios.count(); index++) {
            String id = scenarios.nth(index).getAttribute("data-atf-scenario-id");
            if (id != null && !id.isBlank()) ids.add(id);
        }
        return ids.size();
    }

    private int activeFilterCount(Locator filters) {
        int active = 0;
        for (int index = 0; index < filters.count(); index++) {
            if ("true".equals(filters.nth(index).getAttribute("aria-pressed"))) active++;
        }
        return active;
    }

    public record CategorySnapshot(boolean valid, int declaredCount, int linkCount, int rowCount) { }

    public record NavigationSnapshot(Page overviewPage, ReportAuditSnapshot.Tests baseline,
                                     boolean passSelected, int passScenarioCount, int combinedScenarioCount,
                                     int activeTagCount, int tagSelectionSteps, boolean tagCombinationValid,
                                     int clearedScenarioCount, boolean clearRestoresBaseline,
                                     CategorySnapshot category, ReportAuditSnapshot.Overview overview) { }
}
