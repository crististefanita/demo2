package atf.api.reporting;

/**
 * Owns the static navigation and Fails-view markup injected into the vendor
 * document. Runtime behaviour is deliberately kept in the client behaviour
 * layer; this class only establishes the stable DOM contract.
 */
final class ExtentReportNavigationView {
    private ExtentReportNavigationView() {
    }

    static String inject(String html) {
        String dashboardNav = "<li class=\"nav-item dropdown\" onclick=\"" + guardedActivation("dashboard-view") + "\">"
                + "<a id=\"nav-overview\" class=\"dropdown-toggle\" href=\"#dashboard-view\" title=\"Overview\" aria-label=\"Overview\">"
                + "<span class=\"ico\"><i class=\"fa fa-bar-chart\"></i></span></a></li>";
        String failsNav = "<li class=\"nav-item dropdown\" onclick=\"" + guardedActivation("atf-fails-view") + "\">"
                + "<a id=\"nav-fails\" class=\"dropdown-toggle\" href=\"#atf-fails-view\" title=\"Fails\" aria-label=\"Fails\">"
                + "<span class=\"ico\"><i class=\"fa fa-bug\"></i></span></a></li>";
        html = html.replaceFirst("(?s)<li class=\"nav-item dropdown\" onclick=\"toggleView\\('dashboard-view'\\)\">.*?</li>",
                failsNav + dashboardNav);
        String failures = "<div id=\"atf-fails-view\" class=\"test-wrapper row view exception-view hide atf-fails-view\" data-atf-tab=\"fails\">"
                + "<div class=\"test-list atf-fails-list\"><div class=\"test-list-tools\"><ul class=\"tools\"><li><a href=\"#atf-fails-view\"><span class=\"font-size-14\">Fails</span></a></li></ul></div>"
                + "<div class=\"test-list-wrapper\"><div id=\"atf-failure-list\" class=\"test-list-item\"></div></div></div>"
                + "<div class=\"test-content open\"><div class=\"test-content-detail\"><div id=\"atf-failure-detail\" class=\"detail-body\"></div></div></div></div>";
        html = html.replace("onclick=\"toggleView('test-view')\"",
                        "onclick=\"" + guardedActivation("test-view") + "\"")
                .replace("onclick=\"toggleView('category-view')\"",
                        "onclick=\"" + guardedActivation("category-view") + "\"");
        html = html.replace("id=\"nav-test\" class=\"dropdown-toggle\" href=\"#\"",
                        "id=\"nav-test\" class=\"dropdown-toggle\" href=\"#test-view\"")
                .replace("id=\"nav-category\" class=\"dropdown-toggle\" href=\"#\"",
                        "id=\"nav-category\" class=\"dropdown-toggle\" href=\"#category-view\"");
        return html.replace("<div class=\"container-fluid p-4 view dashboard-view\">", failures
                + "<div id=\"dashboard-view\" data-atf-tab=\"overview\" class=\"container-fluid p-4 view dashboard-view\">");
    }

    private static String guardedActivation(String viewId) {
        return "if(typeof atfActivate==='function'){window.location.hash='#" + viewId
                + "';atfActivate('" + viewId + "');return false;}return true;";
    }
}
