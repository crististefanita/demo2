package atf.api.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Ratchet tests for the single source of truth behind the report palette. */
class AtfReportColorsTest {
    @Test
    void exposesTheReferencePaletteWithoutSemanticDrift() {
        assertAll(
                () -> assertEquals("#101723", AtfReportColors.DARK),
                () -> assertEquals("#29364d", AtfReportColors.BORDER),
                () -> assertEquals("#e5ecf7", AtfReportColors.TEXT),
                () -> assertEquals("#91a3bf", AtfReportColors.MUTED),
                () -> assertEquals("#7aa2ff", AtfReportColors.ACCENT),
                () -> assertEquals("#56b6c2", AtfReportColors.SETUP),
                () -> assertEquals("#35c759", AtfReportColors.GOOD),
                () -> assertEquals("#ffc107", AtfReportColors.WARNING),
                () -> assertEquals("#ff6b81", AtfReportColors.DANGER),
                () -> assertEquals("#04a1f4", AtfReportColors.INFO),
                () -> assertEquals("#070a0e", AtfReportColors.JSON_SURFACE),
                () -> assertEquals("#e6edf3", AtfReportColors.JSON_TEXT),
                () -> assertEquals("#d7e2f4", AtfReportColors.CODE_TEXT),
                () -> assertEquals("#ddd", AtfReportColors.CARD_TEXT),
                () -> assertEquals("#f0b3b3", AtfReportColors.FAILURE_CAUSE_TEXT),
                () -> assertEquals("#9ccc65", AtfReportColors.EXTENT_PASS),
                () -> assertEquals("#ff9900", AtfReportColors.EXTENT_SKIP),
                () -> assertEquals("#e57373", AtfReportColors.EXTENT_FAIL),
                () -> assertEquals(AtfReportColors.INFO, AtfReportColors.OVERVIEW_INFO),
                () -> assertEquals("rgba(122,162,255,.045)", AtfReportColors.ACCENT_045),
                () -> assertEquals("rgba(129,146,173,.98)", AtfReportColors.ANNOTATION_MUTED),
                () -> assertEquals("rgba(0,0,0,.18)", AtfReportColors.SHADOW_SOFT));
    }

    @Test
    void keepsBadgeForegroundsExplicitAndReadable() {
        assertAll(
                () -> assertEquals("#fff", AtfReportColors.PASS_TEXT),
                () -> assertEquals("#fff", AtfReportColors.FAIL_TEXT),
                () -> assertEquals("#f6f7f6", AtfReportColors.WARNING_TEXT),
                () -> assertEquals("#2b2100", AtfReportColors.SKIP_TEXT),
                () -> assertEquals("#002238", AtfReportColors.INFO_TEXT));
    }
}
