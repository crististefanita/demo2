package atf.api.reporting.theme.css;

import atf.api.reporting.theme.tokens.AtfReportThemeTokens;

/**
 * Responsive layout overrides for the Extent test split view.
 *
 * <p>Extent's desktop layout keeps the test list and the selected test side by
 * side.  Its mobile rule intentionally moves the wrapper by the desktop
 * sidebar width, which leaves a 200px horizontal offset in a static report.
 * The report remains usable on a narrow viewport when the wrapper starts at
 * the viewport edge; the existing list/content transition still controls
 * which pane is visible.</p>
 */
public final class AtfReportThemeResponsiveCss {
    private AtfReportThemeResponsiveCss() {
    }

    public static String css() {
        return "@media only screen and (max-width:" + AtfReportThemeTokens.NARROW_VIEWPORT + "){"
                + ".test-wrapper,.test-view{left:var(--atf-narrow-nav-inset)!important;"
                + "width:calc(100% - var(--atf-narrow-nav-inset))!important;max-width:calc(100% - var(--atf-narrow-nav-inset))!important;"
                + "box-sizing:border-box!important}"
                + ".test-wrapper .test-list,.test-view .test-list{width:100%!important;max-width:100%!important;"
                + "box-sizing:border-box!important}"
                + ".test-wrapper .test-content,.test-view .test-content{left:var(--atf-narrow-nav-inset)!important;"
                + "width:calc(100% - var(--atf-narrow-nav-inset))!important;max-width:calc(100% - var(--atf-narrow-nav-inset))!important;"
                + "box-sizing:border-box!important}"
                + "}";
    }
}
