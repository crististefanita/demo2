package atf.ui.tests.support;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;

/** Shared assertion and value-normalization primitives for report audits. */
public final class ReportAuditChecks {
    private ReportAuditChecks() { }

    public static AtfAssertion check(String name, String expected, String actual, boolean passed) {
        return new AtfAssertion(name, expected, actual, passed);
    }

    public static AtfReportStatus status(boolean passed) {
        return passed ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
    }

    public static int number(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    public static int number(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
