package atf.ui.tests.exploratory;

import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.mock.AtfMockTransport;
import atf.api.reporting.ExtentReportSession;
import atf.api.reporting.ReportArtifactInspector;
import atf.api.reporting.UiExtentReportSession;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.tests.support.ApiScenarioSetup;
import atf.api.tests.support.ApiScenarioSetupCatalog;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightUiSession;
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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static atf.ui.tests.support.ReportAuditChecks.check;
import static atf.ui.tests.support.ReportAuditChecks.number;
import static atf.ui.tests.support.ReportAuditChecks.status;

/**
 * Gherkin-shaped exploratory contracts for the real Extent artifact.
 *
 * <p>The report is generated in setup from every API scenario family. The
 * browser then consumes that exact artifact through the loopback debug UI;
 * no HTML fixture is injected and no selector-only smoke test is accepted.</p>
 */
@EnabledIfSystemProperty(named = "atf.ui.enabled", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("API Extent report exploratory acceptance")
@ApiScenarioSetup
class ApiExtentReportExploratoryTest {
    private static final URI API_BASE_URI = URI.create("https://atf.local");
    private UiConfiguration configuration;
    private Path apiInputReport;
    private Path uiReport;
    private ReportAuditContext auditContext;
    private List<String> configuredFamilies;
    private PlaywrightActionSequence activeActions;

    @BeforeAll
    @DisplayName("GIVEN setup, WHEN all API scenario families run, THEN an auditable Extent artifact is published")
    void givenAllApiScenariosGenerateTheReport() {
        given("ATF UI is enabled and the YAML browser policy is loaded");
        configuration = AtfUiConfigurationLoader.load();
        assertTrue(configuration.enabled(), "Exploratory UI tests require atf.ui.enabled=true");
        configuredFamilies = configuredFamilies();

        given("the API composition root uses the deterministic project transport");
        ApiModule api = ApiModuleFactory.fromTransport(
                AtfMockTransport.fromProjectConfiguration(), API_BASE_URI);
        ApiScenarioLifecycleRunner lifecycle = new ApiScenarioLifecycleRunner();

        given("the configured Extent session is opened");
        try (ExtentReportSession session = ExtentReportSession.openConfigured()) {
            apiInputReport = session.reportPath();
            when("the configured API scenario families are published in order: " + configuredFamilies);
            ApiScenarioSetupCatalog.publish(session, api, lifecycle, configuredFamilies);
        }

        then("the generated artifact is fresh and structurally compliant");
        assertTrue(Files.isRegularFile(apiInputReport), "API setup must publish an Extent report");
        var inspection = ReportArtifactInspector.inspectFresh(apiInputReport, Instant.now().minusSeconds(120));
        assertTrue(inspection.isCompliant(), () -> "Exploratory setup violations: " + inspection.violations());

        given("the exact generated artifact is published read-only for Playwright");
        auditContext = ReportAuditContext.open(configuration, apiInputReport,
                "ui-atf-extent-report.html");
        uiReport = auditContext.reportPath();
    }

    private List<String> configuredFamilies() {
        String[] declared = ApiExtentReportExploratoryTest.class
                .getAnnotation(ApiScenarioSetup.class).families();
        return declared.length == 0 ? configuration.apiScenarioFamilies() : List.of(declared);
    }

    @AfterAll
    void closeExplorationSurface() throws Exception {
        if (auditContext != null) {
            auditContext.close();
            System.out.println("[ATF UI][EXPLORATORY] ui-report=" + uiReport);
        }
    }

    @Test
    @DisplayName("GIVEN the generated report WHEN Overview is opened THEN every section is present and reconciles with Tests")
    void overviewIsCompleteAndReconcilesWithTests() {
        audit("Overview — dynamic report contract and cross-tab reconciliation", audit -> {
            given("the Tests tab is opened and every discovered class is inspected");
            TestsSnapshot testsSnapshot = inspectEveryTestClass(openTab("#test-view"));
            when("the Overview content is inspected");
            Page page = openTab("#dashboard-view");
            OverviewSnapshot overviewSnapshot = inspectOverview(page.locator("#dashboard-view"));
            String screenshot = screenshot(page);
            then("all Overview sections and dynamic totals match the Tests tab");
            boolean valid = overviewSnapshot.complete()
                    && overviewSnapshot.classCount() == testsSnapshot.classCount()
                    && overviewSnapshot.scenarioCount() == testsSnapshot.scenarioCount()
                    && overviewSnapshot.auditScenarioTotal() == testsSnapshot.scenarioCount();
            audit.step("THEN", "Overview is complete and reconciles with discovered Tests data",
                    "#dashboard-view ↔ #test-view", screenshot, List.of(
                            check("Overview sections", "all characteristic sections",
                                    String.valueOf(overviewSnapshot.sectionCount()), overviewSnapshot.complete()),
                            check("Test classes", String.valueOf(testsSnapshot.classCount()),
                                    String.valueOf(overviewSnapshot.classCount()), overviewSnapshot.classCount() == testsSnapshot.classCount()),
                            check("Scenarios", String.valueOf(testsSnapshot.scenarioCount()),
                                    String.valueOf(overviewSnapshot.scenarioCount()), overviewSnapshot.scenarioCount() == testsSnapshot.scenarioCount()),
                            check("Scenario audit total", String.valueOf(testsSnapshot.scenarioCount()),
                                    String.valueOf(overviewSnapshot.auditScenarioTotal()), overviewSnapshot.auditScenarioTotal() == testsSnapshot.scenarioCount())),
                    status(valid));
            assertAll(
                    () -> assertTrue(overviewSnapshot.complete(), "Overview is missing a characteristic section"),
                    () -> assertTrue(overviewSnapshot.classCount() == testsSnapshot.classCount()),
                    () -> assertTrue(overviewSnapshot.scenarioCount() == testsSnapshot.scenarioCount()),
                    () -> assertTrue(overviewSnapshot.auditScenarioTotal() == testsSnapshot.scenarioCount()));
        });
    }

    @Test
    @DisplayName("GIVEN the generated report WHEN Tests is opened THEN dynamic classes, phases, requests and validations are expandable")
    void testsTabDiscoversAndExpandsEveryClass() {
        audit("Tests — dynamic class, phase, request and validation contract", audit -> {
            given("the Tests tab is opened");
            Page page = openTab("#test-view");
            when("every class currently present in the report is selected");
            TestsSnapshot snapshot = inspectEveryTestClass(page);
            String screenshot = screenshot(page);
            then("each discovered class exposes scenario and lifecycle details");
            boolean valid = snapshot.classCount() > 0 && snapshot.scenarioCount() > 0
                    && snapshot.phaseCount() >= snapshot.scenarioCount()
                    && snapshot.requestCount() > 0 && snapshot.validationCount() > 0;
            audit.step("THEN", "all discovered Tests nodes are expandable and auditable",
                    "#test-view", screenshot, List.of(
                            check("Class owners", "> 0", String.valueOf(snapshot.classCount()), snapshot.classCount() > 0),
                            check("Scenarios", "> 0", String.valueOf(snapshot.scenarioCount()), snapshot.scenarioCount() > 0),
                            check("Lifecycle phases", ">= scenarios", String.valueOf(snapshot.phaseCount()), snapshot.phaseCount() >= snapshot.scenarioCount()),
                            check("Requests", "> 0", String.valueOf(snapshot.requestCount()), snapshot.requestCount() > 0),
                            check("Aggregate validations", "> 0", String.valueOf(snapshot.validationCount()), snapshot.validationCount() > 0)),
                    status(valid));
            assertTrue(valid, "The Tests tab does not expose the complete dynamic report tree");
        });
    }

    @Test
    @DisplayName("GIVEN the generated report WHEN every Tests owner is selected THEN every duration is numeric, formatted and structurally reconciled")
    void everyTestsNodeExposesReconciledDynamicDuration() {
        audit("Tests — duration contract for every dynamic node", audit -> {
            given("the fresh API report is opened in Tests and each discovered owner is selected");
            Page page = openTab("#test-view");
            Locator tests = page.locator("#test-view");
            Locator owners = tests.locator(".test-list .test-item");
            assertTrue(owners.count() > 0, "The generated API report has no dynamic test owner");

            DurationAuditSummary total = new DurationAuditSummary();
            for (int index = 0; index < owners.count(); index++) {
                Locator owner = tests.locator(".test-list .test-item").nth(index);
                activeActions.click(owner, ".test-list .test-item:nth-of-type(" + (index + 1) + ")",
                        "duration owner is selected");
                Locator detail = tests.locator(".test-content-detail:visible").last();
                activeActions.waitForVisible(detail, ".test-content-detail:visible");
                // The vendor renders the detail asynchronously; the runtime controller
                // deliberately rechecks at 0/150/500 ms before declaring aggregation stable.
                activeActions.waitForTimeout(page, 0.7, detail, ".test-content-detail:visible",
                        "duration aggregation settles after owner selection");
                DurationAuditSummary ownerSummary = inspectDurationContract(page);
                total.add(ownerSummary);
                int ownerIndex = index;
                assertTrue(ownerSummary.violations().isEmpty(),
                        () -> "Duration violations for owner " + ownerIndex + ": " + ownerSummary.violations());
            }

            String screenshot = screenshot(page);
            when("the duration markers, formatter output and parent-child totals are compared");
            boolean valid = total.ownerCount() == owners.count()
                    && total.nodeCount() > 0
                    && total.durationMarkerCount() == total.nodeCount()
                    && total.formattedBadgeCount() == total.nodeCount()
                    && total.aggregatedNodeCount() > 0
                    && total.violations().isEmpty();
            then("every Tests node has a stable duration contract after dynamic rendering");
            audit.step("THEN", "all dynamic Tests durations are numeric, formatted and reconciled",
                    "#test-view", screenshot, List.of(
                            check("Selected owners", String.valueOf(owners.count()), String.valueOf(total.ownerCount()),
                                    total.ownerCount() == owners.count()),
                            check("Structural nodes", "> 0", String.valueOf(total.nodeCount()), total.nodeCount() > 0),
                            check("Numeric duration markers", String.valueOf(total.nodeCount()),
                                    String.valueOf(total.durationMarkerCount()), total.durationMarkerCount() == total.nodeCount()),
                            check("Formatted duration badges", String.valueOf(total.nodeCount()),
                                    String.valueOf(total.formattedBadgeCount()), total.formattedBadgeCount() == total.nodeCount()),
                            check("Aggregated parent nodes", "> 0", String.valueOf(total.aggregatedNodeCount()),
                                    total.aggregatedNodeCount() > 0),
                            check("Parent-child violations", "0", String.valueOf(total.violations().size()),
                                    total.violations().isEmpty())),
                    status(valid));
            assertTrue(valid, () -> "Dynamic duration contract failed: owners=" + total.ownerCount()
                    + ", nodes=" + total.nodeCount()
                    + ", markers=" + total.durationMarkerCount()
                    + ", formatted=" + total.formattedBadgeCount()
                    + ", aggregated=" + total.aggregatedNodeCount()
                    + ", violations=" + total.violations());
        });
    }

    @SuppressWarnings("unchecked")
    private DurationAuditSummary inspectDurationContract(Page page) {
        Object raw = page.evaluate("""
                () => {
                  const violations = [];
                  const visible = element => {
                    if (!element) return false;
                    let current = element;
                    while (current) {
                      const style = getComputedStyle(current);
                      if (style.display === 'none' || style.visibility === 'hidden') return false;
                      current = current.parentElement;
                    }
                    return true;
                  };
                  const text = element => String(element?.textContent || '').replace(/\\s+/g, ' ').trim();
                  const parse = value => {
                    const raw = String(value || '').trim();
                    const seconds = raw.match(/^(\\d+(?:\\.\\d+)?)\\s*s$/i);
                    const millis = raw.match(/^(\\d+(?:\\.\\d+)?)\\s*ms$/i);
                    return seconds ? Number(seconds[1]) * 1000 : millis ? Number(millis[1]) : null;
                  };
                  const format = value => value <= 300
                    ? String(value) + ' ms'
                    : (value / 1000).toFixed(3).replace(/0+$/, '').replace(/\\.$/, '') + ' s';
                  const badge = card => card.querySelector(':scope > .card-header .node-info .badge-default')
                    || card.querySelector(':scope > .card-header .card-toolbar .badge-default')
                    || card.querySelector(':scope > .card-header .card-toolbar .badge');
                  const directChildren = card => Array.from(card.querySelectorAll(':scope > .accordion > .card'))
                    .filter(child => child.matches('[data-atf-node-role], [data-atf-scenario-id]'));
                  const directActions = card => {
                    let total = 0, count = 0;
                    card.querySelectorAll('.atf-ui-action-table tbody tr').forEach(row => {
                      if (row.closest('.card[data-atf-node-role], .card[data-atf-scenario-id]') !== card) return;
                      const cells = row.cells || [];
                      const value = parse(cells.length > 5 ? text(cells[5]) : '');
                      if (value !== null) { total += value; count++; }
                    });
                    return {total, count};
                  };
                  const transportEvidence = card => {
                    const collapse = Array.from(card.children || [])
                      .find(child => child.classList?.contains('collapse'));
                    return Boolean(collapse?.querySelector(
                      '.execution-table, .helm-trace-table, .helm-request-details-content, .helm-response-details-content'));
                  };
                  const nodes = Array.from(document.querySelectorAll(
                    '#test-view .test-content-detail:where(:not(.d-none)) .card[data-atf-node-role],'
                    + '#test-view .test-content-detail:where(:not(.d-none)) .card[data-atf-scenario-id]'
                  ));
                  let markerCount = 0, formattedCount = 0, aggregatedCount = 0;
                  const values = new Map();
                  const inspect = card => {
                    const rawDuration = card.getAttribute('data-duration-ms');
                    const duration = Number(rawDuration);
                    const durationBadge = badge(card);
                    const rawBadge = durationBadge && durationBadge.getAttribute('data-duration-ms');
                    const badgeValue = Number(rawBadge);
                    const markerValid = rawDuration !== null && rawDuration.trim() !== ''
                      && Number.isFinite(duration) && duration >= 0;
                    if (markerValid) markerCount++;
                    else violations.push('missing/non-numeric marker on ' + text(card));
                    if (!durationBadge || rawBadge === null || rawBadge.trim() === ''
                        || !Number.isFinite(badgeValue) || badgeValue !== duration) {
                      violations.push('badge marker mismatch on ' + text(card));
                    } else if (text(durationBadge) !== format(duration)) {
                      violations.push('formatter mismatch on ' + text(card) + ': ' + text(durationBadge));
                    } else {
                      formattedCount++;
                    }
                    const children = directChildren(card);
                    let childTotal = 0;
                    children.forEach(child => {
                      const childValue = inspect(child);
                      if (childValue !== null) childTotal += childValue;
                    });
                    const actions = directActions(card);
                    const measured = Number(card.getAttribute('data-atf-measured-duration-ms'));
                    const transport = transportEvidence(card) && Number.isFinite(measured) ? measured : 0;
                    const source = card.getAttribute('data-duration-source') || '';
                    if (source === 'aggregated') aggregatedCount++;
                    let expected = null;
                    if (children.length > 0 || actions.count > 0) {
                      expected = children.length > 0
                        ? Math.max(childTotal, actions.total, transport)
                        : actions.total;
                      if (markerValid && duration !== expected) {
                        violations.push('parent total mismatch on ' + text(card)
                          + ': expected ' + expected + ', actual ' + duration);
                      }
                    }
                    values.set(card, markerValid ? duration : null);
                    return markerValid ? duration : null;
                  };
                  nodes.forEach(node => { if (!values.has(node)) inspect(node); });
                  const roots = Array.from(document.querySelectorAll('#test-view .test-list-item > .test-item[test-id]'))
                    .filter(visible);
                  roots.forEach(root => {
                    const rawValue = root.getAttribute('data-duration-ms');
                    const value = Number(rawValue);
                    const scenarios = Array.from(root.querySelectorAll('.test-contents > .mt-4 > .accordion > .card'))
                      .filter(card => card.getAttribute('data-atf-filter-hidden') !== 'true');
                    const sum = scenarios.reduce((total, card) => total + (Number(card.getAttribute('data-duration-ms')) || 0), 0);
                    if (rawValue === null || rawValue.trim() === ''
                        || !Number.isFinite(value) || value < 0) violations.push('missing/non-numeric class total');
                    if (scenarios.length > 0 && value !== sum) {
                      violations.push('class total mismatch: expected ' + sum + ', actual ' + value);
                    }
                  });
                  return {nodes: nodes.length, markerCount, formattedCount, aggregatedCount,
                    ownerCount: roots.length, violations};
                }
                """);
        Map<String, Object> result = (Map<String, Object>) raw;
        List<String> violations = (List<String>) result.getOrDefault("violations", List.of());
        return new DurationAuditSummary(
                number(result.get("ownerCount")), number(result.get("nodes")),
                number(result.get("markerCount")), number(result.get("formattedCount")),
                number(result.get("aggregatedCount")), violations.size(), violations);
    }

    @Test
    @DisplayName("GIVEN the generated report WHEN Tags and Fails are explored THEN dynamic annotations and navigation remain actionable")
    void filtersAndFailuresRemainActionable() {
        audit("Annotations and Fails — dynamic filters and failure navigation", audit -> {
            given("the Tags tab is opened");
            Page page = openTab("#category-view");
            Locator category = page.locator("#category-view");
            when("all available annotation categories and their linked scenarios are inspected");
            AnnotationSnapshot annotationSnapshot = inspectAnnotations(category);
            Locator visibleTagLinks = category.locator("a.linked:visible");
            boolean tagLinks = annotationSnapshot.categoryCount() > 0 && visibleTagLinks.count() > 0;
            if (tagLinks) {
                activeActions.click(visibleTagLinks.first(), "#category-view a.linked:first-of-type",
                        "Tests tab opens for selected annotation");
                assertTrue(page.locator("#test-view").isVisible());
            }
            page = openTab("#atf-fails-view");
            Locator fails = page.locator("#atf-fails-view");
            when("the Fails view is inspected");
            String screenshot = screenshot(page);
            FailureSnapshot failureSnapshot = inspectFailures(fails);
            boolean failureLinks = failureSnapshot.failureCount() > 0
                    && failureSnapshot.primaryCauseCount() > 0
                    && failureSnapshot.openLinkCount() > 0;
            audit.step("THEN", "annotations and failure causes remain actionable",
                    "#category-view, #atf-fails-view", screenshot,
                    List.of(
                            check("Annotation categories", "> 0", String.valueOf(annotationSnapshot.categoryCount()), annotationSnapshot.categoryCount() > 0),
                            check("Linked scenarios", "> 0", String.valueOf(annotationSnapshot.linkedScenarioCount()), annotationSnapshot.linkedScenarioCount() > 0),
                            check("Tag filter surface", "dark themed and bounded", annotationSnapshot.filterSurface(), annotationSnapshot.styled()),
                            check("Failure navigation", "causes + open links", failureLinks ? "available" : "missing", failureLinks)),
                    status(annotationSnapshot.categoryCount() > 0 && annotationSnapshot.linkedScenarioCount() > 0 && annotationSnapshot.styled() && failureLinks));
            assertTrue(annotationSnapshot.categoryCount() > 0);
            assertTrue(annotationSnapshot.linkedScenarioCount() > 0);
            assertTrue(annotationSnapshot.styled());
            assertTrue(failureLinks);
            activeActions.click(fails.locator("a.atf-failure-open").first(),
                    "#atf-fails-view a.atf-failure-open:first-of-type",
                    "selected failure opens in Tests");
            assertTrue(page.locator("#test-view").isVisible());
        });
    }

    private AnnotationSnapshot inspectAnnotations(Locator category) {
        Locator categoryItems = category.locator(".test-list .test-item");
        int categoryCount = categoryItems.count();
        if (categoryCount == 0) return new AnnotationSnapshot(0, 0, "missing", false);
        activeActions.click(categoryItems.first(), "#category-view .test-list .test-item:first-of-type",
                "annotation filter opens");
        Locator links = category.locator("a.linked:visible");
        if (links.count() > 0) activeActions.waitForVisible(links.first(), "#category-view a.linked:first-of-type");
        activeActions.getText(category.locator(".test-list"), "#category-view .test-list",
                "annotation filter is readable");
        String filterSurface = category.locator(".test-list").evaluate("element => { const s=getComputedStyle(element); return s.backgroundColor + ' / ' + s.color; }").toString();
        boolean styled = !filterSurface.contains("rgb(255, 255, 255)")
                && category.locator(".test-content").isVisible();
        return new AnnotationSnapshot(categoryCount, links.count(), filterSurface, styled);
    }

    private FailureSnapshot inspectFailures(Locator fails) {
        return new FailureSnapshot(fails.locator("#atf-failure-list .name").count(),
                fails.getByText("PRIMARY:", new Locator.GetByTextOptions().setExact(false)).count(),
                fails.locator("a.atf-failure-open").count());
    }

    @Test
    @DisplayName("GIVEN a request scenario WHEN its evidence is expanded THEN request, response and execution data are distinct")
    void requestEvidenceIsAuditableAndExpandable() {
        audit("Request evidence — expandable API input audit trail", audit -> {
            given("the Tests tab is opened on the first discovered API class");
            Page page = openTab("#test-view");
            Locator tests = page.locator("#test-view");
            Locator classOwners = tests.locator(".test-list .test-item");
            assertTrue(classOwners.count() > 0, "The report has no discoverable API class");
            activeActions.click(classOwners.first(), "#test-view .test-list .test-item:first-of-type",
                    "class detail opens");
            when("the first discovered scenario, TEST BODY and request are expanded");
            Locator scenarioCard = tests.locator(".test-content-detail:visible .test-contents > .mt-4 > .accordion > .card[data-atf-scenario-id]").first();
            activeActions.waitForVisible(scenarioCard,
                    ".test-content-detail:visible .test-contents > .mt-4 > .accordion > .card[data-atf-scenario-id]");
            // The report binds collapse behavior to the semantic node link. Keep the
            // action idempotent: clicking an already-open node would close it again.
            if (!"true".equals(scenarioCard.getAttribute("data-atf-scenario-open"))) {
                activeActions.click(scenarioCard.locator(":scope > .card-header .node"),
                        ":scope > .card-header .node", "scenario is expanded");
                activeActions.waitForTimeout(page, 0.1, scenarioCard,
                        ".test-content-detail:visible .card[data-atf-scenario-id]",
                        "scenario expansion settles");
            }
            Locator testBody = scenarioCard.locator(":scope > .accordion > [data-atf-node-role='phase'][data-atf-phase-name='test-body']");
            activeActions.waitForVisible(testBody,
                    "[data-atf-node-role='phase'][data-atf-phase-name='test-body']");
            Locator phaseNode = testBody.locator(":scope > .card-header .node");
            activeActions.waitForVisible(phaseNode,
                    "[data-atf-phase-name='test-body'] > .card-header .node");
            if (!"true".equals(testBody.locator(":scope > .card-header").getAttribute("aria-expanded"))) {
                activeActions.click(phaseNode,
                        "[data-atf-phase-name='test-body'] > .card-header .node",
                        "TEST BODY is expanded");
                activeActions.waitForTimeout(page, 0.1, testBody,
                        "[data-atf-phase-name='test-body']",
                        "TEST BODY expansion settles");
            }
            Locator requests = testBody.locator(":scope > .accordion > [data-atf-node-role='request']");
            assertTrue(requests.count() > 0, "The first TEST BODY has no request node");
            int selectedRequest = 0;
            for (int index = 0; index < requests.count(); index++) {
                Locator candidate = requests.nth(index);
                if (candidate.locator(".request-json-body").count() > 0) {
                    selectedRequest = index;
                    break;
                }
            }
            Locator request = requests.nth(selectedRequest);
            if (!"true".equals(request.getAttribute("data-atf-node-open"))) {
                activeActions.click(request.locator(":scope > .card-header .node"),
                        ":scope > .card-header .node", "request evidence is expanded");
                activeActions.waitForTimeout(page, 0.1, request,
                        "[data-atf-node-role='request']",
                        "request expansion settles");
            }
            expand(request.locator("details.request-evidence > summary"),
                    "details.request-evidence > summary");
            expand(request.locator("details.response-evidence > summary"),
                    "details.response-evidence > summary");
            expand(request.locator("details.execution-evidence > summary"),
                    "details.execution-evidence > summary");
            activeActions.findElement(request.locator("table.helm-request-table:visible"),
                    "table.helm-request-table:visible");
            activeActions.findElement(request.locator("table.helm-trace-table:visible"),
                    "table.helm-trace-table:visible");
            activeActions.expectText(request.locator("table.helm-request-table:visible"),
                    "table.helm-request-table:visible", "FIELD");
            String screenshot = screenshot(page);
            then("request details, response details, JSON bodies and execution evidence are visible");
            boolean requestBodyExpected = request.locator(".request-json-body").count() > 0;
            boolean responseBodyExpected = request.locator(".response-json-body").count() > 0;
            boolean requestBodyVisible = !requestBodyExpected
                    || request.locator(".request-json-body:visible").count() > 0;
            boolean responseBodyVisible = !responseBodyExpected
                    || request.locator(".response-json-body:visible").count() > 0;
            boolean evidenceVisible = request.locator("table.helm-request-table:visible").count() > 0
                    && request.locator("table.helm-trace-table:visible").count() > 0
                    && request.locator("table.execution-table:visible").count() > 0
                    && requestBodyVisible
                    && responseBodyVisible;
            audit.step("THEN", "request, response and execution evidence are distinct and expandable",
                    "request evidence disclosures", screenshot, List.of(
                            check("Request details", "visible", String.valueOf(request.locator("table.helm-request-table:visible").count()), request.locator("table.helm-request-table:visible").count() > 0),
                            check("Response details", "visible", String.valueOf(request.locator("table.helm-trace-table:visible").count()), request.locator("table.helm-trace-table:visible").count() > 0),
                            check("Execution evidence", "visible", String.valueOf(request.locator("table.execution-table:visible").count()), request.locator("table.execution-table:visible").count() > 0),
                            check("Request JSON body", requestBodyExpected ? "visible" : "not present for this request",
                                    requestBodyExpected ? (requestBodyVisible ? "visible" : "missing") : "not present", requestBodyVisible),
                            check("Response JSON body", responseBodyExpected ? "visible" : "not present for this request",
                                    responseBodyExpected ? (responseBodyVisible ? "visible" : "missing") : "not present", responseBodyVisible)),
                    status(evidenceVisible));
            assertTrue(evidenceVisible);
        });
    }

    private void expand(Locator summary, String locatorTarget) {
        if (summary.count() == 0) return;
        Locator disclosure = summary.locator("xpath=..");
        if (disclosure.getAttribute("open") == null) {
            activeActions.click(summary, locatorTarget, "evidence is expanded");
        }
    }

    private TestsSnapshot inspectEveryTestClass(Page page) {
        Locator tests = page.locator("#test-view");
        Locator owners = tests.locator(".test-list .test-item");
        List<String> names = new ArrayList<>();
        int scenarios = 0;
        int phases = 0;
        int requests = 0;
        int validations = 0;
        for (int index = 0; index < owners.count(); index++) {
            Locator owner = tests.locator(".test-list .test-item").nth(index);
            String name = activeActions.getText(owner.locator(".test-detail .name"),
                    ".test-list .test-item:nth-of-type(" + (index + 1) + ") .name",
                    "class owner name is readable");
            names.add(name);
            activeActions.click(owner, ".test-list .test-item:nth-of-type(" + (index + 1) + ")",
                    "class detail opens");
            Locator detail = tests.locator(".test-content-detail:visible").last();
            activeActions.waitForVisible(detail, ".test-content-detail:visible");
            scenarios += detail.locator("[data-atf-scenario-id]").count();
            phases += detail.locator("[data-atf-node-role='phase']").count();
            requests += detail.locator("[data-atf-node-role='request']").count();
            validations += detail.locator("table.helm-validation-table").count();
        }
        return new TestsSnapshot(names.size(), scenarios, phases, requests, validations, names);
    }

    private OverviewSnapshot inspectOverview(Locator overview) {
        String[] selectors = {
                "#parent-analysis", "#timeline", "#atf-execution-metadata", "#atf-tag-audit",
                ".category-container", ".sysenv-container", "#atf-audit-scenarios",
                "#atf-audit-packages", "#atf-audit-tags"
        };
        int sections = 0;
        for (String selector : selectors) {
            if (overview.locator(selector).count() > 0) sections++;
        }
        Map<String, Integer> metrics = new LinkedHashMap<>();
        Locator metricNodes = overview.locator("#atf-execution-metadata-grid .atf-secondary-metric");
        for (int index = 0; index < metricNodes.count(); index++) {
            Locator metric = metricNodes.nth(index);
            String label = metric.locator("small").textContent().trim();
            metrics.put(label, number(metric.locator("strong").textContent()));
        }
        Locator auditRows = overview.locator("#atf-audit-scenarios tbody tr");
        int auditScenarioTotal = 0;
        for (int index = 0; index < auditRows.count(); index++) {
            Locator cells = auditRows.nth(index).locator("td");
            if (cells.count() >= 2) auditScenarioTotal += number(cells.nth(1).textContent());
        }
        return new OverviewSnapshot(sections, metrics.getOrDefault("Test classes in this run", -1),
                metrics.getOrDefault("Scenarios in this run", -1), auditScenarioTotal,
                sections == selectors.length && metrics.size() >= 2 && auditRows.count() > 0);
    }

    private Page openTab(String hash) {
        return auditContext.openTab(hash, activeActions);
    }

    private String screenshot(Page page) {
        UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                page, configuration.screenValidation(), "Playwright screen validation");
        System.out.println("[ATF UI][EXPLORATORY] inline-base64-screenshot bytes="
                + evidence.base64Png().length());
        activeActions.observation("Screenshot", "current page", "visual state captured",
                "inline Base64 image");
        return evidence.base64Png();
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
        var scenario = auditContext.startScenario(
                "ApiExtentReportExploratoryTest",
                "atf.ui.tests.exploratory",
                title,
                List.of("ui", "exploratory", "reporting"));
        var setup = scenario.startPhase("SETUP");
        setup.step("GIVEN", "the API Extent report is loaded as read-only input",
                apiInputReport.getFileName().toString(), null,
                List.of(check("API input report", "published", apiInputReport.toString(), Files.isRegularFile(apiInputReport))),
                AtfReportStatus.PASS);
        setup.complete(AtfReportStatus.PASS);
        return new UiAudit(scenario, scenario.startPhase("TEST BODY"));
    }

    @FunctionalInterface
    private interface AuditAction {
        void run(UiAudit audit);
    }

    private final class UiAudit {
        private final UiAuditLifecycle lifecycle;

        private UiAudit(UiExtentReportSession.UiScenario scenario,
                        UiExtentReportSession.UiPhase body) {
            this.lifecycle = new UiAuditLifecycle(scenario, body);
        }

        private void step(String keyword, String title, String target, String screenshot,
                          List<AtfAssertion> assertions, AtfReportStatus status) {
            lifecycle.screenValidation(title, target, screenshot, assertions,
                    activeActions == null ? List.of() : activeActions.drain(), status);
        }

        private void pass() {
            lifecycle.pass();
        }

        private void fail(Throwable failure) {
            lifecycle.fail("UI exploration failed", "dynamic API report",
                    activeActions == null ? List.of() : activeActions.drain(), failure);
        }
    }

    private record TestsSnapshot(int classCount, int scenarioCount, int phaseCount,
                                 int requestCount, int validationCount, List<String> classNames) { }

    private record OverviewSnapshot(int sectionCount, int classCount, int scenarioCount,
                                    int auditScenarioTotal, boolean complete) { }

    private record AnnotationSnapshot(int categoryCount, int linkedScenarioCount,
                                      String filterSurface, boolean styled) { }

    private record FailureSnapshot(int failureCount, int primaryCauseCount, int openLinkCount) { }

    private static final class DurationAuditSummary {
        private int ownerCount;
        private int nodeCount;
        private int durationMarkerCount;
        private int formattedBadgeCount;
        private int aggregatedNodeCount;
        private int violationCount;
        private final List<String> violations = new ArrayList<>();

        private DurationAuditSummary() { }

        private DurationAuditSummary(int ownerCount, int nodeCount, int durationMarkerCount,
                                     int formattedBadgeCount, int aggregatedNodeCount,
                                     int violationCount, List<String> violations) {
            this.ownerCount = ownerCount;
            this.nodeCount = nodeCount;
            this.durationMarkerCount = durationMarkerCount;
            this.formattedBadgeCount = formattedBadgeCount;
            this.aggregatedNodeCount = aggregatedNodeCount;
            this.violationCount = violationCount;
            this.violations.addAll(violations);
        }

        private void add(DurationAuditSummary other) {
            ownerCount = Math.max(ownerCount, other.ownerCount);
            nodeCount += other.nodeCount;
            durationMarkerCount += other.durationMarkerCount;
            formattedBadgeCount += other.formattedBadgeCount;
            aggregatedNodeCount += other.aggregatedNodeCount;
            violationCount += other.violationCount;
            violations.addAll(other.violations);
        }

        private int ownerCount() { return ownerCount; }
        private int nodeCount() { return nodeCount; }
        private int durationMarkerCount() { return durationMarkerCount; }
        private int formattedBadgeCount() { return formattedBadgeCount; }
        private int aggregatedNodeCount() { return aggregatedNodeCount; }
        private List<String> violations() { return List.copyOf(violations); }
    }

    private static void given(String step) {
        System.out.println("[ATF UI][EXPLORATORY] GIVEN " + step);
    }

    private static void when(String step) {
        System.out.println("[ATF UI][EXPLORATORY] WHEN " + step);
    }

    private static void then(String step) {
        System.out.println("[ATF UI][EXPLORATORY] THEN " + step);
    }
}
