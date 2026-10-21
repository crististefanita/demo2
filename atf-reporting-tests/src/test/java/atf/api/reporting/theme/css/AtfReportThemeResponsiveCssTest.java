package atf.api.reporting.theme.css;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.asset.ReportAsset;
import atf.api.reporting.asset.ReportAssetLoader;
import atf.api.reporting.theme.AtfReportTheme;
import atf.api.reporting.theme.tokens.AtfReportThemeTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the narrow static-report layout to the viewport edge. */
@AtfUnitTest
class AtfReportThemeResponsiveCssTest {

    @Test
    void keepsTheTestWrapperInsideTheViewportOnNarrowScreens() {
        String css = AtfReportThemeResponsiveCss.css();
        String theme = AtfReportTheme.css();
        String tokens = AtfReportThemeTokens.cssVariables();
        String source = new ReportAssetLoader().load(ReportAsset.EXTENT_THEME_RESPONSIVE_CSS);

        assertAll(
                () -> assertTrue(css.contains("@media only screen and (max-width:992px)")),
                () -> assertTrue(css.contains(".test-wrapper,.test-view{position:relative!important;left:var(--atf-narrow-nav-inset)!important")),
                () -> assertTrue(css.contains("width:calc(100% - var(--atf-narrow-nav-inset))!important")),
                () -> assertTrue(css.contains(".test-wrapper .test-list,.test-view .test-list{position:relative!important;float:none!important;flex:0 0 auto!important;width:100%!important")),
                () -> assertTrue(css.contains(".test-wrapper .test-content,.test-view .test-content{position:relative!important;left:auto!important")),
                () -> assertTrue(source.contains("${NARROW_VIEWPORT}")),
                () -> assertTrue(css.contains("@media only screen and (max-width:" + AtfReportThemeTokens.NARROW_VIEWPORT + ")")),
                () -> assertFalse(css.contains("${")),
                () -> assertTrue(tokens.contains("--atf-narrow-nav-inset:" + AtfReportThemeTokens.NARROW_NAV_INSET)),
                () -> assertTrue(theme.contains(".test-wrapper,.test-view{position:relative!important;left:var(--atf-narrow-nav-inset)!important")));
    }
}
