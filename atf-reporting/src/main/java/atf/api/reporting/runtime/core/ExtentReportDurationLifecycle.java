package atf.api.reporting.runtime.core;

import atf.api.reporting.asset.ReportAssetComposer;

/** Native-asset facade for the duration lifecycle browser seam. */
final class ExtentReportDurationLifecycle {
    private ExtentReportDurationLifecycle() {
    }

    static String script() {
        return ReportAssetComposer.durationLifecycleScript();
    }
}

