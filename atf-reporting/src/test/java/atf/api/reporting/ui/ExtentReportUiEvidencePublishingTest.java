package atf.api.reporting.ui;
import atf.api.testkit.AtfUnitTest;

import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.contract.ReportUiContract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static atf.api.reporting.ui.ExtentReportUiEvidenceTestSupport.EXTENT_UI_TEST_PACKAGE;
import static atf.api.core.reporting.contract.AtfGherkinKeyword.GIVEN;
import static atf.api.core.reporting.contract.AtfGherkinKeyword.THEN;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportUiEvidencePublishingTest {
    @Test
    void publishesUiAuditWithoutImplicitTeardownPhase() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-no-teardown-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-no-teardown.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.api.tests", "visual audit", List.of("ui"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.complete(AtfReportStatus.PASS);
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.step(THEN, "the report is visually auditable", "#test-view", null,
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
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.api.tests", "artifact publication", List.of("ui"));
            var teardown = scenario.startPhase(ReportUiContract.Phase.TEARDOWN.displayName());
            teardown.outcome(THEN, "the UI artifact was published", "UI audit report", AtfReportStatus.PASS);
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
    void publishesSetupAsArtifactEvidenceWithoutSyntheticUiValidationChild() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-setup-evidence-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-setup.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "report setup", List.of("ui"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.setup(GIVEN, "the input report is available",
                    "input-report.html", List.of(new AtfAssertion(
                            "Input report", "published HTML", "input-report.html", true)),
                    AtfReportStatus.PASS);
            setup.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int setupStart = html.indexOf("<details class=\"step-detail setup-evidence atf-setup-evidence\"");
        int setupEnd = html.indexOf("</td>", setupStart);
        String setupMarkup = setupStart >= 0 && setupEnd > setupStart
                ? html.substring(setupStart, setupEnd)
                : "";
        assertAll(
                () -> assertTrue(setupStart >= 0 && setupEnd > setupStart),
                () -> assertTrue(html.contains("data-atf-evidence-type=\"report-setup\"")),
                () -> assertTrue(html.contains("<summary aria-expanded=\"false\"><span class=\"atf-disclosure-glyph\"")),
                () -> assertTrue(html.contains("Setup evidence · 1 checks")),
                () -> assertTrue(html.contains("<div class=\"body\"><table")),
                () -> assertTrue(html.contains("</table></div></details>")),
                () -> assertTrue(html.contains("class=\"atf-setup-evidence-table\"")),
                () -> assertTrue(html.contains("CHECK") && html.contains("EXPECTED")
                        && html.contains("ACTUAL") && html.contains("RESULT")),
                () -> assertTrue(html.contains("✅ PASS")),
                () -> assertTrue(html.contains("input-report.html")),
                () -> assertFalse(setupMarkup.contains("<details open")),
                () -> assertFalse(setupMarkup.contains("Request details")),
                () -> assertFalse(setupMarkup.contains("Response details")),
                () -> assertFalse(setupMarkup.contains("UI validation")),
                () -> assertFalse(setupMarkup.contains("Screen validation")));
    }

    @Test
    void keepsSetupEvidenceInsideOneClosedFrameForMultipleChecks() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-setup-multi-checks-");
        Path report;

        try (var session = UiExtentReportSession.openConfigured(root, "ui-setup-multi-checks.html",
                AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiTest", EXTENT_UI_TEST_PACKAGE,
                    "multiple setup checks", List.of("ui"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.setup(GIVEN, "the input report is available",
                    "input-report.html", List.of(
                            new AtfAssertion("Input report", "published HTML", "input-report.html", true),
                            new AtfAssertion("Input report is readable", "readable", "readable", true)),
                    AtfReportStatus.PASS);
            setup.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        int disclosure = html.indexOf("<details class=\"step-detail setup-evidence atf-setup-evidence\"");
        int closing = html.indexOf("</table></div></details>", disclosure);
        assertAll(
                () -> assertTrue(disclosure >= 0),
                () -> assertTrue(html.contains("Setup evidence · 2 checks")),
                () -> assertTrue(closing > disclosure),
                () -> assertEquals(1, ExtentReportUiEvidenceTestSupport.count(html,
                        "step-detail setup-evidence atf-setup-evidence")));
    }
}


