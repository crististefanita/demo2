package atf.api.reporting.runtime.section;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-resource facade for hash routing, tab activation and runtime bootstrap. */
final class ExtentReportRuntimeNavigationController {
    private ExtentReportRuntimeNavigationController() { }

    static String script() {
        return ReportAssetComposer.extentNavigationControllerScript();
    }
}
