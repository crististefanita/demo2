package atf.ui.practiceshop.support;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportRetryEvent;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.response.ApiAttemptRecord;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps one observed API exchange into the reporting contract. */
public final class PracticeShopApiEvidenceMapper {
    private PracticeShopApiEvidenceMapper() { }

    static ReportExecutionEvidence toExecution(PracticeShopApiNetworkJournal.Exchange exchange) {
        ReportRequest request = new ReportRequest(exchange.method(), exchange.path(),
                query(exchange.url()), PracticeShopJsonEvidence.redactHeaders(exchange.requestHeaders()),
                exchange.authType(), redactJson(exchange.requestBody()));
        ReportResponse response = new ReportResponse(exchange.status(), redactJson(exchange.responseBody()),
                PracticeShopJsonEvidence.redactHeaders(exchange.responseHeaders()), Duration.ZERO, exchange.url());
        var attempt = new ReportAttemptRecord(1, exchange.status(), Duration.ZERO,
                exchange.status() >= 200 && exchange.status() < 400 ? "SUCCESS" : "FAILURE",
                "final response");
        return new ReportExecutionEvidence(response, 1, Duration.ZERO, List.of(), List.of(attempt),
                request, ReportTransportProvenance.LIVE_HTTP);
    }

    /** Maps a direct screen-flow execution for the standalone API artifact. */
    public static ReportExecutionEvidence toExecution(ApiExecutionResult execution) {
        if (execution == null) {
            throw new IllegalArgumentException("API execution must contain request and response");
        }
        var request = execution.request();
        var response = execution.response();
        if (request == null || response == null) {
            throw new IllegalArgumentException("API execution must contain request and response");
        }
        ReportRequest reportRequest = new ReportRequest(request.method(), request.path(), request.query(),
                PracticeShopJsonEvidence.redactHeaders(request.headers()), request.authProfile(),
                redactJson(request.bodyText()));
        ReportResponse reportResponse = new ReportResponse(response.status(), redactJson(response.body()),
                PracticeShopJsonEvidence.redactHeaders(response.headers()), response.duration(), response.url());
        List<ReportAttemptRecord> attempts = execution.attemptHistory().stream()
                .map(PracticeShopApiEvidenceMapper::attempt)
                .toList();
        if (attempts.isEmpty()) {
            attempts = List.of(new ReportAttemptRecord(1, response.status(), response.duration(),
                    response.status() >= 200 && response.status() < 400 ? "SUCCESS" : "FAILURE",
                    "final response"));
        }
        List<ReportRetryEvent> retries = execution.retryEvents().stream()
                .map(retry -> new ReportRetryEvent(retry.attempt(), retry.statusCode(), retry.header(),
                        retry.delay(), retry.reason()))
                .toList();
        return new ReportExecutionEvidence(reportResponse, execution.attempts(), execution.totalDuration(),
                retries, attempts, reportRequest, provenance(execution));
    }

    private static ReportAttemptRecord attempt(ApiAttemptRecord attempt) {
        return new ReportAttemptRecord(attempt.number(), attempt.statusCode(), attempt.duration(),
                attempt.outcome().name(), attempt.reason());
    }

    private static ReportTransportProvenance provenance(ApiExecutionResult execution) {
        return switch (execution.provenance()) {
            case ACCEPTANCE_MOCK -> ReportTransportProvenance.ACCEPTANCE_MOCK;
            case LIVE_HTTP -> ReportTransportProvenance.LIVE_HTTP;
            case UNKNOWN -> ReportTransportProvenance.UNKNOWN;
        };
    }

    static Map<String, String> redactHeaders(Map<String, String> headers) {
        return PracticeShopJsonEvidence.redactHeaders(headers);
    }

    public static String redactJson(String value) {
        return PracticeShopJsonEvidence.redact(value);
    }

    private static Map<String, String> query(String url) {
        String raw = URI.create(url).getRawQuery();
        if (raw == null || raw.isBlank()) return Map.of();
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : raw.split("&")) {
            String[] pair = part.split("=", 2);
            String name = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length == 1 ? "" : URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            values.put(name, value);
        }
        return Map.copyOf(values);
    }
}
