package atf.api.reporting.ui.network;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class NetworkInspectorSummaryRoleTest {
    @Test
    void marksTheNetworkInspectorAsASummaryRatherThanAnAdditionalBusinessStep() throws Exception {
        Path root = Files.createTempDirectory("atf-network-summary-");
        var request = new ReportRequest("GET", "/products/1", Map.of(), Map.of(), "none", "");
        var response = new ReportResponse(-1, "request failed", Map.of(), Duration.ofMillis(1),
                "https://shop.test/products/1");
        var evidence = new ReportExecutionEvidence(response, 1, Duration.ofMillis(1), List.of(),
                List.of(), request, ReportTransportProvenance.LIVE_HTTP);

        Path generatedReport;
        try (var session = UiExtentReportSession.openConfigured(root, "report.html",
                AtfConsoleSink.noOp())) {
            generatedReport = session.reportPath();
            var scenario = session.startScenario("UiTest", "atf.ui", "journey", List.of("ui"));
            var phase = scenario.startPhase("TEST BODY");
            phase.networkInspector("API network inspector — journey summary", List.of(evidence),
                    List.of(new ReportNetworkInspector.Step("THEN", "product is visible", 0, true,
                            List.of(evidence))));
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        assertTrue(generatedReport.startsWith(root));
        String html = Files.readString(generatedReport);
        assertTrue(html.contains("journey summary; not an additional business step"));
        int title = html.indexOf("API network inspector — journey summary");
        String summaryNode = html.substring(Math.max(0, title - 700), title);
        assertTrue(summaryNode.contains("badge pass-bg log \">Pass</span>"),
                "A passive inspector summary stays Pass even when it records a failed exchange");
    }

    @Test
    void publishesOneJourneySummaryAfterEveryUiApiCheckpointAndFailureForEachFlowMode()
            throws Exception {
        for (ReportEvidenceFlowMode flowMode : List.of(
                ReportEvidenceFlowMode.SCREEN_CHECKPOINTS,
                ReportEvidenceFlowMode.UNIFIED_FLOW)) {
            Path root = Files.createTempDirectory("atf-network-summary-order-");
            var request = new ReportRequest("POST", "/orders", Map.of(), Map.of(), "none", "");
            var response = new ReportResponse(201, "{\"id\":\"order-1\"}", Map.of(),
                    Duration.ofMillis(2), "https://shop.test/orders");
            var evidence = new ReportExecutionEvidence(response, 1, Duration.ofMillis(2), List.of(),
                    List.of(), request, ReportTransportProvenance.LIVE_HTTP);
            String summaryTitle = "API network inspector — journey summary";

            Path generatedReport;
            try (var session = UiExtentReportSession.openConfigured(root,
                    "journey-" + flowMode + ".html", AtfConsoleSink.noOp(),
                    ReportEvidenceProfile.builder().flowMode(flowMode).build())) {
                generatedReport = session.reportPath();
                var scenario = session.startScenario("UiTest", "atf.ui", "purchase journey", List.of("ui", "api"));
                var phase = scenario.startPhase("TEST BODY");
                phase.step(AtfGherkinKeyword.GIVEN, "product page is ready", "/products/1", "",
                        List.of(new AtfAssertion("Product", "visible", "visible", true)),
                        List.of(), List.of(evidence), AtfReportStatus.PASS);
                phase.networkInspector(summaryTitle, List.of(evidence),
                        List.of(new ReportNetworkInspector.Step("GIVEN", "product page is ready", 1,
                                true, List.of(evidence))));
                assertThrows(IllegalStateException.class,
                        () -> phase.networkInspector(summaryTitle, List.of(evidence)),
                        "A UI phase has one journey summary, not one per API exchange");
                phase.step(AtfGherkinKeyword.THEN, "purchase confirmation is displayed", "/orders/order-1", "",
                        List.of(new AtfAssertion("Confirmation", "displayed", "displayed", true)),
                        List.of(), List.of(evidence), AtfReportStatus.PASS);
                phase.failureEvidence("late E2E failure evidence", "/checkout", List.of(),
                        new AssertionError("failure evidence is published before phase close"));
                phase.complete(AtfReportStatus.FAIL);
                scenario.complete(AtfReportStatus.FAIL);
            }

            String html = Files.readString(generatedReport);
            int summaryPosition = html.indexOf(summaryTitle);
            int failurePosition = html.indexOf("late E2E failure evidence");
            int finalCheckpointPosition = html.indexOf("purchase confirmation is displayed");
            assertTrue(summaryPosition >= 0 && summaryPosition > failurePosition,
                    "The journey summary must follow failure evidence in " + flowMode);
            assertTrue(summaryPosition > finalCheckpointPosition,
                    "The journey summary must follow the final UI/API checkpoint in " + flowMode);
            assertTrue(html.indexOf("API validation") >= 0 && html.indexOf("API validation") < summaryPosition,
                    "API validation remains part of the Gherkin journey in " + flowMode);
            assertEquals(1, html.split(java.util.regex.Pattern.quote(summaryTitle), -1).length - 1,
                    "A UI phase publishes exactly one journey summary in " + flowMode);
            assertTrue(html.contains("API validation"), "API evidence remains attached in " + flowMode);
        }
    }
}
