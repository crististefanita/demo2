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
        journal.recordCapturedExchange(exchange("GET", "/session/status", "{}", "{}"));
        journal.recordCapturedExchange(exchange("GET", "/session/status", "{}", "{}"));
        journal.recordCapturedExchange(exchange("GET", "/products/search?q=Widget",
                "", "{\"data\":[{\"id\":\"p1\",\"name\":\"Widget\"}]}"));
        journal.recordCapturedExchange(exchange("GET", PracticeShopApiRoutes.product("p1"),
                "", "{\"id\":\"p1\",\"name\":\"Widget\"}"));

        journal.productReady("Widget", null);

        var selected = journal.selectedEvidence();
        assertEquals(List.of("E3", "E4"), selected.stream()
                .map(evidence -> evidence.trace().eventId()).toList());
        assertTrue(selected.stream().allMatch(evidence ->
                evidence.request().path().startsWith("/products")));
        assertEquals(List.of("/session/status", "/session/status", "/products/search?q=Widget",
                        "/products/p1"), journal.allEvidence().stream()
                .map(evidence -> evidence.request().path()).toList());
        assertEquals(4, journal.allEvidence().size());
    }

    @Test
    void unboundRequestsAreRetainedButNotPublishedRegardlessOfTheirRoute() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "product"));
        journal.recordCapturedExchange(exchange("GET", "/session/status", "{}", "{}"));
        journal.recordCapturedExchange(exchange("GET", "/session/status", "{}", "{}"));

        assertTrue(journal.selectedEvidence().isEmpty());
        assertEquals(List.of("/session/status", "/session/status"), journal.allEvidence().stream()
                .map(evidence -> evidence.request().path()).toList());
        assertTrue(journal.inspectorEvidence().isEmpty());
    }

    @Test
    void orderEvidenceIsSelectedOnlyAtTheFinalOrderCheckpoint() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "order"));
        journal.recordCapturedExchange(exchange("POST", PracticeShopApiRoutes.GUEST_INVOICE_CREATE, "{}",
                "{\"id\":\"order-1\",\"invoice_number\":\"INV-1\",\"total\":24.02}"));

        var assertions = journal.orderCreated(0L, "Thanks for your order! Invoice INV-1");

        assertTrue(assertions.stream().allMatch(assertion -> assertion.passed()));
        assertEquals(List.of("/invoices/guest"), journal.evidence(PracticeShopApiCheckpoint.ORDER).stream()
                .map(evidence -> evidence.request().path()).toList());
        assertEquals("checkout", journal.evidence(PracticeShopApiCheckpoint.ORDER).getFirst()
                .trace().phase());
    }

    @Test
    void authenticatedOrderRouteIsSelectedByTheSameBusinessOperation() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "order"));
        journal.recordCapturedExchange(exchange("POST", PracticeShopApiRoutes.INVOICE_CREATE, "{}",
                "{\"id\":\"order-2\",\"invoice_number\":\"INV-2\",\"total\":24.02}"));

        var assertions = journal.orderCreated(0L, "Thanks for your order! Invoice INV-2");

        assertTrue(assertions.stream().allMatch(assertion -> assertion.passed()));
        assertEquals(List.of(PracticeShopApiRoutes.INVOICE_CREATE), journal.evidence(PracticeShopApiCheckpoint.ORDER).stream()
                .map(evidence -> evidence.request().path()).toList());
        assertEquals("checkout", journal.evidence(PracticeShopApiCheckpoint.ORDER).getFirst()
                .trace().phase());
    }

    @Test
    void orderEvidenceCannotReuseAnOrderCapturedBeforeTheConfirmFence() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "order"));
        journal.recordCapturedExchange(exchange("POST", PracticeShopApiRoutes.GUEST_INVOICE_CREATE, "{}",
                "{\"id\":\"old\",\"invoice_number\":\"INV-OLD\",\"total\":24.02}"));
        long fence = journal.sequenceMarker();

        var assertions = journal.orderCreated(fence,
                "Thanks for your order! Your invoice number is INV-NEW.");

        assertFalse(assertions.stream().anyMatch(atf.api.reporting.contract.AtfAssertion::passed));
        assertTrue(journal.evidence(PracticeShopApiCheckpoint.ORDER).isEmpty());
    }

    @Test
    void registrationCheckpointOwnsOnlyTheObservedPostAndRedactsIdentityBeforeAnyProjection() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "registration"));
        long fence = journal.sequenceMarker();
        journal.recordCapturedExchange(exchange("api.practicesoftwaretesting.com", "POST", "/users/register",
                "{\"first_name\":\"Ada\",\"last_name\":\"Lovelace\","
                        + "\"email\":\"ada@owned.example\",\"password\":\"Secret!123\","
                        + "\"street\":\"Secret Street\",\"phone\":\"0700000000\"}",
                "{\"id\":\"user-1\",\"first_name\":\"Ada\",\"email\":\"ada@owned.example\"}"));

        var assertions = journal.registrationConfirmed(fence,
                java.net.URI.create("https://api.practicesoftwaretesting.com"));
        var evidence = journal.evidence(PracticeShopApiCheckpoint.REGISTRATION).getFirst();
        String published = evidence.request().bodyText() + evidence.response().body();

        assertTrue(assertions.stream().allMatch(atf.api.reporting.contract.AtfAssertion::passed));
        assertEquals("account-setup", evidence.trace().phase());
        assertEquals("/users/register", evidence.request().path());
        for (String personalValue : List.of("Ada", "Lovelace", "ada@owned.example", "Secret!123",
                "Secret Street", "0700000000")) {
            assertFalse(published.contains(personalValue), "leaked value: " + personalValue);
        }
        assertFalse(journal.allEvidence().getFirst().request().bodyText().contains("Ada"));
    }

    @Test
    void ordinaryApiExchangesRedactIdentityFieldsBeforeEnteringTheJournal() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "order"));
        journal.recordCapturedExchange(exchange("POST", PracticeShopApiRoutes.GUEST_INVOICE_CREATE,
                "{\"firstName\":\"Ada\",\"email\":\"ada.private@example.test\","
                        + "\"address\":{\"city\":\"Hidden City\",\"postalCode\":\"10101\"}}",
                "{\"phone\":\"0700000000\",\"result\":\"accepted\"}"));

        var evidence = journal.allEvidence().getFirst();
        String publishedBodies = evidence.request().bodyText() + evidence.response().body();

        for (String personalValue : List.of("Ada", "ada.private@example.test", "Hidden City",
                "10101", "0700000000")) {
            assertFalse(publishedBodies.contains(personalValue), "exchange leaked value: " + personalValue);
        }
    }

    @Test
    void registrationCheckpointFailsClosedWhenMoreThanOnePostWasObserved() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "registration"));
        long fence = journal.sequenceMarker();
        journal.recordCapturedExchange(exchange("api.practicesoftwaretesting.com", "POST",
                "/users/register", "{}", "{}"));
        journal.recordCapturedExchange(exchange("api.practicesoftwaretesting.com", "POST",
                "/users/register", "{}", "{}"));

        var assertions = journal.registrationConfirmed(fence,
                java.net.URI.create("https://api.practicesoftwaretesting.com"));

        assertFalse(assertions.getFirst().passed());
        assertFalse(assertions.getLast().passed());
        assertEquals(2, journal.evidence(PracticeShopApiCheckpoint.REGISTRATION).size());
    }

    @Test
    void registrationCheckpointIgnoresPostsToOtherObservedHosts() {
        var journal = new PracticeShopApiNetworkJournal(PracticeShopValidationProfile.UI_API,
                PracticeShopApiEvidenceBinding.fromCsv(PracticeShopValidationProfile.UI_API, "registration"));
        long fence = journal.sequenceMarker();
        journal.recordCapturedExchange(exchange("telemetry.test", "POST", "/events", "{}", "{}"));
        journal.recordCapturedExchange(exchange("api.practicesoftwaretesting.com", "POST",
                "/dynamic-registration-route", "{}", "{}"));

        var assertions = journal.registrationConfirmed(fence,
                java.net.URI.create("https://api.practicesoftwaretesting.com/v1"));

        assertTrue(assertions.stream().allMatch(atf.api.reporting.contract.AtfAssertion::passed));
        assertEquals(1, journal.evidence(PracticeShopApiCheckpoint.REGISTRATION).size());
        assertEquals("/dynamic-registration-route", journal.evidence(PracticeShopApiCheckpoint.REGISTRATION)
                .getFirst().request().path());
    }

    @Test
    void genericInspectorAuthClassificationSurvivesTheConsumerEvidenceAdapter() {
        var exchange = exchange("GET", "/orders", "", "{}")
                .withObservedAuthMethod("bearer");

        var evidence = PracticeShopApiEvidenceMapper.toExecution(exchange);

        assertEquals("bearer", evidence.request().authMethod().wireValue());
    }

    @Test
    void observedExchangeUsesReportingQueryDecoderAndPreservesDecodedValues() {
        var exchange = exchange("GET", "/orders?state=ready+to+ship&filter=a%2Bb&empty=", "", "{}")
                .withObservedAuthMethod("none");

        var evidence = PracticeShopApiEvidenceMapper.toExecution(exchange);

        assertEquals("ready to ship", evidence.request().query().get("state"));
        assertEquals("a+b", evidence.request().query().get("filter"));
        assertEquals("", evidence.request().query().get("empty"));
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
        var aggregate = ReportNetworkInspector.aggregate(evidence, List.of());
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

    private static PracticeShopApiNetworkJournal.Exchange exchange(String host, String method, String path,
                                                                    String requestBody, String responseBody) {
        return new PracticeShopApiNetworkJournal.Exchange(method, path, "https://" + host + path, 200,
                requestBody, responseBody, Map.of("accept", "application/json"), JSON);
    }
}
