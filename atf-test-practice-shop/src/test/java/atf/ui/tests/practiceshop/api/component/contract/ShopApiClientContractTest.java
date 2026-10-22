package atf.ui.tests.practiceshop.api.component.contract;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.config.ApiTransportMode;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.MediaTypes;
import atf.api.core.http.transport.ApiTransport;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.ui.practiceshop.api.ShopApiClient;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.practiceshop.config.ShopCredentials;
import atf.ui.tests.practiceshop.api.support.ShopApiComponentFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiScenarioFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiCallComponentTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ShopApiCallComponentTest
class ShopApiClientContractTest {
    @Test
    void localTransportIsPublishedAsAcceptanceProvenance() {
        var client = ShopApiClient.forTransport(ShopApiScenarioFixture.API_ENDPOINT,
                ShopApiComponentFixture.recordedTransport(
                        jsonTransport(ShopApiScenarioFixture.LOGIN_TOKEN_BODY),
                        ApiTransportProvenance.ACCEPTANCE_MOCK));

        var result = client.login(ShopApiScenarioFixture.validCredentials());

        assertEquals(ApiTransportProvenance.ACCEPTANCE_MOCK,
                result.loginResult().provenance());
    }

    @Test
    void explicitlyLiveTransportIsPublishedAsLiveProvenance() {
        var client = ShopApiClient.forTransport(ShopApiScenarioFixture.API_ENDPOINT,
                ShopApiComponentFixture.recordedTransport(
                        jsonTransport(ShopApiScenarioFixture.LOGIN_TOKEN_BODY),
                        ApiTransportProvenance.ACCEPTANCE_MOCK), ApiTransportMode.LIVE);

        var result = client.login(ShopApiScenarioFixture.validCredentials());

        assertEquals(ApiTransportProvenance.LIVE_HTTP,
                result.loginResult().provenance());
    }

    @Test
    void malformedSuccessfulLoginResponseFailsWithCauseInsteadOfEmptyToken() {
        var client = ShopApiClient.forTransport(ShopApiScenarioFixture.API_ENDPOINT,
                ShopApiComponentFixture.recordedTransport(jsonTransport("not-json"),
                        ApiTransportProvenance.ACCEPTANCE_MOCK));

        assertThrows(IllegalStateException.class, () -> client.login(
                ShopApiScenarioFixture.validCredentials()));
    }

    @Test
    void successfulLoginProducesReusableAuthenticatedSession() {
        var client = ShopApiComponentFixture.client();

        var session = client.login(ShopApiScenarioFixture.validCredentials());

        assertTrue(session.authenticated());
        assertEquals(ShopApiHttpStatus.OK.code(), session.loginResult().response().status());
    }

    @Test
    void invalidLoginClearsPreviousBearer() {
        var client = ShopApiComponentFixture.client();

        client.login(ShopApiScenarioFixture.validCredentials());
        var invalidSession = client.login(ShopApiScenarioFixture.invalidCredentials());

        assertFalse(invalidSession.authenticated());
        assertEquals(ShopApiHttpStatus.UNAUTHORIZED.code(),
                invalidSession.loginResult().response().status());
        assertEquals("", client.bearerToken());
    }

    @Test
    void closingClientClearsSessionAndRejectsLaterNetworkUse() {
        var client = ShopApiComponentFixture.client();
        client.login(ShopApiScenarioFixture.validCredentials());

        client.close();

        assertEquals("", client.bearerToken());
        assertThrows(IllegalStateException.class,
                () -> client.login(ShopApiScenarioFixture.validCredentials()));
    }

    private static ApiTransport jsonTransport(String responseBody) {
        return (request, url, auth) -> CompletableFuture.completedFuture(new ApiResponse(
                ShopApiHttpStatus.OK.code(), responseBody,
                Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON),
                Duration.ZERO, url));
    }
}
