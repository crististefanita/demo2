package atf.api.reporting.runtime.section;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-resource facade for Overview and Categories label normalization. */
final class ExtentReportRuntimeOverviewLabels {
    private ExtentReportRuntimeOverviewLabels() { }

    static String script() {
        return ReportAssetComposer.extentOverviewLabelsScript();
    }
}
