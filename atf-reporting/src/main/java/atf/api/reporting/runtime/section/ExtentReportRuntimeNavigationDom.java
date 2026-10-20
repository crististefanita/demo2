package atf.api.reporting.runtime.section;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-resource facade for DOM normalization and runtime contract projection. */
final class ExtentReportRuntimeNavigationDom {
    private ExtentReportRuntimeNavigationDom() { }

    static String script() {
        return ReportAssetComposer.extentNavigationDomScript();
    }
}
