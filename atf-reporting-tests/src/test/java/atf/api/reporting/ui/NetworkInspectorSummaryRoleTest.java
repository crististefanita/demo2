package atf.api.reporting.ui;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class NetworkInspectorSummaryRoleTest {
    @Test
    void marksTheNetworkInspectorAsASummaryRatherThanAnAdditionalBusinessStep() throws Exception {
        Path root = Files.createTempDirectory("atf-network-summary-");
        var request = new ReportRequest("GET", "/products/1", Map.of(), Map.of(), "none", "");
        var response = new ReportResponse(200, "{}", Map.of(), Duration.ofMillis(1),
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
        assertTrue(Files.readString(generatedReport)
                .contains("journey summary; not an additional business step"));
    }
}
