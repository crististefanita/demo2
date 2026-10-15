package atf.api.reporting;

/** CSS contract for response traces, JSON evidence and assertion disclosures. */
final class AtfReportEvidenceCss {
    private AtfReportEvidenceCss() {
    }

    static String css() {
        return ".test-contents pre,pre.markup-code,pre.code-block{background:var(--atf-code)!important;color:var(--atf-json-text)!important;"
                + "border-color:var(--atf-border)!important;font-family:ui-monospace,Consolas,monospace!important;"
                + "font-size:12.5px!important;line-height:18.75px!important;padding:10px 12px!important;"
                + "overflow:auto!important;max-height:360px!important}"
                + ".dark .test-contents pre.code-block,.dark pre.markup-code{color:var(--atf-json-text)!important}"
                + ".code-block{display:block!important;width:100%!important;box-sizing:border-box!important;margin:0!important;"
                + "white-space:pre-wrap!important;overflow-wrap:anywhere!important;resize:vertical!important}"
                + ".table{color:var(--atf-text)!important;font-size:12.6px!important;line-height:16.884px!important}"
                + ".table td,.table th{border-color:var(--atf-border)!important;padding:7.5px!important;"
                + "font-size:12.6px!important;line-height:16.884px!important;background:var(--atf-surface)!important}"
                + ".table th{color:var(--atf-muted)!important;font-size:11.2px!important;line-height:14.336px!important;"
                + "font-weight:400!important;text-transform:uppercase!important}"
                + ".table td{color:var(--atf-text)!important}"
                + ".helm-evidence-table{width:100%!important;border-collapse:collapse!important;"
                + "font-size:12.6px!important;line-height:16.884px!important}"
                + ".helm-evidence-table th,.helm-evidence-table td{border:1px solid var(--atf-border)!important;"
                + "font-size:12.6px!important;line-height:16.884px!important;padding:7px 9px!important;"
                + "text-align:left!important;vertical-align:top!important}"
                + ".helm-evidence-table th{color:var(--atf-muted)!important;font-size:11.2px!important;"
                + "line-height:14.336px!important;font-weight:400!important;background:" + AtfReportColors.WHITE_024 + "!important}"
                + ".helm-evidence-table td{color:var(--atf-table-text)!important;background:var(--atf-surface)!important}"
                + ".helm-trace-table th{color:var(--atf-table-label)!important;font-weight:500!important}"
                + ".helm-trace-table td{color:var(--atf-code-text)!important}"
                + ".helm-api-classification{padding:8px 10px!important;border-bottom:1px solid var(--atf-border)!important;"
                + "color:var(--atf-muted)!important;font-size:11.2px!important;line-height:14.336px!important;"
                + "letter-spacing:.02em!important;text-transform:uppercase!important}"
                + ".helm-api-classification-label{color:var(--atf-muted)!important}"
                + ".helm-evidence-table.helm-request-table,.helm-evidence-table.helm-trace-table,.helm-evidence-table.execution-table{table-layout:fixed!important}"
                + ".helm-evidence-table.helm-request-table th:first-child,.helm-evidence-table.helm-request-table td:first-child,"
                + ".helm-evidence-table.helm-trace-table th:first-child,.helm-evidence-table.helm-trace-table td:first-child,"
                + ".helm-evidence-table.execution-table th:first-child,.helm-evidence-table.execution-table td:first-child{"
                + "width:var(--atf-evidence-key-column-width)!important;min-width:0!important;max-width:none!important;"
                + "white-space:normal!important;overflow-wrap:anywhere!important;word-break:break-word!important}"
                + ".helm-evidence-table.helm-request-table th:nth-child(2),.helm-evidence-table.helm-request-table td:nth-child(2),"
                + ".helm-evidence-table.helm-trace-table th:nth-child(2),.helm-evidence-table.helm-trace-table td:nth-child(2),"
                + ".helm-evidence-table.execution-table th:nth-child(2),.helm-evidence-table.execution-table td:nth-child(2){"
                + "width:auto!important;min-width:0!important;white-space:normal!important;overflow-wrap:anywhere!important;word-break:break-word!important}"
                + ".helm-evidence-table.helm-request-table col.atf-evidence-key-column,.helm-evidence-table.helm-trace-table col.atf-evidence-key-column,.helm-evidence-table.execution-table col.atf-evidence-key-column{"
                + "width:var(--atf-evidence-key-column-width)!important}"
                + ".helm-evidence-table.helm-request-table col.atf-evidence-value-column,.helm-evidence-table.helm-trace-table col.atf-evidence-value-column,.helm-evidence-table.execution-table col.atf-evidence-value-column{"
                + "width:auto!important}"
                + ".step-detail{margin:0!important;border:1px solid var(--atf-border)!important;border-radius:12px!important;overflow:hidden!important;background:var(--atf-dark)!important}"
                + ".step-detail>summary{list-style:none!important;display:flex!important;align-items:center!important;gap:8px!important;padding:10px 12px!important;color:var(--atf-accent)!important;cursor:pointer!important}"
                + ".step-detail>summary::-webkit-details-marker{display:none!important}.step-detail .se-toggle{display:none!important}"
                + ".atf-disclosure-glyph{display:inline-flex!important;flex:0 0 18px!important;width:18px!important;height:18px!important;align-items:center!important;justify-content:center!important;color:var(--atf-accent)!important;font-size:17px!important;font-weight:500!important;line-height:18px!important}"
                + ".atf-disclosure-glyph:before{content:'＋'!important}.step-detail[open] .atf-disclosure-glyph:before{content:'−'!important}"
                + ".step-detail .se-title{color:var(--atf-accent)!important}.step-detail .body{padding:0 10px 10px!important}";
    }
}
