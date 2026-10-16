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
    void actionTableUsesFixedAuditColumnsAndResponsiveScroll() {
        String css = AtfReportEvidenceCss.css();

        assertAll(
                () -> assertTrue(css.contains(".atf-ui-action-table{width:760px!important;min-width:760px!important;max-width:none!important;table-layout:fixed!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table-wrap{max-width:100%!important;overflow-x:auto!important")),
                () -> assertTrue(css.contains("@media (max-width:700px)")),
                () -> assertTrue(css.contains("white-space:nowrap!important;overflow-wrap:normal!important;word-break:normal!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(3),.atf-ui-action-table td:nth-child(3){width:160px!important;min-width:160px!important;max-width:160px!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(7),.atf-ui-action-table td:nth-child(7){width:70px!important;min-width:70px!important;max-width:70px!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-screenshot{display:flex!important;justify-content:flex-start!important;align-items:flex-end!important;text-align:left!important;vertical-align:bottom!important;")),
                () -> assertTrue(css.contains("padding:4px 0 0!important")),
                () -> assertTrue(css.contains(".atf-ui-action-screenshot-link{display:inline-flex!important;align-items:center!important;align-self:flex-end!important;justify-content:flex-start!important;line-height:16px!important;min-height:16px!important;vertical-align:bottom!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-screenshot-link .badge-gradient-primary{display:inline-flex!important;align-items:center!important;justify-content:center!important;line-height:16px!important;min-height:16px!important;")),
                () -> assertFalse(css.contains("transform:translateY(2px)!important")),
                () -> assertTrue(css.contains("min-width:760px!important;width:760px!important;table-layout:fixed!important")),
                () -> assertTrue(css.contains(".atf-ui-action-result{display:inline-block!important;padding:0!important;border:0!important;border-radius:0!important;background:transparent!important;color:inherit!important")),
                () -> assertFalse(css.contains("border-radius:50%!important;background:transparent!important;font-size:14px!important")));
    }
}
