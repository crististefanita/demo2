package atf.api.reporting.theme.evidence;
import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.theme.AtfReportTheme;
import atf.api.reporting.theme.evidence.AtfReportEvidenceCss;
import atf.api.reporting.theme.evidence.AtfReportEvidenceDisclosureCss;
import atf.api.reporting.theme.evidence.AtfReportEvidenceInteractionCss;
import atf.api.reporting.theme.evidence.AtfReportEvidenceTableCss;
import atf.api.reporting.theme.css.AtfReportThemeBaseCss;
import atf.api.reporting.support.CssContractSupport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the evidence CSS provider remains renderer-neutral and complete. */
@AtfUnitTest
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
                 () -> assertTrue(CssContractSupport.contains(AtfReportThemeBaseCss.css(), ".helm-scroll-block-body>.row{width:100%!important;max-width:100%!important;min-width:0!important;margin:0!important;box-sizing:border-box!important}")),
                 () -> assertTrue(CssContractSupport.contains(AtfReportThemeBaseCss.css(), ".helm-scroll-block-body>.row>[class*='col-']{width:100%!important;max-width:100%!important;min-width:0!important;padding-left:0!important;padding-right:0!important;box-sizing:border-box!important}")),
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
    void evidenceResponsibilitiesRemainSeparatedByProvider() {
        assertAll(
                 () -> assertTrue(CssContractSupport.contains(AtfReportEvidenceTableCss.css(), ".helm-evidence-table")),
                 () -> assertTrue(CssContractSupport.contains(AtfReportEvidenceTableCss.css(), "table.helm-validation-table{width:100%!important;max-width:100%!important;min-width:0!important;table-layout:fixed!important}")),
                 () -> assertFalse(CssContractSupport.contains(AtfReportEvidenceTableCss.css(), "width:72px!important;min-width:72px!important;max-width:72px!important")),
                 () -> assertFalse(CssContractSupport.contains(AtfReportEvidenceTableCss.css(), "width:62px!important;min-width:62px!important;max-width:62px!important")),
                 () -> assertTrue(CssContractSupport.contains(AtfReportEvidenceInteractionCss.css(), ".atf-ui-action-table")),
                 () -> assertTrue(CssContractSupport.contains(AtfReportEvidenceInteractionCss.css(), ".atf-ui-action-screenshot-link")),
                 () -> assertTrue(CssContractSupport.contains(AtfReportEvidenceDisclosureCss.css(), "details.step-detail.request-evidence")),
                 () -> assertTrue(CssContractSupport.contains(AtfReportEvidenceDisclosureCss.css(), "details.failure-evidence")));
    }

    @Test
    void actionTableUsesResponsiveAuditColumnsWithoutForcedHorizontalScroll() {
        CssContractSupport css = CssContractSupport.of(AtfReportEvidenceCss.css());
        CssContractSupport interactionCss = CssContractSupport.of(AtfReportEvidenceInteractionCss.css());

        assertAll(
                () -> assertTrue(css.contains(".atf-ui-action-table{width:100%!important;min-width:0!important;max-width:100%!important;box-sizing:border-box!important;table-layout:fixed!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table-wrap{width:100%!important;max-width:100%!important;min-width:0!important;box-sizing:border-box!important;overflow:hidden!important")),
                () -> assertTrue(css.contains("--atf-evidence-summary-height:var(--atf-evidence-tall-summary-height)"),
                        "action, audit, assertion and network disclosures must share the tall summary token"),
                () -> assertTrue(css.contains(".network-aggregate-sections>thead>tr>th:first-child,.network-aggregate-sections>tbody>tr>td:first-child{width:var(--atf-evidence-status-column-width)!important"),
                        "aggregate sections must use the same status lane as standard report evidence"),
                () -> assertTrue(css.contains(".network-aggregate-sections>tbody>tr>td.details-col{padding:0!important;vertical-align:middle!important"),
                        "aggregate disclosures must align in the shared details lane"),
                () -> assertTrue(css.contains("max-height:var(--atf-table-scroll-height)!important"),
                        "long evidence tables must use the shared System/Environment viewport"),
                () -> assertTrue(css.contains("overflow-y:auto!important"),
                        "bounded evidence tables must scroll vertically"),
                () -> assertTrue(css.contains("position:sticky!important"),
                        "bounded evidence tables must keep their header visible"),
                () -> assertTrue(css.contains(".atf-ui-action-table th,.atf-ui-action-table td{box-sizing:border-box!important;border:1px solid var(--atf-border)!important;padding:5px 6px!important;text-align:left!important;vertical-align:top!important;overflow:hidden!important;overflow-wrap:anywhere!important;word-break:break-word!important}")),
                () -> assertFalse(interactionCss.contains("min-width:720px!important")),
                () -> assertFalse(interactionCss.contains(".atf-ui-action-table-wrap{max-width:100%!important;overflow-x:auto!important")),
                () -> assertTrue(css.contains("@media (max-width:700px)")),
                () -> assertTrue(css.contains(".test-view .test-content-detail .card-body>.table{width:100%!important;max-width:100%!important;min-width:0!important;table-layout:fixed!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-sequence{width:calc(100vw - 30px)!important;max-width:calc(100vw - 30px)!important;min-width:0!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-table-wrap{width:100%!important;max-width:100%!important;min-width:0!important;")),
                () -> assertTrue(css.contains("@media (max-width:1300px){.test-view .test-content-detail .card-body>.table{width:100%!important;max-width:100%!important;min-width:0!important;table-layout:fixed!important}")),
                () -> assertTrue(css.contains("@media (min-width:701px) and (max-width:1300px)")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(5),.atf-ui-action-table td:nth-child(5){width:11%!important;white-space:normal!important;overflow-wrap:anywhere!important;padding:4px 3px!important;font-size:11px!important}")),
                () -> assertTrue(css.contains(".atf-ui-observation-table th:nth-child(5){width:10%!important;min-width:0!important;max-width:none!important;white-space:nowrap!important;padding:4px 2px!important;font-size:9px!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-screenshot{margin:12px 0 8px!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table{font-size:11px!important;line-height:14px!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(1),.atf-ui-action-table td:nth-child(1){width:14%!important}")),
                () -> assertTrue(css.contains("white-space:normal!important;overflow-wrap:anywhere!important;word-break:break-word!important")),
                () -> assertTrue(css.contains("white-space:pre-wrap!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(1),.atf-ui-action-table td:nth-child(1){width:17%!important;min-width:0!important;max-width:none!important;white-space:normal!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(2),.atf-ui-action-table td:nth-child(2){width:25%!important;min-width:0!important;max-width:none!important;white-space:pre-wrap!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(3),.atf-ui-action-table td:nth-child(3){width:22%!important;min-width:0!important;max-width:none!important;white-space:pre-wrap!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(5),.atf-ui-action-table td:nth-child(5){width:8%!important;min-width:0!important;max-width:none!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-table th:nth-child(6){width:7%!important;min-width:0!important;max-width:none!important;")),
                () -> assertTrue(css.contains("white-space:nowrap!important;overflow:hidden!important;padding:4px 2px!important;font-size:9px!important")),
                () -> assertTrue(css.contains(".atf-ui-action-table td:nth-child(6){width:7%!important;min-width:0!important;max-width:none!important;text-align:center!important;white-space:normal!important;overflow-wrap:anywhere!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-identity{display:table-cell!important;vertical-align:top!important;min-width:0!important;max-width:100%!important}")),
                () -> assertTrue(css.contains(".atf-ui-action-operation{display:block!important;color:var(--atf-accent)!important;")),
                () -> assertTrue(css.contains(".atf-ui-action-number{display:inline-block!important;width:max-content!important;max-width:100%!important;min-width:0!important;")),
                () -> assertTrue(css.contains("border:1px solid var(--atf-border)!important;border-radius:9px!important;")),
                () -> assertTrue(css.contains("white-space:nowrap!important;vertical-align:top!important}")),
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
                () -> assertTrue(css.contains(".step-detail:not([open])> :not(summary),.atf-ui-action-sequence:not([open])> :not(summary),")),
                () -> assertTrue(css.contains(".step-detail[open]> :not(summary),.atf-ui-action-sequence[open]> :not(summary),")),
                () -> assertTrue(css.contains("details.step-detail.assertion-evidence{margin:0!important;border-radius:0 0 10px 10px!important;background:var(--atf-panel)!important}")),
                () -> assertTrue(css.contains("details.step-detail.request-evidence")),
                () -> assertTrue(css.contains("details.step-detail.response-evidence")),
                () -> assertTrue(css.contains("details.step-detail.execution-evidence")),
                () -> assertTrue(css.contains("details.step-detail.setup-evidence{display:block!important;margin:0!important;padding:0!important;border:1px solid var(--atf-border)!important;")),
                () -> assertTrue(css.contains("details.step-detail.setup-evidence>.body{margin:0!important;padding:0!important;border-top:1px solid var(--atf-border)!important;")),
                () -> assertTrue(css.contains("details.step-detail.setup-evidence .atf-setup-evidence-table tr:last-child td{")),
                () -> assertTrue(css.contains("details.step-detail.setup-evidence .atf-setup-evidence-table td:last-child{")),
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
                () -> assertTrue(css.contains(".atf-ui-action-screenshot{display:flex!important;justify-content:flex-start!important;align-items:center!important;text-align:left!important;vertical-align:middle!important;width:100%!important;max-width:100%!important;margin:12px 0 8px!important;")),
                () -> assertTrue(css.contains("padding:0!important")),
                () -> assertTrue(css.contains("overflow:visible!important;box-sizing:border-box!important")),
                () -> assertFalse(css.contains(".atf-ui-action-screenshot{" + "display:flex!important;justify-content:flex-start!important;align-items:flex-start!important;text-align:left!important;vertical-align:middle!important;margin:0 8px 8px!important;padding:0!important;max-width:100%!important;overflow:auto!important")),
                  () -> assertTrue(css.contains(".atf-ui-action-screenshot-link{display:flex!important;flex-direction:row!important;align-items:center!important;align-self:center!important;justify-content:flex-start!important;flex-wrap:wrap!important;gap:4px!important;width:100%!important;max-width:100%!important;height:auto!important;min-height:18px!important;")),
                  () -> assertTrue(css.contains("white-space:normal!important;overflow-wrap:anywhere!important;word-break:break-word!important;box-sizing:border-box!important")),
                  () -> assertTrue(css.contains(".test-view .test-contents .atf-ui-action-screenshot-link .badge-gradient-primary{display:inline-flex!important;flex:0 0 auto!important;align-items:center!important;justify-content:center!important;box-sizing:border-box!important;height:18px!important;line-height:18px!important;min-height:18px!important;padding:1px 7px 0!important;margin:0!important;border-radius:4px!important;white-space:nowrap!important;")),
                () -> assertFalse(css.contains(".atf-ui-action-screenshot-link{display:inline-flex!important;flex-direction:column!important")),
                () -> assertTrue(css.contains(".atf-ui-screenshot-duplicate-text{display:inline!important;flex:1 1 0!important;min-width:0!important;white-space:normal!important;overflow-wrap:anywhere!important;word-break:break-word!important;line-height:18px!important}")),
                  () -> assertTrue(css.contains("transform:none!important")),
                () -> assertFalse(css.contains("transform:translateY(2px)!important")),
                  () -> assertFalse(css.contains(".atf-ui-action-screenshot-link .atf-ui-action-screenshot-image")),
                () -> assertFalse(css.contains("min-width:452px!important;width:452px!important")),
                () -> assertTrue(css.contains(".atf-ui-action-result{display:inline-block!important;padding:0!important;border:0!important;border-radius:0!important;background:transparent!important;color:inherit!important")),
                () -> assertFalse(css.contains("border-radius:50%!important;background:transparent!important;font-size:14px!important")),
                () -> assertTrue(css.contains("--atf-evidence-status-column-width")),
                () -> assertTrue(css.contains("min-height:var(--atf-evidence-summary-height)!important;max-height:var(--atf-evidence-summary-height)!important;")),
                () -> assertTrue(css.contains("height:var(--atf-evidence-summary-height)!important")),
                () -> assertTrue(css.contains("outline:2px solid var(--atf-accent)!important;outline-offset:-2px!important")),
                () -> assertTrue(css.contains("overscroll-behavior-x:contain!important")),
                () -> assertTrue(css.contains("details.step-detail.response-evidence{border-left:1px solid var(--atf-border)!important;border-right:1px solid var(--atf-border)!important}")),
                () -> assertTrue(css.contains("details.step-detail.execution-evidence{border-left:1px solid var(--atf-border)!important;border-right:1px solid var(--atf-border)!important;")),
                () -> assertTrue(css.contains(":not(:has(~ tr.event-row:has(details.step-detail)))>td>details.step-detail{")),
                () -> assertTrue(css.contains("details.step-detail[data-atf-evidence-last=\"true\"]"),
                        "The final semantic disclosure must own the closing border dynamically"),
                () -> assertTrue(css.contains("details.step-detail.atf-ui-failure-evidence"),
                        "The explicit UI failure evidence must keep a distinct fail treatment"),
                () -> assertTrue(css.contains("border-bottom:1px solid var(--atf-border)!important;border-radius:0 0 var(--atf-evidence-radius) var(--atf-evidence-radius)!important}")));
    }

    @Test
    void publishedScrollViewportSelectorKeepsDescendantCombinator() {
        String css = AtfReportEvidenceCss.css();
        String selector = ".card[data-atf-node-role='validation'] "
                + ":is(.atf-ui-action-table-wrap,.atf-validation-table-wrap)";

        assertTrue(css.contains(selector + "{max-height:var(--atf-table-scroll-height)!important"),
                "the published rule must select evidence wrappers beneath validation cards");
    }

    @Test
    void everyScrollableTableFamilyUsesTheSystemEnvironmentViewport() {
        String theme = AtfReportTheme.css();
        String fallback = AtfReportTheme.fallbackCss();

        assertAll(
                () -> assertTrue(theme.contains(".dashboard-view .category-container>.card>.card-body")),
                () -> assertTrue(theme.contains("height:var(--atf-table-scroll-height)!important"),
                        "Overview/System table bodies must use the shared viewport token"),
                () -> assertTrue(theme.contains(".atf-tag-scroll{height:var(--atf-table-scroll-height)!important"),
                        "tag audit tables must use the shared viewport token"),
                () -> assertTrue(theme.contains("max-height:var(--atf-table-scroll-height)!important"),
                        "Extent action/audit/assertion tables must use the shared viewport token"),
                () -> assertTrue(fallback.contains(".atf-ui-action-table-wrap,.atf-validation-table-wrap{max-height:var(--atf-table-scroll-height)"),
                        "fallback action and validation tables must use the shared viewport token"),
                () -> assertFalse(theme.contains("height:500px!important"),
                        "the viewport must be configured through the common CSS token"),
                () -> assertFalse(theme.contains("max-height:500px!important"),
                        "the viewport must not be hardcoded in a renderer stylesheet"));
    }
}


