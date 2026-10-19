package atf.api.reporting.runtime.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

/** Java facade for the native evidence-failures asset. */
public final class ExtentReportRuntimeEvidenceFailures {
    private ExtentReportRuntimeEvidenceFailures() {
    }

    public static String script() {
        return ReportAssetComposer.extentEvidenceFailuresScript();
    }
}
