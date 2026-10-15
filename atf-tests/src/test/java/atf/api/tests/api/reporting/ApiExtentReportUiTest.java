package atf.api.tests.api.reporting;

import atf.api.debugui.DebugUiConfiguration;
import atf.api.debugui.DebugUiServer;
import atf.api.mock.AtfMockTransport;
import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.reporting.ExtentReportSession;
import atf.api.tests.api.lifecycle.LifecycleApiScenarios;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.api.ui.playwright.UiScreenValidation;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Browser-level contracts for a generated Extent artifact.
 *
 * <p>The suite is opt-in because it launches a real browser. Its setup first
 * publishes the real lifecycle API scenarios and only then serves that exact
 * report through the loopback debug publisher. Every test owns one view-level
 * contract and navigates from a fresh hash, so test order cannot hide a broken
 * tab transition.</p>
 */
@EnabledIfSystemProperty(named = "atf.ui.enabled", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class ApiExtentReportUiTest {
    private static final URI API_BASE_URI = URI.create("https://atf.local");
    private static final DateTimeFormatter REPORT_TIMESTAMP = DateTimeFormatter
            .ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")
            .withZone(ZoneId.systemDefault());

    private UiConfiguration configuration;
    private Path report;
    private DebugUiServer server;
    private PlaywrightUiSession browser;

    @BeforeAll
    void generateLifecycleReportAndOpenBrowser() throws Exception {
        configuration = AtfUiConfigurationLoader.load();
        assertTrue(configuration.enabled(), "UI tests require atf.ui.enabled=true");

        report = configuration.reportDirectory().resolve(
                "api-atf-ui-report_" + REPORT_TIMESTAMP.format(Instant.now()) + ".html");
        Files.createDirectories(configuration.reportDirectory());
        ApiModule api = ApiModuleFactory.fromTransport(
                AtfMockTransport.fromProjectConfiguration(), API_BASE_URI);
        try (var session = new ExtentReportSession(report)) {
            LifecycleApiScenarios.publish(session, api, new ApiScenarioLifecycleRunner());
        }

        assertTrue(Files.isRegularFile(report), "Lifecycle setup must publish the UI report");
        server = DebugUiServer.start(DebugUiConfiguration.forHtml(report, 0));
        browser = PlaywrightUiSession.open(configuration.browser());
        browser.navigate(server.uriFor(report));
    }

    @AfterAll
    void closeBrowserAndServer() {
        if (browser != null) browser.close();
        if (server != null) server.close();
    }

    @Test
    void overviewTabShowsTheSummaryAndTagTables() {
        Page page = openTab("#dashboard-view");
        Locator overview = page.locator("#dashboard-view");

        assertAll(
                () -> assertTrue(overview.isVisible()),
                () -> assertTrue(overview.getByText("Tests Passed", new Locator.GetByTextOptions()
                        .setExact(true)).isVisible()),
                () -> assertTrue(overview.getByText("Tests Failed", new Locator.GetByTextOptions()
                        .setExact(true)).isVisible()),
                () -> assertTrue(overview.locator("table").count() >= 2),
                () -> assertTrue(overview.locator(".category-container table tbody td")
                        .filter(new Locator.FilterOptions().setHasText("lifecycle"))
                        .first().isVisible()));
    }

    @Test
    void testsTabShowsLifecycleClassAndAllExecutionPhases() {
        Page page = openTab("#test-view");
        Locator tests = page.locator("#test-view");
        classListItem(tests).click();
        expandNode(tests, "Users API — setup → test → teardown");

        assertAll(
                () -> assertTrue(tests.isVisible()),
                () -> assertTrue(tests.locator("h5.test-status:visible")
                        .filter(new Locator.FilterOptions()
                                .setHasText("ApiLifecycleAndCombinationsTests"))
                        .first().isVisible()),
                () -> assertTrue(nodeButton(tests, "Users API — setup → test → teardown")
                        .isVisible()),
                () -> assertTrue(nodeButton(tests, "SETUP").isVisible()),
                () -> assertTrue(nodeButton(tests, "TEST BODY").isVisible()),
                () -> assertTrue(nodeButton(tests, "TEARDOWN").isVisible()));
    }

    @Test
    void categoryTabShowsLifecycleCategoryAndLinkedScenario() {
        Page page = openTab("#category-view");
        Locator category = page.locator("#category-view");
        visibleCategoryName(category, "lifecycle").click();

        assertAll(
                () -> assertTrue(category.isVisible()),
                () -> assertTrue(visibleCategoryName(category, "lifecycle").isVisible()),
                () -> assertTrue(category.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
                        .setName("Users API — setup → test → teardown").setExact(true))
                        .isVisible()));
    }

    @Test
    void testsAndCategoryTabsExposeFunctionalStatusAndTagFilters() {
        Page page = openTab("#test-view");
        Locator tests = page.locator("#test-view");
        Locator testStatusMenu = tests.locator("#status-toggle");
        Locator testTagMenu = tests.locator("#tag-toggle");
        assertTrue(testStatusMenu.locator(".dropdown-item[status]").count() > 0,
                "Tests status filter must expose the statuses present in this report");
        assertTrue(testTagMenu.locator(".dropdown-item").count() > 0);

        page = openTab("#category-view");
        Locator category = page.locator("#category-view");
        Locator categoryStatusMenu = category.locator("#category-status-toggle");
        Locator categoryTagMenu = category.locator("#category-tag-toggle");
        assertTrue(categoryStatusMenu.count() == 1, "Category status filter must be rendered");
        assertTrue(categoryTagMenu.count() == 1, "Category tag filter must be rendered");
        category.locator(".test-list-tools a[aria-label='Filter categories by status']").click();
        String existingCategoryStatus = category.locator(".test-list-wrapper>.test-list-item>.test-item .test-contents .info [status]")
                .first().getAttribute("status");
        assertTrue(existingCategoryStatus != null && !existingCategoryStatus.isBlank(),
                "Category entries must expose their aggregate status");
        categoryStatusMenu.locator(".dropdown-item[data-filter-value='" + existingCategoryStatus + "']").click();
        assertTrue(category.locator(".test-list .test-item[data-atf-filter-hidden='false']").count() > 0,
                "Category status filter must keep matching categories visible");
        category.locator(".test-list-tools a[aria-label='Filter categories by tag']").click();
        Locator categoryTags = categoryTagMenu.locator(".dropdown-item[data-filter-kind='tag']:not([data-filter-value='clear'])");
        assertTrue(categoryTags.count() > 0, "Category tag filter must expose category values");
        categoryTags.first().click();
        assertTrue(category.locator(".test-list .test-item[data-atf-filter-hidden='false']").count() == 1,
                "Category tag filter must select one matching category");
        categoryTagMenu.locator(".dropdown-item[data-filter-value='clear']").click();
        page.keyboard().press("Escape");
    }

    @Test
    void failsTabShowsCauseAndReturnsToTheExactTest() {
        Page page = openTab("#atf-fails-view");
        Locator fails = page.locator("#atf-fails-view");

        assertAll(
                () -> assertTrue(fails.isVisible()),
                () -> assertTrue(fails.locator("#atf-failure-list .name")
                        .filter(new Locator.FilterOptions()
                                .setHasText("Users API — setup → test → teardown"))
                        .first().isVisible()),
                () -> assertTrue(fails.getByText("PRIMARY:", new Locator.GetByTextOptions()
                        .setExact(false)).first().isVisible()),
                () -> assertTrue(fails.locator("a.atf-failure-open").first().isVisible()));

        fails.locator("a.atf-failure-open").first().click();
        assertTrue(page.locator("#test-view").isVisible());
        assertTrue(exact(page.locator("#test-view"), "ApiLifecycleAndCombinationsTests").isVisible());
    }

    @Test
    void expandedRequestResponseAndExecutionTablesShareTheEvidenceContract() {
        Page page = openTab("#test-view");
        Locator tests = page.locator("#test-view");
        classListItem(tests).click();
        expandNode(tests, "Users API — setup → test → teardown");
        expandNode(tests, "TEST BODY");
        expandNode(tests, "GET /public/v2/users/1");

        assertAll(
                () -> assertTrue(tests.locator("table.helm-request-table").count() > 0),
                () -> assertTrue(tests.locator("table.helm-trace-table").count() > 0),
                () -> assertTrue(tests.locator("table.execution-table").count() > 0),
                () -> assertTrue(tests.locator("table.execution-table:visible td")
                        .filter(new Locator.FilterOptions().setHasText("response.duration.ms"))
                        .first().isVisible()));

        for (String evidence : new String[]{
                "Request details · 1 fields",
                "Response details · 2 fields",
                "Execution evidence · 1 attempts"}) {
            Locator disclosure = exact(tests, evidence);
            if (disclosure.isVisible()) disclosure.click();
        }

        assertAll(
                () -> assertEquals("width:var(--atf-evidence-key-column-width)",
                        tests.locator("table.helm-request-table").first().locator("col")
                                .first().getAttribute("style")),
                () -> assertEquals("width:var(--atf-evidence-key-column-width)",
                        tests.locator("table.helm-trace-table").first().locator("col")
                                .first().getAttribute("style")),
                () -> assertEquals("width:var(--atf-evidence-key-column-width)",
                        tests.locator("table.execution-table").first().locator("col")
                                .first().getAttribute("style")),
                () -> assertTrue(tests.locator("table.retry-history-table:visible")
                        .getByText("final response", new Locator.GetByTextOptions()
                                .setExact(true)).first().isVisible()));
    }

    @Test
    void phaseDurationsExposeTheStructuralAggregationContract() {
        Page page = openTab("#test-view");
        Locator tests = page.locator("#test-view");
        classListItem(tests).click();
        expandNode(tests, "Users API — setup → test → teardown");

        Locator scenario = nodeButton(tests, "Users API — setup → test → teardown");
        Locator setup = nodeButton(tests, "SETUP");
        Locator body = nodeButton(tests, "TEST BODY");
        Locator teardown = nodeButton(tests, "TEARDOWN");

        assertAll(
                () -> assertTrue(scenario.isVisible()),
                () -> assertTrue(setup.isVisible()),
                () -> assertTrue(body.isVisible()),
                () -> assertTrue(teardown.isVisible()),
                () -> assertDurationMarker(scenario),
                () -> assertDurationMarker(setup),
                () -> assertDurationMarker(body),
                () -> assertDurationMarker(teardown));
    }

    @Test
    @DisplayName("GIVEN every Extent node is opened WHEN durations are inspected THEN each parent honors direct-child and transport aggregation")
    void everyExtentNodeKeepsItsOwnDurationAndParentsSumDirectChildren() {
        System.out.println("[ATF UI] GIVEN the generated Extent report is opened in Tests");
        Page page = openTab("#test-view");
        Locator tests = page.locator("#test-view");
        Locator owners = tests.locator(".test-list .test-item");
        assertTrue(owners.count() > 0, "The report must expose at least one API test owner");

        System.out.println("[ATF UI] WHEN every class and every visible Extent node is opened one by one");
        for (int ownerIndex = 0; ownerIndex < owners.count(); ownerIndex++) {
            tests.locator(".test-list .test-item").nth(ownerIndex).click();
            Locator headers = tests.locator(".test-content-detail:visible .card-header[role='button']");
            for (int nodeIndex = 0; nodeIndex < headers.count(); nodeIndex++) {
                Locator header = tests.locator(".test-content-detail:visible .card-header[role='button']").nth(nodeIndex);
                if (header.isVisible() && !"true".equals(header.getAttribute("aria-expanded"))) {
                    header.click();
                }
            }
            Map<?, ?> result = durationContract(page);
            int selectedOwnerIndex = ownerIndex;
            Map<?, ?> selectedResult = result;
            assertTrue(((Number) result.get("nodes")).intValue() > 0,
                    "Each selected class must expose duration-bearing Extent nodes");
            assertTrue(((List<?>) result.get("violations")).isEmpty(),
                    () -> "Duration violations for owner " + selectedOwnerIndex + ": " + selectedResult.get("violations"));
        }

        System.out.println("[ATF UI] THEN every node has its own numeric duration and each structural parent honors direct-child plus transport aggregation");
        UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                page, configuration.screenValidation(), "Duration contract — final Tests tree");
        System.out.println("[ATF UI] inline-base64-screenshot bytes=" + evidence.base64Png().length());
        assertTrue(evidence.hasInlineImage(), "The final duration validation must keep visual Base64 evidence");
    }

    @SuppressWarnings("unchecked")
    private static Map<?, ?> durationContract(Page page) {
        return (Map<?, ?>) page.evaluate("""
                () => {
                  const text = element => String(element?.textContent || '').replace(/\\s+/g, ' ').trim();
                  const parse = element => {
                    const marked = element?.getAttribute('data-duration-ms');
                    const value = Number(marked);
                    return marked !== null && marked !== '' && Number.isFinite(value) ? value : null;
                  };
                  const formatted = value => value <= 300
                    ? String(value) + ' ms'
                    : (value / 1000).toFixed(3).replace(/0+$/, '').replace(/\\.$/, '') + ' s';
                  const visible = element => {
                    let current = element;
                    while (current) {
                      const style = getComputedStyle(current);
                      if (style.display === 'none' || style.visibility === 'hidden') return false;
                      current = current.parentElement;
                    }
                    return true;
                  };
                  const header = card => Array.from(card.children || [])
                    .find(child => child.classList?.contains('card-header'));
                  const badge = card => {
                    const ownHeader = header(card);
                    return ownHeader?.querySelector('.node-info .badge-default')
                      || ownHeader?.querySelector('.card-toolbar .badge-default')
                      || ownHeader?.querySelector('.card-toolbar .badge');
                  };
                  const children = card => {
                    const accordion = Array.from(card.children || [])
                      .find(child => child.classList?.contains('accordion'));
                    return accordion ? Array.from(accordion.children)
                      .filter(child => child.classList?.contains('card'))
                      .filter(child => child.matches('[data-atf-node-role], [data-atf-scenario-id]')) : [];
                  };
                  const transportEvidence = card => {
                    const collapse = Array.from(card.children || [])
                      .find(child => child.classList?.contains('collapse'));
                    return Boolean(collapse?.querySelector(
                      '.execution-table, .helm-trace-table, .helm-request-details-content, .helm-response-details-content'));
                  };
                  const nodes = Array.from(document.querySelectorAll(
                    '#test-view .test-content-detail:where(:not(.d-none)) .card[data-atf-node-role],'
                    + '#test-view .test-content-detail:where(:not(.d-none)) .card[data-atf-scenario-id]'))
                    .filter(visible);
                  const violations = [];
                  let checked = 0;
                  const inspect = card => {
                    checked++;
                    const value = parse(card), durationBadge = badge(card);
                    if (value === null) violations.push('missing duration on ' + text(card));
                    if (!durationBadge || durationBadge.getAttribute('data-duration-ms') !== String(value)) {
                      violations.push('badge/card duration mismatch on ' + text(card));
                    } else if (text(durationBadge) !== formatted(value)) {
                      violations.push('duration formatting mismatch on ' + text(card));
                    }
                    const childValues = children(card).map(inspect).filter(value => value !== null);
                    if (childValues.length > 0) {
                      const childTotal = childValues.reduce((total, child) => total + child, 0);
                      const measured = Number(card.getAttribute('data-atf-measured-duration-ms'));
                      const ownAction = transportEvidence(card) && Number.isFinite(measured) ? measured : 0;
                      const expected = Math.max(childTotal, ownAction);
                      if (value !== expected) {
                        violations.push('parent total mismatch on ' + text(card)
                          + ': expected ' + expected + ', actual ' + value);
                      }
                    }
                    return value;
                  };
                  nodes.filter(node => !node.parentElement?.closest('.card[data-atf-node-role], .card[data-atf-scenario-id]'))
                    .forEach(inspect);
                  return {nodes: checked, violations};
                }
                """);
    }

    Path generatedReport() {
        return report;
    }

    private Page openTab(String hash) {
        Page page = browser.page();
        page.navigate(server.uriFor(report).toString() + hash);
        page.waitForLoadState();
        // The report binds tab filters and scenario-collapse normalization on
        // short deferred callbacks.  Let that browser state settle before the
        // test starts querying the visible tree, otherwise a phase card can be
        // observed while its parent scenario is still being normalized.
        page.waitForTimeout(300);
        return page;
    }

    private static Locator classListItem(Locator tests) {
        return tests.locator(".test-list .test-item .name")
                .filter(new Locator.FilterOptions()
                        .setHasText("ApiLifecycleAndCombinationsTests"))
                .first();
    }

    private static Locator exact(Locator scope, String text) {
        return scope.getByText(text, new Locator.GetByTextOptions().setExact(true)).first();
    }

    private static Locator nodeButton(Locator scope, String text) {
        Locator candidates = scope.locator("div.card-header[role='button']")
                .filter(new Locator.FilterOptions().setHasText(text));
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (System.nanoTime() < deadline) {
            for (int index = 0; index < candidates.count(); index++) {
                Locator candidate = candidates.nth(index);
                if (candidate.isVisible()) {
                    return candidate;
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return scope.locator("a.node")
                .filter(new Locator.FilterOptions().setHasText(text))
                .first();
    }

    private static Locator expandNode(Locator scope, String text) {
        Locator node = nodeButton(scope, text);
        if (!"true".equals(node.getAttribute("aria-expanded"))) {
            node.click();
        }
        return node;
    }

    private static void assertDurationMarker(Locator node) {
        Locator marker = node.locator(".node-info .badge-default").first();
        assertTrue(marker.isVisible(), "Expanded node must expose its duration badge");
        assertTrue(marker.getAttribute("data-duration-ms") != null,
                "Expanded node must expose a numeric duration marker");
        assertTrue(marker.getAttribute("data-duration-source") != null,
                "Expanded node must expose the duration source marker");
    }

    private static Locator visibleCategoryName(Locator scope, String name) {
        Locator candidates = scope.locator(".test-list .test-item .name")
                .filter(new Locator.FilterOptions().setHasText(name));
        for (int index = 0; index < candidates.count(); index++) {
            Locator candidate = candidates.nth(index);
            if (candidate.isVisible()) {
                return candidate;
            }
        }
        return candidates.first();
    }
}
