package atf.api.reporting.contract;

import java.util.Locale;

/** Renderer-neutral status vocabulary owned by the standalone reporting module. */
public enum AtfReportStatus {
    INFO(0), PASS(1), WARNING(2), SKIP(3), FAIL(4);

    private final int severity;

    AtfReportStatus(int severity) { this.severity = severity; }

    public int severity() { return severity; }

    public String displayValue() { return name().toLowerCase(Locale.ROOT); }

    public String displayLabel() {
        String value = displayValue();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public static AtfReportStatus worst(AtfReportStatus left, AtfReportStatus right) {
        return left.severity >= right.severity ? left : right;
    }

    public static AtfReportStatus fromDisplayName(String label) {
        if (label == null || label.isBlank()) return INFO;
        return switch (label.trim().toUpperCase(Locale.ROOT)) {
            case "PASS" -> PASS;
            case "WARNING", "WARN" -> WARNING;
            case "SKIP", "SKIPPED" -> SKIP;
            case "FAIL", "FAILED", "ERROR" -> FAIL;
            default -> INFO;
        };
    }
}
