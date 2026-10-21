package atf.ui.practiceshop.support;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopApiEvidenceBinding;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    private static PracticeShopApiNetworkJournal.Exchange exchange(String method, String path,
                                                                    String requestBody, String responseBody) {
        return new PracticeShopApiNetworkJournal.Exchange(method, path, "https://shop.test" + path, 200,
                requestBody, responseBody, Map.of("accept", "application/json"), JSON);
    }
}
