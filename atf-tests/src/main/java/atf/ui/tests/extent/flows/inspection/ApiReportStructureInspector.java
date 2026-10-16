package atf.ui.tests.extent.flows.inspection;

import atf.ui.tests.extent.section.extract.OverviewSectionExtractor;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

/** Owns structural and overview observations for the API report. */
public final class ApiReportStructureInspector {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;
    private final OverviewSectionExtractor overviewExtractor;

    public ApiReportStructureInspector(UiAuditFlow auditFlow) {
        this(auditFlow, new ReportUiLocators());
    }

    public ApiReportStructureInspector(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = auditFlow;
        this.locators = locators;
        this.overviewExtractor = new OverviewSectionExtractor(locators);
    }

    public ReportAuditSnapshot.Tests inspectEveryTestClass(Page page) {
        Locator tests = locators.testsView(page);
        Locator owners = locators.testOwners(tests);
        List<String> names = new ArrayList<>();
        int scenarios = 0;
        int phases = 0;
        int requests = 0;
        int validations = 0;
        List<String> scenarioTitles = new ArrayList<>();
        for (int index = 0; index < owners.count(); index++) {
            Locator owner = locators.testOwnerItem(page, index);
            names.add(auditFlow.actions().getText(locators.testOwnerName(owner),
                    ReportUiContract.TEST_OWNER_SELECTOR + " .name",
                    "class owner name is readable"));
            auditFlow.actions().click(locators.testOwnerItemAction(page, index),
                    "class detail opens");
            Locator detail = locators.visibleTestDetail(page);
            auditFlow.actions().waitForVisible(detail, ReportUiContract.VISIBLE_DETAIL_SELECTOR);
            Locator scenarioCards = locators.scenarios(detail);
            scenarios += scenarioCards.count();
            for (int scenarioIndex = 0; scenarioIndex < scenarioCards.count(); scenarioIndex++) {
                scenarioTitles.add(locators.nodeTitle(scenarioCards.nth(scenarioIndex)));
            }
            phases += locators.phases(detail).count();
            requests += locators.requests(detail).count();
            validations += locators.validationTables(detail).count();
        }
        return new ReportAuditSnapshot.Tests(names.size(), scenarios, phases, requests,
                validations, names, scenarioTitles);
    }

    public int ownerCount(Page page) {
        return locators.testOwners(page).count();
    }

    public ReportAuditSnapshot.Annotations inspectAnnotations(Page category) {
        Locator view = locators.categoryView(category);
        Locator categoryItems = locators.categoryItems(category);
        int categoryCount = categoryItems.count();
        if (categoryCount == 0) {
            return new ReportAuditSnapshot.Annotations(0, 0, "missing", false, List.of());
        }
        List<Integer> linkCardinalities = new ArrayList<>();
        for (int index = 0; index < categoryCount; index++) {
            auditFlow.actions().click(locators.categoryItemAction(category, index),
                    "annotation filter opens");
            Locator detail = locators.categoryContentDetail(category);
            auditFlow.actions().waitForVisible(locators.visibleCategoryDetailAnchor(category));
            Locator links = locators.categoryLinkedTests(detail);
            linkCardinalities.add(links.count());
        }
        auditFlow.actions().getText(locators.categoryList(view), ReportUiContract.TEST_OWNER_SELECTOR,
                "annotation filter is readable");
        String filterSurface = locators.categoryList(view).evaluate(
                "element => { const s=getComputedStyle(element); return s.backgroundColor + ' / ' + s.color; }")
                .toString();
        boolean styled = !filterSurface.contains("rgb(255, 255, 255)")
                && locators.categoryContent(view).isVisible();
        int linkedScenarioCount = linkCardinalities.stream().mapToInt(Integer::intValue).sum();
        return new ReportAuditSnapshot.Annotations(categoryCount, linkedScenarioCount,
                filterSurface, styled, linkCardinalities);
    }

    public ReportAuditSnapshot.Failures inspectFailures(Page fails) {
        Locator view = locators.failsView(fails);
        int failureCount = locators.failureNames(view).count();
        return new ReportAuditSnapshot.Failures(failureCount,
                locators.primaryFailureDetails(view).count(),
                locators.failureOpenLinks(view).count(),
                failureCount == 0 && locators.failureEmptyState(view).count() == 1);
    }

    /** Browser adapter boundary: only immutable facts leave this inspector. */
    public ReportAuditSnapshot.Overview inspectOverviewSnapshot(Page page, boolean includeHierarchy) {
        return overviewExtractor.inspect(page, includeHierarchy);
    }
}
