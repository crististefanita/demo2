package atf.api.reporting;

/** Immutable typography vocabulary shared by all report renderers. */
final class AtfReportTypography {
    static final String SANS_FONT_FAMILY = "'Segoe UI','Inter','Helvetica Neue',Arial,sans-serif";
    static final String CODE_FONT_FAMILY = "ui-monospace,Consolas,monospace";
    static final String BASE_FONT_SIZE = "14px";
    static final String OVERVIEW_FONT_SIZE = "12.6px";
    static final String JSON_FONT_SIZE = "12.5px";
    // The reference report sets the document root to 14px.  Keep the
    // resolved values explicit in the self-contained Java artifact so rem
    // defaults from the embedded Bootstrap bundle cannot make Tests larger.
    static final String TABLE_HEADER_FONT_SIZE = "11.2px";
    static final String TABLE_CELL_FONT_SIZE = "12.6px";
    static final String BADGE_FONT_SIZE = "9.52px";

    private AtfReportTypography() {
    }
}
