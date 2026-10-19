package atf.api.reporting.runtime.core;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-asset facade for the duration memory browser seam. */
final class ExtentReportDurationMemory {
    private ExtentReportDurationMemory() {
    }

    static String script() {
        return ReportAssetComposer.durationMemoryScript();
    }
}

