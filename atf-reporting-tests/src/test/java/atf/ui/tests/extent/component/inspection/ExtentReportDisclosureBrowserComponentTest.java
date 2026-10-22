package atf.ui.tests.extent.component.inspection;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.support.ReportTestScreenshots;
import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.model.AtfUiActionRecord;
import atf.api.testkit.AtfComponentTest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static atf.api.reporting.contract.AtfGherkinKeyword.AND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Browser-level component gate for a request that owns evidence and a validation child. */
@AtfComponentTest
class ExtentReportDisclosureBrowserComponentTest {
    private static final String CATALOG_CHECKPOINT = "catalog home renders product cards, pagination and footer";
    private static final String UI_TARGET = "https://ui.example.test/catalog";

    @Test
    void uiCheckpointHasNoSyntheticHttpTableWhileNestedApiEvidenceRemainsAuditable() {
        Path report = checkpointReport();
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage();
            List<String> errors = new ArrayList<>();
            page.onPageError(errors::add);
            page.navigate(report.toUri() + "#test-view");
            page.locator("#test-view .test-list .test-item").first().click();
            page.waitForFunction("() => document.documentElement.getAttribute('data-atf-test-view-ready') === 'true'");
            Locator scenarios = page.locator(
                    "#test-view .test-content-detail:visible .test-contents > .mt-4 > .accordion > .card");
            assertEquals(3, scenarios.count());
            for (int index = 0; index < scenarios.count(); index++) {
                Locator scenario = scenarios.nth(index);
                openIfClosed(scenario, "data-atf-scenario-open");
                Locator phase = scenario.locator(":scope > .accordion > [data-atf-node-role='phase']").first();
                openIfClosed(phase, "data-atf-phase-open");
                Locator checkpoint = phase.locator(":scope > .accordion > [data-atf-node-role='request']").first();
                openIfClosed(checkpoint, "data-atf-node-open");
                assertAll(
                        () -> assertEquals(0, checkpoint.locator(":scope > .collapse table.table").count(),
                                "UI context must not create a Status/Details log table"),
                        () -> assertEquals(0, checkpoint.locator(":scope > .collapse details.request-evidence").count()),
                        () -> assertFalse(checkpoint.innerText().contains("REQUEST ACTION")),
                        () -> assertFalse(checkpoint.innerText().contains("REQUEST TARGET")));
                Locator validation = checkpoint.locator(":scope > .accordion > [data-atf-node-role='validation']");
                openIfClosed(validation.first(), "data-atf-node-open");
                Locator assertions = validation.first().locator("details.assertion-evidence > summary");
                if (assertions.count() > 0) assertions.click();
                assertTrue(validation.first().locator("table.helm-validation-table").isVisible());
                assertEquals(1, validation.first().locator("a.atf-ui-action-screenshot-link").count(),
                        () -> "Selected validation DOM: " + validation.first().innerHTML());
                if (index == 0) {
                    openIfClosed(validation.nth(1), "data-atf-node-open");
                    Locator exchange = validation.nth(1).locator(
                            ":scope > .accordion > [data-atf-node-role='request']").first();
                    openIfClosed(exchange, "data-atf-node-open");
                    assertEquals(1, exchange.locator("details.request-evidence").count());
                    assertEquals(1, exchange.locator("details.response-evidence").count());
                    assertEquals(1, exchange.locator("details.execution-evidence").count());
                    assertTrue(exchange.textContent().contains("REQUEST JSON"));
                    assertTrue(exchange.textContent().contains("RESPONSE JSON"));
                }
                checkpoint.locator(":scope > .card-header").click();
                assertFalse(validation.first().locator(":scope > .card-header").isVisible());
            }
            assertAll(
                    () -> assertTrue(errors.isEmpty(), () -> "Report runtime errors: " + errors),
                    () -> assertEquals(0, page.locator("hr").count()),
                    () -> assertEquals(Boolean.TRUE, page.evaluate(
                            "document.documentElement.scrollWidth <= document.documentElement.clientWidth")));
        }
    }

    @Test
    void closingRequestHidesItsEvidenceAndValidationForMouseAndKeyboard() {
        Path report = TestReportPaths.report("extent-request-subtree-disclosure.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("DisclosureComponent", "request with evidence and child", List.of("api"));
            var phase = scenario.startPhase("TEST BODY");
            phase.detail("API audit completeness", "1 of 1 exchanges matched");
            var request = phase.startRequest("GET /resource — inspect", "GET", "/resource");
            request.detail("request.header.Accept", "application/json");
            for (int index = 1; index <= 40; index++) {
                request.detail("request.header.X-Diagnostic-" + index, "value-" + index);
            }
            Duration retryDuration = Duration.ofMillis(40);
            var retryRequest = new ReportRequest("GET", "/resource", Map.of(), Map.of(), "none", "");
            var retryResponse = new ReportResponse(200, "{}", Map.of("Content-Type", "application/json"),
                    retryDuration, "https://api.example.test/resource");
            var attempts = new ArrayList<ReportAttemptRecord>();
            for (int number = 1; number <= 40; number++) {
                attempts.add(new ReportAttemptRecord(number, number == 40 ? 200 : 429,
                        Duration.ofMillis(1), number == 40 ? "SUCCESS" : "RETRYABLE_RESPONSE",
                        "attempt " + number));
            }
            request.execution(new ReportExecutionEvidence(retryResponse, attempts.size(), retryDuration,
                    List.of(), attempts, retryRequest, ReportTransportProvenance.ACCEPTANCE_MOCK));
            var validation = request.startValidation("Custom response check");
            validation.assertions(List.of(new AtfAssertion("HTTP status", "200", "200", true)));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage();
            page.navigate(report.toUri() + "#test-view");
            page.locator("#test-view .test-list .test-item").first().click();
            page.waitForFunction("() => document.documentElement.getAttribute('data-atf-test-view-ready') === 'true'");
            Locator scenario = page.locator("#test-view .test-content-detail:visible .test-contents > .mt-4 > .accordion > .card").first();
            openIfClosed(scenario, "data-atf-scenario-open");
            Locator phase = scenario.locator(":scope > .accordion > [data-atf-node-role='phase']").first();
            openIfClosed(phase, "data-atf-phase-open");
            Locator audit = phase.locator("details.phase-details").first();
            assertTrue(audit.count() > 0, "phase audit metadata must be a secondary disclosure");
            assertEquals("false", audit.locator(":scope > summary").getAttribute("aria-expanded"));
            Locator request = phase.locator(":scope > .accordion > [data-atf-node-role='request']").first();
            assertTrue(request.count() > 0, "request node must be present in the generated report");
            Locator validation = request.locator(":scope > .accordion > .card").first();
            assertEquals("validation", validation.getAttribute("data-atf-node-role"),
                    "a custom validation title must not change its semantic role");

            Locator header = request.locator(":scope > .card-header");
            header.click();
            assertState(request, true);
            request.locator("details.request-evidence > summary").click();
            Locator viewport = request.locator(".request-evidence .atf-api-table-wrap");
            assertTrue(Boolean.TRUE.equals(viewport.evaluate("element => "
                    + "element.scrollHeight > element.clientHeight && "
                    + "getComputedStyle(element).overflowY === 'auto'")),
                    "long request metadata must scroll within the shared viewport");
            assertTrue(lastRowReachableWithStickyHeader(viewport),
                    "the final request metadata row must be reachable below the sticky header");
            request.locator("details.execution-evidence > summary").click();
            Locator retryViewport = request.locator(".execution-evidence .atf-api-table-wrap");
            assertTrue(Boolean.TRUE.equals(retryViewport.evaluate("element => "
                    + "element.scrollHeight > element.clientHeight && "
                    + "getComputedStyle(element).overflowY === 'auto'")),
                    "long retry history must scroll within the shared viewport");
            assertEquals("40", retryViewport.locator("tbody tr").last().locator("td").first().innerText(),
                    "the final retry row must remain in the rendered table");
            assertTrue(lastRowReachableWithStickyHeader(retryViewport),
                    "the final retry row must be reachable below the sticky header");
            page.setViewportSize(760, 900);
            assertTrue(lastRowReachableWithStickyHeader(viewport),
                    "the final request metadata row must remain reachable at a narrow viewport");
            assertTrue(lastRowReachableWithStickyHeader(retryViewport),
                    "the final retry row must remain reachable at a narrow viewport");
            assertEquals(Boolean.TRUE, page.evaluate(
                    "document.documentElement.scrollWidth <= document.documentElement.clientWidth"),
                    "narrow report layout must not introduce document-level horizontal overflow");
            page.evaluate("() => window.scrollTo(0, 0)");
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(report.resolveSibling("extent-request-subtree-disclosure-narrow.png"))
                    .setFullPage(true));
            assertTrue(validation.locator(".helm-validation-table").count() > 0,
                    "custom-titled API validation must retain the direct assertion table");
            assertEquals(0, validation.locator("details.assertion-evidence").count());
            header.click();
            assertState(request, false);
            header.focus();
            header.press("Enter");
            assertState(request, true);
            header.press("Space");
            assertState(request, false);
        }
    }

    private static Path checkpointReport() {
        Path directory = TestReportPaths.report("ui-checkpoint-evidence.html").getParent();
        Path report;
        try (var session = UiExtentReportSession.openConfigured(directory,
                "ui-checkpoint-evidence.html", AtfConsoleSink.noOp())) {
            report = session.reportPath();
            for (var status : List.of(AtfReportStatus.PASS, AtfReportStatus.WARNING, AtfReportStatus.FAIL)) {
                var scenario = session.startScenario("UiCheckpointComponent", "atf.ui.component",
                        "Checkpoint evidence — " + status.displayLabel(), List.of("ui", "component"));
                var phase = scenario.startPhase("TEST BODY");
                var assertion = new AtfAssertion("Catalog grid", "visible", status.displayLabel(),
                        status != AtfReportStatus.FAIL, status);
                var action = new AtfUiActionRecord(1, "Get text", "[data-test=product-name]",
                        "product name", "Pliers", 2, status);
                phase.step(AND, CATALOG_CHECKPOINT, UI_TARGET, ReportTestScreenshots.ONE_PIXEL_PNG,
                        List.of(assertion), List.of(action),
                        status == AtfReportStatus.PASS ? List.of(checkpointExchange()) : List.of(), status);
                phase.complete(status);
                scenario.complete(status);
            }
        }
        return report;
    }

    private static ReportExecutionEvidence checkpointExchange() {
        Duration duration = Duration.ofMillis(8);
        var headers = Map.of("Content-Type", "application/json");
        var request = new ReportRequest("POST", "/cart", Map.of(), headers, "none", "{\"productId\":1}");
        var response = new ReportResponse(201, "{\"cartId\":1}", headers,
                duration, "https://api.example.test/cart");
        return new ReportExecutionEvidence(response, 1, duration, List.of(),
                List.of(new ReportAttemptRecord(1, 201, duration, "SUCCESS", "component fixture")),
                request, ReportTransportProvenance.ACCEPTANCE_MOCK);
    }

    private static void openIfClosed(Locator card, String attribute) {
        if (!"true".equals(card.getAttribute(attribute))) {
            card.locator(":scope > .card-header").click();
        }
    }

    private static void assertState(Locator request, boolean open) {
        String state = Boolean.toString(open);
        assertEquals(state, request.getAttribute("data-atf-node-open"));
        assertEquals(state, request.locator(":scope > .card-header").getAttribute("aria-expanded"));
        Object visibility = request.evaluate("card => Array.from(card.children)"
                + ".filter(child => !child.classList.contains('card-header'))"
                + ".map(child => getComputedStyle(child).display)");
        assertEquals(List.of(open ? "block" : "none", open ? "block" : "none"), visibility,
                "the evidence panel and child accordion must share the parent state");
    }

    private static boolean lastRowReachableWithStickyHeader(Locator viewport) {
        return Boolean.TRUE.equals(viewport.evaluate("element => {"
                + "element.scrollTop=element.scrollHeight;"
                + "const bounds=element.getBoundingClientRect(),"
                + "last=element.querySelector('tbody tr:last-child'),"
                + "heading=element.querySelector('thead th');"
                + "return !!last&&!!heading&&last.getBoundingClientRect().bottom<=bounds.bottom+1"
                + "&&heading.getBoundingClientRect().top>=bounds.top-1;}"));
    }
}
