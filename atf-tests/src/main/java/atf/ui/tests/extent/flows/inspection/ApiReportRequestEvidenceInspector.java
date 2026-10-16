package atf.ui.tests.extent.flows.inspection;

import atf.ui.tests.extent.audit.ApiJsonEvidenceAuditor;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.api.ui.playwright.AtfLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

/** Owns deep request/response/execution evidence inspection for one API request. */
public final class ApiReportRequestEvidenceInspector {
    private final UiAuditFlow auditFlow;
    private final ApiJsonEvidenceAuditor jsonAuditor = new ApiJsonEvidenceAuditor();
    private final ReportUiLocators locators;

    public ApiReportRequestEvidenceInspector(UiAuditFlow auditFlow) {
        this(auditFlow, new ReportUiLocators());
    }

    public ApiReportRequestEvidenceInspector(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = auditFlow;
        this.locators = locators;
    }

    public ReportAuditSnapshot.RequestEvidence inspectFirstRequestWithEvidence(Page page) {
        Locator tests = locators.testsView(page);
        Locator classOwners = locators.testOwners(tests);
        if (classOwners.count() == 0) {
            return new ReportAuditSnapshot.RequestEvidence(false, 0, 0, 0,
                    new ReportAuditSnapshot.JsonEvidence("", "", List.of("report has no class owner")));
        }
        auditFlow.actions().click(locators.firstTestOwnerAction(page),
                "class detail opens");
        Locator scenarioCard = locators.visibleScenarios(locators.visibleTestDetail(page)).first();
        if (scenarioCard.count() == 0) {
            return new ReportAuditSnapshot.RequestEvidence(false, 0, 0, 0,
                    new ReportAuditSnapshot.JsonEvidence("", "",
                            List.of("Tests has no visible scenario card")));
        }
        AtfLocator scenarioTarget = locators.scenarioStateTarget(scenarioCard);
        auditFlow.actions().waitForVisible(scenarioTarget);
        auditFlow.actions().waitForVisible(new AtfLocator(
                locators.boundScenarioHeader(scenarioCard),
                ".test-content-detail .card[data-atf-scenario-id] > .card-header"
                        + "[data-atf-scenario-collapse-bound='true']"));
        Locator testBody = locators.visiblePhase(scenarioCard, ReportUiContract.Phase.TEST_BODY).first();
        for (int attempt = 0; attempt < 3 && testBody.count() == 0; attempt++) {
            auditFlow.actions().click(locators.scenarioNodeAction(scenarioCard),
                    "scenario is expanded");
            auditFlow.actions().waitForAttribute(page, scenarioTarget,
                    "data-atf-scenario-open", "true", "scenario expansion is ready");
            testBody = locators.visiblePhase(scenarioCard, ReportUiContract.Phase.TEST_BODY).first();
        }
        AtfLocator testBodyTarget = new AtfLocator(testBody,
                ReportUiContract.phaseSelector(ReportUiContract.Phase.TEST_BODY));
        auditFlow.actions().waitForVisible(testBodyTarget);
        auditFlow.actions().waitForVisible(new AtfLocator(
                locators.boundPhaseHeader(testBody),
                ".test-content-detail [data-atf-node-role='phase'] > .card-header"
                        + "[data-atf-phase-header-bound='true']"));
        Locator phaseNode = locators.nodeControl(locators.nodeHeader(testBody));
        auditFlow.actions().waitForVisible(new AtfLocator(phaseNode,
                ReportUiContract.phaseSelector(ReportUiContract.Phase.TEST_BODY)
                        + " > .card-header .node"));
        if (!"true".equals(locators.nodeHeader(testBody).getAttribute("aria-expanded"))) {
            auditFlow.actions().click(locators.phaseNodeAction(testBody),
                    "TEST BODY is expanded");
        }
        Locator requests = locators.requestsInPhase(testBody);
        if (requests.count() == 0) {
            return new ReportAuditSnapshot.RequestEvidence(false, 0, 0, 0,
                    new ReportAuditSnapshot.JsonEvidence("", "", List.of("TEST BODY has no request")));
        }
        int selectedRequest = 0;
        for (int index = 0; index < requests.count(); index++) {
            if (locators.screenshotLinks(requests.nth(index)).count() > 0) {
                selectedRequest = index;
                break;
            }
        }
        Locator request = requests.nth(selectedRequest);
        if (!"true".equals(request.getAttribute("data-atf-node-open"))) {
            auditFlow.actions().click(locators.requestNodeToggleAction(request),
                    "request evidence is expanded");
        }
        expandDisclosure(locators.requestEvidenceDisclosure(request),
                locators.requestEvidenceDisclosureAction(request), "request evidence");
        expandDisclosure(locators.responseEvidenceDisclosure(request),
                locators.responseEvidenceDisclosureAction(request), "response evidence");
        expandDisclosure(locators.executionEvidenceDisclosure(request),
                locators.executionEvidenceDisclosureAction(request), "execution evidence");
        AtfLocator requestTable = new AtfLocator(locators.visibleRequestTable(request),
                ":scope table.helm-request-table:visible");
        AtfLocator responseTable = new AtfLocator(locators.visibleResponseTable(request),
                ":scope table.helm-trace-table:visible");
        auditFlow.actions().findElement(requestTable);
        auditFlow.actions().findElement(responseTable);
        auditFlow.actions().expectText(requestTable, "FIELD");
        ApiJsonEvidenceAuditor.Result evidence = jsonAuditor.inspect(request);
        int requestTables = locators.visibleRequestTable(request).count();
        int responseTables = locators.visibleResponseTable(request).count();
        int executionTables = locators.visibleExecutionTable(request).count();
        return new ReportAuditSnapshot.RequestEvidence(requestTables > 0 && responseTables > 0
                && executionTables > 0, requestTables, responseTables, executionTables,
                new ReportAuditSnapshot.JsonEvidence(evidence.requestBody(), evidence.responseBody(),
                        evidence.violations()));
    }

    private void expandDisclosure(Locator summary, AtfLocator action, String target) {
        if (summary.count() == 0) return;
        Locator disclosure = locators.parentDisclosure(summary);
        if (disclosure.getAttribute("open") == null) {
            auditFlow.actions().click(action, target + " is expanded");
        }
    }
}
