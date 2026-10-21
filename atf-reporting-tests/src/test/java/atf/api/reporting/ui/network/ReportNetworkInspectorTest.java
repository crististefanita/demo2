package atf.api.reporting.ui.network;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRetryEvent;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTraceMetadata;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.contract.AtfAssertion;
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
        var steps = List.of(
                ReportNetworkInspector.Step.from("GIVEN", "product page is ready",
                        List.of(new AtfAssertion("Product", "visible", "visible", true)),
                        List.of(first, retry)),
                ReportNetworkInspector.Step.from("WHEN", "cart contains the product",
                        List.of(new AtfAssertion("Cart", "quantity=2", "quantity=2", true)),
                        List.of(cart)));

        var fields = ReportNetworkInspector.fields(List.of(first, retry, cart), steps);
        String projection = fields.toString();

        assertAll(
                () -> assertEquals("3 canonical exchanges", fields.get(0).value()),
                () -> assertEquals("2 Gherkin steps; details are shown in Scenario correlation", fields.get(1).value()),
                () -> assertTrue(projection.contains("2 unique request signatures; details are shown once below")),
                () -> assertFalse(projection.contains("network.ui.step.1")),
                () -> assertFalse(projection.contains("GIVEN product page is ready | exchanges=2")),
                () -> assertFalse(projection.contains("network.aggregate")),
                () -> assertFalse(projection.contains("E1")),
                () -> assertFalse(projection.contains("E2")),
                () -> assertFalse(projection.contains("E3")),
                () -> assertFalse(projection.contains("Pliers")),
                () -> assertFalse(projection.contains("product_id")));
    }

    @Test
    void handlesEmptyJourneyAsAnExplicitInspectableProjection() {
        var fields = ReportNetworkInspector.fields(List.of());

        assertAll(
                () -> assertEquals("0 canonical exchanges", fields.get(0).value()),
                () -> assertEquals("0 Gherkin steps; details are shown in Scenario correlation", fields.get(1).value()),
                () -> assertEquals(3, fields.size()));
    }

    @Test
    void keepsCausalAttemptAndAuthFactsInTheCompactProjectionWithoutCredentials() {
        var request = new ReportRequest("POST", "/payment/check", Map.of(), Map.of(),
                "bearer", "{\"amount\":42}");
        var response = new ReportResponse(200, "{\"accepted\":true}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(8),
                "https://api.example.test/payment/check");
        var trace = ReportTraceMetadata.from(request).withCapture("E4", 4, "checkout",
                "ActionCaused", "checkout-payment", "UI_CLICK", "UI_BROWSER",
                "response-digest", "accepted", "accepted");
        var evidence = new ReportExecutionEvidence(response, 2, Duration.ofMillis(12),
                List.of(new ReportRetryEvent(1, 503, "Retry-After", Duration.ofMillis(4), "transient")),
                List.of(new ReportAttemptRecord(1, 503, Duration.ofMillis(4), "RETRY", "transient"),
                        new ReportAttemptRecord(2, 200, Duration.ofMillis(8), "SUCCESS", "final response")),
                request, ReportTransportProvenance.LIVE_HTTP, trace);

        String projection = ReportNetworkInspector.fields(List.of(evidence)).toString();

        assertAll(
                () -> assertTrue(projection.contains("1 canonical exchanges")),
                () -> assertFalse(projection.contains("attempts=2")),
                () -> assertFalse(projection.contains("retries=1")),
                () -> assertFalse(projection.contains("sequence=4")),
                () -> assertFalse(projection.contains("step=checkout-payment")),
                () -> assertFalse(projection.contains("auth=bearer")),
                () -> assertFalse(projection.contains("network.aggregate")),
                () -> assertFalse(projection.contains("amount")),
                () -> assertFalse(projection.contains("accepted")));
    }

    @Test
    void exposesTypedAggregateFactsAndDeduplicatesCrossSourceDiagnostics() {
        var response = evidence("E5", 5, "checkout", "GET", "/users/me", 401,
                "", "{\"message\":\"Unauthorized\"}");
        var console = new ReportNetworkInspector.Diagnostic(
                "console", "GET", "/users/me", 401, "Unauthorized bootstrap");
        var network = new ReportNetworkInspector.Diagnostic(
                "response", "GET", "/users/me", 401, "HTTP 401");
        var aggregate = ReportNetworkInspector.aggregate(List.of(response), List.of(
                ReportNetworkInspector.Step.from("WHEN", "guest bootstrap is observed",
                        List.of(new AtfAssertion("Guest branch", "401", "401", true)), List.of(response))),
                List.of(network, console));

        String projection = ReportNetworkInspector.fields(List.of(response), List.of(),
                List.of(network, console)).toString();
        assertAll(
                () -> assertEquals(1, aggregate.authentication().size()),
                () -> assertEquals(1, aggregate.signatures().size()),
                () -> assertEquals("api.example.test", aggregate.signatures().get(0).service()),
                () -> assertEquals("browser network / Playwright",
                        aggregate.signatures().get(0).mechanism()),
                () -> assertEquals(1, aggregate.diagnostics().size()),
                () -> assertEquals(2, aggregate.diagnostics().get(0).count()),
                () -> assertEquals(List.of("response", "console"),
                        aggregate.diagnostics().get(0).sources()),
                () -> assertFalse(projection.contains("network.aggregate")),
                () -> assertFalse(projection.contains("Unauthorized bootstrap")),
                () -> assertFalse(projection.contains("Unauthorized")));
    }

    @Test
    void rendersAggregateAsNestedSectionsWithoutFlatTechnicalKeys() {
        var exchange = evidence("E6", 6, "catalog", "GET", "/products/1", 200,
                "", "{\"id\":1}");
        var aggregate = ReportNetworkInspector.aggregate(List.of(exchange), List.of(
                ReportNetworkInspector.Step.from("THEN", "product is visible",
                        List.of(new AtfAssertion("Product", "visible", "visible", true)),
                        List.of(exchange))), List.of());

        String html = ReportNetworkAggregateMarkup.disclosure(aggregate);

        assertAll(
                () -> assertTrue(html.contains("Network aggregate · 1 exchanges")),
                () -> assertTrue(html.contains("Authentication evidence · 1")),
                () -> assertTrue(html.contains("Scenario correlation · 1")),
                () -> assertTrue(html.contains("Transport counters · 1")),
                () -> assertTrue(html.contains("Unique request signatures · 1")),
                () -> assertTrue(html.contains("Technical diagnostics · 0")),
                () -> assertTrue(html.contains("network-aggregate-sections")),
                () -> assertTrue(html.contains("<th class=\"status-col\">Status</th><th class=\"details-col\">Details</th>")),
                () -> assertEquals(5, html.split("network-aggregate-section-row", -1).length - 1),
                () -> assertEquals(5, html.split("status-info", -1).length - 1),
                () -> assertTrue(html.contains("network-signature-card")),
                () -> assertTrue(html.contains("network-signatures")),
                () -> assertTrue(html.contains("network-diagnostics")),
                () -> assertFalse(html.contains("network.aggregate.authentication")),
                () -> assertFalse(html.contains("<details open")));
    }

    @Test
    void keepsDistinctUnboundDiagnosticsSeparateAndCountsOnlyTrueRepeats() {
        var first = new ReportNetworkInspector.Diagnostic("console", "", "", -1,
                "first page signal");
        var firstRepeat = new ReportNetworkInspector.Diagnostic("console", "", "", -1,
                "first page signal");
        var second = new ReportNetworkInspector.Diagnostic("page", "", "", -1,
                "second page signal");

        var aggregate = ReportNetworkInspector.aggregate(List.of(), List.of(),
                List.of(first, firstRepeat, second));
        String html = ReportNetworkAggregateMarkup.disclosure(aggregate);

        assertAll(
                () -> assertEquals(2, aggregate.diagnostics().size()),
                () -> assertEquals(List.of(2, 1), aggregate.diagnostics().stream()
                        .map(ReportNetworkInspector.DiagnosticObservation::count).toList()),
                () -> assertEquals(List.of("UNBOUND-1", "UNBOUND-2"), aggregate.diagnostics().stream()
                        .map(ReportNetworkInspector.DiagnosticObservation::signal).toList()),
                () -> assertTrue(ReportNetworkInspector.fields(List.of(), List.of(),
                        List.of(first, firstRepeat, second)).stream()
                        .noneMatch(field -> field.name().startsWith("network.aggregate."))),
                () -> assertTrue(html.contains("UNBOUND-1")),
                () -> assertTrue(html.contains("UNBOUND-2")),
                () -> assertTrue(html.contains("Unbound browser diagnostic")),
                () -> assertFalse(html.contains("<td></td>")),
                () -> assertFalse(html.contains("<td>-1</td>")),
                () -> assertFalse(html.contains("first page signal")),
                () -> assertFalse(html.contains("second page signal")));
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
