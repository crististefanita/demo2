package atf.api.reporting;

/**
 * Composition root for the visual contract shared by the Java report and the
 * reference .NET ATF report. Semantic CSS layers own their own surface; this
 * class only defines their order and public rendering entry points.
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
        return AtfReportThemeTokens.cssVariables()
                + AtfReportThemeBaseCss.css()
                + AtfReportEvidenceCss.css()
                + AtfReportThemeNavigationCss.css()
                + AtfReportFailsLayoutCss.css()
                + AtfReportThemeDetailCss.css();
    }
}

