package atf.api.reporting.theme.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

/** Stable facade for the native evidence stack and responsive geometry rules. */
public final class AtfEvidenceStackCss {
    private AtfEvidenceStackCss() {
    }

    public static String css() {
        return ReportAssetComposer.evidenceStackCss();
    }
}
