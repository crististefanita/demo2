package atf.api.reporting.theme;
import atf.api.reporting.asset.ReportAssetComposer;
import atf.api.reporting.theme.css.AtfReportThemeBaseCss;
import atf.api.reporting.theme.css.AtfReportThemeDetailCss;
import atf.api.reporting.theme.css.AtfReportThemeNavigationCss;
import atf.api.reporting.theme.css.AtfReportThemeResponsiveCss;
import atf.api.reporting.theme.css.AtfReportThemeScenarioCss;
import atf.api.reporting.theme.evidence.AtfReportEvidenceCss;
import atf.api.reporting.theme.tokens.AtfFallbackTheme;
import atf.api.reporting.theme.tokens.AtfReportThemeTokens;

import atf.api.reporting.theme.AtfReportFailsLayoutCss;

/**
 * Composition root for the visual contract shared by the Java report and the
 * reference .NET ATF report. Semantic CSS layers own their own surface; this
 * class only defines their order and public rendering entry points.
 */
public final class AtfReportTheme {
    public static final String FONT_STACK = AtfReportThemeTokens.FONT_STACK;

    private AtfReportTheme() {
    }

    public static String sparkCss() {
        return "body{font-family:" + FONT_STACK + ";line-height:"
                + AtfReportThemeTokens.BASE_LINE_HEIGHT + ";}";
    }

    public static String fallbackCss() {
        return AtfFallbackTheme.css();
    }

    public static String css() {
        return AtfReportThemeTokens.cssVariables()
                + AtfReportThemeBaseCss.css()
                + AtfReportEvidenceCss.css()
                + AtfReportThemeNavigationCss.css()
                + AtfReportThemeResponsiveCss.css()
                + AtfReportFailsLayoutCss.css()
                + AtfReportThemeDetailCss.css()
                + AtfReportThemeScenarioCss.css()
                + ReportAssetComposer.tableViewportCss();
    }
}

