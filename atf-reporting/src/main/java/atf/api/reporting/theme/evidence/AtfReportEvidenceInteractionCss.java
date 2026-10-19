package atf.api.reporting.theme.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

/** Stable facade for the native UI interaction evidence stylesheet. */
public final class AtfReportEvidenceInteractionCss {
    private AtfReportEvidenceInteractionCss() {
    }

    public static String css() {
        return ReportAssetComposer.evidenceInteractionCss();
    }
}
