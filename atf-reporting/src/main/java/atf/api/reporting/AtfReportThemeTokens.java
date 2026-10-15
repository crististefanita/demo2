package atf.api.reporting;

/**
 * Immutable visual vocabulary shared by the Extent and fallback renderers.
 *
 * <p>This class owns only reusable CSS tokens.  It deliberately contains no
 * selectors, layout rules or renderer decisions; those remain in the theme
 * facade and can be split independently without changing the public report
 * contract.</p>
 */
final class AtfReportThemeTokens {
    static final String FONT_STACK = AtfReportTypography.SANS_FONT_FAMILY;
    /** Shared Pass/Warning/Fail geometry used by every report tab. */
    static final String BADGE_HEIGHT = "18.2px";
    static final String BADGE_PADDING = "0 5px";
    static final String BADGE_RADIUS = "4px";
    static final String BADGE_FONT_SIZE = "9.52px";
    static final String BADGE_LINE_HEIGHT = "18.2px";
    /** Meta/duration badges remain slightly taller, as in the report model. */
    static final String META_BADGE_HEIGHT = "22.4px";
    static final String META_BADGE_PADDING = "5px 7px";
    static final String META_BADGE_RADIUS = "4px";
    static final String META_BADGE_FONT_SIZE = "9.52px";
    static final String META_BADGE_LINE_HEIGHT = "10.6624px";
    /** Key/value evidence tables reserve one common column plus a small text delta. */
    static final String EVIDENCE_KEY_COLUMN_WIDTH = "28%";

    private AtfReportThemeTokens() {
    }

    static String cssVariables() {
        return ":root{--atf-dark:" + AtfReportColors.DARK + ";--atf-bg:" + AtfReportColors.DARK
                + ";--atf-surface:" + AtfReportColors.DARK + ";--atf-panel:" + AtfReportColors.DARK + ";--atf-panel-2:" + AtfReportColors.DARK
                + ";--atf-code:" + AtfReportColors.JSON_SURFACE + ";--atf-json-text:" + AtfReportColors.JSON_TEXT
                + ";--atf-code-text:" + AtfReportColors.CODE_TEXT + ";--atf-table-text:" + AtfReportColors.TABLE_TEXT
                + ";--atf-table-label:" + AtfReportColors.TABLE_LABEL + ";--atf-border:" + AtfReportColors.BORDER
                + ";--atf-card-border:" + AtfReportColors.REFERENCE_BORDER + ";--atf-text:" + AtfReportColors.TEXT
                + ";--atf-card-text:" + AtfReportColors.CARD_TEXT + ";--atf-muted:" + AtfReportColors.MUTED
                + ";--atf-overview-muted:" + AtfReportColors.OVERVIEW_MUTED + ";--atf-annotation-muted:" + AtfReportColors.ANNOTATION_MUTED
                + ";--atf-accent:" + AtfReportColors.ACCENT
                + ";--atf-accent-contrast:" + AtfReportColors.ACCENT_CONTRAST
                + ";--atf-pass:" + AtfReportColors.EXTENT_PASS + ";--atf-pass-text:" + AtfReportColors.PASS_TEXT
                + ";--atf-good:" + AtfReportColors.GOOD
                + ";--atf-fail:" + AtfReportColors.EXTENT_FAIL + ";--atf-fail-text:" + AtfReportColors.FAIL_TEXT
                + ";--atf-bad:" + AtfReportColors.DANGER
                + ";--atf-warning:" + AtfReportColors.WARNING + ";--atf-warn:" + AtfReportColors.WARNING
                + ";--atf-info:" + AtfReportColors.INFO + ";--atf-info-text:" + AtfReportColors.INFO_TEXT
                + ";--atf-warning-text:" + AtfReportColors.WARNING_TEXT + ";--atf-skip:" + AtfReportColors.EXTENT_SKIP
                + ";--atf-skip-text:" + AtfReportColors.SKIP_TEXT + ";--atf-accent08:" + AtfReportColors.ACCENT_08
                + ";--atf-accent12:" + AtfReportColors.ACCENT_12 + ";--atf-accent14:" + AtfReportColors.ACCENT_14
                + ";--atf-secondary-heading-bg:" + AtfReportColors.ACCENT_07
                + ";--atf-secondary-heading-bg-hover:" + AtfReportColors.ACCENT_12 + ";--atf-secondary-heading-border:" + AtfReportColors.ACCENT_42
                + ";--atf-font-sans:" + AtfReportTypography.SANS_FONT_FAMILY + ";--atf-font-base-size:" + AtfReportTypography.BASE_FONT_SIZE
                + ";--atf-font-overview-size:" + AtfReportTypography.OVERVIEW_FONT_SIZE + ";--atf-font-json-size:" + AtfReportTypography.JSON_FONT_SIZE
                + ";--atf-font-table-header-size:" + AtfReportTypography.TABLE_HEADER_FONT_SIZE + ";--atf-font-table-cell-size:" + AtfReportTypography.TABLE_CELL_FONT_SIZE
                + ";--atf-font-badge-size:" + AtfReportTypography.BADGE_FONT_SIZE
                + ";--atf-badge-height:" + BADGE_HEIGHT
                + ";--atf-badge-padding:" + BADGE_PADDING
                + ";--atf-badge-radius:" + BADGE_RADIUS
                + ";--atf-badge-font-size:" + BADGE_FONT_SIZE
                + ";--atf-badge-line-height:" + BADGE_LINE_HEIGHT
                + ";--atf-meta-badge-height:" + META_BADGE_HEIGHT
                + ";--atf-meta-badge-padding:" + META_BADGE_PADDING
                + ";--atf-meta-badge-radius:" + META_BADGE_RADIUS
                + ";--atf-meta-badge-font-size:" + META_BADGE_FONT_SIZE
                + ";--atf-meta-badge-line-height:" + META_BADGE_LINE_HEIGHT
                + ";--atf-evidence-key-column-width:" + EVIDENCE_KEY_COLUMN_WIDTH
                + ";--atf-overview-font-family:" + FONT_STACK + ";--atf-overview-font-size:" + AtfReportTypography.OVERVIEW_FONT_SIZE
                + ";--atf-audit-scroll-height:340px}";
    }
}
