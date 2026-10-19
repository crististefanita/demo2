package atf.api.reporting.runtime.section;

/** Overview fragment for vendor label normalization across dashboard and categories. */
final class ExtentReportRuntimeOverviewLabels {
    private static final String NORMALIZE_TAG_TABLE_HEADERS =
            "function normalizeTagTableHeaders(){q('.dashboard-view table th').forEach(function(th){if(String(th.textContent||'').trim().toUpperCase()==='OTHERS')th.textContent='WARNING';});}";
    private static final String NORMALIZE_NATIVE_SUMMARY_LABELS =
            "function normalizeNativeSummaryLabels(){q('.dashboard-view .card-footer small').forEach(function(s){if(String(s.textContent||'').toLowerCase().indexOf('others')>=0)s.innerHTML=s.innerHTML.replace(/\\bothers\\b/gi,'warnings');});}";
    private static final String NORMALIZE_CATEGORY_SUMMARY_LABELS =
            "function normalizeCategorySummaryLabels(){q('.category-view .test-contents .info').forEach(function(info){info.innerHTML=info.innerHTML.replace(/\\bothers\\b/gi,'warnings');});}";

    private ExtentReportRuntimeOverviewLabels() { }

    static String script() {
        return String.join("", NORMALIZE_TAG_TABLE_HEADERS,
                NORMALIZE_NATIVE_SUMMARY_LABELS, NORMALIZE_CATEGORY_SUMMARY_LABELS);
    }
}
