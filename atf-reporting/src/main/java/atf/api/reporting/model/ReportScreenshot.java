package atf.api.reporting.model;

import atf.api.reporting.markup.AtfReportLabels;

import java.util.Objects;

/** Immutable screenshot evidence item shared by Extent and fallback renderers. */
public record ReportScreenshot(String title, String base64Png) {
    public ReportScreenshot {
        title = title == null || title.isBlank() ? AtfReportLabels.UI_SCREENSHOT : title.trim();
        base64Png = requirePayload(base64Png);
    }

    private static String requirePayload(String value) {
        String normalized = Objects.requireNonNull(value, "base64Png").trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("base64Png must not be blank");
        }
        return normalized;
    }
}
