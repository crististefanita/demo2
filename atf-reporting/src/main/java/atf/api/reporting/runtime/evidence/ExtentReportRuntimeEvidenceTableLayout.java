package atf.api.reporting.runtime.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

/** Java facade for the native evidence-table-layout asset. */
public final class ExtentReportRuntimeEvidenceTableLayout {
    private ExtentReportRuntimeEvidenceTableLayout() {
    }

    public static String script() {
        return ReportAssetComposer.extentEvidenceTableLayoutScript();
    }
}
