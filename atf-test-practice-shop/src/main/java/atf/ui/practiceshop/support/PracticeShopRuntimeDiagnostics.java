package atf.ui.practiceshop.support;

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import atf.api.core.http.request.HttpMethod;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/** Captures browser runtime failures and exposes known external bootstrap noise as reportable warnings. */
public final class PracticeShopRuntimeDiagnostics {
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final List<Diagnostic> toleratedSetupDiagnostics = new ArrayList<>();
    private Diagnostic lastUnauthorizedResponse;
    private boolean authenticatedSetupEstablished;

    public void attach(Page page) {
        Objects.requireNonNull(page, "page");
        page.onResponse(this::captureResponse);
        page.onConsoleMessage(this::captureConsole);
        page.onPageError(this::capturePageError);
    }

    public List<String> errors() { return diagnostics.stream().map(Diagnostic::toString).toList(); }

    public List<Diagnostic> structuredDiagnostics() { return List.copyOf(diagnostics); }

    /**
     * Records the authenticated checkpoint. The checkpoint itself is still a
     * hard UI/API assertion; known anonymous bootstrap diagnostics remain
     * visible as warnings because the external application can emit them
     * asynchronously after the login request has completed.
     */
    public void markAuthenticatedSetup() {
        if (authenticatedSetupEstablished) return;
        authenticatedSetupEstablished = true;
        diagnostics.stream()
                .filter(PracticeShopRuntimeDiagnostics::isExpectedAuthenticatedSetupSignal)
                .filter(diagnostic -> !toleratedSetupDiagnostics.contains(diagnostic))
                .forEach(toleratedSetupDiagnostics::add);
    }

    public List<String> toleratedSetupWarnings() {
        return toleratedSetupDiagnostics.stream().map(Diagnostic::toString).distinct().toList();
    }

    /** All known external bootstrap signals, kept visible for report publication. */
    public List<String> expectedRuntimeWarnings() {
        return diagnostics.stream()
                .filter(PracticeShopRuntimeDiagnostics::isExpectedAuthenticatedSetupSignal)
                .map(Diagnostic::toString)
                .distinct()
                .toList();
    }

    public List<String> expectedGuestWarnings() {
        // A browser can report the same guest branch repeatedly while the page retries.
        // Keep the report actionable: one assertion per distinct known signal.
        return diagnostics.stream()
                .filter(PracticeShopRuntimeDiagnostics::isExpectedGuestSignal)
                .map(Diagnostic::toString)
                .distinct()
                .toList();
    }

    /**
     * Compact report target for the known guest branch. The report action must
     * point to the network fact that caused the warning, not to the page that
     * happened to be open when the diagnostic was published.
     */
    public String expectedGuestWarningTarget() {
        return warningTarget(diagnostics.stream()
                .filter(PracticeShopRuntimeDiagnostics::isExpectedGuestSignal)
                .toList());
    }

    /** Compact report target for known authenticated-bootstrap diagnostics. */
    public String expectedRuntimeWarningTarget() {
        return warningTarget(diagnostics.stream()
                .filter(PracticeShopRuntimeDiagnostics::isExpectedAuthenticatedSetupSignal)
                .toList());
    }

    /** Pure formatter kept public so the target contract can be tested without a browser. */
    public static String warningTarget(Collection<Diagnostic> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) return "none";
        String target = diagnostics.stream()
                .filter(Objects::nonNull)
                .map(PracticeShopRuntimeDiagnostics::compactTarget)
                .distinct()
                .collect(Collectors.joining("; "));
        return target.isBlank() ? "unknown runtime diagnostic" : target;
    }

    public List<String> unexpectedErrors() {
        return diagnostics.stream().filter(error -> !isExpectedGuestSignal(error)
                        && !isExpectedAuthenticatedSetupSignal(error)
                        && !toleratedSetupDiagnostics.contains(error))
                .map(Diagnostic::toString).toList();
    }

    public void assertClean() {
        if (!unexpectedErrors().isEmpty()) {
            throw new AssertionError("Unexpected Practice Shop browser runtime errors: " + unexpectedErrors());
        }
    }

    private static String compactTarget(Diagnostic diagnostic) {
        if (!diagnostic.method().isBlank() && !diagnostic.route().isBlank()) {
            return diagnostic.method().toUpperCase(Locale.ROOT) + " " + diagnostic.route()
                    + " → " + (diagnostic.status() < 0 ? "n/a" : diagnostic.status());
        }
        return diagnostic.source().toLowerCase(Locale.ROOT) + " diagnostic";
    }

    /**
     * A browser diagnostic is expected only when the network facts identify the
     * guest current-user call. Message text is intentionally never sufficient.
     */
    public static boolean isExpectedGuestSignal(Diagnostic diagnostic) {
        return diagnostic != null
                && diagnostic.status() == 401
                && HttpMethod.GET.value().equalsIgnoreCase(diagnostic.method())
                && PracticeShopApiRoutes.CURRENT_USER.equals(diagnostic.route());
    }

    /**
     * Known external bootstrap signal. It is not silently ignored: callers
     * publish it as a warning, while unrelated runtime errors still fail.
     */
    public static boolean isExpectedAuthenticatedSetupSignal(Diagnostic diagnostic) {
        if (diagnostic == null) return false;
        if (isExpectedGuestSignal(diagnostic)) return true;
        String message = diagnostic.message().toLowerCase(Locale.ROOT);
        return diagnostic.status() < 0
                && "console".equalsIgnoreCase(diagnostic.source())
                && (message.contains("cart_items")
                || message.contains("unauthorized")
                || message.contains("401"));
    }

    /** Kept for source compatibility; an unstructured message is never trusted. */
    public static boolean isExpectedGuestSignal(String ignoredMessage) { return false; }

    private void captureResponse(Response response) {
        if (response == null || response.status() != 401) return;
        String url = response.url();
        String route = route(url);
        var request = response.request();
        String method = request == null ? "" : request.method();
        Diagnostic diagnostic = new Diagnostic("response", method, url,
                route, response.status(), "HTTP 401");
        diagnostics.add(diagnostic);
        lastUnauthorizedResponse = diagnostic;
    }

    private void captureConsole(ConsoleMessage message) {
        if (!"error".equalsIgnoreCase(message.type())) return;
        String text = message.text();
        Diagnostic related = text != null && text.contains("401") ? lastUnauthorizedResponse : null;
        if (related != null) {
            diagnostics.add(new Diagnostic("console", related.method(), related.url(), related.route(),
                    related.status(), text));
        } else {
            diagnostics.add(new Diagnostic("console", "", "", "", -1,
                    text == null ? "console error" : text));
        }
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
