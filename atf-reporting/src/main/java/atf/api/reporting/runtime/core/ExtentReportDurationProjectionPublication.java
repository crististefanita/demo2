package atf.api.reporting.runtime.core;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-asset facade for the duration projection publication browser seam. */
final class ExtentReportDurationProjectionPublication {
    private ExtentReportDurationProjectionPublication() {
    }

    static String script() {
        return ReportAssetComposer.durationProjectionPublicationScript();
    }
}

