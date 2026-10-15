package atf.api.reporting;

/** Builds the secondary, collapsed-by-default overview audit tables. */
final class ExtentReportOverviewMarkup {
    private ExtentReportOverviewMarkup() {
    }

    static String inject(String html) {
        // Extent Spark emits two valid overview shapes across versions: in
        // one the category card starts a fresh row, in another it follows the
        // preceding card directly. Anchor to the semantic category container,
        // not to incidental row whitespace, so the ATF overview contract stays
        // stable when the vendor changes its grid markup.
        String rowMarker = "<div class=\"row\">\n<div class=\"col-lg-6 col-md-12 category-container\">";
        String marker = html.contains(rowMarker)
                ? rowMarker
                : "<div class=\"col-lg-6 col-md-12 category-container\">";
        String metadata = "<details id=\"atf-execution-metadata\" class=\"atf-secondary-overview\">"
                + "<summary><span class=\"atf-secondary-overview-title\">ATF execution metadata (secondary)</span>"
                + "<span class=\"atf-secondary-overview-badge\">secondary metadata</span></summary>"
                + "<div class=\"atf-secondary-body\"><div id=\"atf-execution-metadata-grid\" class=\"atf-secondary-grid\"></div></div></details>"
                + "<details id=\"atf-tag-audit\" class=\"atf-secondary-overview atf-tag-audit-disclosure\">"
                + "<summary><span class=\"atf-secondary-overview-title\">ATF tag audit (secondary navigation metadata)</span>"
                + "<span class=\"atf-secondary-overview-badge\">collapsed by default</span></summary>"
                + "<div class=\"atf-tag-audit-body\"><div class=\"atf-tag-grid\">"
                + auditPanel("ATF scenario identities by test class", "atf-audit-scenarios", "NAME|COUNT|SHARE")
                + auditPanel("Test packages", "atf-audit-packages", "NAME|COUNT|SHARE")
                + auditPanel("Other annotations / tags", "atf-audit-tags", "NAME|COUNT")
                + "</div></div></details>";
        int markerIndex = html.indexOf(marker);
        if (markerIndex < 0) return html;
        return html.substring(0, markerIndex) + metadata + html.substring(markerIndex);
    }

    private static String auditPanel(String title, String id, String headers) {
        String suffix = "atf-audit-scenarios".equals(id) ? "classes"
                : "atf-audit-tags".equals(id) ? "other" : id.substring("atf-audit-".length());
        StringBuilder table = new StringBuilder("<div class=\"card atf-tag-card atf-tag-card-")
                .append(suffix).append("\"><div class=\"card-header\"><p>")
                .append(title).append("</p></div><div class=\"card-body\"><div class=\"atf-tag-scroll\"><table class=\"table table-sm atf-tag-table\" id=\"")
                .append(id).append("\"><thead><tr>");
        for (String header : headers.split("\\|")) {
            table.append("<th>").append(header).append("</th>");
        }
        return table.append("</tr></thead><tbody></tbody></table></div></div></div>").toString();
    }
}
