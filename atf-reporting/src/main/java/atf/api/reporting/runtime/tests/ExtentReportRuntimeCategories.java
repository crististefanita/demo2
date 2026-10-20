package atf.api.reporting.runtime.tests;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-resource facade for Categories filtering and linked detail projection. */
public final class ExtentReportRuntimeCategories {
    private ExtentReportRuntimeCategories() { }

    public static String script() {
        return ReportAssetComposer.extentCategoriesScript();
    }
}
