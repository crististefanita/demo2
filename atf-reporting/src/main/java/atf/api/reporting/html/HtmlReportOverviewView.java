package atf.api.reporting.html;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/** Renders only the fallback Overview aggregate. */
public final class HtmlReportOverviewView {
    String render(ReportExecutionModel snapshot) {
        var overview = snapshot.overview();
        return "<section " + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.FallbackTab.OVERVIEW.value()) + " "
                + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.TAB_PANEL) + "><h2>Overview</h2><div "
                + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.SUMMARY) + ">"
                + metric("Tests", overview.tests()) + metric("Passed", overview.passed())
                + metric("Failed", overview.failed()) + metric("Skipped", overview.skipped())
                + "</div><p " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.CONTRACT)
                + ">Overview is the default view; its tab remains last in the navigation contract.</p></section>";
    }

    private static String metric(String label, long value) {
        return "<div " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.METRIC) + "><span>"
                + escape(label) + "</span><strong>" + value + "</strong></div>";
    }
}
