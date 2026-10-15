package atf.api.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the evidence CSS provider remains renderer-neutral and complete. */
class AtfReportEvidenceCssTest {

    @Test
    void ownsJsonTraceAndDisclosureRules() {
        String css = AtfReportEvidenceCss.css();

        assertAll(
                () -> assertTrue(css.contains("pre.code-block")),
                () -> assertTrue(css.contains(".helm-evidence-table")),
                () -> assertTrue(css.contains(".helm-evidence-table.helm-request-table,.helm-evidence-table.helm-trace-table,.helm-evidence-table.execution-table{table-layout:fixed!important}")),
                () -> assertTrue(css.contains("width:var(--atf-evidence-key-column-width)!important")),
                () -> assertTrue(css.contains(".step-detail>summary")),
                () -> assertTrue(css.contains(".atf-disclosure-glyph:before")),
                () -> assertFalse(css.contains("<style")));
    }

    @Test
    void themeComposesEvidenceProviderWithoutDroppingContract() {
        String theme = AtfReportTheme.css();

        assertAll(
                () -> assertTrue(theme.contains("pre.code-block")),
                () -> assertTrue(theme.contains(".helm-evidence-table")),
                () -> assertTrue(theme.contains(".step-detail>summary")));
    }

    @Test
    void actionTableUsesReferenceMarkerAndResponsiveNaturalColumns() {
        String css = AtfReportEvidenceCss.css();

        assertAll(
                () -> assertTrue(css.contains(".atf-ui-action-table{width:100%!important;min-width:0!important;max-width:100%!important;table-layout:auto!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table-wrap{max-width:100%!important;overflow-x:auto!important")),
                () -> assertTrue(css.contains("@media (max-width:700px)")),
                () -> assertTrue(css.contains("white-space:nowrap!important;overflow-wrap:normal!important;word-break:normal!important")),
                () -> assertTrue(css.contains("th:nth-child(3),.atf-ui-action-table td:nth-child(3){min-width:12ch!important;max-width:26ch!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(7),.atf-ui-action-table td:nth-child(7){width:4ch!important;min-width:4ch!important;")),
                () -> assertFalse(css.contains("min-width:760px!important")),
                () -> assertTrue(css.contains(".atf-ui-action-result{display:inline-block!important;padding:0!important;border:0!important;border-radius:0!important;background:transparent!important;color:inherit!important")),
                () -> assertFalse(css.contains("border-radius:50%!important;background:transparent!important;font-size:14px!important")));
    }
}
