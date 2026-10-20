package atf.api.reporting.html;
import atf.api.reporting.model.ReportExecutionModel;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;


import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/** Renders only the fallback Overview aggregate. */
public final class HtmlReportOverviewView {
    String render(ReportExecutionModel snapshot) {
        var overview = snapshot.overview();
        return "<section " + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.FallbackTab.OVERVIEW.value()) + " "
                + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.TAB_PANEL) + "><h2>Overview</h2><div class=\"summary\">"
                + metric("Tests", overview.tests()) + metric("Passed", overview.passed())
                + metric("Failed", overview.failed()) + metric("Skipped", overview.skipped())
                + "</div><p class=\"contract\">Overview is the default view; its tab remains last in the navigation contract.</p></section>";
    }

    private static String metric(String label, long value) {
        return "<div class=\"metric\"><span>" + escape(label) + "</span><strong>" + value + "</strong></div>";
    }
}
