package atf.api.reporting.markup;

/** Canonical metadata keys and values shared by report evidence publishers. */
public final class AtfReportEvidenceKeys {
    public static final String REQUEST_ACTION = "request.action";
    public static final String REQUEST_TARGET = "request.target";
    public static final String OUTCOME_TYPE = "outcome.type";
    public static final String ARTIFACT_PUBLICATION = "artifact-publication";
    public static final String EVIDENCE_TYPE = "evidence.type";
    public static final String EVIDENCE_POLICY = "evidence.policy";
    public static final String REPORT_SETUP = "report-setup";
    public static final String SCREEN_VALIDATION = "screen-validation";
    public static final String UI_FAILURE = "ui-failure";
    public static final String SETUP_RESULT = "setup.result";
    public static final String SETUP_CHECK_PREFIX = "setup.check.";

    private AtfReportEvidenceKeys() { }

    public static String setupCheck(int index) {
        if (index < 1) throw new IllegalArgumentException("index must be >= 1");
        return SETUP_CHECK_PREFIX + index;
    }
}
