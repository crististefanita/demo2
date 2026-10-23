package atf.ui.tests.extent.support.inspection.reconciliation;

import atf.api.core.http.request.HttpMethod;
import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.api.contract.ApiReportEvidenceField;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.ui.tests.extent.support.api.ApiReportUiFlow;
import atf.ui.tests.extent.support.audit.ReportAuditContext;
import atf.ui.tests.extent.support.audit.api.ApiReportContractValidation;
import atf.ui.tests.extent.support.audit.mock.MockReportContractAuditor;
import atf.ui.tests.extent.support.config.UiConfiguration;
import atf.ui.tests.extent.support.lifecycle.UiAuditFlow;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import atf.ui.tests.extent.support.section.extract.EvidenceSectionExtractor;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static atf.api.reporting.support.MockApiReportContractFixtures.acceptedInput;
import static atf.api.reporting.support.MockApiReportContractFixtures.acceptedPostRoute;
import static atf.api.reporting.support.MockApiReportContractFixtures.acceptedUrl;
import static atf.api.reporting.support.MockApiReportContractFixtures.configuration;
import static atf.api.reporting.support.MockApiReportContractFixtures.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the same DOM adapters as E2E against a small, controlled Extent publication. */
public final class ApiReportRenderedEvidenceProbe {
    private static final String OWNER = "RenderedApiEvidence";
    private static final String AUDIT = "RenderedEvidenceAudit";
    private static final ReportUiLocators LOCATORS = new ReportUiLocators();

    private ApiReportRenderedEvidenceProbe() { }

    public static void verifyMetadata(boolean missingRequestQuery) {
        Path report = publish(missingRequestQuery ? "rendered-api-query-negative.html" : "rendered-api-fields.html",
                missingRequestQuery);
        var policy = UiConfiguration.defaults(report.getParent()).withReportDirectory(report.getParent());
        try (var context = ReportAuditContext.open(policy, report, AUDIT)) {
            var audit = audit(context, policy);
            var flow = new ApiReportUiFlow(audit, LOCATORS);
            var page = audit.openTab(ReportUiContract.Tab.TESTS.hash());
            var owner = flow.openOwner(page, OWNER);
            assertEquals(1, flow.expandOwnerRequests(owner));
            var request = LOCATORS.requests(owner.detail()).first();
            assertEquals(acceptedUrl(), LOCATORS.responseUrl(request), "response URL remains independently observable");
            assertEquals(List.of(String.valueOf(acceptedInput().actualStatus())),
                    ApiReportEvidenceField.RESPONSE_STATUS.valuesIn(LOCATORS.executionFields(request)));
            var validator = new ApiReportContractValidation(LOCATORS,
                    new MockReportContractAuditor(configuration(acceptedPostRoute())));
            var result = validator.inspect(owner);
            assertEquals(1, result.requestCount());
            if (missingRequestQuery) {
                assertFalse(result.violations().isEmpty());
                assertTrue(result.violations().stream().anyMatch(value -> value.contains("expected query mode")),
                        () -> result.violations().toString());
            } else {
                assertTrue(result.violations().isEmpty(), () -> result.violations().toString());
            }
            assertTrue(context.browserDiagnostics().valid(), () -> context.browserDiagnostics().violations().toString());
        }
    }

    public static void verifyMissingResponseDisclosure() {
        Path report = publish("rendered-api-missing-response-disclosure.html", false);
        var policy = UiConfiguration.defaults(report.getParent()).withReportDirectory(report.getParent());
        try (var context = ReportAuditContext.open(policy, report, AUDIT)) {
            var audit = audit(context, policy);
            var owner = new ApiReportUiFlow(audit, LOCATORS)
                    .openOwner(audit.openTab(ReportUiContract.Tab.TESTS.hash()), OWNER);
            assertEquals(1, LOCATORS.requests(owner.detail()).count());
            var request = LOCATORS.requests(owner.detail()).first();
            var responseDisclosure = LOCATORS.responseEvidenceDisclosure(request);
            assertEquals(1, responseDisclosure.count(), "the baseline fixture must contain response evidence");
            LOCATORS.parentDisclosure(responseDisclosure).evaluate("details => details.remove()");

            var validator = new ApiReportContractValidation(LOCATORS,
                    new MockReportContractAuditor(configuration(acceptedPostRoute())));
            var result = validator.inspect(owner);
            assertFalse(result.compliant(), "missing response evidence must not pass as a valid exchange");
            assertTrue(result.violations().stream().anyMatch(value -> value.contains("response JSON body count")),
                    () -> result.violations().toString());
            assertEquals(0, LOCATORS.responseDetails(request).count(),
                    "the negative fixture removes the rendered response disclosure itself");
            assertTrue(context.browserDiagnostics().valid(),
                    () -> context.browserDiagnostics().violations().toString());
        }
    }

