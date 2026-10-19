package atf.api.reporting.theme.css;
import atf.api.reporting.theme.AtfReportCategoryCss;
import atf.api.reporting.theme.AtfReportFailsStatusCss;
import atf.api.reporting.theme.tokens.AtfReportColors;


/** CSS for report detail metadata, failure projection and evidence chrome. */
public final class AtfReportThemeDetailCss {
    private AtfReportThemeDetailCss() {
    }

    public static String css() {
        return (""
                + ".exception-view.atf-fails-view .test-list{background:var(--atf-dark)!important;border-right:1px solid var(--atf-border)!important}"
                + ".exception-view.atf-fails-view .test-content{background:var(--atf-dark)!important}"
                + ".exception-view.atf-fails-view .test-list-tools{background:var(--atf-dark)!important;border-bottom:1px solid var(--atf-border)!important;color:var(--atf-accent)!important}"
                + ".exception-view.atf-fails-view .test-list-wrapper{background:var(--atf-dark)!important}"
                + ".exception-view.atf-fails-view .test-list-item>.test-item{display:flex!important;align-items:center!important;justify-content:space-between!important;min-height:74px!important;padding:14px 14px 14px 18px!important;background:var(--atf-dark)!important;border-bottom:1px solid var(--atf-border)!important;border-left:3px solid transparent!important;color:var(--atf-text)!important;cursor:pointer!important}"
                + ".exception-view.atf-fails-view .test-list-item>.test-item:hover,.exception-view.atf-fails-view .test-list-item>.test-item.active{background:var(--atf-panel)!important;border-left-color:var(--atf-fail)!important}"
                + ".exception-view.atf-fails-view .test-item .test-detail{min-width:0!important;flex:1 1 auto!important}.exception-view.atf-fails-view .test-item .test-detail .name{display:block!important;margin:0!important;color:var(--atf-text)!important;font-size:14px!important;line-height:1.55!important;white-space:normal!important}.exception-view.atf-fails-view .test-item .test-detail .text-sm{display:block!important;margin:3px 0 0!important;color:var(--atf-muted)!important;font-size:12px!important;line-height:1.55!important}"
                + ".exception-view.atf-fails-view .test-status{flex:0 0 auto!important;margin-left:10px!important}"
                + ".exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item{gap:12px!important;width:100%!important;min-width:0!important;min-height:0!important;padding:10px 15px!important;margin:0!important;justify-content:normal!important}.exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item[status=\"fail\"]{border-left-color:var(--atf-fail)!important}.exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item>.test-detail{padding-left:5px!important}.exception-view.atf-fails-view .test-item .test-detail .name{line-height:18.76px!important}.exception-view.atf-fails-view .test-item .test-detail .text-sm{font-size:10.08px!important;line-height:12.6px!important;margin:0!important}.exception-view.atf-fails-view .test-status{margin-left:auto!important;align-self:center!important;white-space:nowrap!important}"
                + ".exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item[status=\"fail\"]{border-left-color:var(--atf-bad)!important}.exception-view.atf-fails-view .test-item .test-detail .name{font-family:var(--atf-font-sans)!important;font-weight:400!important;color:var(--atf-card-text)!important}.exception-view.atf-fails-view .test-item .test-detail .text-sm{font-family:var(--atf-font-sans)!important}.exception-view.atf-fails-view .test-status .badge{font-family:var(--atf-font-sans)!important;font-weight:400!important}"
                + ".exception-view.atf-fails-view .test-content-detail{min-height:100%!important;background:var(--atf-dark)!important}.exception-view.atf-fails-view .atf-failure-test-detail{display:block!important;min-height:100%!important;padding:0!important;background:var(--atf-dark)!important;overflow:visible!important}.exception-view.atf-fails-view .atf-failure-test-detail>.test-contents{height:auto!important;max-height:none!important;overflow:visible!important}.exception-view.atf-fails-view .atf-failure-index{margin:28px!important;padding:20px!important;border:1px solid var(--atf-border)!important;border-left:3px solid var(--atf-fail)!important;border-radius:4px!important;background:var(--atf-surface)!important;color:var(--atf-text)!important}.exception-view.atf-fails-view .atf-failure-index.atf-failure-evidence{display:block!important;grid-template-columns:none!important;min-width:0!important;width:auto!important;box-sizing:border-box!important}.exception-view.atf-fails-view .atf-failure-index-row{width:100%!important;min-width:0!important;box-sizing:border-box!important}.exception-view.atf-fails-view .atf-failure-index-title{margin:0 0 18px!important;color:var(--atf-text)!important;font-size:16px!important;line-height:1.55!important;font-weight:400!important}.exception-view.atf-fails-view .atf-failure-index-row{display:grid!important;grid-template-columns:110px minmax(0,1fr)!important;gap:12px!important;padding:9px 0!important;border-bottom:1px solid var(--atf-border)!important;font-size:12.6px!important;line-height:1.55!important}.exception-view.atf-fails-view .atf-failure-index-row:last-of-type{border-bottom:0!important}.exception-view.atf-fails-view .atf-failure-label{color:var(--atf-muted)!important;font-size:10px!important;letter-spacing:.06em!important}.exception-view.atf-fails-view .atf-failure-value{color:var(--atf-text)!important;overflow-wrap:anywhere!important}.exception-view.atf-fails-view .atf-failure-index-cause .atf-failure-value{color:" + AtfReportColors.FAILURE_CAUSE_TEXT + "!important}.exception-view.atf-fails-view .atf-failure-open{display:inline-block!important;margin-top:18px!important;padding:7px 12px!important;border:1px solid var(--atf-accent)!important;border-radius:4px!important;background:transparent!important;color:var(--atf-accent)!important;font-size:12px!important;line-height:16px!important;text-decoration:none!important;cursor:pointer!important}.exception-view.atf-fails-view .atf-failure-open:hover{background:" + AtfReportColors.ACCENT_12 + "!important}"
                + AtfReportFailsStatusCss.css()
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-toolbar{order:2!important;margin-left:auto!important;white-space:nowrap!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header{align-items:center!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar{align-self:center!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-toolbar{display:flex!important;align-items:center!important;gap:4px!important;height:var(--atf-badge-height)!important;line-height:var(--atf-badge-line-height)!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar>ul,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-toolbar>ul{display:flex!important;align-items:center!important;gap:4px!important;height:var(--atf-badge-height)!important;margin:0!important;padding:0!important;list-style:none!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar>ul>li,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-toolbar>ul>li{position:static!important;top:auto!important;float:none!important;display:flex!important;align-items:center!important;height:var(--atf-badge-height)!important;margin:0!important;padding:0!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-toolbar .badge,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-toolbar .badge{display:inline-flex!important;align-items:center!important;justify-content:center!important;box-sizing:border-box!important;height:var(--atf-badge-height)!important;min-height:var(--atf-badge-height)!important;margin:0!important;padding:var(--atf-badge-padding)!important;line-height:var(--atf-badge-line-height)!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title{flex-direction:column!important;align-items:center!important;justify-content:flex-start!important;gap:2px!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title{display:grid!important;grid-template-columns:minmax(0,1fr)!important;grid-template-rows:auto auto!important;align-items:center!important;justify-items:start!important;row-gap:0!important;min-width:0!important}"
                + ".test-view .test-content-detail .detail-head .info>.atf-test-package{display:block!important;margin:2px 0 5px!important;color:var(--atf-muted)!important;font-family:var(--atf-font-sans)!important;font-size:11.2px!important;line-height:14.336px!important;font-weight:400!important;letter-spacing:.01em!important;overflow-wrap:anywhere!important}"
                  + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title>a.node{display:flex!important;align-items:center!important;justify-content:flex-start!important;height:18.76px!important;margin:0!important;padding:0!important;line-height:18.76px!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title>a.node{grid-column:1!important;grid-row:1!important;width:100%!important;min-width:0!important;text-align:left!important;justify-content:flex-start!important}"
                 + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title>.atf-scenario-annotations{grid-column:1!important;grid-row:2!important;justify-self:start!important;align-self:center!important;display:flex!important;align-items:center!important;justify-content:flex-start!important;gap:4px!important;height:18.2px!important;min-height:18.2px!important;margin-top:0!important}"
                + ".test-view .test-content-detail .card-title>.atf-scenario-annotations>.atf-tag-annotation{display:inline-flex!important;align-items:center!important;height:18.2px!important;min-height:18.2px!important;padding:0 3px!important;border:1px solid var(--atf-accent14)!important;border-radius:999px!important;background:var(--atf-accent08)!important;color:var(--atf-annotation-muted)!important;font-size:7.5px!important;line-height:9px!important;font-weight:400!important;white-space:nowrap!important}"
                + ".test-view .test-content-detail .card[data-atf-node-role]>.card-header .node{line-height:18.2px!important;font-weight:400!important;display:inline-block!important;padding:6px 10.5px!important}"
                + ".test-view .test-content-detail .card>.card-header .node-attr .badge{height:18.2px!important;min-height:18.2px!important;padding:0 5px!important}"
                + ".test-view .test-content-detail .card[data-atf-node-role]>.card-header{min-height:38.5px!important;height:auto!important}"
                + ".test-view .test-content-detail .card[data-atf-node-role='request'],.test-view .test-content-detail .card[data-atf-node-role='validation']{margin:4px 0!important;padding:0!important;border:1px solid var(--atf-border)!important;border-left:1px solid var(--atf-border)!important;border-radius:6px!important;background:var(--atf-panel-2)!important;box-shadow:none!important}"
                + ".test-contents .card[data-atf-node-role='request']>.card-header{background:" + AtfReportColors.ACCENT_045 + "!important}"
                + ".test-contents .card[data-atf-node-role='validation']>.card-header{background:" + AtfReportColors.WHITE_025 + "!important}"
                + ".card:has(> .card-body #grandchild-analysis){display:none!important}"
                + ".test-contents .card[data-atf-node-role=\"phase\"] > .card-header .node-attr,"
                + ".test-contents .card[data-atf-node-role=\"request\"] > .card-header .node-attr,"
                + ".test-contents .card[data-atf-node-role=\"validation\"] > .card-header .node-attr{display:none!important}"
                + ".test-view .test-content-detail .card[data-atf-node-role=\"phase\"]{border-left:3px solid var(--atf-accent)!important}"
                + ".atf-secondary-metadata,.atf-tag-audit{margin:18px 0 0;border:1px solid var(--atf-border);"
                + "background:var(--atf-panel);color:var(--atf-text);border-radius:4px;overflow:hidden}"
                + ".atf-secondary-metadata>summary,.atf-tag-audit>summary{display:flex;align-items:center;gap:8px;list-style:none;cursor:pointer;"
                + "padding:14px 18px;font-size:14px;line-height:21.7px;color:var(--atf-accent);"
                + "font-weight:400;border-bottom:1px solid var(--atf-border)}"
                + ".atf-secondary-metadata>summary::-webkit-details-marker,.atf-tag-audit>summary::-webkit-details-marker{display:none}"
                + ".atf-secondary-caption{float:right;color:var(--atf-muted);font-size:11.2px;line-height:14.336px}"
                + ".atf-secondary-metadata>summary:before,.atf-tag-audit>summary:before{content:'＋';"
                + "display:inline-flex;align-items:center;justify-content:center;width:18px;height:18px;margin-right:8px;"
                + "border:1px solid var(--atf-accent);border-radius:50%;font-size:15px;line-height:18px;color:var(--atf-accent)}"
                + ".atf-secondary-metadata[open]>summary:before,.atf-tag-audit[open]>summary:before{content:'−'}"
                + ".atf-secondary-metadata-body,.atf-tag-audit-body{padding:14px 18px;background:var(--atf-dark)}"
                + ".atf-secondary-metadata-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:10px}"
                + ".atf-secondary-metric{border:1px solid var(--atf-border);padding:12px;background:var(--atf-surface)}"
                + ".atf-secondary-metric small{display:block;color:var(--atf-muted);font-size:11.2px;line-height:14.336px;text-transform:uppercase}"
                + ".atf-secondary-metric strong{display:block;margin-top:5px;color:var(--atf-text);font-size:14px;line-height:21.7px}"
                + ".atf-tag-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px}"
                + ".atf-audit-panel{border:1px solid var(--atf-border);background:var(--atf-surface);overflow:auto}"
                + ".atf-audit-panel h4{margin:0;padding:12px 14px;border-bottom:1px solid var(--atf-border);"
                + "font-size:14px;line-height:21.7px;color:var(--atf-text)}"
                + ".atf-audit-panel table{margin:0;width:100%;font-size:12.6px;line-height:16.884px;background-color:var(--atf-dark)!important;background-color:" + AtfReportColors.DARK + "!important;color:var(--atf-text)!important}"
                + ".atf-audit-panel th,.atf-audit-panel td{padding:7px 9px;border:1px solid var(--atf-border);"
                + "font-size:12.6px;line-height:16.884px;color:var(--atf-text);background:var(--atf-surface)}"
                + ".atf-audit-panel th{color:var(--atf-muted);font-size:11.2px;line-height:14.336px;font-weight:400;text-transform:uppercase}"
                + ".atf-fails-view .atf-failure{justify-content:space-between;gap:14px}"
                + ".atf-failure-copy{display:flex;flex-direction:column;gap:3px;min-width:0}"
                + ".atf-failure-copy strong{color:var(--atf-text);font-size:14px;line-height:21.7px}"
                + ".atf-failure-copy small{color:var(--atf-muted);font-size:12.6px;line-height:16.884px}"
                + ".atf-failure-evidence{display:grid;grid-template-columns:max-content minmax(0,1fr);gap:2px 12px;"
                + "margin:8px 0 10px;padding:8px 10px;border-left:2px solid var(--atf-fail);"
                + "background:" + AtfReportColors.DANGER_055 + ";font-size:12px;line-height:16px}"
                + ".atf-failure-evidence dt{color:var(--atf-muted);font-size:10px;line-height:16px;letter-spacing:.06em}"
                + ".atf-failure-evidence dd{margin:0;color:var(--atf-text);min-width:0;overflow-wrap:anywhere}"
                + ".atf-failure>.status{order:2;flex:0 0 auto;display:inline-flex!important;align-items:center!important;justify-content:center!important;min-width:0!important;height:var(--atf-badge-height)!important;box-sizing:border-box!important;padding:var(--atf-badge-padding)!important;border:0!important;border-radius:var(--atf-badge-radius)!important;background:var(--atf-fail)!important;color:var(--atf-fail-text)!important;font-family:var(--atf-font-sans)!important;font-size:var(--atf-badge-font-size)!important;line-height:var(--atf-badge-line-height)!important;font-weight:400!important;white-space:nowrap!important}"
                + "@media(max-width:1100px){.atf-tag-grid,.atf-secondary-metadata-grid{grid-template-columns:1fr}.atf-fails-view .atf-failure{align-items:flex-start}}"
                + AtfReportOverviewCss.metrics()
                + AtfReportCategoryCss.css()
                + AtfReportOverviewCss.categoryStatus()
                + failsStatusColorCss()).replace("atf-failure-test-detail", "atf-failure-detail");
    }

    private static String failsStatusColorCss() {
        return ".exception-view.atf-fails-view .test-status .badge.badge-danger{"
                + "background:var(--atf-fail)!important;color:var(--atf-fail-text)!important}";
    }
}

