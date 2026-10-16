package atf.api.reporting;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.http.ApiResponse;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ratchet for the interactive Tests tree observed in the .NET reference.
 * These tests intentionally protect the rendered behaviour contract, not only
 * the existence of individual strings in the report.
 */
class ExtentReportInteractionContractTest {
    @Test
    void testTreePublishesCollapsibleScenarioPhaseAndEvidenceContracts() throws Exception {
        Path report = TestReportPaths.report("extent-interaction-contract.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UsersApiTests", "GET /public/v2/users — collection contract",
                    List.of("api", "users", "contract"));

            var setup = scenario.startPhase("SETUP");
            setup.startRequest("GET /public/v2/users", "GET", "/public/v2/users")
                    .complete(AtfReportStatus.PASS);
            setup.complete(AtfReportStatus.PASS);

            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("GET /public/v2/users", "GET", "/public/v2/users");
            request.detail("RESPONSE DURATION (ms)", "12 ms");
            request.detail("URL", "https://example.test/public/v2/users");
            request.detail("HTTP STATUS", "200");
            request.detail("Content-Type", "application/json");
            request.execution(new ApiExecutionResult(
                    new ApiResponse(200, "OK", Map.of("Content-Type", "application/json"),
                            Duration.ofMillis(12), "https://example.test/public/v2/users"),
                    1, Duration.ofMillis(12), List.of(), List.of()));
            request.json("REQUEST JSON", "{\"limit\":10}");
            request.json("RESPONSE JSON", "[{\"id\":1,\"name\":\"ATF\"}]");
            var validation = request.startValidation("Validate: user collection contract");
            validation.assertions(List.of(
                    new AtfAssertion("HTTP status", "200", "200", true),
                    new AtfAssertion("JSON path exists: $[0].id", "present", "1", true)));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);

            var teardown = scenario.startPhase("TEARDOWN");
            teardown.startRequest("DELETE /public/v2/users/1", "DELETE", "/public/v2/users/1")
                    .complete(AtfReportStatus.PASS);
            teardown.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int validation = html.indexOf("<span>Validate: user collection contract</span>");
        int request = html.lastIndexOf(
                "<span>GET /public/v2/users — collection contract</span>", validation);

