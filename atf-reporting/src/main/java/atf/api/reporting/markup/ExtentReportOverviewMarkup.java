package atf.api.reporting.markup;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;

/** Builds the secondary, collapsed-by-default overview audit tables. */
public final class ExtentReportOverviewMarkup {
    private ExtentReportOverviewMarkup() {
    }

    public static String inject(String html) {
        // Extent Spark emits two valid overview shapes across versions: in
        // one the category card starts a fresh row, in another it follows the
        // preceding card directly. Anchor to the semantic category container,
        // not to incidental row whitespace, so the ATF overview contract stays
        // stable when the vendor changes its grid markup.
        String rowMarker = "<div class=\"row\">\n<div class=\"col-lg-6 col-md-12 category-container\">";
        String marker = html.contains(rowMarker)
                ? rowMarker
                : "<div class=\"col-lg-6 col-md-12 category-container\">";
        String metadata = "<details " + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ElementId.EXECUTION_METADATA) + " "
                + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_OVERVIEW) + ">"
                + "<summary><span " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_OVERVIEW_TITLE)
                + ">ATF execution metadata (secondary)</span>"
                + "<span " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_OVERVIEW_BADGE)
                + ">secondary metadata</span></summary>"
                + "<div " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_BODY) + "><div "
                + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ElementId.EXECUTION_METADATA_GRID) + " "
                + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_GRID)
                + "></div></div></details>"
                + "<details " + AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ElementId.TAG_AUDIT) + " "
                + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_OVERVIEW,
                AtfReportHtmlVocabulary.CssClass.ATF_TAG_AUDIT_DISCLOSURE) + ">"
                + "<summary><span " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_OVERVIEW_TITLE)
                + ">ATF tag audit (secondary navigation metadata)</span>"
                + "<span " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_SECONDARY_OVERVIEW_BADGE)
                + ">collapsed by default</span></summary>"
                + "<div " + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_TAG_AUDIT_BODY) + "><div "
                + AtfReportHtmlVocabulary.classAttribute(
                AtfReportHtmlVocabulary.CssClass.ATF_TAG_GRID) + ">"
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
        StringBuilder table = new StringBuilder("<div ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        "card",
                        AtfReportHtmlVocabulary.CssClass.ATF_TAG_CARD.value(),
                        "atf-tag-card-" + suffix))
                .append("><div class=\"card-header\"><p>")
                .append(title).append("</p></div><div class=\"card-body\"><div ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.ATF_TAG_SCROLL))
                .append("><table ")
                .append(AtfReportHtmlVocabulary.classAttribute(
                        "table",
                        "table-sm",
                        AtfReportHtmlVocabulary.CssClass.ATF_TAG_TABLE.value()))
                .append(" ").append(AtfReportHtmlVocabulary.idAttribute(id))
                .append("><thead><tr>");
        for (String header : headers.split("\\|")) {
            table.append("<th>").append(header).append("</th>");
        }
        return table.append("</tr></thead><tbody></tbody></table></div></div></div>").toString();
    }
}
