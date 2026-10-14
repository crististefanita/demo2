package atf.api.reporting;

import atf.api.core.reporting.AtfReportStatus;

import java.util.List;

import static atf.api.reporting.HtmlReportMarkupSupport.escape;

/** Renders only the fallback Overview aggregate. */
final class HtmlReportOverviewView {
    String render(List<HtmlReportSemanticDocument.ScenarioModel> scenarios) {
        long passed = scenarios.stream().filter(s -> s.status() == AtfReportStatus.PASS).count();
        long failed = scenarios.stream().filter(s -> s.status() == AtfReportStatus.FAIL).count();
        long skipped = scenarios.stream().filter(s -> s.status() == AtfReportStatus.SKIP).count();
        return "<section id=\"overview\" class=\"tab-panel\"><h2>Overview</h2><div class=\"summary\">"
                + metric("Tests", scenarios.size()) + metric("Passed", passed)
                + metric("Failed", failed) + metric("Skipped", skipped)
                + "</div><p class=\"contract\">Overview is the default view; its tab remains last in the navigation contract.</p></section>";
    }

    private static String metric(String label, long value) {
        return "<div class=\"metric\"><span>" + escape(label) + "</span><strong>" + value + "</strong></div>";
    }
}
