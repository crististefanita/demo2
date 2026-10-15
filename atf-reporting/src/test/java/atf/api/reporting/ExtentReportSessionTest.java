package atf.api.reporting;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.http.ApiAttemptOutcome;
import atf.api.core.http.ApiAttemptRecord;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.http.ApiResponse;
import atf.api.core.http.RetryEvent;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ExtentReportSessionTest {
    @Test
    void flushKeepsTheFinalPathPrivateUntilClosePublishesTheCompleteArtifact() throws Exception {
        Path root = Files.createTempDirectory("atf-incomplete-flush-");
        Path report = root.resolve("report.html");
        try {
            try (var session = new ExtentReportSession(report)) {
                var scenario = session.startScenario("FlushTests", "incomplete flush", List.of("reporting"));
                scenario.complete(AtfReportStatus.PASS);
                session.flush();

                assertFalse(Files.exists(report), "flush must not expose the private staged report");
                try (var children = Files.list(root)) {
                    assertTrue(children.anyMatch(path ->
                            path.getFileName().toString().startsWith(".atf-extent-session-")));
                }
            }
            assertTrue(Files.exists(report), "close must publish the finalized report");
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void configuredSessionPublishesOneTimestampedExtentArtifact() throws Exception {
        Path root = Files.createTempDirectory("atf-configured-timestamped-report-");
        try {
            Path config = root.resolve("atf.yaml");
            Files.writeString(config, """
                    reporting:
                      extent:
                        outputDirectory: reports
                        fileName: api-atf-extent-report.html
                        timestampEnabled: true
                        timestampPattern: yyyy-MM-dd_HH-mm-ss-SSS
                        timestampZone: UTC
                    """);

            Path reportPath;
            try (var session = ExtentReportSession.openConfigured(config)) {
                reportPath = session.reportPath();
                var scenario = session.startScenario("ConfiguredReportTests", "timestamped report", List.of("reporting"));
                scenario.complete(AtfReportStatus.PASS);

                assertTrue(reportPath.getFileName().toString()
                        .matches("api-atf-extent-report_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}\\.html"));
                assertFalse(Files.exists(reportPath), "the final artifact is published only on close");
            }

            assertTrue(Files.exists(reportPath));
            assertTrue(Files.readString(reportPath).contains("timestamped report"));
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void parallelSessionsPublishIndependentAutonomousReports() throws Exception {
        Path root = Files.createTempDirectory("atf-parallel-reports-");
        Path first = root.resolve("parallel-one.html");
        Path second = root.resolve("parallel-two.html");
        try {
            try (var executor = Executors.newFixedThreadPool(2)) {
                var firstRun = executor.submit(() -> publishParallelScenario(first, "parallel one"));
                var secondRun = executor.submit(() -> publishParallelScenario(second, "parallel two"));
                firstRun.get(30, TimeUnit.SECONDS);
                secondRun.get(30, TimeUnit.SECONDS);
            }

            String firstHtml = Files.readString(first);
            String secondHtml = Files.readString(second);
            boolean sessionDirectoriesRemain;
            try (var children = Files.list(root)) {
                sessionDirectoriesRemain = children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-"));
            }
            assertTrue(Files.exists(first));
            assertTrue(Files.exists(second));
            assertAll(
                    () -> assertTrue(firstHtml.contains("parallel one")),
                    () -> assertTrue(secondHtml.contains("parallel two")),
                    () -> assertFalse(firstHtml.contains("spark/")),
                    () -> assertFalse(secondHtml.contains("spark/")),
                    () -> assertFalse(sessionDirectoriesRemain));
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void sessionRejectsCrossThreadMutationInsteadOfRacingExtentState() throws Exception {
        Path report = TestReportPaths.report("extent-thread-confined.html");
        try (var session = new ExtentReportSession(report);
             var executor = Executors.newSingleThreadExecutor()) {
            var failure = executor.submit(() -> assertThrows(IllegalStateException.class,
                    () -> session.startScenario("ParallelTests", "cross-thread mutation", List.of("parallel"))))
                    .get(10, TimeUnit.SECONDS);
            assertNotNull(failure);
        }
    }

    @Test
    void competingSessionsKeepTheSharedFinalPathAtomic() throws Exception {
        Path root = Files.createTempDirectory("atf-shared-report-");
        Path report = root.resolve("shared.html");
        try {
            try (var executor = Executors.newFixedThreadPool(2)) {
                var firstRun = executor.submit(() -> publishParallelEvidenceScenario(report, "shared one"));
                var secondRun = executor.submit(() -> publishParallelEvidenceScenario(report, "shared two"));
                firstRun.get(30, TimeUnit.SECONDS);
                secondRun.get(30, TimeUnit.SECONDS);
            }

            var inspection = ReportArtifactInspector.inspect(report);
            assertTrue(inspection.isCompliant(), () -> "Shared report violations: " + inspection.violations());
            String html = Files.readString(report);
            assertTrue(html.contains("shared one") || html.contains("shared two"));
            try (var children = Files.list(root)) {
                assertFalse(children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-")));
            }
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void flushKeepsIntermediateStatePrivateUntilClosePublishesTheAutonomousArtifact() throws Exception {
        Path root = Files.createTempDirectory("atf-flush-boundary-");
        Path report = root.resolve("flush-boundary.html");
        try {
            var session = new ExtentReportSession(report);
            try {
                var scenario = session.startScenario("FlushTests", "flush does not publish partial output",
                        List.of("reporting", "lifecycle"));
                var phase = scenario.startPhase("TEST BODY");
                phase.startRequest("health", "GET", "/health").complete(AtfReportStatus.PASS);
                phase.complete(AtfReportStatus.PASS);
                scenario.complete(AtfReportStatus.PASS);

                session.flush();

                assertFalse(Files.exists(report), "flush must not publish a partial final artifact");
                try (var children = Files.list(root)) {
                    assertTrue(children.anyMatch(path ->
                            path.getFileName().toString().startsWith(".atf-extent-session-")),
                            "flush should keep the private staging workspace alive until close");
                }
            } finally {
                session.close();
            }

            assertTrue(Files.isRegularFile(report));
            assertTrue(Files.readString(report).contains("flush does not publish partial output"));
            assertFalse(Files.exists(root.resolve("spark")));
            try (var children = Files.list(root)) {
                assertFalse(children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-")));
            }
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void failedPublicationStillCleansPrivateWorkspaceAndPreservesBlockedDestination() throws Exception {
        Path root = Files.createTempDirectory("atf-blocked-publication-");
        Path blockedDestination = root.resolve("blocked-report");
        Files.createDirectories(blockedDestination);
        Path sentinel = blockedDestination.resolve("sentinel.txt");
        Files.writeString(sentinel, "destination-owned");
        try {
            var session = new ExtentReportSession(blockedDestination);
            var scenario = session.startScenario("PublicationTests", "blocked destination", List.of("reporting"));
            scenario.complete(AtfReportStatus.PASS);

            assertThrows(IllegalStateException.class, session::close,
                    "a non-empty destination directory must not be replaced by a report file");

            assertTrue(Files.isDirectory(blockedDestination));
            assertEquals("destination-owned", Files.readString(sentinel));
            try (var children = Files.list(root)) {
                assertFalse(children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-")),
                        "a failed publication must not leak the private workspace");
            }
        } finally {
            deleteTree(root);
        }
    }

    private static void publishParallelScenario(Path report, String name) {
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ParallelTests", name, List.of("parallel"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("health", "GET", "/health");
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
    }

    private static void publishParallelEvidenceScenario(Path report, String name) {
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ParallelTests", name, List.of("parallel"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("health", "GET", "/health");
            request.detail("response.duration.ms", "1");
            request.json("RESPONSE JSON", "{\"status\":\"ok\"}");
            var validation = request.startValidation("Validate health response");
            validation.assertions(List.of(new AtfAssertion("HTTP status", "200", "200", true)));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (java.io.IOException error) {
                    throw new RuntimeException(error);
                }
            });
        }
    }

    @Test
    void requestCannotCloseBeforeItsValidation() throws Exception {
        Path report = TestReportPaths.report("extent-api-request-validation-order.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UsersApiTests", "validation order", List.of("api"));
            var phase = scenario.startPhase("TEST BODY");
            var request = phase.startRequest("contract", "GET", "/users/1");
            var validation = request.startValidation("Validate response");

            assertThrows(IllegalStateException.class, () -> request.complete(AtfReportStatus.PASS));

            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        assertTrue(Files.exists(report));
    }

    @Test
    void routeAlreadyUsedAsRequestTitleIsNotDuplicated() throws Exception {
        Path report = TestReportPaths.report("extent-api-request-title-normalization.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UsersApiTests", "route title", List.of("api"));
            var phase = scenario.startPhase("TEST BODY");
            phase.startRequest("GET /public/v2/users", "GET", "/public/v2/users")
                    .complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertTrue(html.contains("GET /public/v2/users"));
        assertFalse(html.contains("GET /public/v2/users — GET /public/v2/users"));
    }

    @Test
    void extentBackendPublishesLifecycleAndAggregateAssertions() throws Exception {
        Path report = TestReportPaths.report("extent-api-report.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UsersApiTests", "GET user — item contract", List.of("api", "contract", "smoke"));
            var setup = scenario.startPhase("SETUP");
            var setupRequest = setup.startRequest("health check", "GET", "/public/v2/users/1");
            setupRequest.detail("auth.mode", "none");
            setupRequest.complete(AtfReportStatus.PASS);
            setup.complete(AtfReportStatus.PASS);

            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("item contract", "GET", "/public/v2/users/1");
            request.detail("response.status", "200");
            request.detail("response.duration.ms", "12");
            request.json("RESPONSE JSON", "{\"id\":1}");
            var validation = request.startValidation("Validate user contract");
            validation.assertions(List.of(
                    new AtfAssertion("HTTP status", "200", "200", true),
                    new AtfAssertion("JSON schema", "matched", "matched", true)));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);

            var teardown = scenario.startPhase("TEARDOWN");
            teardown.startRequest("cleanup", "DELETE", "/public/v2/users/1").complete(AtfReportStatus.PASS);
            teardown.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        assertTrue(Files.exists(report));
        String html = Files.readString(report);
        int requestIndex = html.indexOf("GET /public/v2/users/1");
        int validationIndex = html.indexOf("Validate user contract");
        assertTrue(requestIndex >= 0 && validationIndex > requestIndex);
        assertFalse(html.contains("GET /public/v2/users/1 — item contract"));
        assertEquals(divDepthAt(html, requestIndex), divDepthAt(html, validationIndex),
                "request and validation must be sibling nodes under the phase");
        assertAll(
                () -> assertTrue(html.contains("UsersApiTests")),
                () -> assertTrue(html.contains("SETUP")),
                () -> assertTrue(html.contains("TEST BODY")),
                () -> assertTrue(html.contains("TEARDOWN")),
                () -> assertTrue(html.contains("GET /public/v2/users/1")),
                () -> assertTrue(html.contains("Validate user contract")),
                () -> assertTrue(html.contains("HTTP status")),
                () -> assertTrue(html.contains("JSON schema")),
                () -> assertTrue(html.contains("RESPONSE JSON")),
                () -> assertTrue(html.contains("TECHNICAL TRACE")),
                () -> assertTrue(html.toUpperCase(java.util.Locale.ROOT).contains("RESPONSE DURATION (MS)")),
                 () -> assertTrue(html.contains("Request details · 1 fields")),
                 () -> assertTrue(html.contains("Response details · 1 fields")),
                 () -> assertTrue(html.contains("helm-request-table")),
                () -> assertTrue(html.contains("helm-evidence-table helm-validation-table")),
                () -> assertTrue(html.contains("function normalizeEvidenceDisclosures()")),
                () -> assertTrue(html.contains("function normalizePercentages()")),
                () -> assertTrue(html.contains("function removeUnsupportedOverviewCards()")),
                () -> assertTrue(html.contains("#events-analysis")),
                () -> assertTrue(html.contains("(column||card||canvas).remove()")),
                () -> assertTrue(html.contains("setTimeout(removeUnsupportedOverviewCards,150)")),
                () -> assertTrue(html.contains("function collapseScenarioCards()")),
                () -> assertTrue(html.contains("data-atf-collapse-bound")),
                () -> assertTrue(html.contains("data-atf-scenario-collapse-bound")),
                () -> assertTrue(html.contains("function watchScenarioCards()")),
                () -> assertTrue(html.contains("response-evidence")),
                () -> assertTrue(html.contains("json-evidence")),
                () -> assertTrue(html.contains("assertion-evidence")),
                () -> assertTrue(html.contains("helm-scroll-block")),
                 () -> assertTrue(html.contains(".step-detail .se-toggle")),
                () -> assertTrue(html.contains("id=\"nav-fails\"")),
                () -> assertTrue(html.contains("id=\"nav-overview\"")),
                () -> assertTrue(html.contains("id=\"test-view\"")),
                () -> assertTrue(html.contains("id=\"category-view\"")),
                () -> assertTrue(html.contains("id=\"dashboard-view\"")),
                 () -> assertTrue(html.contains("class=\"test-list atf-fails-list\"")),
                 () -> assertTrue(html.contains("id=\"atf-failure-list\" class=\"test-list-item\"")),
                 () -> assertTrue(html.contains("id=\"atf-failure-detail\" class=\"detail-body\"")),
                 () -> assertTrue(html.contains("function selectFailure(index)")),
                () -> assertTrue(html.contains("function statusDisplay(value)")),
                () -> assertTrue(html.contains("function failureContext(item)")),
                () -> assertTrue(html.contains("var indexMarkup=row.getAttribute('data-atf-index-html')||''")),
                () -> assertTrue(html.contains("detail.insertAdjacentHTML('beforeend',indexMarkup)")),
                 () -> assertTrue(html.contains("function failureIndexMarkup(name,ctx,id,scenarioIndex,ordinal)")),
                 () -> assertTrue(html.contains("data-atf-failure-phase")),
                () -> assertTrue(html.contains("q('.card',item)")),
                () -> assertTrue(html.contains("table.tBodies&&table.tBodies[0]")),
                () -> assertTrue(html.contains("Array.prototype.slice.call(row.cells||[])")),
                () -> assertTrue(html.contains("Validate: status=")),
                () -> assertTrue(html.contains("ctx.validation")),
                () -> assertTrue(html.contains("FAILURE CAUSE")),
                () -> assertTrue(html.contains("class=\"test-wrapper row view test-view hide\"")),
                () -> assertTrue(html.contains("atf-overview-default")),
                () -> assertTrue(html.contains("data-atf-tab=\"overview\"")),
                () -> assertTrue(html.contains("data-atf-tab=\"fails\"")),
                () -> assertTrue(html.contains("atfOpenTest")),
                () -> assertTrue(html.contains("atf-failure-evidence")),
                () -> assertTrue(html.contains("data-atf-failure-phase")),
                () -> assertTrue(html.contains("normalizeCategoryLinks")),
                () -> assertTrue(html.contains("v.classList.toggle('d-none',!active)")),
                () -> assertTrue(html.contains("traceTables")),
                () -> assertTrue(html.contains("v==null?'':v")),
                () -> assertFalse(html.contains("url('fontawesome-webfont.woff2")),
                () -> assertFalse(html.contains("url('fontawesome-webfont.woff")),
                () -> assertFalse(html.contains("spark/")),
                () -> assertFalse(html.contains("class=\"collapse show\"")),
                () -> assertFalse(Files.exists(report.getParent().resolve("spark"))));
    }

    @Test
    void publishesTheSharedVisualContract() throws Exception {
        Path report = TestReportPaths.report("extent-api-theme-contract.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ThemeTests", "theme contract", List.of("reporting"));
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("--atf-surface:#101723")),
                () -> assertTrue(html.contains("line-height:1.55")),
                () -> assertTrue(html.contains("font-size:12.6px")),
                () -> assertTrue(html.contains("padding:7px 9px")),
                () -> assertTrue(html.contains("font-weight:400")),
                () -> assertTrue(html.contains("font-family:ui-monospace,Consolas,monospace")),
                () -> assertTrue(html.contains("pre.code-block")),
                () -> assertTrue(html.contains("padding:10px 12px!important")),
                () -> assertTrue(html.contains("--atf-json-text:#e6edf3")
                        && html.contains("color:var(--atf-json-text)!important")),
                () -> assertTrue(html.contains(".dark .test-contents pre.code-block")),
                () -> assertTrue(html.contains(".dark .card-header")),
                () -> assertTrue(html.contains("System/Environment")),
                () -> assertTrue(html.contains("API base URL")),
                () -> assertTrue(html.contains("ATF ApiTransport (configured per execution)")),
                () -> assertTrue(html.contains("Extent · embedded offline HTML")),
                () -> assertTrue(html.contains(".card:has(> .card-body #grandchild-analysis)")),
                () -> assertTrue(html.contains("data-atf-node-role=\"phase\"] > .card-header .node-attr")),
                () -> assertTrue(html.contains("function normalizeDurations()")),
                () -> assertTrue(html.contains("function normalizeTestNavigatorSeparators()")
                        && html.contains("node.nodeType===3")
                        && html.contains("node.remove()"),
                        "Tests navigation must show duration without the visual slash separator"),
                () -> assertTrue(html.contains("e.textContent=String(ms)+' ms'"),
                        "every normalized duration must carry the ms unit"),
                () -> assertTrue(html.contains("function aggregateClassDurations()")),
                () -> assertTrue(html.contains("root.setAttribute('data-duration-source','aggregated')")),
                () -> assertTrue(html.contains(".test-view .test-content-detail .card-toolbar .badge,.category-view .test-content-detail .card-toolbar .badge")
                                 && html.contains("/^(pass|fail|warning|skip)$/i.test(raw)")
                                 && html.contains("e.textContent=raw+' ms'"),
                         "scenario and lifecycle toolbar durations must carry the ms unit without changing status badges"),
                () -> assertTrue(html.contains(".test-detail .text-sm span")),
                () -> assertTrue(html.contains("function collapseDirectScenarioCards()")),
                () -> assertTrue(html.contains("function directScenarioCards(root){return q('.test-contents > .mt-4 > .accordion > .card',root);}")),
                () -> assertTrue(html.contains("function testTags(item){var stored=item.getAttribute('data-atf-scenario-tags')")),
                () -> assertTrue(html.contains("function readTagValues(container)")),
                () -> assertTrue(html.contains("data-atf-scenario-tags")),
                () -> assertTrue(html.indexOf("var captured=readTagValues(card)") < html.indexOf("source.remove()")),
                () -> assertTrue(html.contains("rows('atf-audit-tags',tags,false,false)")),
                () -> assertTrue(html.contains("data-atf-scenario-open")),
                () -> assertTrue(html.contains("data-atf-phase-open")),
                () -> assertTrue(html.contains(".test-content-detail .test-contents > .mt-4 > .accordion > .card > .accordion > .card > .accordion")),
                () -> assertFalse(html.contains("--atf-surface:#141d2b")),
                // The Fails navigation intentionally uses the compact 1.45
                // line-height from the .NET reference; only the old global
                // theme line-height is forbidden here.
                () -> assertTrue(html.contains(".exception-view.atf-fails-view .test-item .test-detail .name")));
    }

    @Test
    void overviewAuditMaterializesScenarioTagsBeforeProjectingTheOtherTagsTable() throws Exception {
        Path report = TestReportPaths.report("extent-api-tag-audit.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UsersApiTests", "GET users — collection contract",
                    List.of("api", "users", "contract"));
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int populate = html.indexOf("function populateAudit()");
        int items = html.indexOf("items=testItems()", populate);
        int normalize = html.indexOf("normalizeScenarioAnnotations();", items);
        int total = html.indexOf("var total=items.length", normalize);

        assertTrue(populate >= 0, "the overview must contain the tag-audit projection");
        assertTrue(items >= 0 && normalize > items && normalize < total,
                "scenario annotations must be materialized after scenario discovery and before the audit reads scenario tags");
        assertTrue(html.contains("data-atf-scenario-tags"),
                "the audit must have a stable scenario-tag projection after native annotations are normalized");
        assertTrue(html.contains("rows('atf-audit-tags',tags,false,false)"),
                "the overview must render the projected tag rows");
    }

    @Test
    void failurePublicationKeepsEveryFailureAndADeepLinkContract() throws Exception {
        Path report = TestReportPaths.report("extent-api-failure-report.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("FailingApiTests", "GET user — invalid contract", List.of("api", "failure"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("invalid contract", "GET", "/public/v2/users/999");
            request.detail("response.status", "500");
            request.detail("response.duration.ms", "12");
            var validation = request.startValidation("Validate status=404");
            validation.assertions(List.of(new AtfAssertion("HTTP status", "404", "500", false)));
            validation.complete(AtfReportStatus.FAIL);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("FailingApiTests")),
                () -> assertTrue(html.contains("status=\"fail\"")),
                () -> assertTrue(html.contains("function renderFails()")),
                () -> assertTrue(html.contains("function failedAssertionRows(item)")),
                () -> assertTrue(html.contains("function failureContextFor(item,anchor)")),
                () -> assertTrue(html.contains("duration:duration||'No response duration recorded'")),
                () -> assertTrue(html.contains("duration=/ms$/i.test(rawDuration)?rawDuration:rawDuration+' ms'")),
                () -> assertTrue(html.contains("data-atf-failure-ordinal")),
                () -> assertTrue(html.contains("data-atf-failure-id")),
                 () -> assertTrue(html.contains("failureIndexMarkup(name,ctx,id,scenarioIndex,record.ordinal)"),
                         "Fails must expose selected scenario evidence"),
                 () -> assertTrue(html.contains("detail.insertAdjacentHTML('beforeend',indexMarkup)"),
                         "Fails must expose concrete index evidence"),
                 () -> assertFalse(html.contains("sourceScenario?sourceScenario.cloneNode(true)"),
                         "Fails must not duplicate the full test tree below Open in Tests"),
                () -> assertTrue(html.contains("function activateFromHash()")),
                () -> assertTrue(html.contains("window.addEventListener('hashchange',activateFromHash)")),
                () -> assertTrue(html.contains("function normalizeNavigationLinks()")),
                () -> assertTrue(html.contains("activateFromHash();},0)")),
                () -> assertTrue(html.contains("var hash='test-id='+id")),
                () -> assertTrue(html.contains("scenario-index=")),
                () -> assertTrue(html.contains("data-atf-scenario-index")),
                () -> assertTrue(html.indexOf("id=\"atf-report-behaviour\"") < html.indexOf("</head>")),
                () -> assertTrue(html.contains("id=\"atf-report-behaviour\" type=\"text/javascript\"")),
                () -> assertTrue(html.contains("Validate status=404")),
                () -> assertTrue(html.contains("EXPECTED")),
                () -> assertTrue(html.contains("ACTUAL")),
                () -> assertFalse(html.contains("spark/")),
                () -> assertFalse(Files.exists(report.getParent().resolve("spark"))));
    }

    @Test
    void evidenceBoundaryRedactsDetailsJsonAndAssertionValues() throws Exception {
        Path report = TestReportPaths.report("extent-api-redaction-report.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("SecurityTests", "redaction boundary", List.of("security"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("secret response", "GET", "/secure");
            request.detail("Authorization", "Bearer raw-token-123");
            request.json("RESPONSE JSON", "{\"token\":\"raw-json-token\",\"name\":\"safe\"}");
            var validation = request.startValidation("Validate secure response");
            validation.assertions(List.of(new AtfAssertion(
                    "token", "raw-expected-token", "raw-actual-token", true)));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertFalse(html.contains("raw-token-123")),
                () -> assertFalse(html.contains("raw-json-token")),
                () -> assertFalse(html.contains("raw-expected-token")),
                () -> assertFalse(html.contains("raw-actual-token")),
                () -> assertTrue(html.contains("***REDACTED***")));
    }

    @Test
    void retryEvidenceIsRenderedByTheEvidenceBoundaryAndRedactedBeforePublication() throws Exception {
        Path report = TestReportPaths.report("extent-api-retry-evidence.html");
        var response = new ApiResponse(200, "{\"status\":\"ok\"}", Map.of(),
                Duration.ofMillis(8), "https://api.example.test/secure?token=raw-final-token");
        var execution = new ApiExecutionResult(response, 2, Duration.ofMillis(13),
                List.of(new RetryEvent(2, 503, "Retry-After", Duration.ZERO, "retry token=raw-event-token")),
                List.of(
                        new ApiAttemptRecord(1, 503, Duration.ofMillis(5), ApiAttemptOutcome.RETRYABLE_RESPONSE,
                                "retry token=raw-attempt-token"),
                        new ApiAttemptRecord(2, 200, Duration.ofMillis(8), ApiAttemptOutcome.SUCCESS, "")));

        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("RetryTests", "retry evidence", List.of("reporting"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("secure call", "GET", "/secure");
            request.execution(execution);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("retry-history-table")),
                () -> assertTrue(html.contains("RETRYABLE_RESPONSE")),
                () -> assertFalse(html.contains("raw-final-token")),
                () -> assertTrue(html.contains("token=***REDACTED***"),
                        "the URL may retain the query key while the secret value must be redacted"),
                () -> assertFalse(html.contains("raw-event-token")),
                () -> assertFalse(html.contains("raw-attempt-token")),
                () -> assertTrue(html.contains("retry.events")));
    }

    @Test
    void failureCauseIsRedactedAtTheExtentPhaseBoundaryBeforePublication() throws Exception {
        Path report = TestReportPaths.report("extent-api-failure-cause-redaction.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("FailureTests", "redacted failure", List.of("security"));
            var body = scenario.startPhase("TEST BODY");
            body.detail("Failure cause", "ApiValidationException: token=raw-failure-token; mode=full");
            body.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("FAILURE CAUSE")),
                () -> assertTrue(html.contains("token=***REDACTED***")),
                () -> assertTrue(html.contains("mode=full")),
                () -> assertFalse(html.contains("raw-failure-token")));
    }

    @Test
    void authorizationInFailureCauseIsRedactedBeforePublication() throws Exception {
        Path report = TestReportPaths.report("extent-api-authorization-failure-cause-redaction.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("FailureTests", "redacted authorization failure", List.of("security"));
            var body = scenario.startPhase("TEARDOWN");
            body.detail("Failure cause",
                    "teardown cleanup failed: Authorization: Bearer teardown-secret-token-1234567890");
            body.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("teardown cleanup failed")),
                () -> assertTrue(html.contains("Authorization: Bearer ***REDACTED***")),
                () -> assertFalse(html.contains("teardown-secret-token-1234567890")));
    }

    private static int divDepthAt(String html, int markerIndex) {
        String prefix = html.substring(0, markerIndex);
        int depth = 0;
        for (int index = 0; index < prefix.length(); index++) {
            if (prefix.startsWith("<div", index)) depth++;
            if (prefix.startsWith("</div>", index)) depth--;
        }
        return depth;
    }
}
