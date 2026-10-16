package atf.api.core.http;

/** Formats stable, human-readable operation labels from canonical API metadata. */
public final class ApiOperationLabel {
    private ApiOperationLabel() { }

    public static String format(HttpMethod method, String path, String description) {
        String operation = String.format("%s %s", method.value(), path);
        return description == null || description.isBlank()
                ? operation
                : String.format("%s — %s", operation, description);
    }

    public static String chain(HttpMethod firstMethod, String firstPath,
                               HttpMethod secondMethod, String secondPath,
                               String description) {
        String operation = String.format("%s %s → %s %s",
                firstMethod.value(), firstPath, secondMethod.value(), secondPath);
        return description == null || description.isBlank()
                ? operation
                : String.format("%s %s", operation, description);
    }
}
