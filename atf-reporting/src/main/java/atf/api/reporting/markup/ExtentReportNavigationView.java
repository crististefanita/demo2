package atf.api.reporting.markup;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;

/**
 * Owns the static navigation and Fails-view markup injected into the vendor
 * document. Runtime behaviour is deliberately kept in the client behaviour
 * layer; this class only establishes the stable DOM contract.
 */
public final class ExtentReportNavigationView {
    private ExtentReportNavigationView() {
    }

    public static String inject(String html) {
        String dashboardNav = "<li class=\"nav-item dropdown\">"
                + "<a id=\"nav-overview\" class=\"dropdown-toggle\" href=\"#"
                + AtfReportHtmlVocabulary.ViewId.OVERVIEW.value()
                + "\" title=\"Overview\" aria-label=\"Overview\">"
                + "<span class=\"ico\"><i class=\"fa fa-bar-chart\"></i></span></a></li>";
        String failsNav = "<li class=\"nav-item dropdown\">"
                + "<a id=\"nav-fails\" class=\"dropdown-toggle\" href=\"#"
                + AtfReportHtmlVocabulary.ViewId.FAILS.value()
                + "\" title=\"Fails\" aria-label=\"Fails\">"
                + "<span class=\"ico\"><i class=\"fa fa-bug\"></i></span></a></li>";
        html = html.replaceFirst("(?s)<li class=\"nav-item dropdown\" onclick=\"toggleView\\('"
                        + AtfReportHtmlVocabulary.ViewId.OVERVIEW.value() + "'\\)\">.*?</li>",
                failsNav + dashboardNav);
        String failures = "<div " + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ViewId.FAILS) + " class=\"test-wrapper row view exception-view hide "
                + AtfReportHtmlVocabulary.CssClass.ATF_FAILS_VIEW.value() + "\" "
                + AtfReportHtmlVocabulary.attribute(AtfReportHtmlVocabulary.Attribute.TAB,
                AtfReportHtmlVocabulary.TabKey.FAILS.value()) + ">"
                + "<div class=\"test-list " + AtfReportHtmlVocabulary.CssClass.ATF_FAILS_LIST.value()
                + "\"><div class=\"test-list-tools\"><ul class=\"tools\"><li><a href=\"#"
                + AtfReportHtmlVocabulary.ViewId.FAILS.value()
                + "\"><span class=\"font-size-14\">Fails</span></a></li></ul></div>"
                + "<div class=\"test-list-wrapper\"><div "
                + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ElementId.FAILURE_LIST)
                + " class=\"test-list-item\"></div></div></div>"
                + "<div class=\"test-content open\"><div class=\"test-content-detail\"><div "
                + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ElementId.FAILURE_DETAIL)
                + " class=\"detail-body\"></div></div></div></div>";
        html = html.replace(" onclick=\"toggleView('test-view')\"", "")
                .replace(" onclick=\"toggleView('category-view')\"", "");
        html = html.replace("id=\"nav-test\" class=\"dropdown-toggle\" href=\"#\"",
                        "id=\"nav-test\" class=\"dropdown-toggle\" href=\"#test-view\"")
                .replace("id=\"nav-category\" class=\"dropdown-toggle\" href=\"#\"",
                        "id=\"nav-category\" class=\"dropdown-toggle\" href=\"#category-view\"");
        return html.replace("<div class=\"container-fluid p-4 view dashboard-view\">", failures
                + "<div " + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ViewId.OVERVIEW) + " "
                + AtfReportHtmlVocabulary.attribute(AtfReportHtmlVocabulary.Attribute.TAB,
                AtfReportHtmlVocabulary.TabKey.OVERVIEW.value())
                + " class=\"container-fluid p-4 view dashboard-view\">");
    }

}
