package atf.api.reporting;

import atf.api.core.reporting.AtfReportStatus;

import java.util.List;

import static atf.api.reporting.HtmlReportMarkupSupport.escape;

/** Renders the fallback Fails index from validation assertions only. */
final class HtmlReportFailsView {
    String render(List<HtmlReportSemanticDocument.ScenarioModel> scenarios) {
        var html = new StringBuilder("<section id=\"fails\" class=\"tab-panel\"><h2>Fails</h2><div class=\"fail-list\">");
        boolean any = false;
        for (var scenario : scenarios) {
            for (var phase : scenario.phases) {
                for (var child : phase.children) {
                    if (!(child instanceof HtmlReportSemanticDocument.ValidationModel validation)) {
                        continue;
                    }
                    for (var row : validation.assertions) {
                        if (row.resultStatus() != AtfReportStatus.FAIL
                                && (row.passed() || validation.status != AtfReportStatus.FAIL)) {
                            continue;
                        }
                        any = true;
                        html.append("<a class=\"fail-row\" href=\"#").append(scenario.id).append("\">")
                                .append("<span class=\"status status-fail\">Fail</span><strong>")
                                .append(escape(row.name())).append("</strong><span>")
                                .append(escape(row.actual())).append("</span></a>");
                    }
                }
            }
        }
        if (!any) {
            html.append("<p class=\"empty\">No failed assertions.</p>");
        }
        return html.append("</div></section>").toString();
    }
}
