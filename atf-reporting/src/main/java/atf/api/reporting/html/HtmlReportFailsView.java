package atf.api.reporting.html;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/** Renders the fallback Fails index from validation assertions only. */
public final class HtmlReportFailsView {
    String render(ReportExecutionModel snapshot) {
        var html = new StringBuilder("<section ")
                .append(AtfReportHtmlVocabulary.idAttribute(
                        AtfReportHtmlVocabulary.FallbackTab.FAILS.value()))
                .append(' ')
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.TAB_PANEL))
                .append("><h2>Fails</h2><div ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.FAIL_LIST))
                .append(">");
        for (var failure : snapshot.failures().items()) {
            html.append("<a ").append(AtfReportHtmlVocabulary.classAttribute(
                            AtfReportHtmlVocabulary.CssClass.FAIL_ROW))
                    .append(" href=\"#").append(failure.scenarioId()).append("\">")
                    .append("<span ").append(AtfReportHtmlVocabulary.classAttribute(
                            AtfReportHtmlVocabulary.statusClasses(AtfReportStatus.FAIL)))
                    .append(">Fail</span><strong>")
                    .append(escape(failure.assertionName())).append("</strong><span>")
                    .append(escape(failure.actual())).append("</span></a>");
        }
        if (snapshot.failures().isEmpty()) {
            html.append("<p ").append(AtfReportHtmlVocabulary.classAttribute(
                    AtfReportHtmlVocabulary.CssClass.EMPTY))
                    .append(">No failed assertions.</p>");
        }
        return html.append("</div></section>").toString();
    }
}
