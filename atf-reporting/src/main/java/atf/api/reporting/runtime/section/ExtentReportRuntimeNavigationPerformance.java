package atf.api.reporting.runtime.section;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-resource facade for optional browser runtime performance diagnostics. */
final class ExtentReportRuntimeNavigationPerformance {
    private ExtentReportRuntimeNavigationPerformance() { }

    static String script() {
        return ReportAssetComposer.extentNavigationPerformanceScript();
    }
}
