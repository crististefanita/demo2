package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopApiAssertions;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component contract for a JSON login response whose browser body is deferred. */
@AtfComponentTest
class PracticeShopApiLoginAssertionsTest {
    @Test
    void acceptsJsonTransportWhenPlaywrightCannotExposeCompressedBody() {
        var login = new PracticeShopApiNetworkJournal.Exchange(
                HttpMethod.POST.value(), PracticeShopApiRoutes.LOGIN,
                PracticeShopApiScenarioFixture.API_ENDPOINT + PracticeShopApiRoutes.LOGIN,
                PracticeShopApiHttpStatus.OK.code(),
                "{\"email\":\"<redacted>\",\"password\":\"<redacted>\"}", "",
                Map.of("content-type", MediaTypes.APPLICATION_JSON), Map.of(
                "content-type", MediaTypes.APPLICATION_JSON, "content-length", "384"));

        var assertions = PracticeShopApiAssertions.login(login, null, "ATF PracticeShop");

        assertTrue(assertions.stream().allMatch(assertion -> assertion.passed()),
                () -> assertions.toString());
    }

    @Test
    void responseBodyRecoveryPreservesTraceAndRedactsToken() {
        var captured = new PracticeShopApiNetworkJournal.Exchange(
                HttpMethod.POST.value(), PracticeShopApiRoutes.LOGIN,
                PracticeShopApiScenarioFixture.API_ENDPOINT + PracticeShopApiRoutes.LOGIN,
                PracticeShopApiHttpStatus.OK.code(),
                "{\"email\":\"<redacted>\",\"password\":\"***REDACTED***\"}",
                "<body unavailable: Chromium released response payload>",
                Map.of("content-type", MediaTypes.APPLICATION_JSON), Map.of(
                "content-type", MediaTypes.APPLICATION_JSON, "content-length", "384"))
                .withTrace(7, java.time.Instant.parse("2026-10-20T10:00:00Z"), "authentication");

        var recovered = captured.withResponseBody("{\"token\":\"raw-token\",\"user\":\"ATF Practice\"}");

        assertTrue(recovered.responseBody().contains("\"token\" : \"<redacted>\""));
        assertTrue(recovered.responseBody().contains("\"user\" : \"ATF Practice\""));
        assertEquals(captured.sequence(), recovered.sequence());
        assertEquals(captured.signature(), recovered.signature());
        assertTrue(recovered.digest() != null && !recovered.digest().isBlank());
    }
}
