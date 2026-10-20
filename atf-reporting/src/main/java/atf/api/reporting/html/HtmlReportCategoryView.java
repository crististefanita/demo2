package atf.api.reporting.html;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/** Renders the fallback Category projection without owning semantic state. */
public final class HtmlReportCategoryView {
    String render(ReportExecutionModel snapshot) {
        var tags = new StringBuilder();
        snapshot.categories().tags().forEach(tag -> tags.append(
                AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.SPAN,
                        escape(tag), AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.TAG))));
        String body = AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.H2, "Category")
                + AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.DIV, tags.toString(),
                AtfReportHtmlVocabulary.classAttribute(AtfReportHtmlVocabulary.CssClass.TAG_LIST));
        return AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.SECTION, body,
                AtfReportHtmlVocabulary.idAttribute(
                        AtfReportHtmlVocabulary.FallbackTab.CATEGORY.value()),
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.TAB_PANEL));
    }
}
