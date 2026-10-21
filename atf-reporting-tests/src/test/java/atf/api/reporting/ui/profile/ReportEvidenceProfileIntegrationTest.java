package atf.api.reporting.ui.profile;

import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureProvider;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.model.ReportScreenshot;
import atf.api.reporting.support.ReportTestScreenshots;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.testkit.AtfIntegrationTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Proves the typed consumer profile crosses the session boundary without a browser. */
@AtfIntegrationTest
class ReportEvidenceProfileIntegrationTest {
    @Test
    void typedProfileDrivesTheExistingSessionWithoutDuplicatingTheRenderer() throws Exception {
        Path root = Files.createTempDirectory("atf-evidence-profile-");
        ReportEvidenceProfile profile = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.UNIFIED_FLOW)
                .screenshotPolicy(ReportScreenshotPolicy.FINAL_ONLY)
                .captureSource(ReportEvidenceCaptureSource.DETERMINISTIC_MOCK)
                .build();

        Path report;
        ReportEvidenceCaptureProvider provider = new ReportEvidenceCaptureProvider() {
            @Override public ReportEvidenceCaptureSource source() {
                return ReportEvidenceCaptureSource.DETERMINISTIC_MOCK;
            }
            @Override public ReportScreenshot capture(String title, String target) {
                return new ReportScreenshot(title, ReportTestScreenshots.ONE_PIXEL_PNG);
            }
        };
        try (var session = UiExtentReportSession.openConfigured(root, "profile.html",
                AtfConsoleSink.noOp(), profile)) {
            assertEquals(profile, session.evidenceProfile());
            var scenario = session.startScenario("ProfileTest", "atf.api.reporting.ui",
                    "typed evidence profile", List.of("evidence", "component"));
            var body = scenario.startPhase("TEST BODY");
            var finalCapture = profile.capture(ReportScreenshotPolicy.CapturePoint.FINAL,
                    "final state", "#final", provider).orElseThrow();
            body.screenValidation(finalCapture.title(), "#final", finalCapture.base64Png(),
                    List.of(), List.of(), ReportScreenshotPolicy.CapturePoint.FINAL,
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
            report = session.reportPath();
        }

        String html = Files.readString(report);
        assertEquals(1, count(html, "data-atf-screenshot=\"base64\""));
        assertTrue(html.contains("typed evidence profile"));
    }

    private static int count(String html, String fragment) {
        int count = 0;
        int from = 0;
        while ((from = html.indexOf(fragment, from)) >= 0) {
            count++;
            from += fragment.length();
        }
        return count;
    }
}
