package atf.ui.tests.extent.component.layout;

import atf.api.reporting.runtime.evidence.ExtentReportRuntimeEvidenceTableLayout;
import atf.api.reporting.theme.evidence.AtfReportEvidenceTableCss;
import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component contract for content-aware assertion-table sizing. */
@AtfComponentTest
class ValidationTableLayoutComponentTest {
    @Test
    void runtimeAllocatesColumnsFromTheLongestVisibleValue() {
        String script = ExtentReportRuntimeEvidenceTableLayout.script();

        assertAll(
                () -> assertTrue(script.contains("validationCellLength")),
                () -> assertTrue(script.contains("longest[index]")),
                () -> assertTrue(script.contains("data-atf-validation-layout")),
                () -> assertTrue(script.contains("data-atf-validation-column-widths")),
                () -> assertTrue(script.contains("window.addEventListener('resize'")),
                () -> assertTrue(script.contains("normalizeValidationTableColumns()")));
    }

    @Test
    void cssLeavesAssertionColumnWidthsToTheRuntimeNormalizer() {
        String css = AtfReportEvidenceTableCss.css();

        assertAll(
                () -> assertTrue(css.contains("table.helm-validation-table{width:100%!important;max-width:100%!important;min-width:0!important;table-layout:fixed!important}")),
                () -> assertTrue(css.contains("overflow-wrap:anywhere!important;word-break:break-word!important")),
                () -> assertTrue(css.contains("min-width:0!important;max-width:none!important;white-space:nowrap!important")),
                () -> assertFalse(css.contains("width:72px!important;min-width:72px!important;max-width:72px!important")),
                () -> assertFalse(css.contains("width:62px!important;min-width:62px!important;max-width:62px!important")));
    }
}
