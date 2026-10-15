package atf.api.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Gives request evidence one stable, human-readable vocabulary. */
public final class EvidenceTableFormatter {
    private EvidenceTableFormatter() {
    }

    public static String[][] toExtentRows(List<String[]> details) {
        var rows = new ArrayList<String[]>(details.size() + 1);
        rows.add(new String[]{"FIELD", "VALUE"});
        for (String[] detail : details) {
            rows.add(new String[]{displayName(detail[0]), detail[1] == null ? "" : detail[1]});
        }
        return rows.toArray(String[][]::new);
    }

    /**
     * Keeps the technical trace focused on request context and response
     * metadata. HTTP status is intentionally visible here as the primary
     * response verdict; the execution-evidence disclosure keeps the complete
     * attempt history and timing details. The request URL remains visible here
     * because it identifies the operation a reader is inspecting.
     */
    public static List<String[]> technicalTraceDetails(List<String[]> details) {
        if (details == null || details.isEmpty()) return List.of();
        return details.stream()
                .filter(detail -> detail != null && detail.length > 0)
                .filter(detail -> !isRequestDetail(detail[0]))
                .filter(detail -> !isExecutionSummaryField(detail[0]))
                .toList();
    }

    public static List<String[]> requestDetails(List<String[]> details) {
        if (details == null || details.isEmpty()) return List.of();
        return details.stream()
                .filter(detail -> detail != null && detail.length > 0)
                .filter(detail -> isRequestDetail(detail[0]))
                .toList();
    }

    public static boolean isRequestDetail(String key) {
        if (key == null) return false;
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("method") || normalized.equals("path")
                || normalized.equals("url") || normalized.equals("route")
                || normalized.equals("route template") || normalized.equals("request.route")
                || normalized.startsWith("request.") || normalized.startsWith("header.")
                || normalized.startsWith("headers.") || normalized.startsWith("param.")
                || normalized.startsWith("params.") || normalized.startsWith("query.")
                || normalized.startsWith("auth.") || normalized.equals("authorization")
                || normalized.equals("auth profile") || normalized.equals("auth type")
                || normalized.equals("query params") || normalized.equals("request headers")
                || normalized.equals("body") || normalized.equals("request body");
    }

    private static boolean isExecutionSummaryField(String key) {
        if (key == null) return false;
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "duration", "response duration (ms)", "response.duration.ms",
                    "total duration (ms)", "total.duration.ms", "response status",
                    "response.status", "attempt", "attempts", "response.url" -> true;
            default -> false;
        };
    }

    public static String displayName(String key) {
        if (key == null || key.isBlank()) return "DETAIL";
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "method" -> "METHOD";
            case "path" -> "PATH";
            case "url" -> "URL";
            case "auth.mode", "auth.profile" -> "AUTHENTICATION";
            case "response.status" -> "RESPONSE STATUS";
            case "response.content.type" -> "CONTENT TYPE";
            case "response.duration.ms" -> "RESPONSE DURATION (ms)";
            case "exchange.duration.ms" -> "TOTAL DURATION (ms)";
            case "response duration (ms)" -> "RESPONSE DURATION (ms)";
            case "total duration (ms)" -> "TOTAL DURATION (ms)";
            case "attempt" -> "ATTEMPT";
            default -> key.replace('.', ' ').replace('_', ' ').toUpperCase(Locale.ROOT);
        };
    }
}
