package atf.ui.tests.practiceshop.component;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.transport.ApiTransport;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.support.PracticeShopRuntimeDiagnostics;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps expected guest authentication failures separate from browser runtime failures. */
@AtfComponentTest
class PracticeShopGuestResilienceTest {
    @Test
    void guestUser401IsNotAJavaScriptFailure() {
        ApiTransport guestTransport = (request, url, authProfile) -> java.util.concurrent.CompletableFuture
                .completedFuture(new ApiResponse(401, "{\"message\":\"Unauthorized\"}",
                        Map.of("content-type", "application/json"), Duration.ZERO, url));
        ApiExecutionResult guestUser = PracticeShopApiClient.forTransport(
                URI.create("https://api.practicesoftwaretesting.com"), guestTransport).currentUser();

        assertEquals(401, guestUser.response().status());
        // The browser diagnostic channel is deliberately independent of API status.
        PracticeShopRuntimeDiagnostics diagnostics = new PracticeShopRuntimeDiagnostics();
        assertEquals(List.of(), diagnostics.errors());
        assertTrue(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                "console: Failed to load resource: the server responded with a status of 401 ()"));
        assertTrue(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                "console: ERROR TypeError: Cannot read properties of null (reading 'cart_items')"));
        assertFalse(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                "console: ERROR TypeError: Cannot read properties of null (reading 'profile')"));
    }
}
