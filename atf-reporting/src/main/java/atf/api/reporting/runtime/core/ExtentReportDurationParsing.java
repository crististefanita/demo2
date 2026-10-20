package atf.api.reporting.runtime.core;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-asset facade for the duration parsing browser seam. */
final class ExtentReportDurationParsing {
    private ExtentReportDurationParsing() {
    }

    static String script() {
        return ReportAssetComposer.durationParsingScript();
    }
}

