package atf.api.reporting.ui;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.api.ReportTraceMetadata;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the compact journey projection against body duplication and ordering drift. */
@AtfUnitTest
class ReportNetworkInspectorTest {
    @Test
    void projectsOrderedExchangesAndUniqueSignaturesWithoutJsonBodies() {
        var first = evidence("E1", 1, "catalog", "GET", "/products", 200,
                "{\"id\":1}", "{\"name\":\"Pliers\"}");
        var retry = evidence("E2", 2, "catalog", "GET", "/products", 200,
                "{\"id\":1}", "{\"name\":\"Pliers\"}");
        var cart = evidence("E3", 3, "cart", "POST", "/carts", 200,
                "{\"product_id\":1,\"quantity\":2}", "{\"id\":\"cart-1\"}");

        var fields = ReportNetworkInspector.fields(List.of(first, retry, cart));
        String projection = fields.toString();

        assertAll(
                () -> assertEquals("3", fields.get(0).value()),
                () -> assertEquals("2", fields.get(1).value()),
                () -> assertTrue(projection.indexOf("E1") < projection.indexOf("E2")),
                () -> assertTrue(projection.indexOf("E2") < projection.indexOf("E3")),
                () -> assertTrue(projection.contains("GET /products → 200")),
                () -> assertTrue(projection.contains("POST /carts → 200")),
                () -> assertFalse(projection.contains("Pliers")),
                () -> assertFalse(projection.contains("product_id")));
    }

    @Test
    void handlesEmptyJourneyAsAnExplicitInspectableProjection() {
        var fields = ReportNetworkInspector.fields(List.of());

        assertAll(
                () -> assertEquals("0", fields.get(0).value()),
                () -> assertEquals("0", fields.get(1).value()),
                () -> assertEquals(2, fields.size()));
    }

    private static ReportExecutionEvidence evidence(String eventId, long sequence, String phase,
                                                    String method, String path, int status,
                                                    String requestBody, String responseBody) {
        var request = new ReportRequest(method, path, Map.of(),
                Map.of("Content-Type", "application/json"), "none", requestBody);
        var response = new ReportResponse(status, responseBody,
                Map.of("Content-Type", "application/json"), Duration.ofMillis(4),
                "https://api.example.test" + path);
        var trace = ReportTraceMetadata.from(request).withCapture(eventId, sequence, phase,
                "ActionCaused", phase, "UI action", "UI_BROWSER", "digest", "expected", "observed");
        return new ReportExecutionEvidence(response, 1, Duration.ofMillis(4), List.of(),
                List.of(new ReportAttemptRecord(1, status, Duration.ofMillis(4), "SUCCESS", "final response")),
                request, ReportTransportProvenance.LIVE_HTTP, trace);
    }
}
