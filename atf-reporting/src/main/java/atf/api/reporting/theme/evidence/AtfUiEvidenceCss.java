package atf.api.reporting.theme.evidence;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.theme.tokens.AtfReportColors;

import java.util.Locale;
import java.util.Objects;

/**
 * CSS contract for the inline exploratory UI evidence block.
 *
 * <p>The selectors are supplied by the typed HTML vocabulary and the finite
 * colours come from the reporting palette. Runtime capture titles, steps and
 * image data stay outside this class.</p>
 */
public final class AtfUiEvidenceCss {
    private AtfUiEvidenceCss() {
    }

    public static String stylesheet(AtfReportHtmlVocabulary.ElementId sectionId,
                                    AtfReportHtmlVocabulary.CssClass screenshotLinkClass) {
        Objects.requireNonNull(sectionId, "sectionId");
        Objects.requireNonNull(screenshotLinkClass, "screenshotLinkClass");
        String sectionSelector = "#" + sectionId.value();
        String linkClass = screenshotLinkClass.value();
        return String.format(Locale.ROOT,
                "%s{margin-top:24px;padding:18px;border:1px solid %s;border-radius:5px;background:%s;color:%s}"
                        + "%s h2{margin:0 0 12px;font-size:16px;font-weight:400}"
                        + "%s details{margin:10px 0;border:1px solid %s;border-radius:4px;padding:10px;background:%s}"
                        + "%s summary{cursor:pointer;color:%s}"
                        + "%s ol{margin:12px 0;color:%s;font-size:12px}"
                        + "%s .%s{display:inline-flex;align-items:center;align-self:center;justify-content:center;gap:4px;"
                        + "height:18px;min-height:18px;padding:0;line-height:18px;vertical-align:middle;margin:8px 0 0;"
                        + "transform:translateY(2px);text-decoration:none}"
                        + "%s .%s:hover{background:%s;text-decoration:underline}",
                sectionSelector, AtfReportColors.UI_EVIDENCE_BORDER,
                AtfReportColors.UI_EVIDENCE_SURFACE, AtfReportColors.UI_EVIDENCE_TEXT,
                sectionSelector,
                sectionSelector, AtfReportColors.UI_EVIDENCE_BORDER,
                AtfReportColors.UI_EVIDENCE_DETAIL_SURFACE,
                sectionSelector, AtfReportColors.UI_EVIDENCE_SUMMARY,
                sectionSelector, AtfReportColors.UI_EVIDENCE_MUTED,
                sectionSelector, linkClass,
                sectionSelector, linkClass, AtfReportColors.UI_EVIDENCE_HOVER);
    }
}
