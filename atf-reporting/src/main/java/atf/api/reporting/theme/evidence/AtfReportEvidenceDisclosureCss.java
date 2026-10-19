package atf.api.reporting.theme.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

/** Stable facade for native request/response disclosure rules. */
public final class AtfReportEvidenceDisclosureCss {
    private AtfReportEvidenceDisclosureCss() {
    }

    public static String css() {
        return ReportAssetComposer.evidenceDisclosureCss();
    }
}
