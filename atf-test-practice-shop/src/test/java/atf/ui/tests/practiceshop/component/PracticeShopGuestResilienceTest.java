package atf.ui.tests.practiceshop.component;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.MediaTypes;
import atf.api.core.http.transport.ApiTransport;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.support.PracticeShopRuntimeDiagnostics;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import org.junit.jupiter.api.Test;

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
        var unauthorized = PracticeShopApiHttpStatus.UNAUTHORIZED;
        int unauthorizedCode = unauthorized.code();
        ApiTransport guestTransport = (request, url, authProfile) -> java.util.concurrent.CompletableFuture
                .completedFuture(new ApiResponse(unauthorizedCode,
                        PracticeShopApiScenarioFixture.UNAUTHORIZED_BODY,
                        Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON), Duration.ZERO, url));
        ApiExecutionResult guestUser = PracticeShopApiClient.forTransport(
                PracticeShopApiScenarioFixture.API_ENDPOINT, guestTransport).currentUser();

        var guestResponse = guestUser.response();
        assertTrue(unauthorized.matches(guestResponse.status()));
        // The browser diagnostic channel is deliberately independent of API status.
        PracticeShopRuntimeDiagnostics diagnostics = new PracticeShopRuntimeDiagnostics();
        assertEquals(List.of(), diagnostics.errors());
        assertFalse(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                "console: Failed to load resource: the server responded with a status of 401 ()"));
        assertTrue(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                new PracticeShopRuntimeDiagnostics.Diagnostic("response", HttpMethod.GET.value(),
                        PracticeShopApiScenarioFixture.API_ENDPOINT + PracticeShopApiRoutes.CURRENT_USER,
                        PracticeShopApiRoutes.CURRENT_USER, unauthorizedCode,
                        "HTTP 401")));
        assertFalse(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                new PracticeShopRuntimeDiagnostics.Diagnostic("response", HttpMethod.GET.value(),
                        PracticeShopApiScenarioFixture.API_ENDPOINT + PracticeShopApiRoutes.PRODUCT_COLLECTION,
                        PracticeShopApiRoutes.PRODUCT_COLLECTION, unauthorizedCode,
                        "HTTP 401")));
        assertFalse(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                "console: ERROR TypeError: Cannot read properties of null (reading 'cart_items')"));
        assertFalse(PracticeShopRuntimeDiagnostics.isExpectedGuestSignal(
                "console: ERROR TypeError: Cannot read properties of null (reading 'profile')"));
    }

    @Test
    void authenticatedSetupOnlyToleratesAnonymousBootstrapSignals() {
        var bootstrap401 = new PracticeShopRuntimeDiagnostics.Diagnostic("response",
                HttpMethod.GET.value(), PracticeShopApiScenarioFixture.API_ENDPOINT
                + PracticeShopApiRoutes.CURRENT_USER, PracticeShopApiRoutes.CURRENT_USER,
                PracticeShopApiHttpStatus.UNAUTHORIZED.code(), "HTTP 401");
        var cartBootstrap = new PracticeShopRuntimeDiagnostics.Diagnostic("console", "", "", "", -1,
                "ERROR TypeError: Cannot read properties of null (reading 'cart_items')");
        var unrelated = new PracticeShopRuntimeDiagnostics.Diagnostic("console", "", "", "", -1,
                "ERROR TypeError: Cannot read properties of null (reading 'profile')");

        assertTrue(PracticeShopRuntimeDiagnostics.isExpectedAuthenticatedSetupSignal(bootstrap401));
        assertTrue(PracticeShopRuntimeDiagnostics.isExpectedAuthenticatedSetupSignal(cartBootstrap));
        assertFalse(PracticeShopRuntimeDiagnostics.isExpectedAuthenticatedSetupSignal(unrelated));
    }

    @Test
    void warningTargetUsesNetworkFactsInsteadOfTheCurrentPage() {
        var diagnostic = new PracticeShopRuntimeDiagnostics.Diagnostic("response",
                HttpMethod.GET.value(), PracticeShopApiScenarioFixture.API_ENDPOINT
                + PracticeShopApiRoutes.CURRENT_USER, PracticeShopApiRoutes.CURRENT_USER,
                PracticeShopApiHttpStatus.UNAUTHORIZED.code(), "HTTP 401");

        assertEquals("GET " + PracticeShopApiRoutes.CURRENT_USER + " → 401",
                PracticeShopRuntimeDiagnostics.warningTarget(List.of(diagnostic)));
    }

    @Test
    void knownBootstrapSignalsRemainReportableWarningsWhileUnrelatedErrorsFail() {
        var bootstrap = new PracticeShopRuntimeDiagnostics.Diagnostic("console", "", "", "", -1,
                "ERROR TypeError: Cannot read properties of null (reading 'cart_items')");
        var unrelated = new PracticeShopRuntimeDiagnostics.Diagnostic("console", "", "", "", -1,
                "ERROR TypeError: Cannot read properties of null (reading 'profile')");

        assertTrue(PracticeShopRuntimeDiagnostics.isExpectedAuthenticatedSetupSignal(bootstrap));
        assertFalse(PracticeShopRuntimeDiagnostics.isExpectedAuthenticatedSetupSignal(unrelated));
    }
}
