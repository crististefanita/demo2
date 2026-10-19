package atf.api.reporting.runtime.section;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-resource facade for scenario, phase and node disclosure behavior. */
final class ExtentReportRuntimeNavigationDisclosure {
    private ExtentReportRuntimeNavigationDisclosure() { }

    static String script() {
        return ReportAssetComposer.extentNavigationDisclosureScript();
    }
}
