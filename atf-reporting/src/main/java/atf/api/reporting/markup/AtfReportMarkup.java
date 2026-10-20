package atf.api.reporting.markup;
import atf.api.reporting.evidence.EvidenceTableFormatter;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.theme.tokens.AtfReportCssProperty;

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
            html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.HELM_API_CLASSIFICATION,
                                    AtfReportHtmlVocabulary.CssClass.TECHNICAL)))
                    .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.HELM_API_CLASSIFICATION_LABEL)))
                    .append(AtfReportLabels.TECHNICAL_TRACE)
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.DIV));
        }
        html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.TABLE,
                AtfReportHtmlVocabulary.classAttribute(cssClass)));
        if (isKeyValueEvidenceTable(cssClass) && rows != null && rows.length > 0) {
            html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.COLGROUP))
                    .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.COL,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.EVIDENCE_KEY_COLUMN),
                            AtfReportHtmlVocabulary.attribute(AtfReportHtmlVocabulary.Attribute.STYLE,
                                    AtfReportCssProperty.WIDTH.value()
                                            + ":var(--atf-evidence-key-column-width)")))
                    .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.COL,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.EVIDENCE_VALUE_COLUMN)))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.COLGROUP));
        }
        html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.THEAD))
                .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.TR));
        if (rows == null || rows.length == 0) {
            return html.append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TR))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.THEAD))
                    .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.TBODY))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TBODY))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TABLE)).toString();
        }
        for (String value : rows[0]) {
                    html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.TH))
                    .append(AtfReportHtmlVocabulary.escapeHtml(value))
                    .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TH));
        }
        html.append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TR))
                .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.THEAD))
                .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.TBODY));
        for (int rowIndex = 1; rowIndex < rows.length; rowIndex++) {
            html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.TR));
            for (int columnIndex = 0; columnIndex < rows[rowIndex].length; columnIndex++) {
                String value = rows[rowIndex][columnIndex];
                if (isKeyValueEvidenceTable(cssClass) && columnIndex == 1) {
                    value = EvidenceTableFormatter.displayValue(rows[rowIndex][0], value);
                }
                html.append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.TD))
                        .append(AtfReportHtmlVocabulary.escapeHtml(value))
                        .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TD));
            }
            html.append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TR));
        }
        return html.append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TBODY))
                .append(AtfReportHtmlVocabulary.closeTag(AtfReportHtmlVocabulary.Tag.TABLE)).toString();
    }

    private static boolean isTraceTable(String cssClass) {
        return cssClass != null && cssClass.contains(
                AtfReportHtmlVocabulary.CssClass.HELM_TRACE_TABLE.value());
    }

    private static boolean isKeyValueEvidenceTable(String cssClass) {
        return cssClass != null && (cssClass.contains(
                AtfReportHtmlVocabulary.CssClass.HELM_TRACE_TABLE.value())
                || cssClass.contains(AtfReportHtmlVocabulary.CssClass.HELM_REQUEST_TABLE.value())
                || cssClass.contains(AtfReportHtmlVocabulary.CssClass.EXECUTION_TABLE.value()));
    }

}
