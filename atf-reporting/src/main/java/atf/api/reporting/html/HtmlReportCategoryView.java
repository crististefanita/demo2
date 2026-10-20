package atf.api.reporting.html;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/** Renders the fallback Category projection without owning semantic state. */
public final class HtmlReportCategoryView {
    String render(ReportExecutionModel snapshot) {
        var html = new StringBuilder("<section ")
                .append(AtfReportHtmlVocabulary.idAttribute(
                        AtfReportHtmlVocabulary.FallbackTab.CATEGORY.value()))
                .append(' ')
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.TAB_PANEL))
                .append("><h2>Category</h2><div ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.TAG_LIST))
                .append(">");
        snapshot.categories().tags().forEach(tag -> html.append("<span ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.TAG))
                .append(">")
                .append(escape(tag)).append("</span>"));
        return html.append("</div></section>").toString();
    }
}
