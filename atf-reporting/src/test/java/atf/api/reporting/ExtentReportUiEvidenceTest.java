package atf.api.reporting;

import org.junit.jupiter.api.Test;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtentReportUiEvidenceTest {
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
        Files.write(png, new byte[]{1, 2, 3});

        ExtentReportUiEvidence.embedInlinePngs(report, List.of(new ExtentReportUiEvidence.Capture(
                png, "Tests tab", List.of("GIVEN report is open", "THEN class owners are visible"))));

        String html = Files.readString(report);
        assertTrue(html.contains("id=\"atf-ui-exploratory-evidence\""));
        assertTrue(html.contains("href=\"data:image/png;base64,AQID\""));
        assertTrue(html.contains("class=\"atf-ui-evidence-screenshot-link\""));
        assertTrue(html.contains("target=\"_blank\""));
        assertTrue(html.contains(">📷 base64 img</span>"));
        assertTrue(html.contains("aria-label=\"Open Tests tab\"><span class=\"badge badge-gradient-primary\">📷 base64 img</span>"));
        assertTrue(html.contains("data-featherlight=\"image\""));
        assertTrue(html.contains("rel=\"noopener\""));
        assertFalse(html.contains("class=\"atf-ui-action-screenshot\"><img"));
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
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "report inspection", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN report is inspectable", "", "");
            step.screenshot("Playwright screenshot", "AQID");
            step.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertTrue(html.contains(">📷 base64 img</span>"));
        assertTrue(html.contains("aria-label=\"Open Playwright screenshot\"><span class=\"badge badge-gradient-primary\">📷 base64 img</span>"));
        assertTrue(html.contains("data-featherlight=\"image\""));
        assertFalse(html.contains("| Playwright screenshot"));
        assertTrue(html.contains("href=\"data:image/png;base64,AQID\""));
        assertTrue(html.contains("target=\"_blank\""));
        assertTrue(html.contains("rel=\"noopener\""));
        assertFalse(html.contains("class=\"atf-ui-action-screenshot\"><img"));
        assertTrue(html.contains("Playwright screenshot"));
    }

    @Test
    void rendersConsecutivePlaywrightActionsAsOneCollapsibleTableWithEmojiResult() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-action-sequence-");
        Path report = root.resolve("ui-actions.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "action sequence", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN checklist is open", "", "");
            step.uiActions(List.of(
                    new AtfUiActionRecord(1, "Click", "role=tab[name=Checklist]",
                            "checklist view opens", "click completed", 549, AtfReportStatus.PASS),
                    new AtfUiActionRecord(2, "Get text", "role=heading[name=Quote Checklist]",
                            "heading is visible", "Quote Checklist", 12, AtfReportStatus.PASS)));
            step.screenshot("Screen validation", "AQID");
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
                () -> assertTrue(html.contains("class=\"atf-ui-action-screenshot-link\" data-atf-screenshot=\"base64\" href=\"data:image/png;base64,AQID\"")),
                () -> assertTrue(html.contains(">📷 base64 img</span>")),
                () -> assertTrue(html.contains("aria-label=\"Open Screen validation\"><span class=\"badge badge-gradient-primary\">📷 base64 img</span>")),
                () -> assertTrue(html.contains("data-featherlight=\"image\"")),
                () -> assertTrue(html.contains("target=\"_blank\"") && html.contains("rel=\"noopener\"")),
                () -> assertFalse(html.contains("class=\"atf-ui-action-screenshot\"><img")),
                () -> assertFalse(html.contains(">passed</span>")));
    }

    @Test
    void rendersReporterObservationsSeparatelyFromPlaywrightActions() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-observations-");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "ui-observations.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "observation split", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidation("report state", "#test-view", "AQID", List.of(), List.of(
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
                () -> assertTrue(html.contains("Audit observations (1 entries)")),
                () -> assertTrue(html.contains("atf-ui-observation-table")));
    }

    @Test
    void preservesExecutedActionsWhenFailureHasNoScreenshot() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-failure-evidence-");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "ui-failure-evidence.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "failure evidence", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.failureEvidence("exploration failed", "#test-view", List.of(
                    new AtfUiActionRecord(1, "Click", "#tests", "tests opens", "click completed", 4,
                            AtfReportStatus.PASS)), new IllegalStateException("synthetic failure"));
            body.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("preserve executed actions; screenshot optional")),
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
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "validation hierarchy", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidation("report is visible", "#test-view", "AQID", List.of(
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
                () -> assertTrue(html.contains("href=\"data:image/png;base64,AQID\"")),
                () -> assertFalse(html.contains("class=\"atf-ui-action-screenshot\"><img")));
    }

    @Test
    void rendersMediaModeAsActionTablePlusBase64ImageLink() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-media-validation-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-media-validation.html",
                atf.api.core.reporting.AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "media validation", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidationAsMedia("category filter is visible", "#category-view",
                    "AQID", List.of(new atf.api.core.reporting.AtfAssertion(
                            "Category title", "visible", "visible", true)),
                    List.of(new AtfUiActionRecord(1, "Click", "category=contract",
                            "detail opens", "click completed", 8, AtfReportStatus.PASS)),
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("Base64 native media link")
                        && html.contains(">Open screenshot · Base64 image</span>")),
                () -> assertTrue(html.contains(".atf-ui-action-screenshot{display:flex!important;justify-content:flex-start!important;align-items:flex-end!important;text-align:left!important;vertical-align:bottom!important;")
                        && html.contains("padding:4px 0 0!important")),
                () -> assertTrue(html.contains(".atf-ui-action-screenshot-link{display:inline-flex!important;align-items:center!important;align-self:flex-end!important;justify-content:flex-start!important;line-height:16px!important;min-height:16px!important;vertical-align:bottom!important;")),
                () -> assertTrue(html.contains("text-decoration:none!important")),
                () -> assertTrue(html.contains("class=\"atf-ui-action-sequence\"")),
                () -> assertTrue(html.contains("href=\"data:image/png;base64,AQID\"")),
                () -> assertTrue(html.contains("data-featherlight=\"image\"")),
                () -> assertFalse(html.contains("class=\"atf-ui-action-screenshot\"><img")));
    }

    @Test
    void rejectsUiActionTableWithoutExactlyOneBase64Screenshot() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-action-screenshot-contract-");
        Path report = root.resolve("ui-actions-without-screenshot.html");

        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "invalid visual audit", List.of("ui"));
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
            var scenario = session.startScenario("UiTest", "atf.ui.tests", "invalid visual audit", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            var step = phase.startRequest("THEN the page is visible", "", "");
            step.uiActions(List.of(new AtfUiActionRecord(1, "Click", "#submit",
                    "page changes", "click completed", 12, AtfReportStatus.PASS)));
            step.screenshot("first state", "AQID");
            step.screenshot("second state", "BAUG");

            assertThrows(IllegalStateException.class,
                    () -> step.complete(AtfReportStatus.PASS),
                    "an action table must have exactly one local screenshot");
        }
    }
}
