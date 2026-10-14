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
