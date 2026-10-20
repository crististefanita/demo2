package atf.api.reporting.evidence.markup;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;

import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.ARIA_EXPANDED;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.ARIA_HIDDEN;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.AttributeValue.FALSE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.DISCLOSURE_GLYPH;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.SE_TITLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.STEP_DETAIL;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.DETAILS;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.SPAN;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.SUMMARY;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.attribute;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.classAttribute;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.closeTag;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.openTag;

/** Shared semantic primitives for Extent and fallback evidence renderers. */
public final class ReportEvidenceMarkup {
    private ReportEvidenceMarkup() {
    }

    public static String disclosureStart(String kind, String title) {
        String rawTitle = title == null ? "" : title;
        String safeTitle = AtfReportHtmlVocabulary.escapeHtml(rawTitle);
        return openTag(DETAILS, classAttribute(STEP_DETAIL.value(), kind))
                + openTag(SUMMARY, attribute(ARIA_EXPANDED, FALSE))
                + openTag(SPAN, classAttribute(DISCLOSURE_GLYPH),
                attribute(ARIA_HIDDEN, AtfReportHtmlVocabulary.AttributeValue.TRUE))
                + closeTag(SPAN)
                + openTag(SPAN, classAttribute(SE_TITLE),
                attribute(AtfReportHtmlVocabulary.Attribute.TITLE, rawTitle),
                attribute(AtfReportHtmlVocabulary.Attribute.ARIA_LABEL, rawTitle))
                + safeTitle
                + closeTag(SPAN)
                + closeTag(SUMMARY)
                + openTag(AtfReportHtmlVocabulary.Tag.DIV,
                classAttribute(AtfReportHtmlVocabulary.CssClass.BODY));
    }

    public static String disclosureEnd() {
        return closeTag(AtfReportHtmlVocabulary.Tag.DIV) + closeTag(DETAILS);
    }

    public static String disclosure(String kind, String title, String body) {
        return disclosureStart(kind, title) + body + disclosureEnd();
    }
}