        assertAll(
                () -> assertTrue(request >= 0 && validation > request,
                        "request must precede its nested validation substep"),
                () -> assertTrue(divDepthAt(html, validation) > divDepthAt(html, request),
                        "validation must be rendered inside the Gherkin request node, never as a phase sibling"),
                () -> assertTrue(html.contains("function collapseDirectScenarioCards()")),
                () -> assertTrue(html.contains("function collapseScenarioCards()")),
                () -> assertTrue(html.contains("function bindScenarioCollapse()")),
                () -> assertTrue(html.contains("function bindPhaseCollapse()"),
                        "lifecycle phase headers must have an explicit collapse controller"),
                () -> assertTrue(html.contains("function toggleScenario(card,panel,node,e)")),
                () -> assertTrue(html.contains("function initializeScenarioCard(card,panel)")),
                () -> assertTrue(html.contains("data-atf-scenario-node-collapse-bound")),
                () -> assertTrue(html.contains("data-atf-scenario-open")),
                () -> assertTrue(html.contains("data-atf-phase-open")),
                () -> assertTrue(html.contains("data-atf-phase-header-bound")),
                () -> assertTrue(html.contains("e.stopPropagation()")),
                () -> assertTrue(html.contains("e.stopImmediatePropagation()")),
                () -> assertTrue(html.contains("data-atf-node-role=\"phase\"")),
                () -> assertTrue(html.contains("data-atf-node-role=\"request\"")),
                () -> assertTrue(html.contains("data-atf-node-role=\"validation\"")),
                () -> assertTrue(html.contains("function normalizeEvidenceDisclosures()")),
                () -> assertTrue(html.contains("function nodeEventTable(card)"),
                        "evidence normalization must support the live vendor panel class"),
                () -> assertTrue(html.contains("card.querySelector(':scope>.show')"),
                        "evidence normalization must handle Extent panels rendered as .show"),
                () -> assertTrue(html.contains("normalizeCodeBlocks();assignNodeRoles();aggregateClassDurations();normalizeEvidenceDisclosures();bindPhaseCollapse();bindNodeCollapse();if(card.scrollIntoView"),
                        "opening a lazily projected scenario must normalize its evidence after cloning"),
                () -> assertTrue(html.contains("function normalizeCategorySummaryLabels(){q('.category-view .test-contents .info')"),
                        "every category projection must normalize the native 'others' label before selection"),
                () -> assertTrue(html.contains("function normalizeChartPalettes()"),
                        "overview charts must use the same semantic palette as status badges"),
                () -> assertTrue(html.contains("['--atf-pass','--atf-fail','--atf-skip','--atf-skip','--atf-info']"),
                        "chart slices must map pass, fail, warning, skip and info to shared tokens"),
                 () -> assertTrue(html.contains("<td>URL</td><td>https://example.test/public/v2/users</td>"),
                         "technical trace must expose the request URL"),
                 () -> assertTrue(html.contains("<td>response.url</td><td>https://example.test/public/v2/users</td>"),
                         "execution evidence must retain the effective URL"),
                 () -> assertTrue(html.contains("<td>HTTP STATUS</td><td>200</td>"),
                         "technical trace must expose the response status"),
                 () -> assertTrue(html.contains("Request details · 1 fields"),
                         "request context must have its own disclosure"),
                 () -> assertTrue(html.contains("Response details · 2 fields"),
                         "response metadata must have its own disclosure"),
                 () -> assertTrue(html.contains("Execution evidence · 1 attempts"),
                         "execution evidence must have its own semantic label"),
                 () -> assertTrue(html.contains("execution-table"),
                         "execution evidence must retain response execution fields"),
                 () -> assertTrue(html.contains("execution-evidence"),
                         "execution evidence must be normalized into its own disclosure"),
                 () -> assertTrue(html.contains("helm-request-details-content"),
                         "request details must have a dedicated semantic container"),
                 () -> assertTrue(html.contains("helm-response-details-content"),
                         "response details must have a dedicated semantic container"),
                 () -> assertTrue(html.contains("request-json-body"),
                         "request JSON must remain inside request details"),
                 () -> assertTrue(html.contains("response-json-body"),
                         "response JSON must remain inside response details"),
                () -> assertFalse(html.contains("ROUTE TEMPLATE"),
                        "request evidence must not regress to the relative route label"),
                () -> assertTrue(html.contains("function normalizeDisclosureState()"),
                        "all card disclosures must share one accessible state synchronizer"),
                () -> assertTrue(html.contains("atf-disclosure-glyph"),
                        "disclosures must use one compact visual glyph"),
                () -> assertTrue(html.contains("aria-expanded"),
                        "card and evidence disclosure state must be keyboard-readable"),
                () -> assertTrue(html.contains("content:'＋'"),
                        "collapsed disclosure must render the typographic plus glyph"),
                () -> assertTrue(html.contains(">.card-header{display:flex!important;align-items:center!important"),
                        "card disclosure glyph, title and metadata must share one row"),
                () -> assertTrue(html.contains(">.card-header>.card-toolbar{order:2!important"),
                        "duration and status must be ordered after the title"),
                () -> assertTrue(html.contains(".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header{align-items:center!important}")
                                && html.contains(".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar{align-self:center!important}")
                                && html.contains("card[data-atf-scenario-id]>.card-header>.card-toolbar>ul,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-toolbar>ul{display:flex!important;align-items:center!important")
                                && html.contains("position:static!important;top:auto!important;float:none!important;display:flex!important;align-items:center!important"),
                        "scenario status and duration must be centered on the first title row, not on the tag row"),
                () -> assertTrue(html.contains("margin-left:auto!important;white-space:nowrap!important"),
                        "duration and status must stay on the right without wrapping"),
                () -> assertFalse(html.contains("[+] expand"),
                        "verbose expand text must not be rendered"),
                () -> assertFalse(html.contains("[-] collapse"),
                        "verbose collapse text must not be rendered"),
                () -> assertTrue(html.contains("data-atf-scenario-id")),
                () -> assertTrue(html.contains("function normalizeScenarioAnnotations()")),
                () -> assertTrue(html.contains(".card[data-atf-scenario-id]")),
                () -> assertTrue(html.contains("annotation.setAttribute('role','list')"),
                        "scenario tags must be exposed as an accessible inline list"),
                () -> assertTrue(html.contains("annotation.setAttribute('aria-label','Scenario annotations')")),
                () -> assertTrue(html.contains("atf-tag-annotation-selection")),
                () -> assertTrue(html.contains("title.querySelectorAll(':scope > .node-attr')"),
                        "native Extent tags must be replaced by one inline ATF tag list"),
                () -> assertTrue(html.contains("card.querySelectorAll(':scope > .accordion .node-attr')")),
                () -> assertTrue(html.contains(".atf-scenario-annotations{display:flex"),
                        "scenario tags must be styled inline like the .NET reference"),
                        () -> assertTrue(html.contains(".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header{align-items:center!important}")
                                         && html.contains(".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar{align-self:center!important}")
                                         && html.contains(".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title{display:grid!important;grid-template-columns:minmax(0,1fr)!important;grid-template-rows:auto auto!important")
                                         && html.contains("align-items:center!important;justify-items:start!important;row-gap:0!important;min-width:0!important}")
                                         && html.contains(".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title>a.node{grid-column:1!important;grid-row:1!important;width:100%!important;min-width:0!important;text-align:left!important;justify-content:flex-start!important}")
                                         && html.contains(".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title>.atf-scenario-annotations{grid-column:1!important;grid-row:2!important;justify-self:start!important;align-self:center!important"),
                                "scenario title and annotations must stay left aligned, with the complete group vertically centered"),
                        () -> assertTrue(html.contains("margin-top:0!important")
                                && html.contains("padding:0 3px!important;border:1px solid var(--atf-accent14)!important")
                                && html.contains("font-size:7.5px!important;line-height:9px!important")
                                && html.contains("color:var(--atf-annotation-muted)!important")
                                && html.contains(".test-view .test-content-detail .card-title>.atf-scenario-annotations>.atf-tag-annotation"),
                        "scenario annotations must remain compact, close to the title and softly muted"),
                () -> assertTrue(html.contains("line-height:18.2px!important;font-weight:400!important")
                                && html.contains("display:inline-block!important;padding:6px 10.5px!important")
                                && html.contains("height:18.2px!important;min-height:18.2px!important;padding:0 5px!important")
                                && html.contains("display:flex!important;align-items:center!important;gap:4px!important;height:var(--atf-badge-height)!important")
                                && html.contains("min-height:38.5px!important;height:auto!important"),
                        "request, validation and phase rows must use the compact vertical rhythm"),
                () -> assertTrue(html.contains("height:var(--atf-badge-height)!important;min-height:var(--atf-badge-height)!important;margin:0!important;padding:var(--atf-badge-padding)!important;line-height:var(--atf-badge-line-height)!important"),
                        "all status and duration badges in the test tree must reuse the shared geometry"),
                () -> assertTrue(html.contains("margin:4px 0!important;padding:0!important;border:1px solid var(--atf-border)!important")
                                && html.contains("border-left:1px solid var(--atf-border)!important;border-radius:6px!important")
                                && html.contains("background:var(--atf-panel-2)!important;box-shadow:none!important"),
                        "request and validation substeps must follow the restrained .NET card treatment"),
                () -> assertTrue(html.contains("data-atf-node-role='request']>.card-header{background:rgba(122,162,255,.045)!important}")
                                && html.contains("data-atf-node-role='validation']>.card-header{background:rgba(255,255,255,.025)!important}"),
                        "request and validation substeps must remain visually distinct without overpowering the phase"),
                () -> assertTrue(html.contains("function bindNodeCollapse()")
                                && html.contains("data-atf-node-open")
                                && html.contains("bindNodeCollapse()"),
                        "request and validation disclosures must switch from plus to minus when opened"),
                () -> assertFalse(html.contains("details.atf-scenario-annotations"),
                        "the generic Scenario annotations disclosure must not replace the visible tags"),
                () -> assertTrue(html.contains("response-evidence")),
                 () -> assertTrue(html.contains("execution-evidence")),
                 () -> assertTrue(html.contains("Execution evidence")),
                () -> assertTrue(html.contains("kind==='response-evidence'"),
                        "response details must retain the TECHNICAL TRACE classification after DOM normalization"),
                () -> assertTrue(html.contains("json-evidence")),
                () -> assertTrue(html.contains("assertion-evidence")),
                () -> assertTrue(html.contains("helm-validation-table")),
                () -> assertTrue(html.contains("style.setProperty('display',next?'block':'none','important')")),
                () -> assertFalse(html.contains("class=\"collapse show\""),
                        "evidence must start collapsed in the artifact")
        );
    }

    private static int divDepthAt(String html, int index) {
        String prefix = html.substring(0, index);
        int depth = 0;
        for (int i = 0; i < prefix.length(); i++) {
            if (prefix.startsWith("<div", i)) {
                depth++;
            } else if (prefix.startsWith("</div>", i)) {
                depth--;
            }
        }
        return depth;
    }

    @Test
    void lifecyclePhasesRemainOrderedWhenSomePhasesAreSkippedOrFailed() throws Exception {
        Path report = TestReportPaths.report("extent-interaction-lifecycle-contract.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ApiLifecycleAndCombinationsTests",
                    "Users API — pass → teardown fail perspective", List.of("api", "lifecycle", "failure"));
            var body = scenario.startPhase("TEST BODY");
            body.startRequest("GET /public/v2/users", "GET", "/public/v2/users")
                    .complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            var teardown = scenario.startPhase("TEARDOWN");
            teardown.detail("FAILURE CAUSE", "teardown failure perspective");
            teardown.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.indexOf("TEST BODY") < html.indexOf("TEARDOWN")),
                () -> assertTrue(html.contains("FAILURE CAUSE")),
                () -> assertTrue(html.contains("teardown failure perspective")),
                () -> assertTrue(html.contains("status=\"fail\"")),
                () -> assertTrue(html.contains("function failureContext(item)")),
                () -> assertTrue(html.contains("data-atf-failure-phase")),
                () -> assertTrue(html.contains("class=\"atf-failure-open\"")
                                && html.contains("atfOpenTest(id,scenarioIndex)")
                                && html.contains("onclick=\"if(typeof atfOpenTest===")
                                && html.contains("href=\"#test-id="),
                        "Fails must expose an executable deep-link and a native hash fallback")
        );
    }

    @Test
    void failsProjectionKeepsRequestOwnershipAndFailureCauseChain() throws Exception {
        Path report = TestReportPaths.report("extent-fails-request-ownership-contract.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ApiLifecycleAndCombinationsTests",
                    "Users API — setup → test → teardown", List.of("api", "users", "lifecycle"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("GET /public/v2/users/1", "GET", "/public/v2/users/1");
            request.detail("RESPONSE DURATION (ms)", "12 ms");
            request.detail("URL", "https://example.test/public/v2/users/1");
            request.json("RESPONSE JSON", "{\"id\":1}");
            var validation = request.startValidation("Contract validation");
            validation.assertions(List.of(
                    new AtfAssertion("HTTP status", "201", "200", false),
                    new AtfAssertion("Content-Type", "application/json", "application/json", true)));
            validation.complete(AtfReportStatus.FAIL);
            request.complete(AtfReportStatus.FAIL);
            body.complete(AtfReportStatus.FAIL);

            var teardown = scenario.startPhase("TEARDOWN");
            teardown.detail("FAILURE CAUSE", "teardown cleanup failed: Authorization: Bearer ***REDACTED***");
            teardown.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("function scopeFailureContextToPhase(item,context)"),
                        "Fails must resolve the request from the failing phase, not the first request in the scenario"),
                () -> assertTrue(html.contains("context.request=text(request,'.card-title a.node')"),
                        "the request shown in Fails must belong to the failed phase"),
                () -> assertTrue(html.contains("q('.helm-trace-table tbody tr,.execution-table tbody tr',request)"),
                        "the duration shown in Fails must belong to the failed request"),
                () -> assertTrue(html.contains("var durationMs=atfDurationValue(rawDuration)")
                                && html.contains("duration=durationMs===null?rawDuration:atfFormatDuration(durationMs)"),
                        "execution evidence duration must use the shared duration parser and formatter"),
                () -> assertTrue(html.contains("if(!ctx.message||ctx.message.indexOf('PRIMARY:')<0)"),
                        "an assertion row must not overwrite the primary/secondary cause chain"),
                () -> assertTrue(html.contains("PRIMARY: ") && html.contains("SECONDARY: "),
                        "the Fails projection must preserve the ordered cause chain")
        );
    }

    @Test
    void testsTabPublishesIndependentNavigationAndFilterContracts() throws Exception {
        Path report = TestReportPaths.report("extent-tests-tab-contract.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UsersApiTests", "GET /public/v2/users — collection contract",
                    List.of("api", "users", "contract", "smoke"));
            var body = scenario.startPhase("TEST BODY");
            body.startRequest("GET /public/v2/users", "GET", "/public/v2/users")
                    .complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("id=\"nav-test\" class=\"dropdown-toggle\" href=\"#test-view\"")),
                () -> assertTrue(html.contains("id=\"nav-category\" class=\"dropdown-toggle\" href=\"#category-view\"")),
                () -> assertTrue(html.contains("id=\"nav-fails\" class=\"dropdown-toggle\" href=\"#atf-fails-view\"")),
                () -> assertTrue(html.contains("id=\"nav-overview\" class=\"dropdown-toggle\" href=\"#dashboard-view\"")),
                () -> assertTrue(html.contains("data-atf-tab=\"overview\"")),
                () -> assertTrue(html.contains("data-atf-tab=\"fails\"")),
                () -> assertTrue(html.contains("var atfFilterState={status:'',tag:''}")),
                () -> assertTrue(html.contains("function filterMatches(item)")),
                () -> assertTrue(html.contains("function applyTestFilters()")),
                () -> assertTrue(html.contains("function setTestFilter(kind,value)")),
                () -> assertTrue(html.contains("function normalizeTagFilter()")),
                () -> assertTrue(html.contains("function normalizeCategorySummaryLabels()"),
                        "Category summaries must use Warning instead of the native Others label"),
                () -> assertTrue(html.contains("data-atf-filter-hidden")),
                () -> assertTrue(html.contains("e.preventDefault();e.stopPropagation()")),
                () -> assertTrue(html.contains("aria-pressed")),
                () -> assertTrue(html.contains("data-atf-tab=\"fails\"")),
                () -> assertTrue(html.contains("function activateFromHash()")),
                () -> assertTrue(html.contains("window.addEventListener('hashchange',activateFromHash)"))
        );
    }

    @Test
    void testsFiltersPreserveOwnerScopeAndTabIdentity() throws Exception {
        Path report = TestReportPaths.report("extent-tests-filter-owner-contract.html");
        try (var session = new ExtentReportSession(report)) {
            var users = session.startScenario("UsersApiTests", "GET /public/v2/users — collection contract",
                    List.of("api", "users", "contract"));
            var body = users.startPhase("TEST BODY");
            body.startRequest("GET /public/v2/users", "GET", "/public/v2/users")
                    .complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            users.complete(AtfReportStatus.PASS);

            var posts = session.startScenario("PostsApiTests", "GET /public/v2/posts — collection contract",
                    List.of("api", "posts", "contract"));
            var postsBody = posts.startPhase("TEST BODY");
            postsBody.startRequest("GET /public/v2/posts", "GET", "/public/v2/posts")
                    .complete(AtfReportStatus.PASS);
            postsBody.complete(AtfReportStatus.PASS);
            posts.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int tests = html.indexOf("href=\"#test-view\"");
        int category = html.indexOf("href=\"#category-view\"");
        int fails = html.indexOf("href=\"#atf-fails-view\"");
        int overview = html.indexOf("href=\"#dashboard-view\"");

        assertAll(
                () -> assertTrue(tests >= 0 && tests < category && category < fails && fails < overview,
                        "tab order must remain Tests, Category, Fails, Overview"),
                () -> assertTrue(html.contains(
                        "rootStatusMatches=!atfFilterState.status||statusOf(root)===atfFilterState.status"),
                        "status filtering must use the class owner verdict"),
                () -> assertTrue(html.contains(
                        "testTags(item).some(function(tag){return tag.toLowerCase()===atfFilterState.tag;})"),
                        "tag filtering must use the scenario owner tags"),
                () -> assertTrue(html.contains("atfFilterState[kind]===normalized?'':normalized"),
                        "reselecting a filter must deselect it"),
                () -> assertTrue(html.contains("if(!selectedVisible&&firstVisible)"),
                        "filtering must move the detail pane to a visible owner"),
                () -> assertTrue(html.contains("e.preventDefault();e.stopPropagation()")
                                && html.contains("window.location.hash"),
                        "filter clicks must not navigate away from Tests"),
                () -> assertTrue(html.contains("data-atf-filter-hidden")
                                && html.contains("aria-pressed"),
                        "filter state must be inspectable and accessible")
        );
    }

    @Test
    void categoriesUseIndependentProjectionAndExactScenarioDeepLinks() throws Exception {
        Path report = TestReportPaths.report("extent-category-tab-contract.html");
        try (var session = new ExtentReportSession(report)) {
            var users = session.startScenario("UsersApiTests", "GET /public/v2/users — collection contract",
                    List.of("api", "users", "contract"));
            var usersBody = users.startPhase("TEST BODY");
            usersBody.startRequest("GET /public/v2/users", "GET", "/public/v2/users")
                    .complete(AtfReportStatus.PASS);
            usersBody.complete(AtfReportStatus.PASS);
            users.complete(AtfReportStatus.PASS);

            var posts = session.startScenario("PostsApiTests", "GET /public/v2/posts — collection contract",
                    List.of("api", "posts", "contract"));
            var postsBody = posts.startPhase("TEST BODY");
            postsBody.startRequest("GET /public/v2/posts", "GET", "/public/v2/posts")
                    .complete(AtfReportStatus.PASS);
            postsBody.complete(AtfReportStatus.PASS);
            posts.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("var atfCategoryState={selectedName:''}")),
                () -> assertTrue(html.contains("function categoryItems()")),
                () -> assertTrue(html.contains("function renderCategoryDetail(item)")),
                () -> assertTrue(html.contains("function selectCategory(item)")),
                () -> assertTrue(html.contains("function bindCategorySelection()")),
                () -> assertTrue(html.contains("function ensureCategoryDetail()")),
                () -> assertTrue(html.contains("detail.innerHTML=''"),
                        "a category selection must clear the previous category projection"),
                () -> assertTrue(html.contains("projection.classList.add('atf-category-detail')")),
                () -> assertTrue(html.contains("data-atf-category-title")),
                () -> assertTrue(html.contains("data-atf-scenario-index")),
                () -> assertTrue(html.contains("&scenario-index='+index"),
                        "category links must preserve the exact scenario inside a class root"),
                () -> assertTrue(html.contains("function openScenarioCard(card)"),
                        "a category deep-link must open the exact scenario card, not only its class root"),
                () -> assertTrue(html.contains("var detailCards=directScenarioCards(document.querySelector('#test-view .test-content-detail'))"),
                        "scenario selection must target the rendered detail projection"),
                () -> assertTrue(html.contains("openScenarioCard(card)")),
                () -> assertTrue(html.contains("title','Open this test in Tests")),
                () -> assertTrue(html.contains("if(id==='category-view'){ensureCategoryFilters();ensureCategoryDetail();normalizeCategorySummaryLabels();normalizeCategoryLinks();}")),
                () -> assertTrue(html.contains("'category-view':'nav-category'")),
                () -> assertTrue(html.contains("window.addEventListener('hashchange',activateFromHash)"))
        );
    }
}