    public static void verifyPhaseNavigation() {
        Path report = publish("rendered-api-phase-navigation.html", false);
        var policy = UiConfiguration.defaults(report.getParent()).withReportDirectory(report.getParent());
        try (var context = ReportAuditContext.open(policy, report, AUDIT)) {
            var audit = audit(context, policy);
            var extractor = new EvidenceSectionExtractor(audit, LOCATORS);
            var page = audit.openTab(ReportUiContract.Tab.TESTS.hash());
            for (int repeat = 0; repeat < 2; repeat++) {
                var evidence = extractor.extractFirstRequestWithEvidence(page);
                assertTrue(evidence.tablesVisible(), () -> "request/response/execution evidence: " + evidence);
                assertEquals(1, evidence.requestTableCount());
                assertEquals(1, evidence.responseTableCount());
                assertEquals(1, evidence.executionTableCount());
            }
            var inspectedActions = audit.actions().snapshot();
            assertTrue(inspectedActions.size() > 5, "the evidence trace includes phase and request disclosure actions");
            assertSame(page, audit.evidencePage(ReportUiContract.Tab.TESTS));
            assertEquals(inspectedActions, audit.actions().snapshot(), "capture must not reset the action trace");
            var inspectedRequest = LOCATORS.requests(LOCATORS.visibleTestDetail(page)).first();
            assertTrue(LOCATORS.visibleResponseTable(inspectedRequest).isVisible(),
                    "capture must retain the expanded evidence rather than a freshly collapsed page");
            audit.actions().click(LOCATORS.navTabAction(page, ReportUiContract.Tab.OVERVIEW), "leave Tests");
            page = audit.evidencePage(ReportUiContract.Tab.TESTS);
            assertTrue(audit.actions().snapshot().size() > inspectedActions.size());
            assertEquals(inspectedActions, audit.actions().snapshot().subList(0, inspectedActions.size()),
                    "cross-tab evidence navigation must retain the preceding audit actions");
            assertTrue(extractor.extractFirstRequestWithEvidence(page).tablesVisible());
            page = audit.openTab(ReportUiContract.Tab.TESTS.hash());
            assertTrue(extractor.extractFirstRequestWithEvidence(page).tablesVisible(), "fresh document navigation rebinds the phase");
            assertTrue(context.browserDiagnostics().valid(), () -> context.browserDiagnostics().violations().toString());
        }
    }

    public static void verifyMissingPhase() {
        Path report = publish("rendered-api-missing-test-body.html", false, ReportUiContract.Phase.SETUP);
        var policy = UiConfiguration.defaults(report.getParent()).withReportDirectory(report.getParent());
        try (var context = ReportAuditContext.open(policy, report, AUDIT)) {
            var audit = audit(context, policy);
            var page = audit.openTab(ReportUiContract.Tab.TESTS.hash());
            var evidence = new EvidenceSectionExtractor(audit, LOCATORS).extractFirstRequestWithEvidence(page);
            assertFalse(evidence.tablesVisible());
            assertEquals(0, evidence.requestTableCount());
            assertEquals(List.of("TEST BODY is missing"), evidence.jsonEvidence().violations());
        }
    }

    private static UiAuditFlow audit(ReportAuditContext context, UiConfiguration policy) {
        return new UiAuditFlow(context, policy, AUDIT, ApiReportRenderedEvidenceProbe.class.getPackageName(),
                List.of("component"), "the controlled API report is available", "Evidence audit failed", OWNER);
    }

    private static Path publish(String fileName, boolean missingRequestQuery) {
        return publish(fileName, missingRequestQuery, ReportUiContract.Phase.TEST_BODY);
    }

    private static Path publish(String fileName, boolean missingRequestQuery, ReportUiContract.Phase phaseKind) {
        Path report = TestReportPaths.report(fileName);
        var input = acceptedInput();
        var route = acceptedPostRoute();
        Duration elapsed = Duration.ofMillis(8);
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario(OWNER, "controlled API evidence", List.of("api", "component"));
            var phase = scenario.startPhase(phaseKind.displayName());
            var request = phase.startRequest(input.requestTitle(), HttpMethod.POST.value(), route.path());
            input.requestFields().forEach(request::detail);
            request.detail(ApiReportEvidenceField.REQUEST_URL.key(),
                    missingRequestQuery ? acceptedUrl().split("\\?")[0] : acceptedUrl());
            input.responseFields().entrySet().stream()
                    .filter(entry -> !ApiReportEvidenceField.CONTENT_TYPE.matches(entry.getKey()))
                    .forEach(entry -> request.detail(entry.getKey(), entry.getValue()));
            request.detail("response.content.type", JSON);
            request.json("REQUEST JSON", input.requestBody());
            request.json("RESPONSE JSON", input.responseBody());
            var response = new ReportResponse(route.status(), route.body(), route.headers(), elapsed, acceptedUrl());
            var transport = new ReportRequest(route.method(), route.path(), Map.of(), route.requiredHeaders(),
                    route.authProfile(), input.requestBody());
            request.execution(new ReportExecutionEvidence(response, 1, elapsed, List.of(),
                    List.of(new ReportAttemptRecord(1, route.status(), elapsed, "SUCCESS", "controlled fixture")),
                    transport, ReportTransportProvenance.ACCEPTANCE_MOCK));
            request.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }
}
