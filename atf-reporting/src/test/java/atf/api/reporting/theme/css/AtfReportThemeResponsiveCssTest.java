package atf.api.reporting.theme.css;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.theme.AtfReportTheme;
import atf.api.reporting.theme.tokens.AtfReportThemeTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the narrow static-report layout to the viewport edge. */
@AtfUnitTest
class AtfReportThemeResponsiveCssTest {

    @Test
    void keepsTheTestWrapperInsideTheViewportOnNarrowScreens() {
        String css = AtfReportThemeResponsiveCss.css();
        String theme = AtfReportTheme.css();
        String tokens = AtfReportThemeTokens.cssVariables();

        assertAll(
                () -> assertTrue(css.contains("@media only screen and (max-width:992px)")),
                () -> assertTrue(css.contains(".test-wrapper,.test-view{left:var(--atf-narrow-nav-inset)!important")),
                () -> assertTrue(css.contains("width:calc(100% - var(--atf-narrow-nav-inset))!important")),
                () -> assertTrue(css.contains(".test-wrapper .test-list,.test-view .test-list{width:100%!important")),
                () -> assertTrue(css.contains(".test-wrapper .test-content,.test-view .test-content{left:var(--atf-narrow-nav-inset)!important")),
                () -> assertTrue(tokens.contains("--atf-narrow-nav-inset:" + AtfReportThemeTokens.NARROW_NAV_INSET)),
                () -> assertTrue(theme.contains(".test-wrapper,.test-view{left:var(--atf-narrow-nav-inset)!important")));
    }
}
