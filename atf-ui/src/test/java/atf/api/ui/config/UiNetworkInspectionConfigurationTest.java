package atf.api.ui.config;

import atf.api.testkit.AtfComponentTest;
import atf.api.ui.network.UiNetworkInspector;
import atf.api.ui.network.UiNetworkObservation;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class UiNetworkInspectionConfigurationTest {
    @Test
    void disabledPolicyIsFailClosed() {
        var policy = UiNetworkInspectionConfiguration.disabled();

        assertFalse(policy.enabled());
        assertFalse(policy.accepts("https://api.example.test/orders"));
    }

    @Test
    void hostAllowlistIsCaseInsensitiveAndDoesNotAcceptOtherServices() {
        var policy = UiNetworkInspectionConfiguration.forHosts(List.of("API.Example.Test"));

        assertTrue(policy.accepts("https://api.example.test/orders"));
        assertFalse(policy.accepts("https://cdn.example.test/app.js"));
        assertEquals(12_000, policy.maxBodyPreviewCharacters());
    }

    @Test
    void malformedNetworkUrlIsObservableInsteadOfBeingSilentlyIgnored() {
        var policy = UiNetworkInspectionConfiguration.forHosts(List.of("api.example.test"));

        var error = assertThrows(IllegalStateException.class,
                () -> policy.accepts("not a URL"));

        assertTrue(error.getMessage().contains("invalid URL"));
    }

    @Test
    void enabledPolicyRequiresAnExplicitHostBoundary() {
        assertThrows(IllegalArgumentException.class, () ->
                new UiNetworkInspectionConfiguration(true, Set.of(), 32, 500,
                        true, UiNetworkSettlementPolicy.disabled()));
    }

    @Test
    void settlementPolicyIsBoundedAndIndependentFromActionReplay() {
        var settlement = UiNetworkSettlementPolicy.bounded(
                Duration.ofSeconds(5), Duration.ofMillis(100));

        assertTrue(settlement.enabled());
        assertEquals(Duration.ofSeconds(5), settlement.timeout());
        assertEquals(Duration.ofMillis(100), settlement.pollInterval());
    }

    @Test
    void awaitRequiresExplicitSettlementPolicy() {
        var inspector = new UiNetworkInspector(UiNetworkInspectionConfiguration.disabled());

        assertThrows(IllegalStateException.class,
                () -> inspector.await(observation -> true, null));
    }

    @Test
    void typedConsumerObservationUsesTheSameAllowlistAndSubscriberBoundary() {
        var configuration = new UiNetworkInspectionConfiguration(true,
                Set.of("api.example.test"), 1, 64, true,
                UiNetworkSettlementPolicy.disabled());
        var inspector = new UiNetworkInspector(configuration);
        var received = new ArrayList<UiNetworkObservation>();
        inspector.subscribe(received::add);

        var accepted = observation("https://api.example.test/orders", 200);
        var rejected = observation("https://cdn.example.test/app.js", 200);

        assertTrue(inspector.capture(accepted).isPresent());
        assertTrue(inspector.capture(rejected).isEmpty());
        assertTrue(inspector.capture(observation("https://api.example.test/second", 200)).isEmpty(),
                "the configured observation bound must apply to consumer fixtures too");
        assertEquals(1, inspector.observations().size());
        assertEquals(accepted.url(), inspector.observations().get(0).url());
        assertEquals(accepted.requestBody(), inspector.observations().get(0).requestBody());
        assertEquals(inspector.observations(), received);
        assertNotEquals(accepted.requestSignature(), inspector.observations().get(0).requestSignature());
    }

    @Test
    void requestFailureIsTypedAsUnavailableEvidenceInsteadOfDisappearing() {
        var inspector = new UiNetworkInspector(
                UiNetworkInspectionConfiguration.forHosts(List.of("api.example.test")));
        var failure = new UiNetworkObservation(
                1, Instant.EPOCH, "POST", "https://api.example.test/orders", "/orders", -1,
                Map.of("content-type", "application/json"), Map.of(), "{}",
                "<unavailable: request failed before response>", "none", "none",
                "api.example.test", "UI_BROWSER_REQUEST_FAILED", "POST /orders#body=abc",
                false, "net::ERR_CONNECTION_RESET");

        assertTrue(inspector.capture(failure).isPresent());
        assertEquals(-1, inspector.observations().get(0).status());
        assertTrue(inspector.observations().get(0).requestFailed());
        assertEquals("net::ERR_CONNECTION_RESET", inspector.observations().get(0).failureReason());
        assertTrue(inspector.observations().get(0).displayTarget().contains("UNAVAILABLE"));
    }

    @Test
    void customCaptureIsResanitizedBeforeStorageAndSubscriberNotification() {
        var configuration = new UiNetworkInspectionConfiguration(true,
                Set.of("api.example.test"), 2, 32, true,
                UiNetworkSettlementPolicy.disabled());
        var inspector = new UiNetworkInspector(configuration);
        var received = new ArrayList<UiNetworkObservation>();
        inspector.subscribe(received::add);
        String secret = "super-secret-token";
        var unsafe = new UiNetworkObservation(
                77, Instant.EPOCH, "POST",
                "https://api.example.test/orders?token=" + secret,
                "/orders?token=" + secret, 201,
                Map.of("Authorization", "Bearer " + secret,
                        "X-Correlation-Token", secret), Map.of(),
                "{\"token\":\"" + secret + "\",\"padding\":\"abcdefghijklmnopqrstuvwxyz\"}",
                "{\"apiKey\":\"" + secret + "\",\"padding\":\"abcdefghijklmnopqrstuvwxyz\"}",
                "custom-auth", "custom-source", "api.example.test",
                "CUSTOM", "caller-controlled-signature", true, "request failed");

        var captured = inspector.capture(unsafe).orElseThrow();

        assertEquals(1, inspector.observations().size());
        assertEquals(inspector.observations().get(0), received.get(0));
        assertTrue(captured.url().endsWith("?redacted-query"));
        assertTrue(captured.path().endsWith("?redacted-query"));
        assertEquals("REDACTED", captured.requestHeaders().get("Authorization"));
        assertEquals("REDACTED", captured.requestHeaders().get("X-Correlation-Token"));
        assertTrue(captured.requestBody().length() <= configuration.maxBodyPreviewCharacters());
        assertTrue(captured.responseBody().length() <= configuration.maxBodyPreviewCharacters());
        assertFalse(captured.requestBody().contains(secret));
        assertFalse(captured.responseBody().contains(secret));
        assertFalse(captured.toString().contains(secret));
        assertFalse(captured.requestSignature().contains(secret));
        assertFalse(captured.requestSignature().equals("caller-controlled-signature"));
    }

    @Test
    void playwrightRequestFailureListenerPublishesBoundedRedactedEvidenceAndDetaches() {
        var listener = new AtomicReference<Consumer<Request>>();
        Page page = (Page) Proxy.newProxyInstance(Page.class.getClassLoader(),
                new Class<?>[]{Page.class}, (proxy, method, args) -> {
                    if ("onRequestFailed".equals(method.getName())) {
                        @SuppressWarnings("unchecked")
                        Consumer<Request> callback = (Consumer<Request>) args[0];
                        listener.set(callback);
                    } else if ("offRequestFailed".equals(method.getName())) {
                        listener.set(null);
                    }
                    return null;
                });
        Request request = (Request) Proxy.newProxyInstance(Request.class.getClassLoader(),
                new Class<?>[]{Request.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "url" -> "https://api.example.test/orders?token=secret123";
                    case "method" -> "POST";
                    case "headers" -> Map.of("Authorization", "Bearer secret123");
                    case "postData" -> "{\"token\":\"secret123\"}";
                    case "failure" -> "net::ERR_FAILED https://api.example.test/orders?token=secret123";
                    default -> null;
                });
        var inspector = new UiNetworkInspector(UiNetworkInspectionConfiguration.forHosts(
                List.of("api.example.test")));
        var received = new ArrayList<UiNetworkObservation>();

        inspector.attach(page, received::add);
        listener.get().accept(request);

        assertEquals(1, received.size());
        var failure = received.get(0);
        assertTrue(failure.requestFailed());
        assertEquals(-1, failure.status());
        assertEquals("bearer", failure.authMethod());
        assertEquals("REDACTED", failure.requestHeaders().get("Authorization"));
        assertTrue(failure.failureReason().contains("net::ERR_FAILED"));
        assertFalse(failure.toString().contains("secret123"));
        assertTrue(failure.url().endsWith("?redacted-query"));
        inspector.close();
        assertNull(listener.get());
    }

    private static UiNetworkObservation observation(String url, int status) {
        return new UiNetworkObservation(
                1, Instant.EPOCH, "GET", url, "/orders", status,
                Map.of(), Map.of("content-type", "application/json"),
                "", "{}", "none", "none", "api.example.test", "UI_BROWSER",
                "GET /orders#body=empty", true);
    }
}
