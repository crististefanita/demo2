package atf.api.reporting.evidence;
import atf.api.core.reporting.duration.AtfDurationFormatter;
import atf.api.reporting.model.ReportField;


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
            rows.add(new String[]{displayName(detail[0]), displayValue(detail[0], detail[1])});
        }
        return rows.toArray(String[][]::new);
    }

    public static String[][] toExtentRowsModel(List<ReportField> details) {
        var rows = new ArrayList<String[]>(details.size() + 1);
        rows.add(new String[]{"FIELD", "VALUE"});
        for (ReportField detail : details) {
            rows.add(new String[]{displayName(detail.name()), displayValue(detail.name(), detail.value())});
        }
        return rows.toArray(String[][]::new);
    }

    /** Canonical typed serializer for Extent evidence tables. */
    public static List<ReportField> technicalTraceFields(List<ReportField> details) {
        if (details == null || details.isEmpty()) return List.of();
        return details.stream()
                .filter(detail -> detail != null)
                .filter(detail -> !isRequestDetail(detail.name()))
                .filter(detail -> !isExecutionSummaryField(detail.name()))
                .toList();
    }

    public static List<ReportField> requestFields(List<ReportField> details) {
        if (details == null || details.isEmpty()) return List.of();
        return details.stream()
                .filter(detail -> detail != null)
                .filter(detail -> isRequestDetail(detail.name()))
                .toList();
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
                || normalized.equals("transport") || normalized.equals("manual replay")
                || normalized.equals("query params") || normalized.equals("request headers")
                || normalized.equals("body") || normalized.equals("request body");
    }

    private static boolean isExecutionSummaryField(String key) {
        if (key == null) return false;
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "duration", "response duration", "response duration (ms)", "response.duration.ms",
                    "total duration", "total duration (ms)", "total.duration.ms", "exchange.duration.ms", "response status",
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
            case "transport" -> "TRANSPORT PROVENANCE";
            case "manual replay" -> "MANUAL REPLAY";
            case "response.status" -> "RESPONSE STATUS";
            case "response.content.type" -> "CONTENT TYPE";
            case "response.duration.ms" -> "RESPONSE DURATION";
            case "exchange.duration.ms" -> "TOTAL DURATION";
            case "response duration", "response duration (ms)" -> "RESPONSE DURATION";
            case "total duration", "total duration (ms)" -> "TOTAL DURATION";
            case "attempt" -> "ATTEMPT";
            default -> key.replace('.', ' ').replace('_', ' ').toUpperCase(Locale.ROOT);
        };
    }

    /** Applies the shared display policy while keeping source keys backward-compatible. */
    public static String displayValue(String key, String value) {
        if (value == null || !isDurationKey(key)) return value == null ? "" : value;
        String normalized = value.trim();
        if (normalized.matches("(?i)^\\d+(?:\\.\\d+)?\\s*s$")) return normalized;
        String millis = normalized.replaceFirst("(?i)\\s*ms$", "").trim();
        if (!millis.matches("\\d+")) return value;
        try {
            return AtfDurationFormatter.formatMillis(Long.parseLong(millis));
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private static boolean isDurationKey(String key) {
        if (key == null) return false;
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "duration", "duration.ms", "response duration", "response duration (ms)", "response.duration.ms",
                    "total duration", "total duration (ms)", "total.duration.ms", "exchange.duration.ms" -> true;
            default -> false;
        };
    }
}
