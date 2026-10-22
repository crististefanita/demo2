package atf.api.reporting.ui;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.model.AtfUiActionRecord;
import atf.api.reporting.ui.model.AtfUiEvidenceSource;
import atf.api.reporting.support.ReportTestScreenshots;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static atf.api.reporting.ui.ExtentReportUiEvidenceTestSupport.EXTENT_UI_TEST_PACKAGE;
import static atf.api.reporting.ui.ExtentReportUiEvidenceTestSupport.count;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportUiFailureEvidenceTest {
    @Test
    void rendersConcreteUiFailureLeavesAsFailWithAnActionableSummary() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-failure-leaves-");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "ui-failure-leaves.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "failure leaves", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidation("disclosure is reached", "#failure-action",
                    ReportTestScreenshots.ONE_PIXEL_PNG, List.of(), List.of(
                            new AtfUiActionRecord(1, "Click", "#failure-action",
                                    "disclosure opens", "AssertionError: disclosure remained closed",
                                    4, AtfReportStatus.FAIL)), AtfReportStatus.FAIL);
            body.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("Playwright action sequence · 1 failed of 1")),
                () -> assertTrue(html.contains("data-atf-evidence-status=\"fail\"")),
                () -> assertTrue(html.contains("atf-ui-action-result-fail")),
                () -> assertTrue(html.contains("AssertionError: disclosure remained closed")),
                () -> assertTrue(html.contains("<span class=\"badge log fail-bg\">Fail</span>"),
                        "the concrete action evidence event must be Fail, not Info"));
    }

    @Test
    void rendersSetupAuditAndAssertionFailureEvidenceAsFailLeaves() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-failure-surfaces-");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "ui-failure-surfaces.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();

            var setupScenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "setup failure", List.of("ui"));
            var setup = setupScenario.startPhase("SETUP");
            setup.setup("GIVEN", "the report input is available", "report input",
                    List.of(new AtfAssertion(
                            "Input report", "available", "missing", false)), AtfReportStatus.FAIL);
            setup.complete(AtfReportStatus.FAIL);
            setupScenario.complete(AtfReportStatus.FAIL);

            var auditScenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "audit failure", List.of("ui"));
            var auditBody = auditScenario.startPhase("TEST BODY");
            auditBody.screenValidation("badge is visible", "#badge", ReportTestScreenshots.ONE_PIXEL_PNG,
                    List.of(), List.of(new AtfUiActionRecord(1, "Audit observation", "#badge",
                            "badge is visible", "badge is missing", 0, AtfReportStatus.FAIL,
                            AtfUiEvidenceSource.OBSERVE)), AtfReportStatus.FAIL);
            auditBody.complete(AtfReportStatus.FAIL);
            auditScenario.complete(AtfReportStatus.FAIL);

            var assertionScenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "assertion failure", List.of("ui"));
            var assertionBody = assertionScenario.startPhase("TEST BODY");
            assertionBody.screenValidation("status is Fail", "#status", ReportTestScreenshots.ONE_PIXEL_PNG,
                    List.of(new AtfAssertion(
                            "Status badge", "Fail", "Pass", false)), List.of(), AtfReportStatus.FAIL);
            assertionBody.complete(AtfReportStatus.FAIL);
            assertionScenario.complete(AtfReportStatus.FAIL);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("data-atf-evidence-type=\"report-setup\" data-atf-evidence-status=\"fail\"")),
                () -> assertTrue(html.contains("Audit observations · 1 failed of 1")),
                () -> assertTrue(html.contains("class=\"atf-ui-observations\" data-atf-evidence-status=\"fail\"")),
                () -> assertTrue(html.contains("class=\"atf-ui-assertions\" data-atf-evidence-status=\"fail\"")),
                () -> assertTrue(count(html, "<span class=\"badge log fail-bg\">Fail</span>") >= 3,
                        "setup, audit and assertion evidence must each expose a Fail event"));
    }

    @Test
    void preservesExecutedActionsWhenFailureHasNoScreenshot() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-failure-evidence-");
        Path report;
        try (var session = UiExtentReportSession.openConfigured(root, "ui-failure-evidence.html",
                AtfConsoleSink.noOp())) {
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
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE, "validation hierarchy", List.of("ui"));
            var body = scenario.startPhase("TEST BODY");
            body.screenValidation("report is visible", "#test-view", ReportTestScreenshots.ONE_PIXEL_PNG, List.of(
                    new AtfAssertion("Report heading", "visible", "visible", true)),
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
}


