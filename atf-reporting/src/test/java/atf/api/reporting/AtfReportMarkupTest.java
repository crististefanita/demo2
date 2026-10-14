package atf.api.reporting;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtfReportMarkupTest {
    @Test
    void traceTableSizesFieldColumnFromLongestLabelAndLeavesValueFlexible() {
        String html = AtfReportMarkup.tableHtml("helm-evidence-table helm-trace-table", new String[][]{
                {"FIELD", "VALUE"},
                {"AUTHENTICATION", "none"},
                {"ROUTE TEMPLATE", "/public/v2/users/1"}
        });

        assertAll(
                () -> assertTrue(html.contains("class=\"atf-trace-field-column\" style=\"width:18ch;max-width:50%\"")),
                () -> assertTrue(html.contains("class=\"atf-trace-value-column\"")),
                () -> assertTrue(html.contains("<th>FIELD</th><th>VALUE</th>")));
    }

    @Test
    void traceTableCapsVeryLongFieldLabelsAtHalfTheTable() {
        String html = AtfReportMarkup.tableHtml("helm-trace-table", new String[][]{
                {"FIELD", "VALUE"},
                {"A label deliberately long enough to require the half-table cap because it keeps growing", "value"}
        });

        assertAll(
                () -> assertTrue(html.contains("style=\"width:50%;max-width:50%\"")),
                () -> assertFalse(html.contains("width:max-content")));
    }

    @Test
    void regularTablesDoNotReceiveTraceColumnStructure() {
        String html = AtfReportMarkup.tableHtml("helm-validation-table", new String[][]{
                {"ASSERTION", "EXPECTED"}, {"status", "200"}
        });

        assertFalse(html.contains("atf-trace-field-column"));
    }

    @Test
    void traceMarkupUsesTheTestsTabLabelContract() {
        String html = AtfReportMarkup.tableHtml("helm-trace-table", new String[][]{
                {"FIELD", "VALUE"}, {"RESPONSE STATUS", "200"}
        });

        assertAll(
                () -> assertTrue(html.contains("helm-api-classification technical")),
                () -> assertTrue(html.contains("helm-api-classification-label\">" + AtfReportLabels.TECHNICAL_TRACE + "</span>")),
                () -> assertTrue(html.contains("<table class=\"helm-trace-table\">")));
    }

    @Test
    void canonicalEvidenceLabelsKeepTheirExactCapitalization() {
        assertAll(
                () -> assertTrue(AtfReportLabels.responseDetails(8).equals("Response details · 8 fields")),
                () -> assertTrue(AtfReportLabels.assertions(3).equals("Assertions · 3")),
                () -> assertTrue(AtfReportLabels.REQUEST_JSON.equals("REQUEST JSON")),
                () -> assertTrue(AtfReportLabels.RESPONSE_JSON.equals("RESPONSE JSON")));
    }

    @Test
    void technicalTraceKeepsFullUrlAsUrlField() {
        String[][] rows = EvidenceTableFormatter.toExtentRows(
                java.util.Collections.singletonList(new String[]{"URL", "https://atf.local/public/v2/posts"}));

        assertAll(
                () -> assertTrue("URL".equals(rows[1][0])),
                () -> assertTrue("https://atf.local/public/v2/posts".equals(rows[1][1])));
    }
}
