package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.config.ApiTransportMode;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.MediaTypes;
import atf.api.core.http.transport.ApiTransport;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiComponentFixture;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiClientContractTest {
    @Test
    void localTransportIsPublishedAsAcceptanceProvenance() {
        var client = PracticeShopApiClient.forTransport(PracticeShopApiScenarioFixture.API_ENDPOINT,
                jsonTransport(PracticeShopApiScenarioFixture.LOGIN_TOKEN_BODY));

        var result = client.login(PracticeShopApiScenarioFixture.validCredentials());

        assertEquals(ApiTransportProvenance.ACCEPTANCE_MOCK,
                result.loginResult().provenance());
    }

    @Test
    void explicitlyLiveTransportIsPublishedAsLiveProvenance() {
        var client = PracticeShopApiClient.forTransport(PracticeShopApiScenarioFixture.API_ENDPOINT,
                jsonTransport(PracticeShopApiScenarioFixture.LOGIN_TOKEN_BODY), ApiTransportMode.LIVE);

        var result = client.login(PracticeShopApiScenarioFixture.validCredentials());

        assertEquals(ApiTransportProvenance.LIVE_HTTP,
                result.loginResult().provenance());
    }

    @Test
    void malformedSuccessfulLoginResponseFailsWithCauseInsteadOfEmptyToken() {
        var client = PracticeShopApiClient.forTransport(PracticeShopApiScenarioFixture.API_ENDPOINT,
                jsonTransport("not-json"));

        assertThrows(IllegalStateException.class, () -> client.login(
                PracticeShopApiScenarioFixture.validCredentials()));
    }

    @Test
    void successfulLoginProducesReusableSessionForCurrentUserPrecondition() {
        var client = PracticeShopApiComponentFixture.client();

        var session = client.login(PracticeShopApiScenarioFixture.validCredentials());
        var currentUser = client.currentUser();

        assertTrue(session.authenticated());
        assertEquals(PracticeShopApiHttpStatus.OK.code(), session.loginResult().response().status());
        assertEquals(PracticeShopApiHttpStatus.OK.code(), currentUser.response().status());
        assertEquals(PracticeShopApiScenarioFixture.CURRENT_USER_BODY, currentUser.response().body());
    }

    @Test
    void invalidLoginClearsPreviousBearerBeforeCurrentUserIsCalled() {
        var client = PracticeShopApiComponentFixture.client();

        client.login(PracticeShopApiScenarioFixture.validCredentials());
        var invalidSession = client.login(PracticeShopApiScenarioFixture.invalidCredentials());
        var currentUser = client.currentUser();

        assertFalse(invalidSession.authenticated());
        assertEquals(PracticeShopApiHttpStatus.UNAUTHORIZED.code(),
                invalidSession.loginResult().response().status());
        assertEquals(PracticeShopApiHttpStatus.UNAUTHORIZED.code(), currentUser.response().status());
        assertEquals(PracticeShopApiScenarioFixture.UNAUTHORIZED_BODY, currentUser.response().body());
    }

    @Test
    void anonymousCurrentUserReturnsStructuredUnauthorizedJson() {
        var client = PracticeShopApiComponentFixture.client();

        var currentUser = client.currentUser();

        assertEquals(PracticeShopApiHttpStatus.UNAUTHORIZED.code(), currentUser.response().status());
        assertEquals(PracticeShopApiScenarioFixture.UNAUTHORIZED_BODY, currentUser.response().body());
    }

    @Test
    void closingClientClearsSessionAndRejectsLaterNetworkUse() {
        var client = PracticeShopApiComponentFixture.client();
        client.login(PracticeShopApiScenarioFixture.validCredentials());

        client.close();

        assertEquals("", client.bearerToken());
        assertThrows(IllegalStateException.class,
                () -> client.currentUser());
    }

    private static ApiTransport jsonTransport(String responseBody) {
        return (request, url, auth) -> CompletableFuture.completedFuture(new ApiResponse(
                PracticeShopApiHttpStatus.OK.code(), responseBody,
                Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON),
                Duration.ZERO, url));
    }
}
