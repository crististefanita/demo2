package atf.api.reporting.html;
import atf.api.reporting.markup.AtfReportLabels;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/** Renders only the fallback Overview aggregate. */
public final class HtmlReportOverviewView {
    String render(ReportExecutionModel snapshot) {
        var overview = snapshot.overview();
        String summary = metric("Tests", overview.tests()) + metric("Passed", overview.passed())
                + metric("Failed", overview.failed()) + metric("Skipped", overview.skipped());
        String body = AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.H2,
                        AtfReportHtmlVocabulary.FallbackTab.OVERVIEW.label())
                + AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.DIV, summary,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.SUMMARY))
                + AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.P,
                AtfReportLabels.OVERVIEW_DEFAULT_COPY,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.CONTRACT));
        return AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.SECTION, body,
                AtfReportHtmlVocabulary.idAttribute(
                        AtfReportHtmlVocabulary.FallbackTab.OVERVIEW.value()),
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.TAB_PANEL));
    }

    private static String metric(String label, long value) {
        return AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.DIV,
                AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.SPAN, escape(label))
                        + AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.STRONG,
                        Long.toString(value)),
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.METRIC));
    }
}
