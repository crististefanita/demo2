package atf.api.reporting.theme;

/** Owns the deterministic list-left/detail-right geometry of the Fails view. */
public final class AtfReportFailsLayoutCss {
    private AtfReportFailsLayoutCss() {
    }

    public static String css() {
        return "#atf-fails-view.exception-view.atf-fails-view:not(.hide){"
                + "display:flex!important;flex-direction:row!important;flex-wrap:nowrap!important;"
                + "align-items:stretch!important;width:100%!important;box-sizing:border-box!important;"
                + "padding:0!important;min-height:calc(100vh - 56px)!important;overflow:hidden!important}"
                + ".exception-view.atf-fails-view .test-list{"
                + "flex:0 0 32%!important;max-width:32%!important;min-width:280px!important;"
                + "overflow:auto!important;box-sizing:border-box!important}"
                + ".exception-view.atf-fails-view .test-content{"
                + "display:block!important;position:relative!important;left:auto!important;right:auto!important;"
                + "top:auto!important;bottom:auto!important;width:auto!important;max-width:none!important;"
                + "height:auto!important;flex:1 1 auto!important;align-self:stretch!important;"
                + "box-sizing:border-box!important;min-width:0!important;overflow:auto!important}"
                + "@media(max-width:760px){#atf-fails-view.exception-view.atf-fails-view:not(.hide){"
                + "overflow:auto!important}.exception-view.atf-fails-view .test-list{"
                + "min-width:240px!important;flex-basis:36%!important;max-width:36%!important}"
                + ".exception-view.atf-fails-view .test-content{flex-basis:64%!important}}";
    }
}
