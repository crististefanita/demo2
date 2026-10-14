package atf.api.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtfReportOverviewCssTest {
    @Test
    void overviewProviderOwnsTheScrollableTableAndDisclosureContract() {
        String overview = AtfReportOverviewCss.metrics();

        assertAll(
                () -> assertTrue(overview.contains(".atf-secondary-overview>summary")),
                () -> assertTrue(overview.contains(".atf-tag-table{width:100%!important")),
                () -> assertTrue(overview.contains(".dashboard-view .category-container>.card>.card-body")),
                () -> assertTrue(overview.contains("height:500px!important;max-height:500px!important")),
                () -> assertTrue(overview.contains("overflow-y:auto!important")),
                () -> assertTrue(overview.contains("position:sticky!important;top:0!important")),
                () -> assertTrue(overview.contains("var(--atf-overview-font-family)")),
                () -> assertTrue(overview.contains("var(--atf-overview-font-size)")));
    }

    @Test
    void themeComposesOverviewAfterExecutionRules() {
        String theme = AtfReportTheme.css();
        String overview = AtfReportOverviewCss.metrics();

        int overviewStart = theme.indexOf(overview);
        int executionAnchor = theme.indexOf(".test-view .test-content-detail");

        assertAll(
                () -> assertTrue(overviewStart >= 0, "overview provider must be present in the final theme"),
                () -> assertTrue(executionAnchor >= 0),
                () -> assertTrue(executionAnchor < overviewStart,
                        "overview precedence must remain after execution-page rules"),
                () -> assertTrue(theme.contains(AtfReportOverviewCss.categoryStatus())));
    }
}
