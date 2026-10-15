package atf.api.reporting;

import com.aventstack.extentreports.markuputils.Markup;

/** Builds the semantic evidence tables consumed by the shared report theme. */
public final class AtfReportMarkup {
    private AtfReportMarkup() {
    }

    public static Markup table(String cssClass, String[][] rows) {
        return () -> tableHtml(cssClass, rows);
    }

    public static String tableHtml(String cssClass, String[][] rows) {
        var html = new StringBuilder();
        if (isTraceTable(cssClass)) {
            html.append("<div class=\"helm-api-classification technical\"><span class=\"helm-api-classification-label\">")
                    .append(AtfReportLabels.TECHNICAL_TRACE)
                    .append("</span></div>");
        }
        html.append("<table class=\"").append(escape(cssClass)).append("\">");
        if (isKeyValueEvidenceTable(cssClass) && rows != null && rows.length > 0) {
            html.append("<colgroup><col class=\"atf-evidence-key-column\" style=\"width:var(--atf-evidence-key-column-width)\">")
                    .append("<col class=\"atf-evidence-value-column\"></colgroup>");
        }
        html.append("<thead><tr>");
        if (rows == null || rows.length == 0) {
            return html.append("</tr></thead><tbody></tbody></table>").toString();
        }
        for (String value : rows[0]) {
            html.append("<th>").append(escape(value)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (int rowIndex = 1; rowIndex < rows.length; rowIndex++) {
            html.append("<tr>");
            for (String value : rows[rowIndex]) {
                html.append("<td>").append(escape(value)).append("</td>");
            }
            html.append("</tr>");
        }
        return html.append("</tbody></table>").toString();
    }

    private static boolean isTraceTable(String cssClass) {
        return cssClass != null && cssClass.contains("helm-trace-table");
    }

    private static boolean isKeyValueEvidenceTable(String cssClass) {
        return cssClass != null && (cssClass.contains("helm-trace-table")
                || cssClass.contains("helm-request-table")
                || cssClass.contains("execution-table"));
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
