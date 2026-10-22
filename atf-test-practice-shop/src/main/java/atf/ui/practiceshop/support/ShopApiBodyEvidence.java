package atf.ui.practiceshop.support;

/**
 * Makes an unavailable browser/API payload explicit without turning it into an
 * empty or apparently valid JSON body.
 */
public final class ShopApiBodyEvidence {
    private static final String PREFIX = "<body unavailable: ";

    private ShopApiBodyEvidence() { }

    public static String unavailable(String source, Throwable error) {
        String owner = source == null || source.isBlank() ? "API transport" : source.trim();
        String cause = error == null
                ? "payload was not exposed"
                : ShopValidationDiagnostics.unavailable(error)
                .replaceFirst("^unavailable:\s*", "");
        return PREFIX + owner + " — " + cause + ">";
    }

    public static String unavailable(String source, String detail) {
        String owner = source == null || source.isBlank() ? "API transport" : source.trim();
        String cause = detail == null || detail.isBlank() ? "payload was not exposed" : detail.trim();
        return PREFIX + owner + " — " + cause + ">";
    }

    public static boolean isUnavailable(String body) {
        return body != null && body.startsWith(PREFIX);
    }
}
