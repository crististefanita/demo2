package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.request.HttpMethod;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiOperation;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.support.PracticeShopApiExchangeSelector;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiExchangeSelectorTest {
    @Test
    void latestSelectsTheMostRecentMatchingExchangeWithoutChangingCaptureOrder() {
        String cartPath = PracticeShopApiRoutes.cart(PracticeShopApiScenarioFixture.MUTATION_CART_ID);
        var first = exchange(HttpMethod.GET.value(), cartPath, PracticeShopApiHttpStatus.OK, "first");
        var second = exchange(HttpMethod.GET.value(), cartPath, PracticeShopApiHttpStatus.OK, "second");

        assertEquals(second, PracticeShopApiExchangeSelector.latest(
                List.of(first, second), HttpMethod.GET.value(), cartPath,
                item -> PracticeShopApiHttpStatus.OK.matches(item.status())));
    }

    @Test
    void operationSelectionAcceptsObservedQueryAliasForCatalogSearch() {
        var observed = exchange("QUERY", PracticeShopApiRoutes.PRODUCT_SEARCH,
                PracticeShopApiHttpStatus.OK, "search-result");

        assertEquals(observed, PracticeShopApiExchangeSelector.latest(
                List.of(observed), PracticeShopApiOperation.SEARCH, item -> true));
        assertEquals("GET /products/search → 200 application/json (observed method=QUERY)",
                observed.reportSummary());
    }

    @Test
    void selectionUsesTheNormalizedRouteInsteadOfAContainsMatch() {
        var misleading = exchange(HttpMethod.GET.value(), PracticeShopApiRoutes.product(
                PracticeShopApiScenarioFixture.PRODUCT_ID) + "-archive", PracticeShopApiHttpStatus.OK, "wrong");
        assertThrows(IllegalStateException.class, () -> PracticeShopApiExchangeSelector.latest(
                List.of(misleading), HttpMethod.GET.value(),
                PracticeShopApiRoutes.product(PracticeShopApiScenarioFixture.PRODUCT_ID), item -> true));
    }

    @Test
    void tracedExchangesKeepSequencePhaseSignatureAndDigest() {
        var traced = exchange(HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK,
                PracticeShopApiHttpStatus.OK, "payment")
                .withTrace(7, Instant.parse("2026-10-20T10:00:00Z"), "checkout");

        assertEquals(7, traced.sequence());
        assertEquals("checkout", traced.phase());
        assertFalse(traced.signature().isBlank());
        assertEquals(64, traced.digest().length());
    }

    @Test
    void requestSignatureIsStableForRetriesButChangesWithTheRequestBody() {
        var first = exchange(HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK,
                PracticeShopApiHttpStatus.OK, "{\"payment_method\":\"cash-on-delivery\"}")
                .withTrace(1, Instant.parse("2026-10-20T10:00:00Z"), "checkout");
        var retry = exchange(HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK,
                PracticeShopApiHttpStatus.OK, "{\"payment_method\":\"cash-on-delivery\"}")
                .withTrace(2, Instant.parse("2026-10-20T10:00:01Z"), "checkout");
        var differentBody = exchange(HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK,
                PracticeShopApiHttpStatus.OK, "{\"payment_method\":\"bank-transfer\"}")
                .withTrace(3, Instant.parse("2026-10-20T10:00:02Z"), "checkout");

        assertEquals(first.signature(), retry.signature());
        assertFalse(first.signature().isBlank());
        assertFalse(first.signature().equals(differentBody.signature()));
    }

    @Test
    void productIdExtractionIgnoresQueryAndFragmentButRejectsNonProductRoutes() {
        assertEquals("product-42", PracticeShopApiExchangeSelector.productIdFromPath(
                PracticeShopApiRoutes.product("product-42") + "?source=search#details"));
        assertNull(PracticeShopApiExchangeSelector.productIdFromPath(PracticeShopApiRoutes.PRODUCTS));
        assertNull(PracticeShopApiExchangeSelector.productIdFromPath(null));
    }

    private static PracticeShopApiNetworkJournal.Exchange exchange(
            String method, String path, PracticeShopApiHttpStatus status, String body) {
        return new PracticeShopApiNetworkJournal.Exchange(
                method, path, PracticeShopApiScenarioFixture.API_ENDPOINT + path, status.code(),
                body, body, Map.of(), Map.of("content-type", "application/json"));
    }
}
