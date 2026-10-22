package atf.ui.tests.practiceshop.api.component.auth;

import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.ShopApiAssertions;
import atf.ui.practiceshop.support.ShopApiNetworkJournal;
import atf.ui.practiceshop.api.ShopApiRoutes;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.tests.practiceshop.api.support.ShopApiScenarioFixture;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component contract for a JSON login response whose browser body is deferred. */
@AtfComponentTest
class ShopApiLoginAssertionsTest {
    @Test
    void acceptsJsonTransportWhenPlaywrightCannotExposeCompressedBody() {
        var login = new ShopApiNetworkJournal.Exchange(
                HttpMethod.POST.value(), ShopApiRoutes.LOGIN,
                ShopApiScenarioFixture.API_ENDPOINT + ShopApiRoutes.LOGIN,
                ShopApiHttpStatus.OK.code(),
                "{\"email\":\"<redacted>\",\"password\":\"<redacted>\"}", "",
                Map.of("content-type", MediaTypes.APPLICATION_JSON), Map.of(
                "content-type", MediaTypes.APPLICATION_JSON, "content-length", "384"));

        var assertions = ShopApiAssertions.login(login);

        assertTrue(assertions.stream().allMatch(assertion -> assertion.passed()),
                () -> assertions.toString());
    }

    @Test
    void responseBodyRecoveryPreservesTraceAndRedactsToken() {
        var captured = new ShopApiNetworkJournal.Exchange(
                HttpMethod.POST.value(), ShopApiRoutes.LOGIN,
                ShopApiScenarioFixture.API_ENDPOINT + ShopApiRoutes.LOGIN,
                ShopApiHttpStatus.OK.code(),
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
