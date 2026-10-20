package atf.ui.practiceshop.support;

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Captures browser runtime failures without confusing them with expected API 401 guest branches. */
public final class PracticeShopRuntimeDiagnostics {
    private final List<String> errors = new ArrayList<>();

    public void attach(Page page) {
        Objects.requireNonNull(page, "page");
        page.onConsoleMessage(this::captureConsole);
        page.onPageError(this::capturePageError);
    }

    public List<String> errors() { return List.copyOf(errors); }

    public List<String> expectedGuestWarnings() {
        // A browser can report the same guest branch repeatedly while the page retries.
        // Keep the report actionable: one assertion per distinct known signal.
        return errors.stream()
                .filter(PracticeShopRuntimeDiagnostics::isExpectedGuestSignal)
                .distinct()
                .toList();
    }

    public List<String> unexpectedErrors() {
        return errors.stream().filter(error -> !isExpectedGuestSignal(error)).toList();
    }

    public void assertClean() {
        if (!unexpectedErrors().isEmpty()) {
            throw new AssertionError("Unexpected Practice Shop browser runtime errors: " + unexpectedErrors());
        }
    }

    public static boolean isExpectedGuestSignal(String error) {
        if (error == null || error.isBlank()) return false;
        return error.contains("responded with a status of 401")
                || error.contains("Unauthorized")
                || error.contains("Cannot read properties of null (reading 'cart_items')");
    }

    private void captureConsole(ConsoleMessage message) {
        if ("error".equalsIgnoreCase(message.type())) {
            errors.add("console: " + message.text());
        }
    }

    private void capturePageError(String message) {
        if (message != null && !message.isBlank()) errors.add("page: " + message);
    }
}
