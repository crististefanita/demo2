package atf.ui.practiceshop.config;

import atf.api.reporting.evidence.ReportScreenshotPolicy;

import java.util.Objects;

/** Report-only policy owned by the Practice Shop consumer. */
public record PracticeShopReportConfiguration(int systemBodyPreviewCharacters,
                                              ReportScreenshotPolicy screenshotPolicy) {
    public static final int DEFAULT_SYSTEM_BODY_PREVIEW_CHARACTERS = 100;

    public PracticeShopReportConfiguration(int systemBodyPreviewCharacters) {
        this(systemBodyPreviewCharacters, ReportScreenshotPolicy.EACH_CHECKPOINT);
    }

    public PracticeShopReportConfiguration {
        if (systemBodyPreviewCharacters <= 0) {
            throw new IllegalArgumentException(
                    "reporting.systemBodyPreviewCharacters must be positive");
        }
        screenshotPolicy = Objects.requireNonNull(screenshotPolicy, "screenshotPolicy");
    }

    public static PracticeShopReportConfiguration defaults() {
        return new PracticeShopReportConfiguration(DEFAULT_SYSTEM_BODY_PREVIEW_CHARACTERS,
                ReportScreenshotPolicy.EACH_CHECKPOINT);
    }
}
