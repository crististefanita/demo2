package atf.api.reporting.markup;
import atf.api.reporting.contract.AtfReportStatus;

import atf.api.reporting.configuration.ReportingSettings;


/** Small, renderer-local policies shared by the Extent tree scopes. */
public final class ExtentReportNodeSupport {
    private ExtentReportNodeSupport() { }

    public static String renderRequestTitle(String title, String method, String path) {
        String route = ((method == null ? "" : method.trim()) + " "
                + (path == null ? "" : path.trim())).trim();
        String normalizedTitle = title == null ? "" : title.trim();
        if (route.isBlank()) return normalizedTitle.isBlank() ? "API request" : normalizedTitle;
        if (normalizedTitle.isBlank() || normalizedTitle.equals(route)) return route;
        if (normalizedTitle.startsWith(route + " — ")
                || normalizedTitle.startsWith(route + " · ")) {
            return normalizedTitle;
        }
        return route + " — " + normalizedTitle;
    }

    public static String normalizeDetailName(String name) {
        if (name == null || name.isBlank()) return "DETAIL";
        return switch (name.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "response.duration.ms", "duration", "duration.ms" -> "response duration";
            case "response.status", "http.status", "status" -> "HTTP STATUS";
            case "route", "request.route" -> "ROUTE TEMPLATE";
            default -> name;
        };
    }

    public static String display(String value, ReportingSettings settings) {
        String safe = value == null ? "" : value;
        int limit = settings.assertionMaxDisplayCharacters();
        return truncate(safe, limit);
    }

    public static String displayBody(String value, ReportingSettings settings) {
        String safe = value == null ? "" : value;
        return truncate(safe, settings.bodyMaxDisplayCharacters());
    }

    private static String truncate(String value, int limit) {
        return value.length() <= limit ? value : value.substring(0, limit) + "… [truncated]";
    }

    public static String resultMarker(AtfReportStatus status) {
        return AtfReportStatus.orInfo(status).marker();
    }
}
