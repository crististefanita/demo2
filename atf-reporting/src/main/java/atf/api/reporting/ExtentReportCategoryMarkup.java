package atf.api.reporting;

/**
 * Owns the stable category-view shell that the vendor Spark renderer does not
 * emit when a report has no tags.  Keeping the shell identical for zero, one
 * and many categories lets the browser audit use one locator contract.
 */
final class ExtentReportCategoryMarkup {
    private static final String CATEGORY_VIEW_ID = "id=\"category-view\"";
    private static final String CATEGORY_VIEW_MARKER = "<div id=\"atf-fails-view\"";

    private ExtentReportCategoryMarkup() {
    }

    static String ensureView(String html) {
        if (html.contains(CATEGORY_VIEW_ID)) return html;

        String marker = CATEGORY_VIEW_MARKER;
        int insertionPoint = html.indexOf(marker);
        if (insertionPoint < 0) {
            throw new IllegalStateException("Report has no stable insertion point for the category view");
        }
        return html.substring(0, insertionPoint) + emptyView() + html.substring(insertionPoint);
    }

    static String emptyView() {
        return "<div id=\"category-view\" class=\"test-wrapper row view category-view attributes-view hide\">"
                + "<div class=\"test-list\">"
                + "<div class=\"test-list-tools\"><ul class=\"tools pull-left\"><li>"
                + "<a href=\"\"><span class=\"font-size-14\">Category</span></a>"
                + "</li></ul><ul class=\"tools text-right\"><li><a href=\"#\">"
                + "<span class=\"badge badge-primary\">0</span></a></li></ul></div>"
                + "<div class=\"test-list-wrapper scrollable\"><ul class=\"test-list-item\"></ul></div>"
                + "</div>"
                + "<div class=\"test-content scrollable\"><div class=\"test-content-tools\">"
                + "<ul><li><a class=\"back-to-test\" href=\"#\"><i class=\"fa fa-arrow-left\"></i></a></li></ul>"
                + "</div><div class=\"test-content-detail\"><div class=\"detail-body atf-empty-state\""
                + " data-atf-empty-state=\"categories\" role=\"status\">"
                + "<strong class=\"atf-empty-state-title\">No categories available</strong>"
                + "<span class=\"atf-empty-state-copy\">No category annotations were published for this report.</span>"
                + "</div></div></div></div>";
    }
}
