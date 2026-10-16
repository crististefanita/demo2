package atf.api.reporting.theme;

/** Semantic CSS owned by the Categories surface, including its empty state. */
public final class AtfReportCategoryCss {
    private AtfReportCategoryCss() {
    }

    public static String css() {
        return ".category-view .atf-empty-state{display:flex!important;flex-direction:column!important;"
                + "align-items:flex-start!important;justify-content:center!important;min-height:180px!important;"
                + "margin:24px!important;padding:22px 24px!important;border:1px solid var(--atf-border)!important;"
                + "border-left:3px solid var(--atf-accent)!important;background:var(--atf-surface)!important;"
                + "color:var(--atf-text)!important}"
                + ".category-view .atf-empty-state-title{color:var(--atf-text)!important;"
                + "font-size:14px!important;line-height:21.7px!important;font-weight:400!important}"
                + ".category-view .atf-empty-state-copy{margin-top:5px!important;color:var(--atf-muted)!important;"
                + "font-size:12.6px!important;line-height:16.884px!important}"
                + "@media(max-width:760px){.category-view .atf-empty-state{margin:16px!important;padding:18px!important}}";
    }
}
