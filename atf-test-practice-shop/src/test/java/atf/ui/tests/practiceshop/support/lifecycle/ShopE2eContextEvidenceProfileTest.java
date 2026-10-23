package atf.ui.tests.practiceshop.support.lifecycle;


import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.model.AtfUiActionRecord;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopE2eContextEvidenceProfileTest {
    private static final String ONE_PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
    @TempDir
    Path temporaryReports;

    @Test
    void reportCompositionHonorsExplicitCapturePolicyOverYamlDefault() {
        ShopConfiguration yaml = ShopConfigurationLoader.load();
        ShopConfiguration isolated = new ShopConfiguration(
                yaml.baseUrl(), yaml.accountFile(), yaml.productName(), yaml.productPath(),
                yaml.checkout(), yaml.browser(), yaml.uiRetry(), yaml.validationProfile(), yaml.api(),
                temporaryReports, temporaryReports, yaml.authenticatedE2eEnabled(),
                yaml.reporting());
        ReportEvidenceProfile explicit = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.UNIFIED_FLOW)
                .screenshotPolicy(ReportScreenshotPolicy.NONE)
                .build();

        try (var report = ShopE2eContext.openReport(isolated, explicit)) {
            assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, report.evidenceProfile().flowMode());
            assertEquals(ReportScreenshotPolicy.NONE,
                    report.evidenceProfile().screenshotPolicy());
        }
    }

    @Test
    void sharedSuiteScenariosUseTheirOwnEvidenceProfile() throws Exception {
        var suiteDefault = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS)
                .screenshotPolicy(ReportScreenshotPolicy.NONE)
                .build();
        var scenarioCapture = suiteDefault.toBuilder()
                .screenshotPolicy(ReportScreenshotPolicy.EACH_CHECKPOINT)
                .build();
        var report = atf.api.reporting.ui.UiExtentReportSession.openConfigured(
                temporaryReports, "scenario-profiles.html", AtfConsoleSink.noOp(), suiteDefault);
        Path reportPath = report.reportPath();

        publishCheckpoint(report, scenarioCapture, "profile override is applied");
        publishCheckpoint(report, suiteDefault, "suite default remains isolated");
        report.close();

        String html = Files.readString(reportPath);
        assertTrue(html.contains("profile override is applied"));
        assertTrue(html.contains("suite default remains isolated"));
        assertEquals(1, occurrences(html, "data-atf-screenshot=\"base64\""),
                "a scenario override must not mutate the shared report default or its siblings");
    }

    private static void publishCheckpoint(
            atf.api.reporting.ui.UiExtentReportSession report,
            ReportEvidenceProfile profile, String title) {
        var scenario = ShopE2eContext.startReportScenario(report, profile, "ShopSuiteTest", title);
        var phase = scenario.startPhase("TEST BODY");
        phase.screenValidation(title, "/checkout", ONE_PIXEL_PNG,
                List.of(new AtfAssertion("Checkout", "visible", "visible", true)),
                List.of(new AtfUiActionRecord(1, "Click", "button", "checkout opens",
                        "click completed", 1, AtfReportStatus.PASS)), AtfReportStatus.PASS);
        phase.complete(AtfReportStatus.PASS);
        scenario.complete(AtfReportStatus.PASS);
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        for (int index = 0; (index = text.indexOf(needle, index)) >= 0; index += needle.length()) count++;
        return count;
    }
}
