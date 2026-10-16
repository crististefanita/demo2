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
                () -> assertTrue(theme.contains(":has(details.atf-ui-action-sequence)>td")),
                () -> assertTrue(theme.contains(".step-detail>summary")));
    }

    @Test
    void actionTableUsesFixedAuditColumnsAndResponsiveScroll() {
        String css = AtfReportEvidenceCss.css();

        assertAll(
                () -> assertTrue(css.contains(".atf-ui-action-table{width:100%!important;min-width:760px!important;max-width:none!important;table-layout:fixed!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table-wrap{max-width:100%!important;overflow-x:auto!important")),
                () -> assertTrue(css.contains("@media (max-width:700px)")),
                () -> assertTrue(css.contains(".test-view .test-content-detail .card-body>.table{width:100%!important;max-width:100%!important;min-width:0!important;table-layout:fixed!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-sequence{width:calc(100vw - 30px)!important;max-width:calc(100vw - 30px)!important;min-width:0!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-table-wrap{width:100%!important;max-width:100%!important;min-width:0!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-table{font-size:11px!important;line-height:14px!important;min-width:452px!important;width:452px!important;max-width:none!important;table-layout:fixed!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(2),.atf-ui-action-table td:nth-child(2){width:70px!important;min-width:70px!important;max-width:70px!important;white-space:normal!important;overflow-wrap:anywhere!important;word-break:break-word!important}")),
                () -> assertTrue(css.contains("white-space:nowrap!important;overflow-wrap:normal!important;word-break:normal!important")),
                () -> assertTrue(css.contains("white-space:pre-wrap!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(1),.atf-ui-action-table td:nth-child(1){width:32px!important;min-width:32px!important;max-width:32px!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(2),.atf-ui-action-table td:nth-child(2){width:94px!important;min-width:94px!important;max-width:94px!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(3),.atf-ui-action-table td:nth-child(3){width:30%!important;min-width:160px!important;max-width:none!important;white-space:pre-wrap!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(4),.atf-ui-action-table td:nth-child(4){width:auto!important;min-width:0!important;max-width:none!important;white-space:pre-wrap!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(5),.atf-ui-action-table td:nth-child(5){width:auto!important;min-width:0!important;max-width:none!important;white-space:pre-wrap!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(6),.atf-ui-action-table td:nth-child(6){width:51px!important;min-width:51px!important;max-width:51px!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(7),.atf-ui-action-table td:nth-child(7){width:70px!important;min-width:70px!important;max-width:70px!important;")),
                () -> assertTrue(css.contains(".atf-ui-observations{margin:10px 0!important;border:1px solid var(--atf-border)!important;border-radius:10px!important;overflow:hidden!important;background:var(--atf-panel)!important}")),
                () -> assertTrue(css.contains(".atf-ui-observations>summary{list-style:none!important;display:flex!important;align-items:center!important;padding:9px 12px!important;cursor:pointer!important;color:var(--atf-accent)!important;font-size:12px!important;line-height:16px!important}")),
                () -> assertTrue(css.contains(".atf-ui-observations>summary::-webkit-details-marker{display:none!important}")),
                () -> assertTrue(css.contains(".atf-ui-observations>summary:before{content:'＋'!important;display:inline-flex!important;width:18px!important;align-items:center!important;justify-content:center!important;font-size:15px!important}")),
                () -> assertTrue(css.contains(".atf-ui-observations[open]>summary:before{content:'−'!important}")),
                () -> assertTrue(css.contains(":has(details.atf-ui-action-sequence)>td")),
                () -> assertTrue(css.contains(":has(details.atf-ui-observations)>td")),
                () -> assertTrue(css.contains(":has(table.helm-validation-table)>td")),
                () -> assertTrue(css.contains("padding-top:0!important;padding-bottom:0!important")),
                () -> assertTrue(css.contains("details.atf-ui-action-sequence{margin:0!important;border-radius:10px 10px 0 0!important}")),
                () -> assertTrue(css.contains("details.atf-ui-observations{margin:0!important;border-radius:0!important}")),
                () -> assertTrue(css.contains("table.helm-validation-table{margin:0!important;border-radius:0 0 10px 10px!important;overflow:hidden!important}")),
                () -> assertTrue(css.contains("details.assertion-evidence{display:block!important;margin:0!important;border:1px solid var(--atf-border)!important;border-top:0!important;border-radius:0 0 10px 10px!important")),
                () -> assertTrue(css.contains("details.step-detail.request-evidence,")),
                () -> assertTrue(css.contains(".step-detail>summary{list-style:none!important;display:flex!important;align-items:center!important;gap:0!important;padding:9px 12px!important;")),
                () -> assertTrue(css.contains("details.step-detail.assertion-evidence{margin:0!important;border-radius:0 0 10px 10px!important;background:var(--atf-panel)!important}")),
                () -> assertTrue(css.contains("details.step-detail.request-evidence")),
                () -> assertTrue(css.contains("details.step-detail.response-evidence")),
                () -> assertTrue(css.contains("details.step-detail.execution-evidence")),
                () -> assertTrue(css.contains("border-top:0!important;border-bottom:0!important;padding:0!important;margin:0!important;height:auto!important")),
                () -> assertTrue(css.contains("details.step-detail)>td{border:0!important;border-top:0!important;border-bottom:0!important;padding:0!important;margin:0!important}")),
                () -> assertTrue(css.contains("border-collapse:separate!important;border-spacing:0!important")),
                () -> assertTrue(css.contains("border:0!important;border-radius:0!important;box-shadow:none!important;overflow:hidden!important;background:var(--atf-panel)!important")),
                () -> assertTrue(css.contains("border:1px solid var(--atf-border)!important;border-bottom:0!important;border-radius:10px 10px 0 0!important")),
                () -> assertTrue(css.contains("details.step-detail.response-evidence>summary{border-top:0!important}")),
                () -> assertTrue(css.contains("details.step-detail.execution-evidence>summary{border-top:0!important;border-bottom:0!important;")),
                () -> assertTrue(css.contains("border-bottom:1px solid var(--atf-border)!important;border-top:0!important;border-radius:0 0 10px 10px!important")),
                () -> assertTrue(css.contains(".test-view .card[data-atf-node-role=\"validation\"] .card-body>.table>tbody>tr.event-row:has(details.atf-ui-observations)>td>details.atf-ui-observations{border-left:1px solid var(--atf-border)!important;border-right:1px solid var(--atf-border)!important;border-top:0!important}")),
                () -> assertTrue(css.contains(".test-view .card[data-atf-node-role=\"validation\"] .card-body>.table>tbody>tr.event-row:has(details.assertion-evidence)>td>details.assertion-evidence{border-left:1px solid var(--atf-border)!important;border-right:1px solid var(--atf-border)!important;border-bottom:1px solid var(--atf-border)!important;border-top:0!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-screenshot{display:flex!important;justify-content:flex-start!important;align-items:center!important;text-align:left!important;vertical-align:middle!important;margin:12px 8px 8px!important;")),
                () -> assertTrue(css.contains("padding:0!important")),
                () -> assertTrue(css.contains("max-width:100%!important;overflow:visible!important")),
                () -> assertFalse(css.contains(".atf-ui-action-screenshot{" + "display:flex!important;justify-content:flex-start!important;align-items:center!important;text-align:left!important;vertical-align:middle!important;margin:0 8px 8px!important;padding:0!important;max-width:100%!important;overflow:auto!important")),
                  () -> assertTrue(css.contains(".atf-ui-action-screenshot-link{display:inline-flex!important;flex-direction:row!important;align-items:center!important;align-self:center!important;justify-content:center!important;gap:4px!important;height:18px!important;min-height:18px!important;padding:0!important;line-height:18px!important;vertical-align:middle!important;transform:none!important;")),
                  () -> assertTrue(css.contains(".test-view .test-contents .atf-ui-action-screenshot-link .badge-gradient-primary{display:inline-flex!important;align-items:center!important;justify-content:center!important;box-sizing:border-box!important;height:18px!important;line-height:18px!important;min-height:18px!important;padding:1px 7px 0!important;margin:0!important;border-radius:4px!important;white-space:nowrap!important;")),
                () -> assertFalse(css.contains("flex-direction:column!important")),
                  () -> assertTrue(css.contains("transform:none!important")),
                () -> assertFalse(css.contains("transform:translateY(2px)!important")),
                  () -> assertFalse(css.contains(".atf-ui-action-screenshot-link .atf-ui-action-screenshot-image")),
                  () -> assertTrue(css.contains("min-width:452px!important;width:452px!important;max-width:none!important;table-layout:fixed!important")),
                () -> assertTrue(css.contains(".atf-ui-action-result{display:inline-block!important;padding:0!important;border:0!important;border-radius:0!important;background:transparent!important;color:inherit!important")),
                () -> assertFalse(css.contains("border-radius:50%!important;background:transparent!important;font-size:14px!important")),
                () -> assertTrue(css.contains("--atf-evidence-status-column-width")),
                () -> assertTrue(css.contains("min-height:var(--atf-evidence-summary-height)!important;max-height:var(--atf-evidence-summary-height)!important;")),
                () -> assertTrue(css.contains("height:var(--atf-evidence-summary-height)!important")),
                () -> assertTrue(css.contains("outline:2px solid var(--atf-accent)!important;outline-offset:-2px!important")),
                () -> assertTrue(css.contains("overscroll-behavior-x:contain!important")),
                () -> assertTrue(css.contains("details.step-detail.response-evidence{border-left:1px solid var(--atf-border)!important;border-right:1px solid var(--atf-border)!important}")),
                () -> assertTrue(css.contains("details.step-detail.execution-evidence{border-left:1px solid var(--atf-border)!important;border-right:1px solid var(--atf-border)!important;")));
    }
}


