package atf.api.reporting.ui;
import atf.api.testkit.AtfUnitTest;

import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.core.ui.AtfUiEvidenceSource;
import atf.api.reporting.evidence.ExtentReportUiEvidence;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.support.ReportTestScreenshots;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static atf.api.reporting.ui.ExtentReportUiEvidenceTestSupport.EXTENT_UI_TEST_PACKAGE;
import static atf.api.reporting.ui.ExtentReportUiEvidenceTestSupport.count;
import static atf.api.reporting.ui.ExtentReportUiEvidenceTestSupport.screenshotAnchorContainsDirectDataUri;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportUiEvidenceRenderingTest {
    @Test
    void embedsPlaywrightPngAndAuditStepsInsideOverview() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-evidence-");
        Path report = root.resolve("report.html");
        Path png = root.resolve("capture.png");
        Files.writeString(report, "<body><div id=\"dashboard-view\">overview</div></body>");
        Files.write(png, Base64.getDecoder().decode(ReportTestScreenshots.ONE_PIXEL_PNG));

        ExtentReportUiEvidence.embedInlinePngs(report, List.of(new ExtentReportUiEvidence.Capture(
                png, "Tests tab", List.of("GIVEN report is open", "THEN class owners are visible"))));

        String html = Files.readString(report);
        assertTrue(html.contains("id=\"atf-ui-exploratory-evidence\""));
        assertTrue(html.contains("href=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\""));
        assertTrue(html.contains("class=\"atf-ui-evidence-screenshot-link\""));
        assertTrue(html.contains("align-items:center;align-self:center;justify-content:center;gap:4px;height:18px;min-height:18px;"));
        assertTrue(html.contains("vertical-align:middle;margin:8px 0 0;transform:translateY(2px);"));
        assertTrue(html.contains("target=\"_blank\""));
        assertTrue(html.contains(">📷 base64 img</span>"));
        assertTrue(html.contains("aria-label=\"Open Tests tab\""));
        assertTrue(html.contains("data-featherlight=\"image\""));
        assertTrue(html.contains("rel=\"noopener\""));
        assertFalse(html.contains("atf-ui-action-screenshot-image"));
        assertTrue(html.contains("GIVEN report is open"));
        assertTrue(html.contains("<details>"));
        assertFalse(html.contains("<details open>"));
        assertFalse(html.contains("capture.png"));
    }

    @Test
    void rendersUiScreenshotAsBase64LinkEvidenceOnTheUiStep() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-report-");
        Path report = root.resolve("ui-report.html");

        try (ExtentReportSession session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "report inspection", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN report is inspectable", "", "");
            step.screenshot("Playwright screenshot", ReportTestScreenshots.ONE_PIXEL_PNG);
            step.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertTrue(html.contains(">📷 base64 img</span>"));
        assertTrue(html.contains("aria-label=\"Open Playwright screenshot\""));
        assertTrue(html.contains("data-featherlight=\"image\""));
        assertFalse(html.contains("| Playwright screenshot"));
        assertTrue(html.contains("href=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\""));
        assertTrue(html.contains("target=\"_blank\""));
        assertTrue(html.contains("rel=\"noopener\""));
        assertFalse(html.contains("atf-ui-action-screenshot-image"));
        assertTrue(html.contains("Playwright screenshot"));
    }

    @Test
    void rendersConsecutivePlaywrightActionsAsOneCollapsibleTableWithEmojiResult() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-action-sequence-");
        Path report = root.resolve("ui-actions.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "action sequence", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN checklist is open", "", "");
            step.uiActions(List.of(
                    new AtfUiActionRecord(1, "Click", "role=tab[name=Checklist]",
                            "checklist view opens", "click completed", 549, AtfReportStatus.PASS),
                    new AtfUiActionRecord(2, "Get text", "role=heading[name=Quote Checklist]",
                            "heading is visible", "Quote Checklist", 12, AtfReportStatus.PASS)));
            step.screenshot("Screen validation", ReportTestScreenshots.ONE_PIXEL_PNG);
            step.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("class=\"atf-ui-action-sequence\"")),
                () -> assertTrue(html.contains("Playwright action sequence (2 entries)")),
                () -> assertFalse(html.contains("<details class=\"atf-ui-action-sequence\" open")),
                () -> assertTrue(html.contains("<th>ACTION</th>") && html.contains("<th>TARGET</th>")),
                () -> assertTrue(html.contains("class=\"atf-ui-action-identity\"><span class=\"atf-ui-action-operation\">Get text</span>")),
                () -> assertTrue(html.contains("class=\"atf-ui-action-sequence\">#2</span>")),
                () -> assertTrue(html.contains("Get text")),
                () -> assertTrue(html.contains("role=heading[name=Quote Checklist]")),
                () -> assertTrue(html.contains("class=\"atf-ui-action-time\">549 ms</span>")),
                () -> assertTrue(html.contains("atf-ui-action-result-pass")),
                () -> assertTrue(html.contains("role=\"img\" aria-label=\"Pass\" title=\"Pass\">✅</span>")),
                () -> assertFalse(html.contains("class=\"atf-ui-action-result atf-ui-action-result-pass\">Pass</span>")),
                () -> assertTrue(html.contains("class=\"atf-ui-action-screenshot\"")),
                () -> assertTrue(html.contains("class=\"atf-ui-action-screenshot-link\" data-atf-screenshot=\"base64\" href=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\"")),
                () -> assertTrue(html.contains(">📷 base64 img</span>")),
                () -> assertTrue(html.contains("aria-label=\"Open Screen validation\"")),
                () -> assertTrue(html.contains("data-featherlight=\"image\"")),
                () -> assertTrue(html.contains("target=\"_blank\"") && html.contains("rel=\"noopener\"")),
                () -> assertFalse(html.contains("atf-ui-action-screenshot-image")),
                () -> assertFalse(html.contains(">passed</span>")));
    }

    @Test
    void rendersOneScreenshotReferenceAndOneResultEmojiPerActionWithoutDuplication() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-deduplicated-evidence-");
        Path report = root.resolve("ui-deduplicated-evidence.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "deduplicated evidence", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN the page is auditable", "", "");
            step.uiActions(List.of(
                    new AtfUiActionRecord(1, "Click", "#tests", "Tests opens", "click completed",
                            4, AtfReportStatus.PASS),
                    new AtfUiActionRecord(2, "Get text", "h1", "heading is visible", "Tests",
                            2, AtfReportStatus.PASS)));
            step.screenshot("One visual state", ReportTestScreenshots.ONE_PIXEL_PNG);
            step.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(count(html, "data-atf-screenshot=\"base64\"") == 1,
                        "one visual state must produce one screenshot reference"),
                () -> assertTrue(count(html, ">📷 base64 img</span>") == 1,
                        "the screenshot label must not be duplicated"),
                () -> assertTrue(count(html, "class=\"atf-ui-action-result atf-ui-action-result-pass\"") == 2,
                        "each consecutive action must have exactly one result emoji"),
                () -> assertTrue(html.contains("data-featherlight=\"image\"")),
                () -> assertFalse(html.contains("atf-ui-action-screenshot-image")),
                () -> assertTrue(count(html, "target=\"_blank\"") == 1),
                () -> assertTrue(count(html, "rel=\"noopener\"") == 1),
                () -> assertTrue(screenshotAnchorContainsDirectDataUri(html),
                        "the screenshot anchor must carry the one Base64 payload"),
                () -> assertTrue(count(html, "atf-ui-action-screenshot-image") == 0,
                        "one visual state must not render a duplicate image"),
                () -> assertTrue(count(html, "data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG) == 1,
                        "one visual state must contain one Base64 payload"));
    }

    @Test
    void rendersReporterObservationsSeparatelyFromPlaywrightActions() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-observations-");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "ui-observations.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "observation split", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidation("report state", "#test-view", ReportTestScreenshots.ONE_PIXEL_PNG, List.of(), List.of(
                    new AtfUiActionRecord(1, "Click", "#tests", "tests opens", "click completed", 4, AtfReportStatus.PASS),
                    new AtfUiActionRecord(2, "Screenshot", "current page", "visual state captured",
                            "Base64 media link", 0, AtfReportStatus.PASS, AtfUiEvidenceSource.OBSERVE)), AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("Playwright action sequence (1 entries)")),
                () -> assertTrue(html.contains("Audit observations · 1 checkpoint")),
                () -> assertTrue(html.contains("aria-label=\"Audit observations · 1 checkpoint\"")),
                () -> assertFalse(html.contains("Audit observations · 1 checkpoint · Screenshot")),
                () -> assertTrue(html.contains("atf-ui-observation-table")),
                () -> assertTrue(html.contains(
                        "class=\"atf-ui-action-table atf-ui-observation-table\"><thead><tr>"
                                + "<th>ACTION</th><th>TARGET</th><th>EXPECTED</th>"
                                + "<th>OBSERVED</th><th>RESULT</th>")),
                () -> assertFalse(html.contains(
                        "class=\"atf-ui-action-table atf-ui-observation-table\"><thead><tr>"
                                + "<th>ACTION</th><th>TARGET</th><th>EXPECTED</th>"
                                + "<th>OBSERVED</th><th>TIME</th>")));
    }
}


