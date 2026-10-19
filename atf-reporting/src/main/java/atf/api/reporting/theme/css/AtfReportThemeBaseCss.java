package atf.api.reporting.theme.css;
import atf.api.reporting.theme.tokens.AtfReportColors;
import atf.api.reporting.theme.tokens.AtfReportThemeTokens;

/**
 * Semantic base layer for the Extent theme: shared surfaces, test-card
 * structure, scenario annotations and status-badge geometry.
 *
 * <p>This is intentionally separate from navigation, evidence and Overview
 * rules. The composition root can therefore change one visual responsibility
 * without turning the entire renderer stylesheet into one hotspot.</p>
 */
public final class AtfReportThemeBaseCss {
    private static final String FONT_STACK = AtfReportThemeTokens.FONT_STACK;

    private AtfReportThemeBaseCss() {
    }

    public static String css() {
        return "html,body,body.dark,.app,.layout,.vcontainer,.main-content,.view,.test-content,.test-list{"
                + "background:var(--atf-dark)!important;color:var(--atf-text)!important;font-family:" + FONT_STACK + "!important}"
                + "body{font-size:14px!important;line-height:1.55!important}"
                + ".header.navbar,.side-nav,.side-nav-inner,.test-list-tools,.test-content-tools{"
                + "background:var(--atf-dark)!important;border-color:var(--atf-border)!important}"
                + ".card,.test-list-item>li,.test-item,.category-view .card,.dashboard-view .card{"
                + "background:var(--atf-surface)!important;border-color:var(--atf-card-border)!important;"
                + "color:var(--atf-card-text)!important}"
                + ".dark .card,.dark .card-header,.dark .card-body,.dark .card-footer{"
                + "background:var(--atf-surface)!important}"
                + ".card-header,.card-body,.card-footer,.detail-body{background:var(--atf-surface)!important;"
                + "border-color:var(--atf-card-border)!important;"
                + "color:var(--atf-card-text)!important}"
                + ".card-title,.card-title a,.card-title span,.test-name,.test-detail .name,.dashboard-view h3,"
                + ".dashboard-view h6{color:var(--atf-text)!important;font-size:14px!important;line-height:1.55!important}"
                + ".test-detail .text-sm,.duration,.test-list .duration{color:var(--atf-muted)!important}"
                + ".test-item.active,.test-item:hover,.side-nav .nav-item.active{background:var(--atf-panel)!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header,.test-view .test-content-detail .card[data-atf-node-role]>.card-header{display:flex!important;align-items:center!important;column-gap:8px!important;row-gap:4px!important;flex-wrap:wrap!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-title{display:flex!important;align-items:center!important;flex:1 1 auto!important;min-width:0!important;margin:0!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header:before,.test-view .test-content-detail .card[data-atf-node-role]>.card-header:before{display:inline-flex!important;flex:0 0 18px!important;width:18px!important;height:18px!important;align-items:center!important;justify-content:center!important;color:var(--atf-accent)!important;font-size:17px!important;font-weight:500!important;line-height:18px!important;content:'＋'!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-open='true']>.card-header:before,.test-view .test-content-detail .card[data-atf-phase-open='true']>.card-header:before,.test-view .test-content-detail .card[data-atf-node-open='true']>.card-header:before{content:'−'!important}"
                + ".test-view .test-content-detail .card[data-atf-node-expandable='false']>.card-header:before{display:none!important}"
                + ".atf-scenario-annotations{display:flex!important;align-items:center!important;gap:5px!important;"
                + "flex-wrap:wrap!important;margin-top:7px!important;min-height:18px!important}"
                + ".atf-tag-annotation{display:inline-flex!important;align-items:center!important;padding:2px 7px!important;"
                + "border:1px solid var(--atf-accent)!important;border-radius:999px!important;"
                + "color:var(--atf-accent)!important;background:transparent!important;"
                + "font-size:11px!important;line-height:14px!important;font-weight:400!important;white-space:nowrap!important}"
                + ".helm-scroll-block{max-height:360px!important;overflow:auto!important;border:1px solid var(--atf-border)!important;background:var(--atf-code)!important}"
                + ".helm-scroll-block-title{padding:8px 10px!important;border-bottom:1px solid var(--atf-border)!important;color:var(--atf-muted)!important;font-size:11.2px!important;text-transform:uppercase!important}"
                + ".helm-scroll-block-body{padding:0!important}.helm-scroll-block pre.code-block{max-height:none!important;border:0!important}"
                + ".badge{display:inline-flex!important;align-items:center!important;justify-content:center!important;"
                + "box-sizing:border-box!important;min-width:0!important;width:auto!important;"
                + "height:var(--atf-badge-height)!important;padding:var(--atf-badge-padding)!important;"
                + "border:0!important;border-radius:var(--atf-badge-radius)!important;"
                + "font-family:var(--atf-font-sans)!important;font-size:var(--atf-badge-font-size)!important;"
                + "line-height:var(--atf-badge-line-height)!important;font-weight:400!important;white-space:nowrap!important}"
                + ".badge:not(.badge-default){background:var(--atf-accent)!important;color:var(--atf-accent-contrast)!important}"
                + ".badge.badge-default{height:var(--atf-meta-badge-height)!important;min-height:var(--atf-meta-badge-height)!important;"
                + "padding:var(--atf-meta-badge-padding)!important;border-radius:var(--atf-meta-badge-radius)!important;"
                + "font-size:var(--atf-meta-badge-font-size)!important;line-height:var(--atf-meta-badge-line-height)!important}"
                + ".pass-bg,.pass-bg.badge{background:var(--atf-pass)!important;color:var(--atf-pass-text)!important}"
                + ".fail-bg,.fail-bg.badge{background:var(--atf-fail)!important;color:var(--atf-fail-text)!important}"
                + ".warning-bg,.warning-bg.badge{background:var(--atf-warning)!important;color:var(--atf-warning-text)!important}"
                + ".skip-bg,.skip-bg.badge{background:var(--atf-skip)!important;color:var(--atf-skip-text)!important}"
                + ".info-bg,.info-bg.badge{background:var(--atf-info)!important;color:var(--atf-info-text)!important}"
                + ".status-info{color:var(--atf-info-text)!important;background:var(--atf-info)!important}"
                + ".test-contents>.mt-4>.accordion>.card{background:transparent!important;"
                + "border-color:var(--atf-border)!important;border-radius:0!important;"
                + "box-shadow:0 10px 26px " + AtfReportColors.SHADOW_STRONG + "!important}"
                + ".test-contents>.mt-4>.accordion>.card>.card-header{background:transparent!important}"
                + ".test-contents>.mt-4>.accordion>.card .node{background:transparent!important;"
                + "border:0!important;border-radius:0!important;box-shadow:none!important;margin:0!important}"
                + ".atf-scenario-annotations{display:flex!important;flex-wrap:wrap!important;align-items:center!important;gap:6px!important;margin-top:8px!important}"
                + ".atf-tag-annotation{display:inline-flex!important;align-items:center!important;padding:4px 9px!important;border:1px solid var(--atf-border)!important;border-radius:999px!important;background:var(--atf-panel)!important;color:var(--atf-text)!important;font-size:11px!important;line-height:16px!important}"
                + ".test-view .test-list{background:var(--atf-dark)!important}"
                + ".test-view .test-content-detail{background:var(--atf-dark)!important}";
    }
}
