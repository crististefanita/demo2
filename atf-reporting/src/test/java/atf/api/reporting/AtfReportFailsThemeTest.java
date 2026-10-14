package atf.api.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtfReportFailsThemeTest {
    @Test
    void failsViewUsesTheSharedDarkSurfaceAndOwnsTheVendorStatusConflict() {
        String css = AtfReportTheme.css();

        assertAll(
                () -> assertTrue(css.contains(".atf-fails-view{padding:1.5rem!important;overflow:auto!important;background:var(--atf-dark)!important")),
                () -> assertTrue(css.contains(".atf-failure>.status{order:2;flex:0 0 auto;display:inline-flex!important")),
                () -> assertTrue(css.contains("background:var(--atf-fail)!important;color:var(--atf-fail-text)!important")),
                () -> assertTrue(css.contains("#atf-fails-view.exception-view.atf-fails-view{display:none!important}")),
                () -> assertTrue(css.contains("#atf-fails-view.exception-view.atf-fails-view:not(.hide){display:flex!important")),
                () -> assertTrue(css.contains(".exception-view.atf-fails-view .test-list{flex:0 0 32%!important")),
                () -> assertTrue(css.contains(".exception-view.atf-fails-view .test-content{display:block!important")),
                () -> assertTrue(css.contains(".atf-failure-index{margin:28px!important")),
                () -> assertTrue(css.contains(".atf-failure-index-row")),
                () -> assertTrue(css.contains(".atf-failure-open")),
                () -> assertTrue(css.contains("gap:12px!important;width:100%!important;min-width:0!important;min-height:0!important")),
                () -> assertTrue(css.contains("font-size:10.08px!important;line-height:12.6px!important")),
                () -> assertTrue(css.contains("[status=\"fail\"]{border-left-color:var(--atf-bad)!important")),
                () -> assertTrue(css.contains(".test-item .test-detail .name{font-family:var(--atf-font-sans)!important;font-weight:400!important;color:var(--atf-card-text)!important")),
                () -> assertTrue(!css.contains("color:#ddd"), "Fails must consume the shared card-text token"),
                () -> assertTrue(css.contains(".test-item .test-detail .text-sm{font-family:var(--atf-font-sans)!important}")),
                () -> assertTrue(css.contains(".exception-view.atf-fails-view .test-status .badge{display:inline-flex!important;align-items:center!important;justify-content:center!important;min-width:0!important;width:auto!important;height:var(--atf-badge-height)!important")
                        && css.contains("padding:var(--atf-badge-padding)!important;border:0!important;border-radius:var(--atf-badge-radius)!important")
                        && css.contains("font-size:var(--atf-badge-font-size)!important;line-height:var(--atf-badge-line-height)!important;font-weight:400!important"),
                        "Fails status labels must use the compact shared geometry"),
                () -> assertTrue(css.contains(".exception-view.atf-fails-view .test-status .badge.badge-danger{background:var(--atf-fail)!important;color:var(--atf-fail-text)!important}"),
                        "Fails must use the shared red Fail status colors instead of the vendor accent"));
    }
}
