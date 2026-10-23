package atf.ui.tests.extent.support.inspection.reconciliation;

import atf.api.core.http.request.HttpMethod;
import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.api.reporting.ui.validation.UiApiValidationEvidence;
import atf.ui.tests.extent.support.api.ApiReportUiFlow;
import atf.ui.tests.extent.support.audit.ReportAuditContext;
import atf.ui.tests.extent.support.config.UiConfiguration;
import atf.ui.tests.extent.support.lifecycle.UiAuditFlow;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static atf.api.reporting.contract.AtfGherkinKeyword.THEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Browser proof that mixed UI/API verdicts preserve their failure owner and replay link. */
public final class UiApiVerdictProjectionProbe {
    private static final String OWNER = "UiApiVerdictProjectionFixture";
    private static final String UI_PASS_API_FAIL = "UI passes while API cart contract fails";
    private static final String UI_FAIL_API_PASS = "UI notice fails while API contract passes";
    private static final ReportUiLocators LOCATORS = new ReportUiLocators();

    private UiApiVerdictProjectionProbe() { }

    public static void verifyMixedVerdictsAndFailsReplay() {
        Path report = publish();
        var policy = UiConfiguration.defaults(report.getParent()).withReportDirectory(report.getParent());
        try (var context = ReportAuditContext.open(policy, report, "ui-api-verdict-projection-audit")) {
            var audit = new UiAuditFlow(context, policy, OWNER,
                    UiApiVerdictProjectionProbe.class.getPackageName(), List.of("component", "ui", "api"),
                    "the controlled UI/API report is available", "mixed verdict audit failed", OWNER);
            var page = audit.openTab(ReportUiContract.Tab.TESTS.hash());
            var flow = new ApiReportUiFlow(audit, LOCATORS);
            var owner = flow.openOwner(page, OWNER);
            assertEquals(2, LOCATORS.scenarios(owner.detail()).count(),
                    "both independent verdict scenarios must remain under one test owner");

            assertScenarioStatus(owner.detail(), UI_PASS_API_FAIL);
            assertScenarioStatus(owner.detail(), UI_FAIL_API_PASS);

            for (var expected : List.of(
                    new FailureExpectation(UI_PASS_API_FAIL, "API cart quantity contract failed"),
                    new FailureExpectation(UI_FAIL_API_PASS, "UI confirmation notice is missing"))) {
                page = audit.openTab(ReportUiContract.Tab.FAILS.hash());
                Locator fails = LOCATORS.failsView(page);
                assertEquals(2, LOCATORS.failureItems(fails).count(),
                        "Fails must project both failed scenarios, not merge their causes");
                Locator matching = LOCATORS.failureItemByName(fails, expected.scenario());
                assertEquals(1, matching.count(), "Fails must retain the scenario identity");
                audit.actions().click(LOCATORS.failureItemActionByName(page, expected.scenario()),
                        "inspect the independently failed UI/API scenario");
                Locator detail = LOCATORS.failureDetail(fails);
                assertTrue(detail.innerText().contains(expected.cause()),
                        () -> "Fails detail lost the owning cause: " + detail.innerText());
                Locator openLink = LOCATORS.failureDetailOpenLink(fails);
                assertEquals(1, openLink.count());
                String href = openLink.getAttribute("href");
                assertTrue(href != null && href.contains("scenario-index="),
                        "Open in Tests must preserve the failed scenario index");

                audit.actions().click(LOCATORS.failureDetailOpenAction(page),
                        "replay the selected failure in Tests");
                audit.actions().waitForVisible(LOCATORS.testsView(page), ReportUiContract.Tab.TESTS.hash());
                assertTrue(page.url().contains("#test-id=") && page.url().contains("scenario-index="),
                        "Fails deep-link must land on the owning Tests scenario");
                Locator selected = LOCATORS.visibleTestDetail(page);
                assertTrue(selected.isVisible());
                assertTrue(selected.innerText().contains(expected.scenario()),
                        "deep-link selected a sibling scenario instead of the failure owner");
            }
            assertTrue(context.browserDiagnostics().valid(),
                    () -> context.browserDiagnostics().violations().toString());
        }
    }

    private static void assertScenarioStatus(Locator ownerDetail, String scenarioTitle) {
        Locator scenarios = LOCATORS.scenarios(ownerDetail);
        for (int index = 0; index < scenarios.count(); index++) {
            Locator scenario = scenarios.nth(index);
            if (scenario.innerText().contains(scenarioTitle)) {
                assertTrue(LOCATORS.nodeStatusBadge(scenario).innerText().contains("Fail"),
                        () -> "scenario must aggregate the failing channel: " + scenarioTitle);
                return;
            }
        }
        throw new AssertionError("scenario is missing from the visible owner: " + scenarioTitle);
    }

    private static Path publish() {
        Path requested = TestReportPaths.report("ui-api-verdict-projection.html");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(requested.getParent(),
                requested.getFileName().toString(), atf.api.reporting.console.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            publishScenario(session, UI_PASS_API_FAIL,
                    assertion("UI cart checkpoint passes", "cart line visible", "cart line visible", true),
                    exchangeAssertion(false), AtfReportStatus.PASS);
            publishScenario(session, UI_FAIL_API_PASS,
                    assertion("UI confirmation notice is missing", "notice visible", "notice absent", false),
                    exchangeAssertion(true), AtfReportStatus.FAIL);
        }
        return report;
    }

    private static void publishScenario(UiExtentReportSession session, String title,
                                        AtfAssertion uiAssertion, AtfAssertion apiAssertion,
                                        AtfReportStatus uiStatus) {
        var scenario = session.startScenario(OWNER, "atf.ui.tests.extent.component.audit",
                title, List.of("ui", "api", "mixed-verdict"));
        var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
        var api = new UiApiValidationEvidence(List.of(), List.of(
                new UiApiValidationEvidence.Exchange(exchange(title), List.of(apiAssertion))));
        body.step(THEN, title, "/cart", null, List.of(uiAssertion), List.of(), api,
                ReportScreenshotPolicy.CapturePoint.CHECKPOINT, uiStatus);
        AtfReportStatus phaseStatus = uiStatus == AtfReportStatus.FAIL || !apiAssertion.passed()
                ? AtfReportStatus.FAIL : AtfReportStatus.PASS;
        body.complete(phaseStatus);
        scenario.complete(phaseStatus);
    }

    private static ReportExecutionEvidence exchange(String title) {
        String path = title.equals(UI_PASS_API_FAIL) ? "/cart/quantity" : "/checkout/confirmation";
        String url = "https://report-fixture.invalid" + path;
        Duration elapsed = Duration.ofMillis(5);
        var request = new ReportRequest(HttpMethod.GET.value(), path, Map.of(),
                Map.of("Accept", "application/json"), "none", "");
        var response = new ReportResponse(200, "{\"ok\":true}",
                Map.of("Content-Type", "application/json"), elapsed, url);
        return new ReportExecutionEvidence(response, 1, elapsed, List.of(),
                List.of(new ReportAttemptRecord(1, 200, elapsed, "SUCCESS", "controlled fixture")),
                request, ReportTransportProvenance.ACCEPTANCE_MOCK);
    }

    private static AtfAssertion exchangeAssertion(boolean passed) {
        return assertion(passed ? "API confirmation contract passes" : "API cart quantity contract failed",
                passed ? "confirmed" : "quantity=2", passed ? "confirmed" : "quantity=1", passed);
    }

    private static AtfAssertion assertion(String label, String expected, String actual, boolean passed) {
        return new AtfAssertion(label, expected, actual, passed);
    }

    private record FailureExpectation(String scenario, String cause) { }
}
