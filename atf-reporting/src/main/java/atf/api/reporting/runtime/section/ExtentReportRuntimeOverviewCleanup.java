package atf.api.reporting.runtime.section;

/** Overview fragment for removal of unsupported vendor cards. */
final class ExtentReportRuntimeOverviewCleanup {
    private static final String REMOVE_UNSUPPORTED_OVERVIEW_CARDS =
            "function removeUnsupportedOverviewCards(){q('#events-analysis').forEach(function(canvas){var column=canvas.closest('.col-md-3'),card=canvas.closest('.card');(column||card||canvas).remove();});q('.dashboard-view .card-header .card-title').forEach(function(title){if(String(title.textContent||'').trim().toLowerCase()==='log events'){var card=title.closest('.card'),column=card&&card.closest('.col-md-3');(column||card||title).remove();}});}";

    private ExtentReportRuntimeOverviewCleanup() { }

    static String script() {
        return REMOVE_UNSUPPORTED_OVERVIEW_CARDS;
    }
}
