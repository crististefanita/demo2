package atf.api.reporting;

import org.junit.jupiter.api.Test;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtentReportUiEvidenceTest {
    private static final String EXTENT_UI_TEST_PACKAGE = "atf.ui.tests.extent";
    @Test
    void publishesUiAuditWithoutImplicitTeardownPhase() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-no-teardown-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-no-teardown.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.api.tests", "visual audit", List.of("ui"));
            var setup = scenario.startPhase("SETUP");
            setup.complete(AtfReportStatus.PASS);
            var body = scenario.startPhase("TEST BODY");
            body.step("THEN", "the report is visually auditable", "#test-view", null,
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("<span>SETUP</span>")),
                () -> assertTrue(html.contains("<span>TEST BODY</span>")),
                () -> assertFalse(html.contains("<span>TEARDOWN</span>")),
                () -> assertTrue(html.contains("the report is visually auditable")));
    }

    @Test
    void publishesArtifactOutcomeWithoutSyntheticUiValidationChild() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-outcome-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-outcome.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.api.tests", "artifact publication", List.of("ui"));
            var teardown = scenario.startPhase("TEARDOWN");
            teardown.outcome("THEN", "the UI artifact was published", "UI audit report", AtfReportStatus.PASS);
            teardown.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("THEN the UI artifact was published")),
                () -> assertTrue(html.contains("OUTCOME TYPE")),
                () -> assertTrue(html.contains("artifact-publication")),
                () -> assertFalse(html.contains("UI validation")));
    }

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
                () -> assertTrue(html.contains("OPERATION") && html.contains("TARGET")),
                () -> assertTrue(html.contains("Get text")),
                () -> assertTrue(html.contains("role=heading[name=Quote Checklist]")),
                () -> assertTrue(html.contains("549 ms")),
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
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "observation split", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidation("report state", "#test-view", ReportTestScreenshots.ONE_PIXEL_PNG, List.of(), List.of(
                    new AtfUiActionRecord(1, "Click", "#tests", "tests opens", "click completed", 4, AtfReportStatus.PASS),
                    new AtfUiActionRecord(2, "Screenshot", "current page", "visual state captured",
                            "Base64 media link", 0, AtfReportStatus.PASS,
                            atf.api.core.ui.AtfUiEvidenceSource.OBSERVE)), AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("Playwright action sequence (1 entries)")),
                () -> assertTrue(html.contains("Audit observations · 1 checkpoint")),
                () -> assertTrue(html.contains("aria-label=\"Audit observations · 1 checkpoint\"")),
                () -> assertFalse(html.contains("Audit observations · 1 checkpoint · Screenshot")),
                () -> assertTrue(html.contains("atf-ui-observation-table")));
    }

    @Test
    void preservesExecutedActionsWhenFailureHasNoScreenshot() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-failure-evidence-");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "ui-failure-evidence.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "failure evidence", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.failureEvidence("exploration failed", "#test-view", List.of(
                    new AtfUiActionRecord(1, "Click", "#tests", "tests opens", "click completed", 4,
                            AtfReportStatus.PASS)), new IllegalStateException("synthetic failure"));
            body.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("preserve executed actions; screenshot disabled or unavailable")),
                () -> assertTrue(html.contains("Playwright action sequence (1 entries)")),
                () -> assertTrue(html.contains("synthetic failure")),
                () -> assertTrue(html.contains("Failure evidence")));
    }

    @Test
    void keepsPlaywrightTableAndScreenshotInsideScreenValidation() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-validation-evidence-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-validation-evidence.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "validation hierarchy", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidation("report is visible", "#test-view", ReportTestScreenshots.ONE_PIXEL_PNG, List.of(
                    new atf.api.core.reporting.AtfAssertion("Report heading", "visible", "visible", true)),
                    List.of(new AtfUiActionRecord(1, "Get text", "h1", "heading is visible",
                            "Report", 4, AtfReportStatus.PASS)), AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int validation = html.indexOf(">Screen validation<");
        int actionSequence = html.indexOf("class=\"atf-ui-action-sequence\"");
        int screenshot = html.indexOf("class=\"atf-ui-action-screenshot\"");
        assertAll(
                () -> assertTrue(validation >= 0, "Screen validation node is missing"),
                () -> assertTrue(actionSequence > validation,
                        "Playwright action table must be rendered inside Screen validation"),
                () -> assertTrue(screenshot > actionSequence,
                        "Screenshot must follow the Playwright action table"),
                  () -> assertTrue(html.contains("href=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\"")),
                 () -> assertTrue(count(html, "data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG) == 1));
    }

    @Test
    void rendersMediaModeAsActionTablePlusBase64ImageLink() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-media-validation-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-media-validation.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "media validation", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidationAsMedia("category filter is visible", "#category-view",
                    ReportTestScreenshots.ONE_PIXEL_PNG, List.of(new atf.api.core.reporting.AtfAssertion(
                            "Category title", "visible", "visible", true)),
                    List.of(new AtfUiActionRecord(1, "Click", "category=contract",
                            "detail opens", "click completed", 8, AtfReportStatus.PASS)),
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                 () -> assertTrue(html.contains("href=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\"")
                        && html.contains(">📷 base64 img</span>")),
                () -> assertTrue(html.contains(".atf-ui-action-screenshot{display:flex!important;justify-content:flex-start!important;align-items:center!important;text-align:left!important;vertical-align:middle!important;")
                        && html.contains("padding:0!important")
                        && html.contains("max-width:100%!important;overflow:visible!important")),
                 () -> assertTrue(html.contains(".atf-ui-action-screenshot-link{display:inline-flex!important;flex-direction:row!important;align-items:center!important;align-self:center!important;justify-content:center!important;gap:4px!important;height:18px!important;min-height:18px!important;padding:0!important;line-height:18px!important;vertical-align:middle!important;transform:none!important;")),
                () -> assertTrue(html.contains("text-decoration:none!important")),
                () -> assertTrue(html.contains("class=\"atf-ui-action-sequence\"")),
                 () -> assertTrue(html.contains("href=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\"")),
                 () -> assertTrue(html.contains("data-featherlight=\"image\"")),
                 () -> assertFalse(html.contains("atf-ui-action-screenshot-image")));
    }

    @Test
    void collapsesRepeatedScreenshotPayloadsBeforePublishingTheActionTable() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-repeated-screenshot-");
        Path report = root.resolve("ui-repeated-screenshot.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "repeated visual state", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN the same visual state is reused", "", "");
            step.uiActions(List.of(new AtfUiActionRecord(1, "Click", "#tests",
                    "tests opens", "click completed", 4, AtfReportStatus.PASS)));
            step.screenshot("first visual state", ReportTestScreenshots.ONE_PIXEL_PNG);
            step.screenshot("same visual state again", " " + ReportTestScreenshots.ONE_PIXEL_PNG + " ");
            step.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertEquals(1, count(html, "data-atf-screenshot=\"base64\"")),
                () -> assertEquals(1, count(html, "data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG)),
                () -> assertTrue(html.contains("aria-label=\"Open first visual state\"")),
                () -> assertFalse(html.contains("same visual state again")));
    }

    @Test
    void collapsesRepeatedOverviewCaptureBytesWithoutRenderingInlineImages() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-overview-dedup-");
        Path report = root.resolve("report.html");
        Path first = root.resolve("first.png");
        Path duplicate = root.resolve("duplicate.png");
        Files.writeString(report, "<body><div id=\"dashboard-view\">overview</div></body>");
        Files.write(first, Base64.getDecoder().decode(ReportTestScreenshots.ONE_PIXEL_PNG));
        Files.write(duplicate, Base64.getDecoder().decode(ReportTestScreenshots.ONE_PIXEL_PNG));

        ExtentReportUiEvidence.embedInlinePngs(report, List.of(
                new ExtentReportUiEvidence.Capture(first, "first capture", List.of("GIVEN first")),
                new ExtentReportUiEvidence.Capture(duplicate, "duplicate capture", List.of("GIVEN duplicate"))));

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("Playwright exploratory evidence · 1 captures")),
                () -> assertEquals(1, count(html, "data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG)),
                () -> assertTrue(html.contains("first capture")),
                () -> assertFalse(html.contains("duplicate capture")),
                () -> assertFalse(html.contains("src=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\"")));
    }

    @Test
    void rejectsUiActionTableWithoutExactlyOneBase64Screenshot() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-action-screenshot-contract-");
        Path report = root.resolve("ui-actions-without-screenshot.html");

        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "invalid visual audit", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN the page is visible", "", "");
            step.uiActions(List.of(new AtfUiActionRecord(1, "Click", "#submit",
                    "page changes", "click completed", 12, AtfReportStatus.PASS)));

            assertThrows(IllegalStateException.class,
                    () -> step.complete(AtfReportStatus.PASS),
                    "an action table without its local screenshot must not be published");
        }
    }

    @Test
    void rejectsUiActionTableWithMoreThanOneBase64Screenshot() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-action-multiple-screenshots-");
        Path report = root.resolve("ui-actions-with-multiple-screenshots.html");

        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "invalid visual audit", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN the page is visible", "", "");
            step.uiActions(List.of(new AtfUiActionRecord(1, "Click", "#submit",
                    "page changes", "click completed", 12, AtfReportStatus.PASS)));
            step.screenshot("first state", ReportTestScreenshots.ONE_PIXEL_PNG);
            step.screenshot("second state", ReportTestScreenshots.SECOND_PIXEL_PNG);

            assertThrows(IllegalStateException.class,
                    () -> step.complete(AtfReportStatus.PASS),
                    "an action table must have exactly one local screenshot");
        }
    }

    private static int count(String text, String fragment) {
        int occurrences = 0;
        int from = 0;
        while ((from = text.indexOf(fragment, from)) >= 0) {
            occurrences++;
            from += fragment.length();
        }
        return occurrences;
    }

    private static boolean screenshotAnchorContainsDirectDataUri(String html) {
        int anchorStart = html.indexOf("<a class=\"atf-ui-action-screenshot-link\"");
        int anchorEnd = anchorStart < 0 ? -1 : html.indexOf("</a>", anchorStart);
        if (anchorStart < 0 || anchorEnd < 0) {
            return false;
        }
        String anchor = html.substring(anchorStart, anchorEnd);
        return anchor.contains("data-atf-screenshot=\"base64\"")
                && anchor.contains("href=\"data:image/png;base64," + ReportTestScreenshots.ONE_PIXEL_PNG + "\"");
    }
}


