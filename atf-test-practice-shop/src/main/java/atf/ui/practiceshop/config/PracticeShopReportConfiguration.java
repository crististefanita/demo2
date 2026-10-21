package atf.ui.practiceshop.config;

/** Report-only policy owned by the Practice Shop consumer. */
public record PracticeShopReportConfiguration(int systemBodyPreviewCharacters) {
    public static final int DEFAULT_SYSTEM_BODY_PREVIEW_CHARACTERS = 100;

    public PracticeShopReportConfiguration {
        if (systemBodyPreviewCharacters <= 0) {
            throw new IllegalArgumentException(
                    "reporting.systemBodyPreviewCharacters must be positive");
        }
    }

    public static PracticeShopReportConfiguration defaults() {
        return new PracticeShopReportConfiguration(DEFAULT_SYSTEM_BODY_PREVIEW_CHARACTERS);
    }
}
