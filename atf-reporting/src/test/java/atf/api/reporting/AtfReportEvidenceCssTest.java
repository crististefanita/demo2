package atf.api.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the evidence CSS provider remains renderer-neutral and complete. */
class AtfReportEvidenceCssTest {

    @Test
    void ownsJsonTraceAndDisclosureRules() {
        String css = AtfReportEvidenceCss.css();

        assertAll(
                () -> assertTrue(css.contains("pre.code-block")),
                () -> assertTrue(css.contains(".helm-evidence-table")),
                () -> assertTrue(css.contains(".helm-evidence-table.helm-request-table,.helm-evidence-table.helm-trace-table,.helm-evidence-table.execution-table{table-layout:fixed!important}")),
                () -> assertTrue(css.contains("width:var(--atf-evidence-key-column-width)!important")),
                () -> assertTrue(css.contains(".step-detail>summary")),
                () -> assertTrue(css.contains(".atf-disclosure-glyph:before")),
                () -> assertFalse(css.contains("<style")));
    }

    @Test
    void themeComposesEvidenceProviderWithoutDroppingContract() {
        String theme = AtfReportTheme.css();

        assertAll(
                () -> assertTrue(theme.contains("pre.code-block")),
                () -> assertTrue(theme.contains(".helm-evidence-table")),
                () -> assertTrue(theme.contains(".step-detail>summary")));
    }
}
