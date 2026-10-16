package atf.api.reporting;

import java.util.List;

import static atf.api.reporting.HtmlReportMarkupSupport.escape;

/** Renders the fallback Category projection without owning semantic state. */
final class HtmlReportCategoryView {
    String render(HtmlReportSemanticSnapshot snapshot) {
        var html = new StringBuilder("<section id=\"category\" class=\"tab-panel\"><h2>Category</h2><div class=\"tag-list\">");
        snapshot.scenarios().stream().flatMap(scenario -> scenario.tags().stream()).distinct().sorted()
                .forEach(tag -> html.append("<span class=\"tag\">").append(escape(tag)).append("</span>"));
        return html.append("</div></section>").toString();
    }
}
