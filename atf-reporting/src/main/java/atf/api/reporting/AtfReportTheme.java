package atf.api.reporting;

/**
 * Single owner for the visual contract shared by the Java report and the
 * reference .NET ATF report.  Keeping the tokens here prevents the Extent
 * adapter from becoming a second, drifting stylesheet.
 */
final class AtfReportTheme {
    static final String FONT_STACK = AtfReportThemeTokens.FONT_STACK;

    private AtfReportTheme() {
    }

    static String sparkCss() {
        return "body{font-family:" + FONT_STACK + ";line-height:1.55;}";
    }

    static String fallbackCss() {
        return AtfFallbackTheme.css();
    }

    static String css() {
        return (AtfReportThemeTokens.cssVariables()
                + "html,body,body.dark,.app,.layout,.vcontainer,.main-content,.view,.test-content,.test-list{"
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
                + AtfReportEvidenceCss.css()
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header,.test-view .test-content-detail .card[data-atf-node-role]>.card-header{display:flex!important;align-items:center!important;column-gap:8px!important;row-gap:4px!important;flex-wrap:wrap!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header>.card-title,.test-view .test-content-detail .card[data-atf-node-role]>.card-header>.card-title{display:flex!important;align-items:center!important;flex:1 1 auto!important;min-width:0!important;margin:0!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-id]>.card-header:before,.test-view .test-content-detail .card[data-atf-node-role]>.card-header:before{display:inline-flex!important;flex:0 0 18px!important;width:18px!important;height:18px!important;align-items:center!important;justify-content:center!important;color:var(--atf-accent)!important;font-size:17px!important;font-weight:500!important;line-height:18px!important;content:'＋'!important}"
                + ".test-view .test-content-detail .card[data-atf-scenario-open='true']>.card-header:before,.test-view .test-content-detail .card[data-atf-phase-open='true']>.card-header:before,.test-view .test-content-detail .card[data-atf-node-open='true']>.card-header:before{content:'−'!important}"
                + ".test-view .test-content-detail .card[data-atf-node-expandable='false']>.card-header:before{display:none!important}"
                // Scenario annotations follow the .NET reference: real tags are
                // visible inline under the scenario title; the generic label is
                // retained only as an accessible aria-label on the tag list.
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
// Status badges remain compact; duration, anchor, and controls
// use separate metadata geometry so Pass/Fail do not grow.
                + ".badge.badge-default{height:var(--atf-meta-badge-height)!important;min-height:var(--atf-meta-badge-height)!important;"
                + "padding:var(--atf-meta-badge-padding)!important;border-radius:var(--atf-meta-badge-radius)!important;"
                + "font-size:var(--atf-meta-badge-font-size)!important;line-height:var(--atf-meta-badge-line-height)!important}"
                + ".pass-bg,.pass-bg.badge{background:var(--atf-pass)!important;color:var(--atf-pass-text)!important}"
                + ".fail-bg,.fail-bg.badge{background:var(--atf-fail)!important;color:var(--atf-fail-text)!important}"
                + ".warning-bg,.warning-bg.badge{background:var(--atf-warning)!important;color:var(--atf-warning-text)!important}"
                + ".skip-bg,.skip-bg.badge{background:var(--atf-skip)!important;color:var(--atf-skip-text)!important}"
                + ".info-bg,.info-bg.badge{background:var(--atf-info)!important;color:var(--atf-info-text)!important}"
                + ".status-info{color:var(--atf-info-text)!important;background:var(--atf-info)!important}"
                // The .NET adapter deliberately leaves direct scenario cards
                // transparent and reserves the strong card border for evidence.
                // Keep this precedence layer after the broad Extent card rules.
                + ".test-contents>.mt-4>.accordion>.card{background:transparent!important;"
                + "border-color:var(--atf-border)!important;border-radius:0!important;"
                + "box-shadow:0 10px 26px " + AtfReportColors.SHADOW_STRONG + "!important}"
                + ".test-contents>.mt-4>.accordion>.card>.card-header{background:transparent!important}"
                + ".test-contents>.mt-4>.accordion>.card .node{background:transparent!important;"
                + "border:0!important;border-radius:0!important;box-shadow:none!important;margin:0!important}"
                + ".atf-scenario-annotations{display:flex!important;flex-wrap:wrap!important;align-items:center!important;gap:6px!important;margin-top:8px!important}"
                + ".atf-tag-annotation{display:inline-flex!important;align-items:center!important;padding:4px 9px!important;border:1px solid var(--atf-border)!important;border-radius:999px!important;background:var(--atf-panel)!important;color:var(--atf-text)!important;font-size:11px!important;line-height:16px!important}"
                + ".test-view .test-list{background:var(--atf-dark)!important}"
                + ".test-view .test-content-detail{background:var(--atf-dark)!important}"
                // Category view follows the .NET reference surface contract:
                // both panes and the selected category use one dark token.
                + ".category-view,.category-view .test-list,.category-view .test-list-tools,"
                + ".category-view .test-content,.category-view .test-content-tools,"
                + ".category-view .test-content-detail,.category-view .detail-body{"
                + "background:var(--atf-dark)!important;color:var(--atf-text)!important;"
                + "border-color:var(--atf-border)!important}"
                + ".category-view .test-list-wrapper{background:var(--atf-dark)!important}"
                + ".category-view .test-list-wrapper>.test-list-item>.test-item{"
                + "background:var(--atf-dark)!important;border-bottom:1px solid var(--atf-border)!important;"
                + "color:var(--atf-text)!important}"
                + ".category-view .test-list-wrapper>.test-list-item>.test-item:hover,"
                + ".category-view .test-list-wrapper>.test-list-item>.test-item.active{"
                + "background:var(--atf-panel)!important}"
                + ".category-view .test-content-detail>.test-contents>.mt-4>.accordion>.card,"
                + ".category-view .test-content-detail>.test-contents>.mt-4>.accordion>.card>.card-header,"
                + ".category-view .test-content-detail>.test-contents>.mt-4>.accordion>.card>.card-title{"
                + "background:var(--atf-dark)!important;color:var(--atf-text)!important;"
                + "border-color:var(--atf-border)!important}"
                + ".category-view a.linked{color:var(--atf-accent)!important;text-decoration:none!important;"
                + "font-family:var(--atf-font-sans)!important}"
                + ".category-view a.linked:hover,.category-view a.linked:focus{"
                + "color:var(--atf-text)!important;text-decoration:underline!important}"
                + ".category-view .test-content-detail .card-title a.node{"
                + "color:var(--atf-text)!important;text-decoration:none!important}"
                + ".test-view .test-list-tools .dropdown-menu{background:var(--atf-dark)!important;"
                + "border:1px solid var(--atf-border)!important;color:var(--atf-text)!important;"
                + "box-shadow:0 10px 26px " + AtfReportColors.SHADOW_SOFT + "!important}"
                + ".test-view .test-list-tools .dropdown-menu .dropdown-item{background:var(--atf-dark)!important;"
                + "color:var(--atf-text)!important;font-family:var(--atf-font-sans)!important;"
                + "font-size:var(--atf-font-base-size)!important;line-height:1.55!important}"
                + ".test-view .test-list-tools .dropdown-menu .dropdown-item:hover,"
                + ".test-view .test-list-tools .dropdown-menu .dropdown-item:focus,"
                + ".test-view .test-list-tools .dropdown-menu .dropdown-item.active{"
                + "background:var(--atf-panel)!important;color:var(--atf-text)!important}"
                + ".test-view .test-list-wrapper>.test-list-item>.test-item{"
                + "background:var(--atf-dark)!important;border-bottom:1px solid var(--atf-border)!important}"
                + ".test-view .test-list-wrapper>.test-list-item>.test-item.active{"
                + "background:var(--atf-panel)!important}"
                + ".test-view .test-content-detail .card>.card-header .card-title .node{"
                + "color:var(--atf-text)!important;font-family:var(--atf-font-sans)!important;"
                + "font-size:var(--atf-font-base-size)!important;line-height:1.55!important;font-weight:400!important;"
                + "display:inline-block!important;padding:10.5px!important}"
                + ".test-view .test-content-detail .card>.card-header .card-title .node::after{"
                + "content:none!important;display:none!important}"
                + ".test-view .test-content-detail .card[data-atf-node-role=\"phase\"]{"
                + "background:var(--atf-dark)!important;border:0!important;"
                + "border-left:3px solid var(--atf-accent)!important;"
                + "box-shadow:0 6px 16px " + AtfReportColors.SHADOW_SOFT + "!important}"
                + ".test-view .test-content-detail .card>.card-header .node-attr{"
                + "display:flex!important;align-items:center!important;gap:6px!important;"
                + "margin:5px 0 0!important;padding:0 6px 0 0!important;height:auto!important;"
                + "font-family:var(--atf-font-sans)!important;line-height:1.12!important}"
                + ".test-view .test-content-detail .card>.card-header .node-attr .badge{"
                + "display:inline-block!important;box-sizing:border-box!important;margin:0 0 5px!important;"
                + "padding:1px 7px!important;border:1px solid var(--atf-accent)!important;"
                + "border-radius:999px!important;background:var(--atf-accent08)!important;"
                + "color:var(--atf-accent)!important;font-size:var(--atf-font-badge-size)!important;"
                + "line-height:1.2!important;font-weight:400!important;white-space:nowrap!important}"
                + ".test-view .detail-body>table thead th:nth-child(2),"
                + ".test-view .detail-body>table tbody td:nth-child(2),"
                + ".test-view .test-content-detail table.table>thead>tr>th.timestamp-col,"
                + ".test-view .test-content-detail table.table>tbody>tr>td:nth-child(2),"
                + ".atf-fails-view .test-content-detail table.table>thead>tr>th.timestamp-col,"
                + ".atf-fails-view .test-content-detail table.table>tbody>tr>td:nth-child(2){display:none!important}"
                + ".test-contents .card[data-atf-node-role=\"phase\"]{margin:12px 0!important;padding:0!important;"
                + "border:1px solid var(--atf-border)!important;border-left:3px solid var(--atf-accent)!important;"
                + "border-radius:8px!important;background:var(--atf-dark)!important;"
                + "box-shadow:0 10px 26px " + AtfReportColors.SHADOW_SOFT + "!important;overflow:hidden!important}"
                + ".test-contents .card[data-atf-node-role=\"phase\"]>.card-header{"
                + "background:" + AtfReportColors.ACCENT_045 + "!important;border-bottom:1px solid var(--atf-border)!important}"
                + ".test-contents .card[data-atf-node-role=\"phase\"][data-atf-phase-name=\"setup\"]{border-left-color:var(--atf-setup)!important}"
                + ".test-contents .card[data-atf-node-role=\"phase\"][data-atf-phase-name=\"test-body\"]{border-left-color:var(--atf-accent)!important}"
                + ".test-contents .card[data-atf-node-role=\"phase\"][data-atf-phase-name=\"teardown\"]{border-left-color:var(--atf-good)!important}"
                + ".test-contents .card[data-atf-node-role=\"phase\"] [data-atf-node-role=\"request\"],"
                + ".test-contents .card[data-atf-node-role=\"phase\"] [data-atf-node-role=\"validation\"]{border-left-width:1px!important}"
                + ".test-view .test-content-detail .card-title,.test-view .test-content-detail .card-title a.node,"
                + ".test-view .test-content-detail .card-title span{font-family:var(--atf-font-sans)!important;"
                + "font-size:var(--atf-font-base-size)!important;line-height:1.34!important;font-weight:400!important;letter-spacing:0!important}"
                + ".test-content h5.test-status{color:var(--atf-text)!important;font-family:var(--atf-font-sans)!important;"
                + "font-size:15.4px!important;line-height:1.34!important;font-weight:400!important;letter-spacing:0!important}"
                + ".test-view .test-list-wrapper>.test-list-item>.test-item{display:flex!important;align-items:stretch!important}"
                // Keep the compact Tests navigation: only the duration remains
                // visible below the test name; the timestamp stays in details.
                + ".test-view .test-list .test-item .test-detail .text-sm>span:first-child{display:none!important}"
                + ".test-view .test-list .badge,.test-view .test-contents .badge,.test-content .badge.badge-success,"
                + ".test-content .badge.badge-danger,.test-content .badge.badge-default{display:inline-flex!important;align-items:center!important;justify-content:center!important;font-size:var(--atf-badge-font-size)!important;"
                + "line-height:var(--atf-badge-line-height)!important;font-weight:400!important}"
                + ".test-view .test-list .badge.warning-bg,.test-view .test-list .badge.badge-warning,.test-view .test-contents .badge.warning-bg,.test-view .test-contents .badge.badge-warning,.test-content .badge.warning-bg,.test-content .badge.badge-warning{background:var(--atf-skip)!important;color:" + AtfReportColors.WHITE + "!important}"
                + ".test-view .test-list .test-item .text-sm{display:flex!important;align-items:center!important;justify-content:space-between!important;gap:8px!important}"
                + ".test-view .test-list .test-item .text-sm>.badge{display:inline-flex!important;align-items:center!important;justify-content:center!important;float:none!important;margin-left:auto!important;height:var(--atf-badge-height)!important;min-height:var(--atf-badge-height)!important;padding:var(--atf-badge-padding)!important;line-height:var(--atf-badge-line-height)!important}"
                // The reference keeps execution timestamps in the DOM for
                // provenance, but hides them from the compact test header.
                // The duration remains visible and uses the same unit label.
                + ".test-view .test-content-detail .detail-head .info>span.badge-success,"
                + ".test-view .test-content-detail .detail-head .info>span.badge-danger{display:none!important}"
                + ".atf-fails-view{padding:1.5rem!important;overflow:auto!important;background:var(--atf-dark)!important;color:var(--atf-text)!important;font-family:" + FONT_STACK + "!important}.atf-fails-view .atf-failure{"
                + "display:flex;gap:1rem;align-items:center;padding:1rem;margin-bottom:.65rem;border:1px solid var(--atf-border);"
                + "border-left:3px solid var(--atf-fail);background:var(--atf-surface);color:var(--atf-text)}"
                + ".atf-fails-view a{color:var(--atf-accent)!important}.atf-fails-empty{padding:1.5rem;color:var(--atf-muted)}"
                + ".view.hide{display:none!important}.view.d-none.hide{display:none!important}"
                + "#test-view:not(.hide){display:flex!important}#category-view:not(.hide),#dashboard-view:not(.hide),"
                + "#atf-fails-view:not(.hide){display:block!important}"
                + "body.atf-overview-default #test-view,body.atf-overview-default #category-view,"
                + "body.atf-overview-default #atf-fails-view{display:none!important}"
                + "body.atf-overview-default #dashboard-view{display:block!important}"
                // Hash targets keep the four top-level tabs usable in strict
                // offline/DOM-only viewers where inline JavaScript is disabled.
                + ".view#test-view:target{display:flex!important}.view#category-view:target,.view#atf-fails-view:target{display:block!important}"
                + "body:has(#test-view:target) #dashboard-view,body:has(#test-view:target) #category-view,body:has(#test-view:target) #atf-fails-view,"
                + "body:has(#category-view:target) #dashboard-view,body:has(#category-view:target) #test-view,body:has(#category-view:target) #atf-fails-view,"
                + "body:has(#atf-fails-view:target) #dashboard-view,body:has(#atf-fails-view:target) #test-view,body:has(#atf-fails-view:target) #category-view{display:none!important}"
                + "#atf-fails-view.exception-view.atf-fails-view{display:none!important}#atf-fails-view.exception-view.atf-fails-view:not(.hide){display:flex!important;align-items:stretch!important;padding:0!important;min-height:calc(100vh - 56px)!important;overflow:hidden!important}"
                + ".exception-view.atf-fails-view .test-list{flex:0 0 32%!important;max-width:32%!important;min-width:280px!important;overflow:auto!important;background:var(--atf-dark)!important;border-right:1px solid var(--atf-border)!important}"
                + ".exception-view.atf-fails-view .test-content{display:block!important;flex:1 1 auto!important;min-width:0!important;overflow:auto!important;background:var(--atf-dark)!important}"
                + ".exception-view.atf-fails-view .test-list-tools{background:var(--atf-dark)!important;border-bottom:1px solid var(--atf-border)!important;color:var(--atf-accent)!important}"
                + ".exception-view.atf-fails-view .test-list-wrapper{background:var(--atf-dark)!important}"
                + ".exception-view.atf-fails-view .test-list-item>.test-item{display:flex!important;align-items:center!important;justify-content:space-between!important;min-height:74px!important;padding:14px 14px 14px 18px!important;background:var(--atf-dark)!important;border-bottom:1px solid var(--atf-border)!important;border-left:3px solid transparent!important;color:var(--atf-text)!important;cursor:pointer!important}"
                + ".exception-view.atf-fails-view .test-list-item>.test-item:hover,.exception-view.atf-fails-view .test-list-item>.test-item.active{background:var(--atf-panel)!important;border-left-color:var(--atf-fail)!important}"
                + ".exception-view.atf-fails-view .test-item .test-detail{min-width:0!important;flex:1 1 auto!important}.exception-view.atf-fails-view .test-item .test-detail .name{display:block!important;margin:0!important;color:var(--atf-text)!important;font-size:14px!important;line-height:1.55!important;white-space:normal!important}.exception-view.atf-fails-view .test-item .test-detail .text-sm{display:block!important;margin:3px 0 0!important;color:var(--atf-muted)!important;font-size:12px!important;line-height:1.55!important}"
                + ".exception-view.atf-fails-view .test-status{flex:0 0 auto!important;margin-left:10px!important}"
                + ".exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item{gap:12px!important;width:100%!important;min-width:0!important;min-height:0!important;padding:10px 15px!important;margin:0!important;justify-content:normal!important}.exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item[status=\"fail\"]{border-left-color:var(--atf-fail)!important}.exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item>.test-detail{padding-left:5px!important}.exception-view.atf-fails-view .test-item .test-detail .name{line-height:18.76px!important}.exception-view.atf-fails-view .test-item .test-detail .text-sm{font-size:10.08px!important;line-height:12.6px!important;margin:0!important}.exception-view.atf-fails-view .test-status{margin-left:auto!important;align-self:center!important;white-space:nowrap!important}"
                + ".exception-view.atf-fails-view .test-list-wrapper>.test-list-item>.test-item[status=\"fail\"]{border-left-color:var(--atf-bad)!important}.exception-view.atf-fails-view .test-item .test-detail .name{font-family:var(--atf-font-sans)!important;font-weight:400!important;color:var(--atf-card-text)!important}.exception-view.atf-fails-view .test-item .test-detail .text-sm{font-family:var(--atf-font-sans)!important}.exception-view.atf-fails-view .test-status .badge{font-family:var(--atf-font-sans)!important;font-weight:400!important}"
                + ".exception-view.atf-fails-view .test-content-detail{min-height:100%!important;background:var(--atf-dark)!important}.exception-view.atf-fails-view .atf-failure-test-detail{display:block!important;min-height:100%!important;padding:0!important;background:var(--atf-dark)!important;overflow:visible!important}.exception-view.atf-fails-view .atf-failure-test-detail>.test-contents{height:auto!important;max-height:none!important;overflow:visible!important}.exception-view.atf-fails-view .atf-failure-index{margin:28px!important;padding:20px!important;border:1px solid var(--atf-border)!important;border-left:3px solid var(--atf-fail)!important;border-radius:4px!important;background:var(--atf-surface)!important;color:var(--atf-text)!important}.exception-view.atf-fails-view .atf-failure-index-title{margin:0 0 18px!important;color:var(--atf-text)!important;font-size:16px!important;line-height:1.55!important;font-weight:400!important}.exception-view.atf-fails-view .atf-failure-index-row{display:grid!important;grid-template-columns:110px minmax(0,1fr)!important;gap:12px!important;padding:9px 0!important;border-bottom:1px solid var(--atf-border)!important;font-size:12.6px!important;line-height:1.55!important}.exception-view.atf-fails-view .atf-failure-index-row:last-of-type{border-bottom:0!important}.exception-view.atf-fails-view .atf-failure-label{color:var(--atf-muted)!important;font-size:10px!important;letter-spacing:.06em!important}.exception-view.atf-fails-view .atf-failure-value{color:var(--atf-text)!important;overflow-wrap:anywhere!important}.exception-view.atf-fails-view .atf-failure-index-cause .atf-failure-value{color:" + AtfReportColors.FAILURE_CAUSE_TEXT + "!important}.exception-view.atf-fails-view .atf-failure-open{margin-top:18px!important;padding:7px 12px!important;border:1px solid var(--atf-accent)!important;border-radius:4px!important;background:transparent!important;color:var(--atf-accent)!important;font-size:12px!important;line-height:16px!important;cursor:pointer!important}.exception-view.atf-fails-view .atf-failure-open:hover{background:" + AtfReportColors.ACCENT_12 + "!important}"
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
                 + overviewMetricsCss()
                 + categoryStatusCss()
                 + failsStatusColorCss()).replace("atf-failure-test-detail", "atf-failure-detail");
    }

    private static String categoryStatusCss() {
        return AtfReportOverviewCss.categoryStatus();
    }

    private static String failsStatusColorCss() {
        return ".exception-view.atf-fails-view .test-status .badge.badge-danger{"
                + "background:var(--atf-fail)!important;color:var(--atf-fail-text)!important}";
    }

    private static String overviewMetricsCss() {
        return AtfReportOverviewCss.metrics();
    }

}
