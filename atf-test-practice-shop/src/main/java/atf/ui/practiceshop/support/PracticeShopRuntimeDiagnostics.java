package atf.ui.practiceshop.support;

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Captures browser runtime failures while excluding non-contract ambient session probes. */
public final class PracticeShopRuntimeDiagnostics {
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private Diagnostic lastUnauthorizedResponse;
    private boolean lastUnauthorizedResponseSuppressed;
    private int authenticatedBoundary = -1;

    public void attach(Page page) {
        Objects.requireNonNull(page, "page");
        page.onResponse(this::captureResponse);
        page.onConsoleMessage(this::captureConsole);
        page.onPageError(this::capturePageError);
    }

    public List<String> errors() { return diagnostics.stream().map(Diagnostic::toString).toList(); }

    /** Marks the point after the browser has proved a real authenticated account session. */
    public void markAuthenticated() {
        authenticatedBoundary = diagnostics.size();
    }

    public List<String> unexpectedErrors() {
        List<String> unexpected = new ArrayList<>();
        for (int index = 0; index < diagnostics.size(); index++) {
            Diagnostic diagnostic = diagnostics.get(index);
            boolean knownCartBootstrap = isKnownCartBootstrapSignal(diagnostic);
            boolean knownAnonymousUnauthorized = authenticatedBoundary >= 0
                    && index < authenticatedBoundary && isUnauthorizedBootstrapSignal(diagnostic);
            if (!knownCartBootstrap && !knownAnonymousUnauthorized) {
                unexpected.add(diagnostic.toString());
            }
        }
        return List.copyOf(unexpected);
    }

    public void assertClean() {
        if (!unexpectedErrors().isEmpty()) {
            throw new AssertionError("Unexpected Practice Shop browser runtime errors: " + unexpectedErrors());
        }
    }

    private void captureResponse(Response response) {
        if (response == null || response.status() != 401) return;
        String url = response.url();
        String route = route(url);
        var request = response.request();
        String method = request == null ? "" : request.method();
        if (!PracticeShopApiTrafficCapture.isPublishable(method, route)) {
            lastUnauthorizedResponse = null;
            lastUnauthorizedResponseSuppressed = true;
            return;
        }
        Diagnostic diagnostic = new Diagnostic("response", method, url,
                route, response.status(), "HTTP 401");
        lastUnauthorizedResponse = diagnostic;
        lastUnauthorizedResponseSuppressed = false;
        diagnostics.add(diagnostic);
    }

    private void captureConsole(ConsoleMessage message) {
        if (!"error".equalsIgnoreCase(message.type())) return;
        String text = message.text();
        boolean unauthorizedSignal = isUnauthorizedSignal(text);
        Diagnostic related = unauthorizedSignal ? lastUnauthorizedResponse : null;
        if (related != null) {
            diagnostics.add(new Diagnostic("console", related.method(), related.url(), related.route(),
                    related.status(), text));
            lastUnauthorizedResponse = null;
        } else if (unauthorizedSignal && lastUnauthorizedResponseSuppressed) {
            lastUnauthorizedResponseSuppressed = false;
        } else {
            diagnostics.add(new Diagnostic("console", "", "", "", -1,
                    text == null ? "console error" : text));
        }
    }

    private static boolean isUnauthorizedSignal(String text) {
        if (text == null) return false;
        String normalized = text.toLowerCase(Locale.ROOT);
        return normalized.contains("401") || normalized.contains("unauthorized");
    }

    private static boolean isUnauthorizedBootstrapSignal(Diagnostic diagnostic) {
        String message = diagnostic.message().toLowerCase(Locale.ROOT);
        return message.contains("401") || message.contains("unauthorized");
    }

    private static boolean isKnownCartBootstrapSignal(Diagnostic diagnostic) {
        String message = diagnostic.message().toLowerCase(Locale.ROOT);
        return message.contains("cannot read properties of null")
                && message.contains("cart_items");
    }

    private void capturePageError(String message) {
        if (message != null && !message.isBlank()) {
            diagnostics.add(new Diagnostic("page", "", "", "", -1, message));
        }
    }

    private static String route(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            return uri.getPath() == null ? "" : uri.getPath();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    public record Diagnostic(String source, String method, String url, String route,
                             int status, String message) {
        public Diagnostic {
            source = source == null ? "unknown" : source;
            method = method == null ? "" : method;
            url = url == null ? "" : url;
            route = route == null ? "" : route;
            message = message == null ? "" : message;
        }

        @Override public String toString() {
            return "source=" + source.toLowerCase(Locale.ROOT)
                    + "; method=" + method + "; route=" + route
                    + "; status=" + (status < 0 ? "n/a" : status)
                    + "; url=" + url + "; message=" + message;
        }
    }
}
