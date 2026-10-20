package atf.api.reporting.runtime.section;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-resource facade for Overview chart palette and percentage normalization. */
final class ExtentReportRuntimeOverviewCharts {
    private ExtentReportRuntimeOverviewCharts() { }

    static String script() {
        return ReportAssetComposer.extentOverviewChartsScript();
    }
}
