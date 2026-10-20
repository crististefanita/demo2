package atf.ui.tests.extent.support.locators;

import atf.api.reporting.theme.tokens.AtfReportCssSelectors;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.api.ui.playwright.AtfLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Locators for report surfaces, tabs and the overview projection. */
final class ReportPageLocators {
    private static final String TESTS_VIEW = ReportLocatorSupport.idSelector(
            AtfReportCssSelectors.Surface.TEST_VIEW);
    private static final String OVERVIEW_VIEW = ReportLocatorSupport.idSelector(
            AtfReportCssSelectors.Surface.DASHBOARD_VIEW);
    private static final String CATEGORY_VIEW = ReportLocatorSupport.idSelector(
            AtfReportCssSelectors.Surface.CATEGORY_VIEW);
    private static final String FAILS_VIEW = ReportLocatorSupport.idSelector(
            AtfReportCssSelectors.Surface.FAILS_VIEW);
    private static final String TEST_LIST = AtfReportCssSelectors.Component.TEST_LIST.attributeSelector();
    private static final String VISIBLE_TEST_DETAIL = ReportLocatorSupport.VISIBLE_TEST_DETAIL;
    private static final String EXECUTION_METADATA = ReportLocatorSupport.idSelector("atf-execution-metadata");
    private static final String OPEN_DISCLOSURES = "details[open], [class~='collapse'][class~='show']";
    private static final String DECORATIVE_SEPARATORS = "hr, [class~='atf-ui-zone-separator']";
    private static final String IMAGES = "img";

    Locator exactText(Locator scope, String text) {
        return scope.getByText(text, new Locator.GetByTextOptions().setExact(true));
    }

    Locator exactLink(Locator scope, String text) {
        return scope.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(text).setExact(true));
    }

    Locator testsView(Page page) { return page.locator(TESTS_VIEW); }
    Locator runtimeRoot(Page page) { return page.locator("html"); }
    Locator decorativeSeparators(Page page) { return page.locator(DECORATIVE_SEPARATORS); }
    Locator images(Page page) { return page.locator(IMAGES); }
    Locator navTab(Page page, ReportUiContract.Tab tab) { return page.locator(navTabSelector(tab)); }

    AtfLocator navTabAction(Page page, ReportUiContract.Tab tab) {
        return new AtfLocator(navTab(page, tab), navTabSelector(tab));
    }

    Locator overviewView(Page page) { return page.locator(OVERVIEW_VIEW); }
    Locator categoryView(Page page) { return page.locator(CATEGORY_VIEW); }
    Locator failsView(Page page) { return page.locator(FAILS_VIEW); }

    Locator tabView(Page page, ReportUiContract.Tab tab) {
        return switch (tab) {
            case TESTS -> testsView(page);
            case OVERVIEW -> overviewView(page);
            case CATEGORIES -> categoryView(page);
            case FAILS -> failsView(page);
        };
    }

    Locator tabViewByHash(Page page, String hash) {
        for (ReportUiContract.Tab tab : ReportUiContract.Tab.values()) {
            if (tab.hash().equals(hash)) return tabView(page, tab);
        }
        throw new IllegalArgumentException("Unsupported report tab hash: " + hash);
    }

    ReportUiContract.Tab tabByHash(String hash) {
        for (ReportUiContract.Tab tab : ReportUiContract.Tab.values()) {
            if (tab.hash().equals(hash)) return tab;
        }
        throw new IllegalArgumentException("Unsupported report tab hash: " + hash);
    }

    Locator testsList(Page page) { return testsView(page).locator(TEST_LIST); }
    Locator visibleDisclosureHeaders(Page page, ReportUiContract.Tab tab) {
        return tabView(page, tab).locator(AtfReportCssSelectors.Component.CARD_HEADER.attributeSelector()
                + "[role='button']:visible");
    }
    Locator disclosureHeaderNodes(Locator header) {
        return header.locator(AtfReportCssSelectors.Component.CARD_TITLE.attributeSelector()
                + " " + AtfReportCssSelectors.Component.NODE.attributeSelector());
    }
    Locator openDisclosures(Locator scope) { return scope.locator(OPEN_DISCLOSURES); }
    Locator overviewCard(Page page) {
        return overviewView(page).locator(AtfReportCssSelectors.Component.CARD.attributeSelector()).first();
    }
    Locator categoryList(Page page) { return categoryView(page).locator(TEST_LIST); }
    Locator categoryList(Locator category) { return category.locator(TEST_LIST); }
    Locator testContentDetail(Page page) { return testsView(page).locator(VISIBLE_TEST_DETAIL).last(); }
    Locator categoryContentDetail(Page page) { return categoryView(page).locator(VISIBLE_TEST_DETAIL); }
    Locator overviewTables(Locator overview) { return overview.locator("table"); }
    Locator overviewSection(Locator overview, String selector) { return overview.locator(selector); }
    Locator overviewMetrics(Locator overview) {
        return overview.locator(ReportUiContract.OVERVIEW_EXECUTION_METADATA_GRID
                + " " + ReportUiContract.OVERVIEW_SECONDARY_METRIC);
    }
    Locator overviewExecutionMetadata(Locator overview) { return overview.locator(EXECUTION_METADATA); }
    AtfLocator overviewExecutionMetadataAction(Page page) {
        return new AtfLocator(overviewExecutionMetadata(overviewView(page)).locator("summary"),
                OVERVIEW_VIEW + " " + EXECUTION_METADATA + " > summary");
    }
    Locator overviewAuditRows(Locator overview) {
        return overview.locator(ReportUiContract.OVERVIEW_SCENARIO_AUDIT + " tbody tr");
    }
    Locator metricLabel(Locator metric) { return metric.locator("small"); }
    Locator metricValue(Locator metric) { return metric.locator("strong"); }
    Locator tableCells(Locator row) { return row.locator("td"); }
    Locator overviewCategoryCells(Locator overview) {
        return overview.locator("[class~='category-container'] table tbody td");
    }

    private static String navTabSelector(ReportUiContract.Tab tab) {
        return "[id='nav-" + switch (tab) {
            case TESTS -> "test";
            case CATEGORIES -> "category";
            case FAILS -> "fails";
            case OVERVIEW -> "overview";
        } + "']";
    }
}

