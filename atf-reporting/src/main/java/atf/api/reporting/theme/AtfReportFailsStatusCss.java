package atf.api.reporting.theme;

import atf.api.reporting.asset.ReportAssetComposer;

/** Stable facade for native Fails status badge rules. */
public final class AtfReportFailsStatusCss {
    private AtfReportFailsStatusCss() {
    }

    public static String css() {
        return ReportAssetComposer.themeFailsStatusCss();
    }
}
