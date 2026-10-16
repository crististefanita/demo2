package atf.ui.tests.extent.locators;

import atf.api.ui.playwright.AtfLocator;
import atf.ui.tests.extent.support.ReportUiContract;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.Map;

/** Selector boundary for the rendered ATF report. */
public final class ReportUiLocators {
    private final ReportEvidenceLocators evidence = new ReportEvidenceLocators();
    private final ReportCategoryLocators categories = new ReportCategoryLocators();
    private final ReportFailureLocators failures = new ReportFailureLocators();
    private static final String TESTS_VIEW = ReportUiContract.Tab.TESTS.hash();
    private static final String OVERVIEW_VIEW = ReportUiContract.Tab.OVERVIEW.hash();
    private static final String CATEGORY_VIEW = ReportUiContract.Tab.CATEGORIES.hash();
    private static final String FAILS_VIEW = ReportUiContract.Tab.FAILS.hash();
    private static final String TEST_LIST = ".test-list";
    private static final String TEST_OWNERS = ReportUiContract.TEST_OWNER_SELECTOR;
    private static final String VISIBLE_TEST_DETAIL = ReportUiContract.VISIBLE_DETAIL_SELECTOR;
    private static final String STATUS_TOGGLE = "#status-toggle";
    private static final String TAG_TOGGLE = "#tag-toggle";
    private static final String TAG_FILTER = "[data-atf-filter-bound='true']";
    private static final String EXECUTION_METADATA = "#atf-execution-metadata";
    private static final String SCREEN_VALIDATION_CARD = "[data-atf-node-role='validation']";
    private static final String ACTION_ZONE = "details.atf-ui-action-sequence";
    private static final String OBSERVATION_ZONE = "details.atf-ui-observations";
    private static final String ASSERTION_ZONE = "details.assertion-evidence";
    private static final String SCREEN_VALIDATION_ZONE = ACTION_ZONE + ","
            + OBSERVATION_ZONE + "," + ASSERTION_ZONE;
    /** Only leaf request cards own transport evidence; scenario/phase cards must not leak children. */
    private static final String REQUEST_NODE =
            "[data-atf-node-role='request']:has(> .collapse .execution-table)";
    private static final String PHASE_NODE = "[data-atf-node-role='phase']";
    private static final String FAILURE_BADGE = ".card-toolbar .badge-danger, .card-toolbar .fail-bg";
    private static final String PHASE_DIRECT_BODY = ":scope > .card-body";
    private static final String PHASE_FAILURE_DISCLOSURE = "details.failure-evidence";
    private static final String PHASE_FAILURE_TRACE = PHASE_FAILURE_DISCLOSURE
            + " .helm-trace-table";
    private static final String PHASE_FAILURE_CAUSE = PHASE_FAILURE_TRACE
            + " tbody tr";

    public Locator exactText(Locator scope, String text) {
        return scope.getByText(text, new Locator.GetByTextOptions().setExact(true));
    }

