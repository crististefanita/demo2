package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.reporting.AtfReportStatus;


/** Small, renderer-local policies shared by the Extent tree scopes. */
final class ExtentReportNodeSupport {
    private ExtentReportNodeSupport() { }

    static String renderRequestTitle(String title, String method, String path) {
        String route = ((method == null ? "" : method.trim()) + " "
                + (path == null ? "" : path.trim())).trim();
        String normalizedTitle = title == null ? "" : title.trim();
        return !route.isBlank() ? route : normalizedTitle.isBlank() ? "API request" : normalizedTitle;
    }

    static String normalizeDetailName(String name) {
        if (name == null || name.isBlank()) return "DETAIL";
        return switch (name.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "response.duration.ms", "duration", "duration.ms" -> "response duration";
            case "response.status", "http.status", "status" -> "HTTP STATUS";
            case "route", "request.route" -> "ROUTE TEMPLATE";
            default -> name;
        };
    }

    static String display(String value, ReportingSettings settings) {
        String safe = value == null ? "" : value;
        int limit = settings.assertionMaxDisplayCharacters();
        return truncate(safe, limit);
    }

    static String displayBody(String value, ReportingSettings settings) {
        String safe = value == null ? "" : value;
        return truncate(safe, settings.bodyMaxDisplayCharacters());
    }

    private static String truncate(String value, int limit) {
        return value.length() <= limit ? value : value.substring(0, limit) + "… [truncated]";
    }

    static String resultMarker(AtfReportStatus status) {
        return switch (status) {
            case PASS -> "✅";
            case WARNING -> "⚠️";
            case FAIL -> "❌";
            case SKIP -> "⏭️";
            case INFO -> "ℹ️";
        };
    }
}
