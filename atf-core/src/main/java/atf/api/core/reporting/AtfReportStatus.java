package atf.api.core.reporting;

/** Status independent of JUnit, Extent or the HTML renderer. */
public enum AtfReportStatus {
    INFO(0),
    PASS(1),
    WARNING(2),
    SKIP(3),
    FAIL(4);

    private final int severity;

    AtfReportStatus(int severity) {
        this.severity = severity;
    }

    public int severity() {
        return severity;
    }

    public static AtfReportStatus worst(AtfReportStatus left, AtfReportStatus right) {
        return left.severity >= right.severity ? left : right;
    }
}
