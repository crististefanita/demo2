package atf.api.reporting.evidence;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTraceMetadata;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ReportTraceMetadataRenderingTest {
    @Test
    void rendersCausalTraceInsideExecutionEvidence() throws Exception {
        Path report = Files.createTempDirectory("atf-trace-render-").resolve("trace.html");
        var request = new ReportRequest("POST", "/carts/cart-1", Map.of(), Map.of(),
                "bearer", "{\"quantity\":2}");
        var response = new ReportResponse(200, "{\"ok\":true}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(4),
                "https://api.example.test/carts/cart-1");
        var trace = new ReportTraceMetadata("E7", 7, "UI_ACTION", "ActionCaused",
                "cart-checkpoint", "Click add to cart", "POST /carts/cart-1",
                "POST /carts/cart-1#body=body-digest", "request-digest", "response-digest",
                "UI_BROWSER", "HTTP 2xx + JSON", "status=200");
        var execution = new ReportExecutionEvidence(response, 1, Duration.ofMillis(4), List.of(),
                List.of(new ReportAttemptRecord(1, 200, Duration.ofMillis(4), "SUCCESS", "final")),
                request, ReportTransportProvenance.LIVE_HTTP, trace);

        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("TraceTests", "trace metadata", List.of("api"));
            var phase = scenario.startPhase("TEST BODY");
            var node = phase.startRequest("cart request", "POST", "/carts/cart-1");
            node.execution(execution);
            node.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("REQUEST SIGNATURE")),
                () -> assertTrue(html.contains("EVENT ID")),
                () -> assertTrue(html.contains("E7")),
                () -> assertTrue(html.contains("ActionCaused")),
                () -> assertTrue(html.contains("cart-checkpoint")),
                () -> assertTrue(html.contains("response-digest")));
    }
}
