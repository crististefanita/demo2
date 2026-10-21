package atf.ui.practiceshop.support;

import atf.api.testkit.AtfComponentTest;
import atf.api.reporting.ui.network.ReportNetworkAggregateMarkup;
import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.ui.config.UiNetworkInspectionConfiguration;
import atf.api.ui.network.UiNetworkInspector;
import atf.api.ui.network.UiNetworkObservation;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopApiEvidenceBinding;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiNetworkJournalTest {
    private static final Map<String, String> JSON = Map.of("content-type", "application/json");

    @Test
    void selectedEvidencePublishesOnlyConfiguredAndValidatedCheckpointsInCaptureOrder() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "product"));
        journal.recordCapturedExchange(exchange("GET", "/users/me", "{}", "{}"));
        journal.recordCapturedExchange(exchange("GET", "/products/search?q=Widget",
                "", "{\"data\":[{\"id\":\"p1\",\"name\":\"Widget\"}]}"));
        journal.recordCapturedExchange(exchange("GET", PracticeShopApiRoutes.product("p1"),
                "", "{\"id\":\"p1\",\"name\":\"Widget\"}"));

        journal.productReady("Widget", null);

        var selected = journal.selectedEvidence();
        assertEquals(List.of("E2", "E3"), selected.stream()
                .map(evidence -> evidence.trace().eventId()).toList());
        assertTrue(selected.stream().allMatch(evidence ->
                evidence.request().path().startsWith("/products")));
        assertEquals(3, journal.allEvidence().size());
        assertFalse(selected.stream().anyMatch(evidence -> "/users/me".equals(evidence.request().path())));
    }

    @Test
    void selectedEvidenceIsEmptyUntilAConfiguredCheckpointProducesEvidence() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "product"));
        journal.recordCapturedExchange(exchange("GET", "/users/me", "{}", "{}"));

        assertTrue(journal.selectedEvidence().isEmpty());
    }

    @Test
    void genericInspectorAuthClassificationSurvivesTheConsumerEvidenceAdapter() {
        var exchange = exchange("GET", "/orders", "", "{}")
                .withObservedAuthMethod("bearer");

        var evidence = PracticeShopApiEvidenceMapper.toExecution(exchange);

        assertEquals("bearer", evidence.request().authMethod().wireValue());
    }

    @Test
    void consumerServiceAliasIsInjectedAtTheAdapterBoundaryAndRetainedInTrace() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "product"),
                host -> "shop.test".equals(host) ? "Practice Shop API" : host);
        journal.recordCapturedExchange(exchange("GET", "/products/search?q=Widget",
                "", "{\"data\":[{\"id\":\"p1\",\"name\":\"Widget\"}]}") );
        journal.recordCapturedExchange(exchange("GET", PracticeShopApiRoutes.product("p1"),
                "", "{\"id\":\"p1\",\"name\":\"Widget\"}"));

        journal.productReady("Widget", null);

        assertEquals("Practice Shop API", journal.selectedEvidence().get(0).trace().service());
    }

    @Test
    void requestFailureSurvivesJournalAndRendersAsUnavailableDiagnostic() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "product"));
        var inspector = new UiNetworkInspector(UiNetworkInspectionConfiguration.forHosts(
                List.of("shop.test")));
        journal.attach(inspector);
        inspector.capture(new UiNetworkObservation(1, Instant.EPOCH, "GET",
                "https://shop.test/products/search", "/products/search", -1,
                Map.of(), Map.of(), "", "<unavailable: request failed before response>",
                "none", "none", "shop.test", "UI_BROWSER_REQUEST_FAILED",
                "GET /products/search#body=empty", false, "net::ERR_CONNECTION_RESET"));

        assertTrue(journal.selectedEvidence().isEmpty());
        var evidence = journal.inspectorEvidence();
        assertEquals(1, evidence.size());
        assertEquals("Unavailable", evidence.get(0).response().statusLabel());
        assertEquals("Unavailable", evidence.get(0).attemptHistory().get(0).statusLabel());
        assertEquals("FAILURE", evidence.get(0).attemptHistory().get(0).outcome());
        assertEquals("net::ERR_CONNECTION_RESET", evidence.get(0).attemptHistory().get(0).reason());
        assertEquals("UI_BROWSER_REQUEST_FAILED", evidence.get(0).trace().evidenceSource());
        var aggregate = ReportNetworkInspector.aggregate(evidence, List.of(), List.of());
        assertEquals(1, aggregate.transport().failed());
        assertEquals("net::ERR_CONNECTION_RESET", aggregate.diagnostics().get(0).signal());
        String markup = ReportNetworkAggregateMarkup.disclosure(aggregate);
        assertTrue(markup.contains("Status unavailable"));
        assertTrue(markup.contains("net::ERR_CONNECTION_RESET"));
    }

    private static PracticeShopApiNetworkJournal.Exchange exchange(String method, String path,
                                                                    String requestBody, String responseBody) {
        return new PracticeShopApiNetworkJournal.Exchange(method, path, "https://shop.test" + path, 200,
                requestBody, responseBody, Map.of("accept", "application/json"), JSON);
    }
}
