package atf.api.reporting.runtime.core;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-asset facade for the duration scheduling browser seam. */
final class ExtentReportDurationScheduling {
    private ExtentReportDurationScheduling() {
    }

    static String script() {
        return ReportAssetComposer.durationSchedulingScript();
    }
}

