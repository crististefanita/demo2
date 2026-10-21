package atf.api.reporting.api;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component contract for compact, safe replay facts shared by API and UI traces. */
@AtfUnitTest
class ApiManualReplayProjectionTest {
    @Test
    void keepsSemanticHeadersAndRemovesBrowserTransportNoise() {
        var headers = new LinkedHashMap<String, String>();
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("Accept", "application/json");
        headers.put("content-type", "application/json");
        headers.put("sec-ch-ua", "browser fingerprint");
        headers.put("User-Agent", "browser fingerprint");
        headers.put("Referer", "https://example.test");
        headers.put("X-Correlation-Id", "corr-001");
        headers.put("Authorization", "Bearer real-secret");
        var request = new ReportRequest("POST", "/checkout", Map.of(), headers,
                "bearer", "{\"ok\":true}");

        String visibleHeaders = ApiManualReplay.redactedHeaders(request);
        String replay = ApiManualReplay.describe(ReportTransportProvenance.LIVE_HTTP,
                request, "https://api.example.test/checkout", request.bodyText());

        assertAll(
                () -> assertTrue(visibleHeaders.contains("Accept=application/json")),
                () -> assertTrue(visibleHeaders.contains("Content-Type=application/json")),
                () -> assertTrue(visibleHeaders.indexOf("Accept=")
                        < visibleHeaders.indexOf("Content-Type=")),
                () -> assertTrue(visibleHeaders.contains("X-Correlation-Id=corr-001")),
                () -> assertTrue(visibleHeaders.contains("Authorization=***REDACTED***")),
                () -> assertFalse(visibleHeaders.contains("sec-ch-ua")),
                () -> assertFalse(visibleHeaders.contains("User-Agent")),
                () -> assertFalse(visibleHeaders.contains("Referer")),
                () -> assertFalse(replay.contains("real-secret")),
                () -> assertFalse(replay.contains("sec-ch-ua")),
                () -> assertTrue(replay.contains("--data-raw")));
    }

    @Test
    void keepsOnlyStableResponseHeadersForCompactProjections() {
        String headers = ApiManualReplay.relevantResponseHeaders(Map.of(
                "Content-Type", "application/json",
                "Content-Length", "812",
                "Date", "Wed, 21 Oct 2026 06:03:41 GMT",
                "Access-Control-Allow-Origin", "*",
                "X-Request-Id", "req-123"));

        assertAll(
                () -> assertTrue(headers.contains("X-Request-Id=req-123")),
                () -> assertFalse(headers.contains("Content-Type")),
                () -> assertFalse(headers.contains("Content-Length")),
                () -> assertFalse(headers.contains("Access-Control-Allow-Origin")));
    }
}
