package atf.ui.tests.exploratory;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.support.ReportAuditContext;
import atf.ui.tests.support.UiAuditLifecycle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static atf.ui.tests.support.ReportAuditChecks.check;
import static atf.ui.tests.support.ReportAuditChecks.number;
import static atf.ui.tests.support.ReportAuditChecks.status;

/**
 * Read-only exploratory audit for an already published Extent HTML file.
 *
 * <p>This class never executes API setup and never edits the input artifact.
 * The input is resolved from {@code -Datf.ui.report} first, then from
 * {@code ui.audit.inputReport} in {@code atf.yaml}. The test publishes its
 * own UI audit report containing Playwright actions, grouped screen
 * validations and Base64 screenshot links.</p>
 */
@EnabledIf("isConfigured")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Existing Extent report exploratory audit")
class ExistingExtentReportExploratoryTest {
    private UiConfiguration configuration;
    private Path inputReport;
    private Path auditReport;
    private ReportAuditContext auditContext;
    private PlaywrightActionSequence activeActions;

    static boolean isConfigured() {
        UiConfiguration loaded = AtfUiConfigurationLoader.load();
        return loaded.enabled() && loaded.auditInputReport().isPresent();
    }

    @BeforeAll
    @DisplayName("GIVEN an existing Extent artifact WHEN debug UI starts THEN the input is opened read-only")
    void openConfiguredReport() {
        configuration = AtfUiConfigurationLoader.load();
        inputReport = configuration.auditInputReport().orElseThrow(() ->
                new IllegalStateException("No existing Extent report configured; set -Datf.ui.report "
                        + "or ui.audit.inputReport in atf.yaml"));
        assertTrue(Files.isRegularFile(inputReport), "Configured Extent report does not exist: " + inputReport);
        auditContext = ReportAuditContext.open(configuration, inputReport,
                "existing-ui-atf-extent-report.html");
        auditReport = auditContext.reportPath();
        System.out.println("[ATF UI][EXISTING-REPORT] input=" + inputReport);
    }

