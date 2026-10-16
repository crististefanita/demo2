package atf.ui.tests.extent.section.extract;

import atf.api.ui.playwright.AtfLocator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.Objects;

/** Browser adapter for one request evidence node; validation stays renderer-neutral. */
public final class EvidenceSectionExtractor {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;
    private final ApiJsonEvidenceExtractor jsonExtractor;

    public EvidenceSectionExtractor(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = Objects.requireNonNull(auditFlow, "auditFlow");
        this.locators = Objects.requireNonNull(locators, "locators");
        this.jsonExtractor = new ApiJsonEvidenceExtractor(locators);
    }

    public ReportAuditSnapshot.RequestEvidence extractFirstRequestWithEvidence(Page page) {
        Locator tests = locators.testsView(page);
        if (locators.testOwners(tests).count() == 0) return missing("report has no class owner");
        auditFlow.actions().click(locators.firstTestOwnerAction(page), "class detail opens");
        Locator scenario = locators.visibleScenarios(locators.visibleTestDetail(page)).first();
        if (scenario.count() == 0) return missing("Tests has no visible scenario card");
        AtfLocator scenarioTarget = locators.scenarioStateTarget(scenario);
        auditFlow.actions().waitForVisible(scenarioTarget);
        auditFlow.actions().waitForVisible(new AtfLocator(locators.boundScenarioHeader(scenario),
                ".test-content-detail .card[data-atf-scenario-id] > .card-header"
                        + "[data-atf-scenario-collapse-bound='true']"));
        Locator testBody = locators.visiblePhase(scenario, ReportUiContract.Phase.TEST_BODY).first();
        for (int attempt = 0; attempt < 3 && testBody.count() == 0; attempt++) {
            auditFlow.actions().click(locators.scenarioNodeAction(scenario), "scenario is expanded");
            auditFlow.actions().waitForAttribute(page, scenarioTarget, "data-atf-scenario-open", "true",
                    "scenario expansion is ready");
            testBody = locators.visiblePhase(scenario, ReportUiContract.Phase.TEST_BODY).first();
        }
        if (testBody.count() == 0) return missing("TEST BODY is missing");
        auditFlow.actions().waitForVisible(new AtfLocator(locators.boundPhaseHeader(testBody),
                ".test-content-detail [data-atf-node-role='phase'] > .card-header"
                        + "[data-atf-phase-header-bound='true']"));
        if (!"true".equals(locators.nodeHeader(testBody).getAttribute("aria-expanded"))) {
            auditFlow.actions().click(locators.phaseNodeAction(testBody), "TEST BODY is expanded");
        }
        Locator requests = locators.requestsInPhase(testBody);
        if (requests.count() == 0) return missing("TEST BODY has no request");
        Locator request = requests.nth(firstRequestWithScreenshot(requests));
        if (!"true".equals(request.getAttribute("data-atf-node-open"))) {
            auditFlow.actions().click(locators.requestNodeToggleAction(request), "request evidence is expanded");
        }
        expand(locators.requestEvidenceDisclosure(request), locators.requestEvidenceDisclosureAction(request), "request evidence");
        expand(locators.responseEvidenceDisclosure(request), locators.responseEvidenceDisclosureAction(request), "response evidence");
        expand(locators.executionEvidenceDisclosure(request), locators.executionEvidenceDisclosureAction(request), "execution evidence");
        int requestTables = locators.visibleRequestTable(request).count();
        int responseTables = locators.visibleResponseTable(request).count();
        int executionTables = locators.visibleExecutionTable(request).count();
        return new ReportAuditSnapshot.RequestEvidence(requestTables > 0 && responseTables > 0
                && executionTables > 0, requestTables, responseTables, executionTables,
                jsonExtractor.extract(request));
    }

    private int firstRequestWithScreenshot(Locator requests) {
        for (int index = 0; index < requests.count(); index++) {
            if (locators.screenshotLinks(requests.nth(index)).count() > 0) return index;
        }
        return 0;
    }

    private void expand(Locator summary, AtfLocator action, String target) {
        if (summary.count() == 0) return;
        Locator disclosure = locators.parentDisclosure(summary);
        if (disclosure.getAttribute("open") == null) auditFlow.actions().click(action, target + " is expanded");
    }

    private static ReportAuditSnapshot.RequestEvidence missing(String message) {
        return new ReportAuditSnapshot.RequestEvidence(false, 0, 0, 0,
                new ReportAuditSnapshot.JsonEvidence("", "", List.of(message)));
    }
}
