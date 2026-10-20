package atf.ui.practiceshop.support;

/** Keeps failed UI assertions useful without leaking an unbounded Playwright exception. */
public final class PracticeShopValidationDiagnostics {
    private static final int MAX_DIAGNOSTIC_LENGTH = 210;

    private PracticeShopValidationDiagnostics() { }

    public static String unavailable(Throwable error) {
        if (error == null) return "unavailable";
        String type = error.getClass().getSimpleName();
        String message = error.getMessage();
        if (message == null || message.isBlank()) return "unavailable: " + type;
        String compact = message.replaceAll("\\s+", " ").trim();
        String diagnostic = "unavailable: " + type + " — " + compact;
        if (diagnostic.length() > MAX_DIAGNOSTIC_LENGTH) {
            return diagnostic.substring(0, MAX_DIAGNOSTIC_LENGTH - 1) + "…";
        }
        return diagnostic;
    }
}