    @AfterAll
    void closeAudit() throws Exception {
        if (auditContext != null) {
            auditContext.close();
            System.out.println("[ATF UI][EXISTING-REPORT] audit-report=" + auditReport);
        }
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN Overview and Tests are reconciled THEN static sections and dynamic totals agree")
    void overviewAndTestsReconcileDynamically() {
        audit("Existing report — Overview ↔ Tests reconciliation", audit -> {
            Page testsPage = openTab("#test-view");
            TestsSnapshot tests = discoverTests(testsPage);
            Page overviewPage = openTab("#dashboard-view");
            OverviewSnapshot overview = inspectOverview(overviewPage.locator("#dashboard-view"));
            UiScreenValidation.Evidence evidence = capture(overviewPage, "Overview and Tests reconciliation");

            boolean valid = overview.complete()
                    && tests.ownerCount() > 0
                    && tests.scenarioCount() > 0
                    && overview.classCount() == tests.ownerCount()
                    && overview.scenarioCount() == tests.scenarioCount()
                    && overview.auditScenarioTotal() == tests.scenarioCount();
            audit.screen("Overview contains the complete static contract and reconciles dynamic totals",
                    "#dashboard-view ↔ #test-view", evidence, List.of(
                            check("Overview sections", "all characteristic sections",
                                    String.valueOf(overview.sectionCount()), overview.complete()),
                            check("Test owners", "> 0", String.valueOf(tests.ownerCount()), tests.ownerCount() > 0),
                            check("Scenario total", String.valueOf(tests.scenarioCount()),
                                    String.valueOf(overview.scenarioCount()), overview.scenarioCount() == tests.scenarioCount()),
                            check("Scenario audit table", String.valueOf(tests.scenarioCount()),
                                    String.valueOf(overview.auditScenarioTotal()), overview.auditScenarioTotal() == tests.scenarioCount())),
                    status(valid));
            assertAll(
                    () -> assertTrue(overview.complete(), "Overview is missing a characteristic section"),
                    () -> assertTrue(tests.ownerCount() > 0),
                    () -> assertTrue(tests.scenarioCount() > 0),
                    () -> assertTrue(overview.classCount() == tests.ownerCount()),
                    () -> assertTrue(overview.scenarioCount() == tests.scenarioCount()),
                    () -> assertTrue(overview.auditScenarioTotal() == tests.scenarioCount()));
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN every discovered class and phase is expanded THEN UI nodes remain auditable")
    void testsTreeIsDynamicallyExpandable() {
        audit("Existing report — dynamic Tests tree", audit -> {
            Page page = openTab("#test-view");
            TestsSnapshot snapshot = discoverTests(page);
            ExpansionSnapshot expansion = expandEveryOwnerScenarioAndPhase(page);
            UiScreenValidation.Evidence evidence = capture(page, "Dynamic Tests tree after expansion");
            boolean valid = snapshot.ownerCount() > 0
                    && snapshot.scenarioCount() > 0
                    && snapshot.phaseCount() >= snapshot.scenarioCount()
                    && snapshot.validationCount() > 0
                    && snapshot.disclosureCount() > 0;
            boolean exhaustive = expansion.ownerCount() == snapshot.ownerCount()
                    && expansion.scenarioCount() == snapshot.scenarioCount()
                    && expansion.phaseCount() == snapshot.phaseCount()
                    && expansion.expandedScenarioCount() == snapshot.scenarioCount()
                    && expansion.expandedPhaseCount() == snapshot.phaseCount();
            audit.screen("Every discovered class, scenario and phase is expandable",
                    "#test-view", evidence, List.of(
                            check("Class owners", "> 0", String.valueOf(snapshot.ownerCount()), snapshot.ownerCount() > 0),
                            check("Scenarios", "> 0", String.valueOf(snapshot.scenarioCount()), snapshot.scenarioCount() > 0),
                            check("Phases", ">= scenarios", String.valueOf(snapshot.phaseCount()), snapshot.phaseCount() >= snapshot.scenarioCount()),
                            check("Validation nodes", "> 0", String.valueOf(snapshot.validationCount()), snapshot.validationCount() > 0),
                            check("Disclosure controls", "> 0", String.valueOf(snapshot.disclosureCount()), snapshot.disclosureCount() > 0),
                            check("All discovered nodes expanded", "owners/scenarios/phases reconciled",
                                    expansion.toString(), exhaustive)),
                    status(valid && exhaustive));
            assertAll(
                    () -> assertTrue(valid, "The existing report does not expose an auditable dynamic Tests tree"),
                    () -> assertTrue(exhaustive, "The Tests audit did not expand every discovered owner, scenario and phase"));
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN categories and Fails are explored THEN navigation is dynamic and actionable")
    void categoriesAndFailsAreActionable() {
        audit("Existing report — category and failure navigation", audit -> {
            Page page = openTab("#category-view");
            Locator categories = page.locator("#category-view .test-list .test-item");
            int categoryCount = categories.count();
            if (categoryCount > 0) activeActions.click(categories.first(),
                    "#category-view .test-list .test-item:first-of-type", "annotation filter opens");
            Locator linked = page.locator("#category-view a.linked:visible");
            int linkedScenarios = linked.count();
            boolean themed = page.locator("#category-view .test-list").evaluate(
                    "element => { const s=getComputedStyle(element); return s.backgroundColor + ' / ' + s.color; }")
                    .toString().contains("rgb(");
            if (linkedScenarios > 0) activeActions.click(linked.first(),
                    "#category-view a.linked:first-of-type", "linked scenario opens in Tests");
            page = openTab("#atf-fails-view");
            Locator fails = page.locator("#atf-fails-view");
            int failureRows = fails.locator("#atf-failure-list .name").count();
            int failureLinks = fails.locator("a.atf-failure-open").count();
            boolean hasFailures = failureRows > 0;
            boolean emptyState = fails.locator("#atf-failure-list .atf-fails-empty").count() == 1;
            boolean failureStateValid = hasFailures
                    ? failureLinks == failureRows
                    && fails.locator(".atf-failure-index-cause .atf-failure-value").count() >= failureRows
                    : emptyState && failureLinks == 0;
            UiScreenValidation.Evidence evidence = capture(page, "Categories and Fails navigation");
            boolean valid = categoryCount > 0 && linkedScenarios > 0 && themed
                    && fails.count() > 0 && failureStateValid;
            audit.screen("Categories remain linked and Fails remains a valid optional view",
                    "#category-view → #atf-fails-view", evidence, List.of(
                            check("Categories", "> 0", String.valueOf(categoryCount), categoryCount > 0),
                            check("Linked scenarios", "> 0", String.valueOf(linkedScenarios), linkedScenarios > 0),
                            check("Dark report surface", "styled", themed ? "styled" : "missing", themed),
                            check("Fails view", "present", String.valueOf(fails.count()), fails.count() > 0),
                            check("Failure rows", "zero or dynamic", String.valueOf(failureRows), failureRows >= 0),
                            check("Failure navigation contract",
                                    hasFailures ? "each failure has cause + Open in Tests" : "empty state and no links",
                                    hasFailures ? failureRows + " causes + " + failureLinks + " links"
                                            : (emptyState ? "empty state" : "missing empty state"),
                                    failureStateValid)),
                    status(valid));
            assertTrue(valid, "Category/Fails navigation is not structurally valid");
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN visual evidence is expanded THEN Base64 screenshot links and assertions are grouped")
    void visualEvidenceIsLinkedAndGrouped() {
        audit("Existing report — grouped screen evidence", audit -> {
            Page page = openTab("#test-view");
            Locator owners = page.locator("#test-view .test-list .test-item");
            assertTrue(owners.count() > 0);
            activeActions.click(owners.first(), "#test-view .test-list .test-item:first-of-type",
                    "class detail opens");
            Locator detail = page.locator("#test-view .test-content-detail:visible").last();
            Locator validations = detail.locator("table.helm-validation-table");
            Locator disclosures = detail.locator("details");
            Locator actionSequences = detail.locator("details.atf-ui-action-sequence");
            int screenshotLinks = actionSequences.locator("a.atf-ui-action-screenshot-link[href^=\"data:image/png;base64,\"]").count();
            Locator screenshotAnchors = actionSequences.locator(
                    "a.atf-ui-action-screenshot-link[href^=\"data:image/png;base64,\"]");
            activeActions.findElement(validations.first(), "table.helm-validation-table:first-of-type");
            activeActions.expectText(validations.first(), "table.helm-validation-table:first-of-type",
                    "ASSERTION");
            Locator visibleDisclosure = detail.locator("details:visible").first();
            if (visibleDisclosure.count() > 0 && !"true".equals(visibleDisclosure.getAttribute("open"))) {
                activeActions.click(visibleDisclosure.locator("summary"),
                        "details:visible > summary", "screen evidence is expanded");
            }
            Locator visibleActionSequence = detail.locator("details.atf-ui-action-sequence:visible").first();
            if (visibleActionSequence.count() > 0
                    && !"true".equals(visibleActionSequence.getAttribute("open"))) {
                activeActions.click(visibleActionSequence.locator("summary"),
                        "details.atf-ui-action-sequence:visible > summary", "action table is expanded");
            }
            Locator visibleActionTable = actionSequences.locator(".atf-ui-action-table:visible").first();
            String tableGeometry = visibleActionTable.count() == 0 ? "missing"
                    : visibleActionTable.evaluate("element -> { const style = getComputedStyle(element); "
                    + "return style.tableLayout + '|' + Math.round(element.getBoundingClientRect().width); }")
                    .toString();
            int tableWidth = tableGeometry.startsWith("fixed|")
                    ? Integer.parseInt(tableGeometry.substring("fixed|".length())) : 0;
            boolean fixedActionTable = tableGeometry.startsWith("fixed|") && tableWidth >= 760;
            UiScreenValidation.Evidence evidence = capture(page, "Base64 screenshot link and grouped validations");
            boolean inputHasUiActions = actionSequences.count() > 0;
            boolean valid = validations.count() > 0 && disclosures.count() > 0
                    && (!inputHasUiActions || screenshotLinks > 0)
                    && (!inputHasUiActions || fixedActionTable)
                    && actionSequences.locator("img[src^=\"data:image\"]").count() == 0
                    && (!inputHasUiActions || screenshotAnchors.count() == screenshotLinks)
                    && (!inputHasUiActions || screenshotAnchors.filter(
                            new Locator.FilterOptions().setHasText("📷 base64 img")).count() == screenshotLinks)
                    && evidence.hasScreenshotPayload();
            audit.screen("One visual state carries one Base64 screenshot link and grouped validation evidence",
                    "#test-view", evidence, List.of(
                            check("Validation tables", "> 0", String.valueOf(validations.count()), validations.count() > 0),
                            check("Collapsible evidence", "> 0", String.valueOf(disclosures.count()), disclosures.count() > 0),
                            check("Existing Base64 screenshot links", inputHasUiActions ? "> 0" : "optional for API input",
                                    String.valueOf(screenshotLinks), !inputHasUiActions || screenshotLinks > 0),
                            check("Playwright table geometry", "fixed layout; width >= 760px",
                                    tableGeometry, !inputHasUiActions || fixedActionTable),
                            check("New Base64 screenshot", "present", String.valueOf(evidence.hasScreenshotPayload()), evidence.hasScreenshotPayload())),
                    status(valid));
            assertTrue(valid, "Existing report visual evidence is not linked/grouped as required");
        });
    }

    private TestsSnapshot discoverTests(Page page) {
        Locator tests = page.locator("#test-view");
        Locator owners = tests.locator(".test-list .test-item");
        int scenarios = 0, phases = 0, validations = 0, disclosures = 0;
        for (int index = 0; index < owners.count(); index++) {
            activeActions.click(owners.nth(index),
                    ".test-list .test-item:nth-of-type(" + (index + 1) + ")", "class detail opens");
            Locator detail = tests.locator(".test-content-detail:visible").last();
            activeActions.waitForVisible(detail, ".test-content-detail:visible");
            scenarios += detail.locator("[data-atf-scenario-id]").count();
            phases += detail.locator("[data-atf-node-role='phase']").count();
            validations += detail.locator("table.helm-validation-table").count();
            disclosures += detail.locator("details").count()
                    + detail.locator("[data-atf-node-role] > .card-header .node").count();
        }
        return new TestsSnapshot(owners.count(), scenarios, phases, validations, disclosures);
    }

    private ExpansionSnapshot expandEveryOwnerScenarioAndPhase(Page page) {
        Locator tests = page.locator("#test-view");
        Locator owners = tests.locator(".test-list .test-item");
        int ownerCount = owners.count();
        int scenarioCount = 0;
        int phaseCount = 0;
        int expandedScenarioCount = 0;
        int expandedPhaseCount = 0;
        for (int ownerIndex = 0; ownerIndex < ownerCount; ownerIndex++) {
            Locator owner = tests.locator(".test-list .test-item").nth(ownerIndex);
            activeActions.click(owner, ".test-list .test-item:nth-of-type(" + (ownerIndex + 1) + ")",
                    "class detail opens");
            Locator detail = tests.locator(".test-content-detail:visible").last();
            activeActions.waitForVisible(detail, ".test-content-detail:visible");
            Locator scenarios = detail.locator("[data-atf-scenario-id]");
            scenarioCount += scenarios.count();
            for (int scenarioIndex = 0; scenarioIndex < scenarios.count(); scenarioIndex++) {
                Locator scenario = scenarios.nth(scenarioIndex);
                expandNode(scenario, "data-atf-scenario-open");
                if ("true".equals(scenario.getAttribute("data-atf-scenario-open"))) {
                    expandedScenarioCount++;
                }
                Locator phases = scenario.locator("[data-atf-node-role='phase']");
                phaseCount += phases.count();
                for (int phaseIndex = 0; phaseIndex < phases.count(); phaseIndex++) {
                    Locator phase = phases.nth(phaseIndex);
                    expandNode(phase, "data-atf-phase-open");
                    if ("true".equals(phase.getAttribute("data-atf-phase-open"))) {
                        expandedPhaseCount++;
                    }
                }
            }
        }
        return new ExpansionSnapshot(ownerCount, scenarioCount, phaseCount,
                expandedScenarioCount, expandedPhaseCount);
    }

    private void expandNode(Locator card, String stateAttribute) {
        Locator node = card.locator(":scope > .card-header .node");
        if (node.count() == 0) return;
        if (!"true".equals(card.getAttribute(stateAttribute))) {
            activeActions.click(node, ":scope > .card-header .node", "node is expanded");
        }
    }

    private OverviewSnapshot inspectOverview(Locator overview) {
        String[] selectors = {"#parent-analysis", "#timeline", "#atf-execution-metadata",
                "#atf-tag-audit", ".category-container", ".sysenv-container",
                "#atf-audit-scenarios", "#atf-audit-packages", "#atf-audit-tags"};
        int sections = 0;
        for (String selector : selectors) if (overview.locator(selector).count() > 0) sections++;
        int classes = metric(overview, "Test classes in this run");
        int scenarios = metric(overview, "Scenarios in this run");
        Locator rows = overview.locator("#atf-audit-scenarios tbody tr");
        int auditTotal = 0;
        for (int index = 0; index < rows.count(); index++) {
            Locator cells = rows.nth(index).locator("td");
            if (cells.count() >= 2) auditTotal += number(cells.nth(1).textContent());
        }
        return new OverviewSnapshot(sections, classes, scenarios, auditTotal,
                sections == selectors.length && classes > 0 && scenarios > 0 && rows.count() > 0);
    }

    private static int metric(Locator overview, String label) {
        Locator metrics = overview.locator("#atf-execution-metadata-grid .atf-secondary-metric");
        for (int index = 0; index < metrics.count(); index++) {
            Locator metric = metrics.nth(index);
            if (label.equals(metric.locator("small").textContent().trim())) {
                return number(metric.locator("strong").textContent());
            }
        }
        return 0;
    }

    private Page openTab(String hash) {
        return auditContext.openTab(hash, activeActions);
    }

    private UiScreenValidation.Evidence capture(Page page, String title) {
        UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                page, configuration.screenValidation(), title);
        System.out.println("[ATF UI][EXISTING-REPORT] screen-validation title=" + title
                + " base64Link=" + evidence.hasScreenshotPayload()
                + " chars=" + evidence.base64Png().length());
        activeActions.observation("Screenshot", "current page", "visual state captured",
                "Base64 screenshot link");
        return evidence;
    }

    private void audit(String title, AuditAction action) {
        UiAudit audit = beginAudit(title);
        try {
            action.run(audit);
            audit.pass();
        } catch (AssertionError | RuntimeException failure) {
            audit.fail(failure);
            throw failure;
        }
    }

    private UiAudit beginAudit(String title) {
        activeActions = auditContext.newActionSequence();
        var scenario = auditContext.startScenario("ExistingExtentReportExploratoryTest",
                "atf.ui.tests.exploratory", title, List.of("ui", "exploratory", "existing-report"));
        var setup = scenario.startPhase("SETUP");
        setup.step("GIVEN", "the configured Extent report is loaded read-only",
                inputReport.getFileName().toString(), null,
                List.of(check("Input report", "published HTML", inputReport.toString(), Files.isRegularFile(inputReport))),
                AtfReportStatus.PASS);
        setup.complete(AtfReportStatus.PASS);
        return new UiAudit(scenario, scenario.startPhase("TEST BODY"));
    }

    @FunctionalInterface
    private interface AuditAction { void run(UiAudit audit); }

    private final class UiAudit {
        private final UiAuditLifecycle lifecycle;

        private UiAudit(UiExtentReportSession.UiScenario scenario, UiExtentReportSession.UiPhase body) {
            this.lifecycle = new UiAuditLifecycle(scenario, body);
        }

        private void screen(String title, String target, UiScreenValidation.Evidence evidence,
                            List<AtfAssertion> assertions, AtfReportStatus status) {
            lifecycle.screenValidation(title, target, evidence.base64Png(), assertions,
                    activeActions == null ? List.of() : activeActions.drain(), status);
        }

        private void pass() {
            lifecycle.pass();
        }

        private void fail(Throwable failure) {
            lifecycle.fail("UI exploration failed", inputReport.toString(),
                    activeActions == null ? List.of() : activeActions.drain(), failure);
        }
    }

    private record TestsSnapshot(int ownerCount, int scenarioCount, int phaseCount,
                                 int validationCount, int disclosureCount) { }

    private record ExpansionSnapshot(int ownerCount, int scenarioCount, int phaseCount,
                                     int expandedScenarioCount, int expandedPhaseCount) { }

    private record OverviewSnapshot(int sectionCount, int classCount, int scenarioCount,
                                    int auditScenarioTotal, boolean complete) { }
}
