package atf.api.reporting.markup;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Builds the secondary, collapsed-by-default overview audit tables. */
public final class ExtentReportOverviewMarkup {
    private static final Pattern CATEGORY_CONTAINER = Pattern.compile(
            "<div\\b(?=[^>]*\\bclass\\s*=\\s*\"[^\"]*\\bcategory-container\\b)[^>]*>");

    private ExtentReportOverviewMarkup() {
    }

    public static String inject(String html) {
        if (html == null || html.isBlank()) return html;
        String metadataId = AtfReportHtmlVocabulary.idAttribute(
                AtfReportHtmlVocabulary.ElementId.EXECUTION_METADATA);
        if (html.contains(metadataId)) return html;

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
        Matcher marker = CATEGORY_CONTAINER.matcher(html);
        if (!marker.find()) return html;
        return html.substring(0, marker.start()) + metadata + html.substring(marker.start());
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
