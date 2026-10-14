package atf.api.reporting;

/**
 * Canonical labels shared by every report surface.
 *
 * <p>The Tests tab is the visual source of truth. Fails, Category, Overview
 * projections and the fallback renderer must reuse these labels instead of
 * inventing surface-specific variants.</p>
 */
public final class AtfReportLabels {
    public static final String RESPONSE_DETAILS = "Response details";
    public static final String TECHNICAL_TRACE = "TECHNICAL TRACE";
    public static final String REQUEST_JSON = "REQUEST JSON";
    public static final String RESPONSE_JSON = "RESPONSE JSON";
    public static final String ASSERTION = "ASSERTION";
    public static final String EXPECTED = "EXPECTED";
    public static final String ACTUAL = "ACTUAL";
    public static final String RESULT = "RESULT";

    private AtfReportLabels() {
    }

    public static String responseDetails(int fields) {
        return RESPONSE_DETAILS + " · " + fields + " fields";
    }

    public static String assertions(int rows) {
        return "Assertions · " + rows;
    }

    public static String executionEvidence(int attempts) {
        return "Execution evidence · " + attempts + " attempts";
    }
}
