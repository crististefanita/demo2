package atf.ui.tests.practiceshop.api.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.support.PracticeShopApiExchangeSelector;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopApiExchangeSelectorTest {
    @Test
    void latestSelectsTheMostRecentMatchingExchangeWithoutChangingCaptureOrder() {
        var first = exchange("GET", PracticeShopApiRoutes.cart("cart-1"), 200, "first");
        var second = exchange("GET", PracticeShopApiRoutes.cart("cart-1"), 200, "second");

        assertEquals(second, PracticeShopApiExchangeSelector.latest(
                List.of(first, second), "GET", PracticeShopApiRoutes.cart("cart-1"), item -> item.status() == 200));
    }

    @Test
    void optionalSelectionDoesNotInventAnExchangeWhenThePredicateDoesNotMatch() {
        var exchange = exchange("GET", PracticeShopApiRoutes.CURRENT_USER, 401, "unauthorized");

        assertNull(PracticeShopApiExchangeSelector.latestOptional(
                List.of(exchange), "GET", PracticeShopApiRoutes.CURRENT_USER, item -> item.status() == 200));
        assertThrows(IllegalStateException.class, () -> PracticeShopApiExchangeSelector.latest(
                List.of(exchange), "GET", PracticeShopApiRoutes.CURRENT_USER, item -> item.status() == 200));
    }

    @Test
    void productIdExtractionIgnoresQueryAndFragmentButRejectsNonProductRoutes() {
        assertEquals("product-42", PracticeShopApiExchangeSelector.productIdFromPath(
                PracticeShopApiRoutes.product("product-42") + "?source=search#details"));
        assertNull(PracticeShopApiExchangeSelector.productIdFromPath(PracticeShopApiRoutes.PRODUCTS));
        assertNull(PracticeShopApiExchangeSelector.productIdFromPath(null));
    }

    private static PracticeShopApiNetworkJournal.Exchange exchange(
            String method, String path, int status, String body) {
        return new PracticeShopApiNetworkJournal.Exchange(
                method, path, "https://api.practicesoftwaretesting.com" + path, status,
                body, body, Map.of(), Map.of("content-type", "application/json"));
    }
}