    public Locator exactLink(Locator scope, String text) {
        return scope.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(text).setExact(true));
    }

    public Locator testsView(Page page) { return page.locator(TESTS_VIEW); }
    public Locator runtimeRoot(Page page) { return page.locator("html"); }
    public Locator navTab(Page page, ReportUiContract.Tab tab) {
        return page.locator(navTabSelector(tab));
    }
    public AtfLocator navTabAction(Page page, ReportUiContract.Tab tab) {
        return new AtfLocator(navTab(page, tab), navTabSelector(tab));
    }
    private static String navTabSelector(ReportUiContract.Tab tab) {
        return "#nav-" + switch (tab) {
            case TESTS -> "test";
            case CATEGORIES -> "category";
            case FAILS -> "fails";
            case OVERVIEW -> "overview";
        };
    }
    public Locator overviewView(Page page) { return page.locator(OVERVIEW_VIEW); }
    public Locator categoryView(Page page) { return page.locator(CATEGORY_VIEW); }
    public Locator failsView(Page page) { return page.locator(FAILS_VIEW); }
    public Locator tabView(Page page, ReportUiContract.Tab tab) {
        return switch (tab) {
            case TESTS -> testsView(page);
            case OVERVIEW -> overviewView(page);
            case CATEGORIES -> categoryView(page);
            case FAILS -> failsView(page);
        };
    }
    public Locator tabViewByHash(Page page, String hash) {
        for (ReportUiContract.Tab tab : ReportUiContract.Tab.values()) {
            if (tab.hash().equals(hash)) return tabView(page, tab);
        }
        throw new IllegalArgumentException("Unsupported report tab hash: " + hash);
    }
    public Locator testsList(Page page) { return testsView(page).locator(TEST_LIST); }
    public Locator categoryList(Page page) { return categoryView(page).locator(TEST_LIST); }
    public Locator categoryList(Locator category) { return category.locator(TEST_LIST); }
    public Locator testContentDetail(Page page) { return testsView(page).locator(VISIBLE_TEST_DETAIL).last(); }
    public Locator categoryContentDetail(Page page) { return categoryView(page).locator(VISIBLE_TEST_DETAIL); }
    public Locator overviewTables(Locator overview) { return overview.locator("table"); }
    public Locator overviewSection(Locator overview, String selector) { return overview.locator(selector); }
    public Locator overviewMetrics(Locator overview) {
        return overview.locator(ReportUiContract.OVERVIEW_EXECUTION_METADATA_GRID
                + " " + atf.ui.tests.extent.support.ReportUiContract.OVERVIEW_SECONDARY_METRIC);
    }
    public Locator overviewExecutionMetadata(Locator overview) {
        return overview.locator(EXECUTION_METADATA);
    }
    public AtfLocator overviewExecutionMetadataAction(Page page) {
        return new AtfLocator(overviewExecutionMetadata(overviewView(page)).locator("summary"),
                ReportUiContract.Tab.OVERVIEW.hash() + " " + EXECUTION_METADATA + " > summary");
    }
    public Locator overviewAuditRows(Locator overview) {
        return overview.locator(ReportUiContract.OVERVIEW_SCENARIO_AUDIT + " tbody tr");
    }
    public Locator metricLabel(Locator metric) { return metric.locator("small"); }
    public Locator metricValue(Locator metric) { return metric.locator("strong"); }
    public Locator tableCells(Locator row) { return row.locator("td"); }
    public Locator overviewCategoryCells(Locator overview) {
        return overview.locator(".category-container table tbody td");
    }

    /** Screen-validation node boundary; callers never compose this selector. */
    public Locator screenValidationNodes(Page page) {
        return testsView(page).locator(".test-content-detail .card-title a.node")
                .filter(new Locator.FilterOptions().setHasText("Screen validation"));
    }

    public Locator visibleScreenValidationNode(Page page) {
        Locator nodes = screenValidationNodes(page);
        for (Locator node : nodes.all()) {
            if (node.isVisible()) return node;
        }
        return nodes.first();
    }

    public Locator visibleScreenValidationNodeWithScreenshot(Page page) {
        Locator nodes = screenValidationNodes(page);
        for (Locator node : nodes.all()) {
            if (node.isVisible() && screenValidationScreenshotLinks(screenValidationCard(node)).count() > 0) {
                return node;
            }
        }
        return visibleScreenValidationNode(page);
    }

    public AtfLocator screenValidationNodeAction(Page page) {
        Locator node = visibleScreenValidationNodeWithScreenshot(page);
        String id = requiredAttribute(node, "id", "Screen validation node");
        return new AtfLocator(node, "#test-view .test-content-detail .card-title .node[id='"
                + cssAttribute(id) + "']");
    }

    public Locator screenValidationCard(Locator node) {
        return node.locator("xpath=..").locator("xpath=..").locator("xpath=..");
    }

    public Locator testOwners(Page page) { return testsView(page).locator(TEST_OWNERS); }
    public Locator testOwners(Locator tests) { return tests.locator(TEST_OWNERS); }
    public Locator testOwnerNames(Locator tests) { return tests.locator(".test-list .test-item .name"); }
    public Locator testOwnerName(Locator owner) { return owner.locator(".test-detail .name"); }
    public Locator testOwnerByName(Locator tests, String className) {
        return testOwnerNames(tests).filter(new Locator.FilterOptions().setHasText(className)).first();
    }
    public Locator testOwnerItemByName(Locator tests, String className) {
        return testOwners(tests)
                .filter(new Locator.FilterOptions().setHasText(className)).first();
    }
    public Locator testOwnerItem(Page page, int zeroBasedIndex) {
        return testOwners(testsView(page)).nth(requireIndex(zeroBasedIndex));
    }
    public AtfLocator testOwnerItemAction(Page page, int zeroBasedIndex) {
        int index = requireIndex(zeroBasedIndex);
        return new AtfLocator(testOwnerItem(page, index),
                TESTS_VIEW + " " + TEST_OWNERS + ":nth-of-type(" + (index + 1) + ")");
    }
    public AtfLocator firstTestOwnerAction(Page page) {
        return testOwnerItemAction(page, 0);
    }
    public AtfLocator testOwnerNameAction(Page page, int zeroBasedIndex) {
        int index = requireIndex(zeroBasedIndex);
        return new AtfLocator(testOwnerName(testOwnerItem(page, index)),
                TESTS_VIEW + " " + TEST_OWNERS + ":nth-of-type(" + (index + 1) + ") .test-detail .name");
    }
    public AtfLocator testOwnerNameAction(Locator owner) {
        String testId = requiredAttribute(owner, "test-id", "API owner");
        return new AtfLocator(testOwnerName(owner),
                TESTS_VIEW + " " + TEST_OWNERS + "[test-id='" + cssAttribute(testId)
                        + "'] .test-detail .name");
    }
    public AtfLocator testOwnerItemAction(Page page, String className) {
        Locator owner = testOwnerItemByName(testsView(page), className);
        String testId = requiredAttribute(owner, "test-id", "API owner " + className);
        return new AtfLocator(owner,
                "#test-view .test-list .test-item[test-id='" + cssAttribute(testId) + "']");
    }
    public Locator visibleTestOwners(Locator tests) {
        return tests.locator(".test-content-detail:visible .test-list .test-item");
    }
    public Locator testStatusMenu(Locator tests) { return tests.locator(STATUS_TOGGLE); }
    public Locator testTagMenu(Locator tests) { return tests.locator(TAG_TOGGLE); }
    public Locator testStatusMenu(Page page) { return testStatusMenu(testsView(page)); }
    public Locator testTagMenu(Page page) { return testTagMenu(testsView(page)); }
    public Locator testStatusTrigger(Page page) {
        return testsView(page).locator(".test-list-tools .dropdown:nth-child(1) .dropdown-toggle");
    }
    public Locator testTagTrigger(Page page) {
        return testsView(page).locator(".test-list-tools .dropdown:nth-child(2) .dropdown-toggle");
    }
    public AtfLocator testStatusTriggerAction(Page page) {
        return new AtfLocator(testStatusTrigger(page),
                TESTS_VIEW + " .test-list-tools .dropdown:nth-child(1) .dropdown-toggle");
    }
    public AtfLocator testTagTriggerAction(Page page) {
        return new AtfLocator(testTagTrigger(page),
                TESTS_VIEW + " .test-list-tools .dropdown:nth-child(2) .dropdown-toggle");
    }
    public Locator statusFilterOptions(Locator menu) { return menu.locator(".dropdown-item[status]"); }
    public Locator filterOptions(Locator menu) { return menu.locator(".dropdown-item"); }
    public Locator statusFilterOption(Page page, String status) {
        return testStatusMenu(page).locator("[status='" + cssAttribute(status) + "']");
    }
    public AtfLocator statusFilterAction(Page page, String status) {
        String safeStatus = cssAttribute(status);
        return new AtfLocator(statusFilterOption(page, safeStatus),
                STATUS_TOGGLE + " [status='" + safeStatus + "']");
    }
    public AtfLocator clearStatusFilterAction(Page page) {
        return statusFilterAction(page, "clear");
    }
    public Locator tagFilters(Page page) { return testTagMenu(page).locator(TAG_FILTER); }
    public Locator activeTagFilters(Page page) { return tagFilters(page).locator("[aria-pressed='true']"); }
    public AtfLocator tagFilterAction(Page page, int zeroBasedIndex) {
        int index = requireIndex(zeroBasedIndex);
        return new AtfLocator(tagFilters(page).nth(index),
                TAG_TOGGLE + " " + TAG_FILTER + ":nth-of-type(" + (index + 1) + ")");
    }
    public AtfLocator activeTagFilterAction(Page page) {
        return new AtfLocator(activeTagFilters(page).first(),
                TAG_TOGGLE + " " + TAG_FILTER + "[aria-pressed='true']");
    }
    public Locator statusBadges(Locator scope) { return scope.locator("h5.test-status:visible"); }
    public Locator nodeHeaders(Locator scope) { return scope.locator("div.card-header[role='button']"); }
    public Locator nodeTitles(Locator scope) { return scope.locator(".card-title .node"); }
    public Locator nodeTitleByText(Locator scope, String text) {
        return nodeTitles(scope).filter(new Locator.FilterOptions().setHasText(text)).first();
    }
    public String nodeTitle(Locator card) {
        return card.locator(":scope > .card-header > .card-title > .node").innerText().trim();
    }
    public Locator nodeHeader(Locator card) {
        return card.locator(":scope > .card-header[role='button']");
    }
    public Locator boundScenarioHeader(Locator scenario) {
        return scenario.locator(":scope > .card-header[data-atf-scenario-collapse-bound='true']");
    }
    public Locator boundPhaseHeader(Locator phase) {
        return phase.locator(":scope[data-atf-phase-header-bound='true'] > .card-header");
    }
    public Locator nodeControl(Locator header) { return header.locator(".node"); }
    public Locator nodeHeader(Locator scope, String text) {
        return nodeHeaders(scope).filter(new Locator.FilterOptions().setHasText(text));
    }
    public Locator nodeLinks(Locator scope) { return scope.locator("a.node"); }
    public Locator visibleTestDetail(Page page) {
        return testsView(page).locator(VISIBLE_TEST_DETAIL).last();
    }
    /** Resolves the detail panel from the clicked owner, avoiding stale visible panels. */
    public Locator detailForOwner(Locator owner) {
        return owner.locator(":scope > .test-contents:visible").first();
    }
    public Locator scenarios(Locator detail) { return detail.locator("[data-atf-scenario-id]"); }
    public Locator visibleScenarios(Locator detail) {
        return detail.locator("[data-atf-scenario-id]:visible");
    }
    public Locator scenarioCards(Page page) { return scenarios(testContentDetail(page)); }
    public Locator phases(Locator detail) { return detail.locator(PHASE_NODE); }
    public Locator scenarioAccordion(Locator scenario) {
        return scenario.locator(":scope > .accordion");
    }
    public Locator phaseAccordion(Locator phase) {
        return phase.locator(":scope > .accordion");
    }
    public Locator failedScenarioBadge(Locator scenario) {
        return scenario.locator(":scope > .card-header " + FAILURE_BADGE);
    }
    public Locator failedPhaseBadge(Locator phase) {
        return phase.locator(":scope > .card-header " + FAILURE_BADGE);
    }
    public Locator failedRequestBadge(Locator request) {
        return request.locator(":scope > .card-header " + FAILURE_BADGE);
    }
    public Locator phaseDirectBody(Locator phase) {
        return phase.locator(PHASE_DIRECT_BODY
                + ", :scope > div:not(.card-header):not(.accordion)");
    }
    public Locator phaseFailureDisclosures(Locator phase) {
        return phase.locator(PHASE_FAILURE_DISCLOSURE);
    }
    public Locator phaseFailureTraceTables(Locator phase) {
        return phase.locator(PHASE_FAILURE_TRACE);
    }
    public Locator phaseFailureCauseValues(Locator phase) {
        return phase.locator(PHASE_FAILURE_CAUSE)
                .filter(new Locator.FilterOptions().setHasText("FAILURE CAUSE"));
    }
    public Locator phaseFailureFieldValues(Locator phase, String field) {
        return phase.locator(PHASE_FAILURE_TRACE + " tbody tr")
                .filter(new Locator.FilterOptions().setHasText(field))
                .locator("td").nth(1);
    }
    public Locator phaseFailureDisclosureSummary(Locator disclosure) {
        return disclosure.locator(":scope > summary");
    }
    public AtfLocator phaseFailureDisclosureAction(Locator phase) {
        Locator disclosure = phaseFailureDisclosures(phase).first();
        String selector = phaseCardSelector(phase) + " " + PHASE_FAILURE_DISCLOSURE + " > summary";
        return new AtfLocator(phaseFailureDisclosureSummary(disclosure), selector);
    }
    public Locator expandedScenarioHeaders(Locator scope) {
        return scope.locator("[data-atf-scenario-id]:visible > .card-header[role='button'][aria-expanded='true']");
    }
    public Locator expandedPhaseHeaders(Locator scope) {
        return scope.locator("[data-atf-node-role='phase']:visible > .card-header[role='button'][aria-expanded='true']");
    }
    public Locator expandedRequestOrValidationHeaders(Locator scope) {
        return scope.locator("[data-atf-node-role='request']:visible > .card-header[role='button'][aria-expanded='true'],"
                + "[data-atf-node-role='validation']:visible > .card-header[role='button'][aria-expanded='true']");
    }
    public Locator openEvidenceDisclosures(Locator scope) {
        return scope.locator("details[open]:visible");
    }
    public Locator phase(Locator scope, atf.ui.tests.extent.support.ReportUiContract.Phase phase) {
        return scope.locator(atf.ui.tests.extent.support.ReportUiContract.phaseSelector(phase));
    }
    public Locator visiblePhase(Locator scope, atf.ui.tests.extent.support.ReportUiContract.Phase phase) {
        return scope.locator(atf.ui.tests.extent.support.ReportUiContract.phaseSelector(phase) + ":visible");
    }
    public Locator validationTables(Locator scope) { return scope.locator("table.helm-validation-table"); }
    public Locator validationCards(Locator scope) {
        return scope.locator(":scope > .accordion > [data-atf-node-role='validation']");
    }
    public Locator visibleValidationCards(Locator scope) {
        return scope.locator(SCREEN_VALIDATION_CARD + ":visible");
    }
    public Locator firstVisibleActionSequence(Locator scope) {
        Locator candidates = actionSequences(scope);
        for (int index = 0; index < candidates.count(); index++) {
            Locator candidate = candidates.nth(index);
            if (candidate.isVisible()) return candidate;
        }
        return candidates.first();
    }
    public Locator validationCardForEvidence(Locator evidence) {
        Locator validation = evidence.locator(
                "xpath=ancestor::*[@data-atf-node-role='validation'][1]");
        if (validation.count() > 0) return validation;
        return evidence.locator("xpath=ancestor::div[contains(concat(' ', "
                + "normalize-space(@class), ' '), ' card ')][1]");
    }
    /** Returns the first rendered card that exposes the complete three-zone contract. */
    public Locator firstCompleteScreenValidationCard(Locator scope) {
        Locator typed = scope.locator("div.card[data-atf-node-role='validation']:visible:has("
                + ACTION_ZONE + "):has(" + OBSERVATION_ZONE + "):has(" + ASSERTION_ZONE + ")").first();
        if (typed.count() > 0) return typed;
        return scope.locator("div.card:visible:has(" + ACTION_ZONE + "):has("
                + OBSERVATION_ZONE + "):has(" + ASSERTION_ZONE + ")").first();
    }
    public Locator screenValidationActionZones(Locator scope) {
        return scope.locator(ACTION_ZONE);
    }
    public Locator screenValidationObservationZones(Locator scope) {
        return scope.locator(OBSERVATION_ZONE);
    }
    public Locator screenValidationAssertionZones(Locator scope) {
        return scope.locator(ASSERTION_ZONE);
    }
    public Locator screenValidationZones(Locator scope) {
        return scope.locator(SCREEN_VALIDATION_ZONE);
    }
    public Locator screenValidationZoneRows(Locator scope) {
        return scope.locator(":scope > .collapse .card-body > table > tbody > tr.event-row");
    }

    /** The three semantic evidence zones are event rows in the same report table. */
    public Locator screenValidationActionSequences(Locator card) {
        return card.locator("details.atf-ui-action-sequence");
    }

    public Locator screenValidationObservations(Locator card) {
        return card.locator("details.atf-ui-observations");
    }

    public Locator screenValidationAssertionTables(Locator card) {
        return card.locator("table.helm-validation-table");
    }

    public Locator screenValidationScreenshotLinks(Locator card) {
        return base64ScreenshotLinks(screenValidationActionSequences(card));
    }

    /** The visible label must preserve the current inline camera marker. */
    public Locator screenValidationScreenshotLabels(Locator card) {
        return screenValidationScreenshotLinks(card).locator("span.badge-gradient-primary");
    }
    public Locator screenValidationSeparators(Locator scope) {
        return scope.locator("hr, .atf-ui-zone-separator");
    }
    public Locator evidenceDisclosures(Locator scope) { return scope.locator("details"); }
    public Locator nodeControls(Locator scope) {
        return scope.locator("[data-atf-node-role] > .card-header .node");
    }
    public Locator requests(Locator scope) { return scope.locator(REQUEST_NODE); }
    public Locator requestsInPhase(Locator phase) {
        return phase.locator(":scope > .accordion > [data-atf-node-role='request']");
    }
    public AtfLocator nodeHeaderAction(Locator header) {
        String nodeId = nodeId(header);
        return new AtfLocator(header, nodeHeaderSelector(nodeId));
    }

    /** Selector boundary for the structural card whose state changes after a click. */
    public AtfLocator nodeCardAction(Locator card) {
        Locator header = nodeHeader(card);
        String nodeId = nodeId(header);
        return new AtfLocator(card,
                "#test-view .test-content-detail div.card:has(> .card-header > .card-title > .node[id='"
                        + cssAttribute(nodeId) + "'])");
    }

    public String nodeOpenAttribute(Locator card) {
        String role = card.getAttribute("data-atf-node-role");
        return "phase".equals(role) ? "data-atf-phase-open"
                : "scenario".equals(role) || card.getAttribute("data-atf-scenario-id") != null
                ? "data-atf-scenario-open" : "data-atf-node-open";
    }
    public AtfLocator scenarioNodeAction(Locator scenario) {
        return nodeHeaderAction(nodeHeader(scenario));
    }

    /**
     * Absolute identity for the selected scenario card.  A relative
     * {@code [data-atf-scenario-id]} selector is not safe for page-level
     * state polling because hidden owner panels can contain cards with the
     * same shape.  State waits must target the exact rendered card.
     */
    public AtfLocator scenarioStateTarget(Locator scenario) {
        String id = scenario.getAttribute("data-atf-scenario-id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("scenario must expose data-atf-scenario-id");
        }
        return new AtfLocator(scenario,
                "#test-view .test-content-detail .card[data-atf-scenario-id='"
                        + cssAttribute(id) + "']");
    }
    public AtfLocator phaseNodeAction(Locator phase) {
        return nodeHeaderAction(nodeHeader(phase));
    }
    public AtfLocator phaseStateTarget(Locator phase) {
        return new AtfLocator(phase, phaseCardSelector(phase));
    }

    private String phaseCardSelector(Locator phase) {
        return nodeScopedSelector(nodeId(nodeHeader(phase)), "").trim();
    }

    /** Returns a human-readable identity for the exact structural card being expanded. */
    public String nodeDescription(Locator header) {
        Locator card = cardForHeader(header);
        String role = card.getAttribute("data-atf-node-role");
        String title = card.locator(":scope > .card-header > .card-title > .node").innerText().trim();
        String nodeId = nodeId(header);
        String kind = role == null || role.isBlank() ? "scenario" : role;
        return kind + " '" + title + "' (node id=" + nodeId + ")";
    }

    public Locator requestDetails(Locator request) {
        return evidence.requestDetails(request);
    }
    public Locator responseDetails(Locator request) {
        return evidence.responseDetails(request);
    }
    public Locator executionEvidence(Locator request) {
        return evidence.executionEvidence(request);
    }
    public Locator requestTable(Locator scope) { return evidence.requestTable(scope); }
    public Locator responseTable(Locator scope) { return evidence.responseTable(scope); }
    public Locator executionTable(Locator scope) { return evidence.executionTable(scope); }
    public Locator visibleRequestTable(Locator scope) { return evidence.visibleRequestTable(scope); }
    public Locator visibleResponseTable(Locator scope) { return evidence.visibleResponseTable(scope); }
    public Locator visibleExecutionTable(Locator scope) { return evidence.visibleExecutionTable(scope); }
    public Locator retryHistoryTable(Locator scope) { return evidence.retryHistoryTable(scope); }
    public Locator tableColumns(Locator table) { return evidence.tableColumns(table); }
    public Locator requestNodeByName(Locator scope, String requestName) { return evidence.requestNodeByName(scope, requestName); }
    public Locator requestNodeHeader(Locator request) { return evidence.requestNodeHeader(request); }
    public Locator requestNodeToggle(Locator request) { return evidence.requestNodeToggle(request); }
    public AtfLocator requestNodeToggleAction(Locator request) { return evidence.requestNodeToggleAction(request); }
    public String requestTitle(Locator request) { return evidence.requestTitle(request); }
    public String requestUrl(Locator request) { return evidence.requestUrl(request); }
    public Map<String, String> requestFields(Locator request) { return evidence.requestFields(request); }
    public Map<String, String> responseFields(Locator request) { return evidence.responseFields(request); }
    public Map<String, String> executionFields(Locator request) { return evidence.executionFields(request); }
    public int responseStatus(Locator request) { return evidence.responseStatus(request); }
    public String responseUrl(Locator request) { return evidence.responseUrl(request); }
    public Locator requestJsonBodies(Locator request) { return evidence.requestJsonBodies(request); }
    public Locator responseJsonBodies(Locator request) { return evidence.responseJsonBodies(request); }
    public Locator requestJsonBody(Locator scope) { return evidence.requestJsonBody(scope); }
    public Locator requestJsonBodyElements(Locator scope) { return evidence.requestJsonBodyElements(scope); }
    public Locator responseJsonBody(Locator scope) { return evidence.responseJsonBody(scope); }
    public Locator responseJsonBodyElements(Locator scope) { return evidence.responseJsonBodyElements(scope); }
    public Locator evidenceDisclosure(Locator scope, String text) { return exactText(scope, text); }
    public Locator requestEvidenceDisclosure(Locator request) {
        return evidence.requestEvidenceDisclosure(request);
    }
    public Locator responseEvidenceDisclosure(Locator request) {
        return evidence.responseEvidenceDisclosure(request);
    }
    public Locator executionEvidenceDisclosure(Locator request) {
        return evidence.executionEvidenceDisclosure(request);
    }
    public AtfLocator requestEvidenceDisclosureAction(Locator request) {
        return evidence.requestEvidenceDisclosureAction(request);
    }
    public AtfLocator responseEvidenceDisclosureAction(Locator request) {
        return evidence.responseEvidenceDisclosureAction(request);
    }
    public AtfLocator executionEvidenceDisclosureAction(Locator request) {
        return evidence.executionEvidenceDisclosureAction(request);
    }
    public Locator parentDisclosure(Locator summary) { return evidence.parentDisclosure(summary); }
    public Locator durationBadge(Locator node) { return evidence.durationBadge(node); }
    public Locator actionSequences(Locator scope) { return evidence.actionSequences(scope); }
    public Locator screenshotLinks(Locator scope) { return evidence.screenshotLinks(scope); }
    public Locator base64ScreenshotLinks(Locator scope) { return evidence.base64ScreenshotLinks(scope); }
    public Locator dataUriScreenshotLinks(Locator scope) { return evidence.dataUriScreenshotLinks(scope); }
    public Locator featherlightScreenshotLinks(Locator scope) { return evidence.featherlightScreenshotLinks(scope); }
    public Locator nestedScreenshotImages(Locator scope) { return evidence.nestedScreenshotImages(scope); }
    public Locator inlineScreenshotImages(Locator screenshotLink) { return evidence.inlineScreenshotImages(screenshotLink); }
    public Locator duplicateScreenshotMarkers(Locator scope) { return evidence.duplicateScreenshotMarkers(scope); }
    public Locator actionRows(Locator sequence) { return evidence.actionRows(sequence); }
    public Locator sequenceSummary(Locator sequence) { return evidence.sequenceSummary(sequence); }
    public Locator actionRowCells(Locator row) { return evidence.actionRowCells(row); }
    public Locator disclosureSummary(Locator disclosure) { return evidence.disclosureSummary(disclosure); }
    public Locator visibleDisclosure(Locator scope) { return evidence.visibleDisclosure(scope); }
    public Locator nodeAncestors(Locator scope) { return evidence.nodeAncestors(scope); }
    public Locator lightbox(Page page) { return evidence.lightbox(page); }
    public Locator lightboxImage(Locator lightbox) { return evidence.lightboxImage(lightbox); }
    public Locator lightboxClose(Locator lightbox) { return evidence.lightboxClose(lightbox); }

    public Locator categoryItems(Page page) { return categories.items(page); }
    public Locator categoryItem(Locator items, int index) { return categories.item(items, index); }
    public Locator categoryItemNames(Locator items) { return categories.itemNames(items); }
    public Locator categoryItemName(Locator item) { return categories.itemName(item); }
    public Locator categoryItemDuration(Locator item) { return categories.itemDuration(item); }
    public Locator categoryStatusMenu(Locator category) { return categories.statusMenu(category); }
    public Locator categoryTagMenu(Locator category) { return categories.tagMenu(category); }
    public Locator categoryStatusFilterTrigger(Locator category) { return categories.statusFilterTrigger(category); }
    public Locator categoryTagFilterTrigger(Locator category) { return categories.tagFilterTrigger(category); }
    public Locator categoryAggregateStatuses(Locator category) { return categories.aggregateStatuses(category); }
    public Locator categoryStatusOption(Locator menu, String status) { return categories.statusOption(menu, status); }
    public Locator categoryStatusOptions(Locator category) { return categories.statusOptions(category); }
    public Locator categoryTagOptions(Locator category) { return categories.tagOptions(category); }
    public Locator clearCategoryFilter(Locator categoryOrMenu) { return categories.clearFilter(categoryOrMenu); }
    public Locator categoryVisibleItems(Locator category) { return categories.visibleItems(category); }
    public Locator visibleCategoryDetail(Page page) { return categories.visibleDetail(page); }
    public Locator categoryTitle(Locator detail) { return categories.title(detail); }
    public Locator categoryLinkedTests(Locator detail) { return categories.linkedTests(detail); }
    public Locator categoryStatusRows(Locator detail) { return categories.statusRows(detail); }
    public AtfLocator categoryItemAction(Page page, int zeroBasedIndex) { return categories.itemAction(page, zeroBasedIndex); }
    public AtfLocator visibleCategoryDetailAnchor(Page page) { return categories.visibleDetailAnchor(page); }
    public AtfLocator firstCategoryItemAction(Page page) { return categories.firstItemAction(page); }
    public Locator categorySurface(Page page) { return categories.surface(page); }
    public Locator categoryContent(Page page) { return categories.content(page); }
    public Locator categoryContent(Locator category) { return categories.content(category); }
    public AtfLocator firstCategoryLinkAction(Page page) { return categories.firstLinkAction(page); }
    public String categoryItemSelector(int zeroBasedIndex) { return categories.itemSelector(zeroBasedIndex); }

    public Locator failureNames(Locator fails) { return failures.names(fails); }
    public Locator failureItems(Locator fails) { return failures.items(fails); }
    public Locator failureListSurface(Locator fails) { return failures.listSurface(fails); }
    public Locator failureDetailSurface(Locator fails) { return failures.detailSurface(fails); }
    public Locator failureOpenLinks(Locator fails) { return failures.openLinks(fails); }
    public Locator failureDetail(Locator fails) { return failures.detail(fails); }
    public Locator failureCauseValues(Locator fails) { return failures.causeValues(fails); }
    public Locator failureEmptyState(Locator fails) { return failures.emptyState(fails); }
    public AtfLocator failureItemAction(Page page, int zeroBasedIndex) { return failures.itemAction(page, zeroBasedIndex); }
    public AtfLocator firstFailureOpenAction(Page page) { return failures.firstOpenAction(page); }
    public Locator primaryFailureDetails(Locator fails) { return failures.primaryDetails(fails); }

    private static String cssAttribute(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Unsafe selector attribute value");
        }
        return value;
    }

    private static int requireIndex(int zeroBasedIndex) {
        if (zeroBasedIndex < 0) throw new IllegalArgumentException("zeroBasedIndex must be >= 0");
        return zeroBasedIndex;
    }

    private static Locator cardForHeader(Locator header) {
        return header.locator("xpath=..");
    }

    public Locator cardForNode(Locator node) {
        return node.locator("xpath=ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' card ')][1]");
    }

    private static String nodeId(Locator header) {
        Locator node = cardForHeader(header).locator(":scope > .card-header > .card-title > .node");
        return requiredAttribute(node, "id", "structural report node");
    }

    private static String requiredAttribute(Locator locator, String attribute, String description) {
        String value = locator.getAttribute(attribute);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(description + " is missing required " + attribute
                    + " attribute; the report cannot be replayed unambiguously");
        }
        return value;
    }

    private static String nodeHeaderSelector(String nodeId) {
        String safeId = cssAttribute(nodeId);
        return "#test-view .test-content-detail div.card:has(> .card-header > .card-title > .node[id='"
                + safeId + "']) > .card-header[role='button']";
    }

    private static String nodeScopedSelector(String nodeId, String suffix) {
        String safeId = cssAttribute(nodeId);
        return "#test-view .test-content-detail div.card:has(> .card-header > .card-title > .node[id='"
                + safeId + "']) " + suffix;
    }
}
