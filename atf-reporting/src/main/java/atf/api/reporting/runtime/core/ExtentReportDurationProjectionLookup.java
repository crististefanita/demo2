package atf.api.reporting.runtime.core;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-asset facade for the duration projection lookup browser seam. */
final class ExtentReportDurationProjectionLookup {
    private ExtentReportDurationProjectionLookup() {
    }

    static String script() {
        return ReportAssetComposer.durationProjectionLookupScript();
    }
}

