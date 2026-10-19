package atf.ui.tests.extent.support.locators;

import atf.api.reporting.theme.tokens.AtfReportCssSelectors;
import atf.api.reporting.ui.contract.ReportUiContract;
import com.microsoft.playwright.Locator;

/** Shared structural selector primitives for the report locator boundary. */
final class ReportLocatorSupport {
    static final String TESTS_VIEW = AtfReportCssSelectors.Surface.TEST_VIEW.hash();
    static final String VISIBLE_TEST_DETAIL = ReportUiContract.VISIBLE_DETAIL_SELECTOR;

    private ReportLocatorSupport() {
    }

    static String cssAttribute(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Unsafe selector attribute value");
        }
        return value;
    }

    static int requireIndex(int zeroBasedIndex) {
        if (zeroBasedIndex < 0) throw new IllegalArgumentException("zeroBasedIndex must be >= 0");
        return zeroBasedIndex;
    }

    static Locator cardForHeader(Locator header) {
        return header.locator("xpath=..");
    }

    static String requiredAttribute(Locator locator, String attribute, String description) {
        String value = locator.getAttribute(attribute);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(description + " is missing required " + attribute
                    + " attribute; the report cannot be replayed unambiguously");
        }
        return value;
    }

    static String nodeId(Locator header) {
        Locator node = cardForHeader(header).locator(":scope > .card-header > .card-title > .node");
        return requiredAttribute(node, "id", "structural report node");
    }

    static String nodeHeaderSelector(String nodeId) {
        String safeId = cssAttribute(nodeId);
        return TESTS_VIEW + " " + VISIBLE_TEST_DETAIL
                + " div.card:has(> .card-header > .card-title > .node[id='"
                + safeId + "']) > .card-header[role='button']";
    }

    static String nodeScopedSelector(String nodeId, String suffix) {
        String safeId = cssAttribute(nodeId);
        return TESTS_VIEW + " " + VISIBLE_TEST_DETAIL
                + " div.card:has(> .card-header > .card-title > .node[id='"
                + safeId + "']) " + suffix;
    }
}

