package atf.ui.tests.extent.support.inspection.viewport;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.html.HtmlReportSession;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.support.TestReportPaths;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Controlled fallback fixture and computed-style assertions for the common API viewport. */
public final class ReportApiViewportProbe {
    private static final int LONG_ROWS = 40;

    private ReportApiViewportProbe() { }

    public static void verifyExtentShortTables() {
        Path report = TestReportPaths.report("extent-short-api-viewport.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ViewportComponent", "short API tables", List.of("api"));
            var phase = scenario.startPhase("TEST BODY");
            addRequest(phase, "short", 1);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage();
            page.setViewportSize(1280, 900);
            page.navigate(report.toUri() + "#test-view");
            page.locator("#test-view .test-list .test-item").first().click();
            page.waitForFunction("() => document.documentElement.getAttribute('data-atf-test-view-ready') === 'true'");
            Locator scenario = page.locator(
                    "#test-view .test-content-detail:visible .test-contents > .mt-4 > .accordion > .card").first();
            openNode(scenario, "data-atf-scenario-open");
            Locator phase = scenario.locator(":scope > .accordion > [data-atf-node-role='phase']").first();
            openNode(phase, "data-atf-phase-open");
            Locator request = phase.locator(":scope > .accordion > [data-atf-node-role='request']").first();
            openNode(request, "data-atf-node-open");
            request.locator("details.request-evidence > summary").click();
            request.locator("details.execution-evidence > summary").click();
            Locator requestTable = request.locator(".request-evidence .atf-api-table-wrap");
            Locator retryTable = request.locator(".execution-evidence .atf-api-table-wrap");
            assertNatural(requestTable);
            assertNatural(retryTable);
            page.setViewportSize(760, 900);
            assertNatural(requestTable);
            assertNatural(retryTable);
            assertEquals(Boolean.TRUE, page.evaluate(
                    "document.documentElement.scrollWidth <= document.documentElement.clientWidth"));
        }
    }

    public static void verifyFallbackTables() {
        Path report = fallbackReport();
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage();
            page.setViewportSize(1280, 900);
            page.navigate(report.toUri().toString());
            page.locator("button[data-tab='tests']").click();
            Locator requests = page.locator("#tests .request");
            assertEquals(2, requests.count());
            for (int index = 0; index < requests.count(); index++) {
                Locator request = requests.nth(index);
                request.locator("details.request-evidence > summary").click();
                request.locator("details.execution-evidence > summary").click();
            }
            verifyAtWidth(page, requests);
            page.setViewportSize(760, 900);
            verifyAtWidth(page, requests);
        }
    }

    private static void verifyAtWidth(Page page, Locator requests) {
        Locator shortRequest = requests.nth(0);
        Locator longRequest = requests.nth(1);
        assertNatural(shortRequest.locator(".request-evidence .atf-api-table-wrap"));
        assertNatural(shortRequest.locator(".execution-evidence .atf-api-table-wrap"));
        assertScrollable(longRequest.locator(".request-evidence .atf-api-table-wrap"), LONG_ROWS);
        assertScrollable(longRequest.locator(".execution-evidence .atf-api-table-wrap"), LONG_ROWS);
        assertEquals(Boolean.TRUE, page.evaluate(
                "document.documentElement.scrollWidth <= document.documentElement.clientWidth"),
                "API tables must not create document-level horizontal overflow");
    }

    private static void openNode(Locator node, String attribute) {
        if (!"true".equals(node.getAttribute(attribute))) {
            node.locator(":scope > .card-header").click();
        }
    }

    private static void assertNatural(Locator viewport) {
        assertEquals(Boolean.TRUE, viewport.evaluate("element => {"
                + "const style=getComputedStyle(element);"
                + "return style.overflowY==='auto' && element.scrollHeight<=element.clientHeight"
                + "&&element.getBoundingClientRect().height<500;}"),
                "short table must keep natural height without empty reserved space");
    }

    private static void assertScrollable(Locator viewport, int expectedRows) {
        assertEquals(expectedRows, viewport.locator("tbody tr").count());
        assertTrue(Boolean.TRUE.equals(viewport.evaluate("element => {"
                + "const style=getComputedStyle(element), header=element.querySelector('thead th'),"
                + "last=element.querySelector('tbody tr:last-child');"
                + "if(style.overflowY!=='auto'||element.scrollHeight<=element.clientHeight"
                + "||!header||!last)return false;"
                + "element.scrollTop=element.scrollHeight;"
                + "const bounds=element.getBoundingClientRect();"
                + "return getComputedStyle(header).position==='sticky'"
                + "&&header.getBoundingClientRect().top>=bounds.top-1"
                + "&&last.getBoundingClientRect().bottom<=bounds.bottom+1;}")),
                "long table must scroll locally with sticky header and reachable last row");
    }

    private static Path fallbackReport() {
        Path report = TestReportPaths.report("fallback-api-viewport-parity.html");
        try (var session = new HtmlReportSession(report)) {
            var scenario = session.startScenario("ViewportComponent", "short and long API tables", List.of("api"));
            var phase = scenario.startPhase("TEST BODY");
            addRequest(phase, "short", 1);
            addRequest(phase, "long", LONG_ROWS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }

    private static void addRequest(atf.api.reporting.contract.AtfReportPorts.Phase phase,
                                   String title, int rows) {
        var scope = phase.startRequest(title, "GET", "/resource");
        for (int index = 1; index <= rows; index++) {
            scope.detail("request.header.X-Diagnostic-" + index, "value-" + index);
        }
        Duration duration = Duration.ofMillis(rows);
        var request = new ReportRequest("GET", "/resource", Map.of(), Map.of(), "none", "");
        var response = new ReportResponse(200, "{}", Map.of(), duration,
                "https://api.example.test/resource");
        var attempts = new ArrayList<ReportAttemptRecord>();
        for (int index = 1; index <= rows; index++) {
            attempts.add(new ReportAttemptRecord(index, 200, Duration.ofMillis(1),
                    "SUCCESS", "attempt " + index));
        }
        scope.execution(new ReportExecutionEvidence(response, rows, duration,
                List.of(), attempts, request, ReportTransportProvenance.ACCEPTANCE_MOCK));
        scope.complete(AtfReportStatus.PASS);
    }
}
