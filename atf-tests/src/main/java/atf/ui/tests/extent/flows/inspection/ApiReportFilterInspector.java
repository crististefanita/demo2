package atf.ui.tests.extent.flows.inspection;

import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import atf.ui.tests.extent.flows.ApiReportExplorationFlow;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Owns category/failure filter navigation for the API report. */
public final class ApiReportFilterInspector {
    private final UiAuditFlow auditFlow;
    private final ApiReportStructureInspector structureInspector;
    private final ReportUiLocators locators;

    public ApiReportFilterInspector(UiAuditFlow auditFlow,
                                    ApiReportStructureInspector structureInspector) {
        this(auditFlow, structureInspector, new ReportUiLocators());
    }

    public ApiReportFilterInspector(UiAuditFlow auditFlow,
                                    ApiReportStructureInspector structureInspector,
                                    ReportUiLocators locators) {
        this.auditFlow = auditFlow;
        this.structureInspector = structureInspector;
        this.locators = locators;
    }

    public ApiReportExplorationFlow.FiltersSnapshot inspectFiltersAndFailures() {
        Page category = auditFlow.openTab(ReportUiContract.Tab.CATEGORIES.hash());
        ReportAuditSnapshot.Annotations annotations = structureInspector.inspectAnnotations(category);
        boolean tagLinks = annotations.categoryCount() > 0 && annotations.linkedScenarioCount() > 0;
        if (tagLinks) {
            auditFlow.actions().click(locators.firstCategoryLinkAction(category),
                    "Tests tab opens for selected annotation");
            auditFlow.actions().waitForVisible(locators.testsView(category),
                    ReportUiContract.Tab.TESTS.hash());
        }
        Page fails = auditFlow.openTab(ReportUiContract.Tab.FAILS.hash());
        ReportAuditSnapshot.Failures failure = structureInspector.inspectFailures(fails);
        boolean failureLinks = failure.failureCount() > 0
                && failure.primaryCauseCount() > 0 && failure.openLinkCount() > 0;
        return new ApiReportExplorationFlow.FiltersSnapshot(category, fails, annotations, failure,
                tagLinks, failureLinks);
    }

    public boolean openFirstFailureInTests(ApiReportExplorationFlow.FiltersSnapshot snapshot) {
        if (snapshot.failures().openLinkCount() == 0) return false;
        auditFlow.actions().click(locators.firstFailureOpenAction(snapshot.failsPage()),
                "selected failure opens in Tests");
        return locators.testsView(snapshot.failsPage()).isVisible();
    }
}
