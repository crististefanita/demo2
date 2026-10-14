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
        if (isTraceTable(cssClass) && rows != null && rows.length > 0) {
            html.append("<colgroup><col class=\"atf-trace-field-column\" style=\"")
                    .append(traceFieldStyle(rows)).append(";max-width:50%\">")
                    .append("<col class=\"atf-trace-value-column\"></colgroup>");
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

    private static String traceFieldStyle(String[][] rows) {
        int longest = 0;
        for (int rowIndex = 1; rowIndex < rows.length; rowIndex++) {
            if (rows[rowIndex] != null && rows[rowIndex].length > 0 && rows[rowIndex][0] != null) {
                longest = Math.max(longest, rows[rowIndex][0].length());
            }
        }
        int width = longest + 4;
        return width >= 50 ? "width:50%" : "width:" + width + "ch";
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
