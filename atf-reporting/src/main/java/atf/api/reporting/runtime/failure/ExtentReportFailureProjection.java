package atf.api.reporting.runtime.failure;

import atf.api.reporting.asset.ReportAssetComposer;

/** Java facade for the native failure-projection asset. */
public final class ExtentReportFailureProjection {
    private ExtentReportFailureProjection() {
    }

    public static String script() {
        return ReportAssetComposer.extentFailureProjectionScript();
    }
}
