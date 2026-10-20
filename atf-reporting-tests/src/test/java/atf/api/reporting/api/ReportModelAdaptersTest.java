package atf.api.reporting.api;

import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.support.ApiReportTestUrls;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportModelAdaptersTest {
    @Test
    void adaptsLegacyExecutionWithoutDroppingTypedEvidence() {
        var request = new LegacyRequest("POST", "/users", Map.of("expand", "roles"),
                Map.of("Content-Type", "application/json"), "bearer", "{\"name\":\"ATF\"}");
        var response = new LegacyResponse(201, "{\"id\":1}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(12),
                ApiReportTestUrls.atf(ApiReportTestUrls.GENERIC_USERS));
        var execution = new LegacyExecution(response, 2, Duration.ofMillis(31),
                List.of(new LegacyRetry(1, 429, "2", Duration.ofMillis(5), "rate limited")),
                List.of(new LegacyAttempt(1, 429, Duration.ofMillis(7), "RETRY", "rate limited"),
                        new LegacyAttempt(2, 201, Duration.ofMillis(12), "PASS", "")),
                request, "LIVE_HTTP");

        ReportExecutionEvidence adapted = ReportModelAdapters.execution(execution);

        assertEquals(201, adapted.response().status());
        assertEquals("POST", adapted.request().method());
        assertEquals("{\"name\":\"ATF\"}", adapted.request().bodyText());
        assertEquals(ReportTransportProvenance.LIVE_HTTP, adapted.provenance());
        assertEquals(2, adapted.attempts());
        assertEquals(1, adapted.retryEvents().size());
        assertEquals(2, adapted.attemptHistory().size());
    }

    @Test
    void preservesTypedReportingModelsWithoutReflection() {
        var request = new ReportRequest("GET", "/users", Map.of(), Map.of(), "none", "");
        assertSame(request, ReportModelAdapters.request(request));
        assertEquals(ReportTransportProvenance.ACCEPTANCE_MOCK,
                ReportModelAdapters.provenance(ReportTransportProvenance.ACCEPTANCE_MOCK));
    }

    @Test
    void missingRequiredFieldFailsWithSourceAndField() {
        ReportModelAdapterException error = assertThrows(ReportModelAdapterException.class,
                () -> ReportModelAdapters.request(new MissingMethodRequest("/users")));

        assertEquals("method", error.field());
        assertTrue(error.getMessage().contains("MissingMethodRequest"));
        assertTrue(error.getMessage().contains("required"));
    }

    @Test
    void invalidMapValueFailsInsteadOfBeingStringified() {
        ReportModelAdapterException error = assertThrows(ReportModelAdapterException.class,
                () -> ReportModelAdapters.request(new LegacyRequest("GET", "/users",
                        Map.of("page", 1), Map.of(), "none", "")));

        assertEquals("query", error.field());
        assertTrue(error.getMessage().contains("non-text value"));
    }

    @Test
    void unknownProvenanceFailsInsteadOfBeingGuessedBySubstring() {
        ReportModelAdapterException error = assertThrows(ReportModelAdapterException.class,
                () -> ReportModelAdapters.provenance("LIVE_HTTP_COMPAT"));

        assertEquals("provenance", error.field());
        assertTrue(error.getMessage().contains("unsupported value"));
    }

    @Test
    void invalidRetryContainerFailsWithTheResponsibleField() {
        var response = new LegacyResponse(200, "{}", Map.of(), Duration.ZERO,
                ApiReportTestUrls.atf(ApiReportTestUrls.GENERIC_USERS));
        var execution = new LegacyExecution(response, 1, Duration.ZERO, "not-a-list",
                List.of(), null, null);

        ReportModelAdapterException error = assertThrows(ReportModelAdapterException.class,
                () -> ReportModelAdapters.execution(execution));

        assertEquals("retryEvents", error.field());
        assertTrue(error.getMessage().contains("Iterable"));
    }

    @Test
    void queryAdapterAcceptsMapsAndRejectsUnsupportedShapes() {
        assertEquals("details", ReportQueryParameters.from(Map.of("include", "details"))
                .asMap().get("include"));

        ReportModelAdapterException error = assertThrows(ReportModelAdapterException.class,
                () -> ReportQueryParameters.from(new Object()));

        assertEquals("asMap", error.field());
        assertTrue(error.getMessage().contains("method is missing"));
    }

    @Test
    void queryAdapterDoesNotStringifyNonTextValues() {
        ReportModelAdapterException error = assertThrows(ReportModelAdapterException.class,
                () -> ReportQueryParameters.from(Map.of("page", 1)));

        assertEquals("queryParameters", error.field());
        assertTrue(error.getMessage().contains("non-text value"));
    }

    record LegacyRequest(String method, String path, Object query, Object headers,
                         String authProfile, String bodyText) {
    }

    record LegacyResponse(int status, String body, Object headers, Duration duration, String url) {
    }

    record LegacyRetry(int attempt, int statusCode, String header, Duration delay, String reason) {
    }

    record LegacyAttempt(int number, int statusCode, Duration duration, String outcome, String reason) {
    }

    record LegacyExecution(LegacyResponse response, int attempts, Duration totalDuration,
                           Object retryEvents, Object attemptHistory, Object request,
                           Object provenance) {
    }

    record MissingMethodRequest(String path) {
    }
}
