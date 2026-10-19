package atf.api.reporting.theme.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

/** Stable facade for native evidence and validation table rules. */
public final class AtfReportEvidenceTableCss {
    private AtfReportEvidenceTableCss() {
    }

    public static String css() {
        return ReportAssetComposer.evidenceTableCss();
    }
}
