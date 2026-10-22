package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfUnitTest;
import atf.ui.practiceshop.support.PracticeShopRuntimeDiagnostics;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class PracticeShopRuntimeDiagnosticsTest {
    @Test
    void unauthorizedConsoleErrorsFailInsteadOfBecomingWarningsOrBeingSilentlyTolerated() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);
        ConsoleMessage error = consoleError("401 Unauthorized",
                "https://practicesoftwaretesting.com/chunk-runtime.js:6:12810");

        fire(listeners, "onConsoleMessage", error);

        AssertionError failure = assertThrows(AssertionError.class, diagnostics::assertClean);
        assertTrue(failure.getMessage().contains("401 Unauthorized"));
    }

    @Test
    void pageErrorsRemainFailureDiagnostics() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);

        fire(listeners, "onPageError", "Unexpected page error");

        AssertionError failure = assertThrows(AssertionError.class, diagnostics::assertClean);
        assertTrue(failure.getMessage().contains("Unexpected page error"));
    }

    @Test
    void uncorrelatedConsoleFailuresRetainPlaywrightSourceLocation() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);
        ConsoleMessage error = consoleError("401 Unauthorized", "https://shop.example/runtime.js:12:8");

        fire(listeners, "onConsoleMessage", error);

        AssertionError failure = assertThrows(AssertionError.class, diagnostics::assertClean);
        assertTrue(failure.getMessage().contains("https://shop.example/runtime.js:12:8"));
    }

    @Test
    void observedAnonymousSessionProbeAndItsFrontendEchoAreNotPublishedAsRuntimeEvidence() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);

        fire(listeners, "onConsoleMessage", consoleError("Failed to load resource: status 401",
                "https://api.practicesoftwaretesting.com/users/me:0:0"));
        fire(listeners, "onConsoleMessage", consoleError("ERROR {message: Unauthorized}",
                "https://practicesoftwaretesting.com/chunk-runtime.js:6:12810"));
        fire(listeners, "onConsoleMessage", consoleError("ERROR {message: Unauthorized}",
                "https://practicesoftwaretesting.com/chunk-runtime.js:6:12810"));

        diagnostics.assertClean();
        assertEquals(java.util.List.of(), diagnostics.errors());
    }

    @Test
    void excludedExternalSessionProbeAndItsConsoleEchoStayOutOfRuntimeEvidence() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);
        String url = "https://api.practicesoftwaretesting.com/users/me";
        Request request = proxy(Request.class, (proxy, method, arguments) -> switch (method.getName()) {
            case "method" -> "GET";
            case "url" -> url;
            default -> null;
        });
        Response response = proxy(Response.class, (proxy, method, arguments) -> switch (method.getName()) {
            case "status" -> 401;
            case "url" -> url;
            case "request" -> request;
            default -> null;
        });
        ConsoleMessage error = consoleError("401 Unauthorized",
                "https://practicesoftwaretesting.com/chunk-runtime.js:6:12810");

        fire(listeners, "onResponse", response);
        fire(listeners, "onConsoleMessage", error);

        diagnostics.assertClean();
        assertEquals(java.util.List.of(), diagnostics.errors());
    }

    @Test
    void sameSessionPathOnAnotherHostIsNotTreatedAsTheKnownGuestProbe() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);
        String url = "https://unrelated.example/users/me";

        fire(listeners, "onResponse", response(401, url, request("GET", url)));

        AssertionError failure = assertThrows(AssertionError.class, diagnostics::assertClean);
        assertTrue(failure.getMessage().contains(url));
    }

    @Test
    void knownExternalBootstrapNoiseDoesNotFailAnAuthenticatedJourney() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);
        fire(listeners, "onConsoleMessage", consoleError("401 Unauthorized"));
        diagnostics.markAuthenticated();
        fire(listeners, "onConsoleMessage", consoleError(
                "ERROR TypeError: Cannot read properties of null (reading 'cart_items')"));

        diagnostics.assertClean();
    }

    @Test
    void unknownBootstrapErrorsAndErrorsAfterLoginStillFail() {
        var beforeLogin = new PracticeShopRuntimeDiagnostics();
        var beforeLoginListeners = attach(beforeLogin);
        fire(beforeLoginListeners, "onConsoleMessage", consoleError("Unexpected JavaScript error"));
        beforeLogin.markAuthenticated();
        assertTrue(assertThrows(AssertionError.class, beforeLogin::assertClean)
                .getMessage().contains("Unexpected JavaScript error"));

        var afterLogin = new PracticeShopRuntimeDiagnostics();
        var afterLoginListeners = attach(afterLogin);
        afterLogin.markAuthenticated();
        fire(afterLoginListeners, "onConsoleMessage", consoleError("401 Unauthorized"));
        assertTrue(assertThrows(AssertionError.class, afterLogin::assertClean)
                .getMessage().contains("401 Unauthorized"));
    }

    @Test
    void unauthorizedErrorsAfterLoginRemainFailures() {
        var diagnostics = new PracticeShopRuntimeDiagnostics();
        var listeners = attach(diagnostics);
        diagnostics.markAuthenticated();
        fire(listeners, "onConsoleMessage", consoleError("401 Unauthorized"));

        AssertionError failure = assertThrows(AssertionError.class, diagnostics::assertClean);
        assertTrue(failure.getMessage().contains("401 Unauthorized"));
    }

    private static ConsoleMessage consoleError(String text) {
        return consoleError(text, null);
    }

    private static ConsoleMessage consoleError(String text, String location) {
        return proxy(ConsoleMessage.class, (proxy, method, arguments) -> switch (method.getName()) {
            case "type" -> "error";
            case "text" -> text;
            case "location" -> location;
            default -> null;
        });
    }

    private static Request request(String method, String url) {
        return proxy(Request.class, (proxy, invoked, arguments) -> switch (invoked.getName()) {
            case "method" -> method;
            case "url" -> url;
            default -> null;
        });
    }

    private static Response response(int status, String url, Request request) {
        return proxy(Response.class, (proxy, invoked, arguments) -> switch (invoked.getName()) {
            case "status" -> status;
            case "url" -> url;
            case "request" -> request;
            default -> null;
        });
    }

    private static Map<String, Consumer<?>> attach(PracticeShopRuntimeDiagnostics diagnostics) {
        Map<String, Consumer<?>> listeners = new HashMap<>();
        Page page = proxy(Page.class, (proxy, method, arguments) -> {
            if (arguments != null && arguments.length == 1 && arguments[0] instanceof Consumer<?> listener) {
                listeners.put(method.getName(), listener);
            }
            return null;
        });
        diagnostics.attach(page);
        return listeners;
    }

    @SuppressWarnings("unchecked")
    private static <T> void fire(Map<String, Consumer<?>> listeners, String eventName, T event) {
        ((Consumer<T>) listeners.get(eventName)).accept(event);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> contract, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, handler);
    }
}
