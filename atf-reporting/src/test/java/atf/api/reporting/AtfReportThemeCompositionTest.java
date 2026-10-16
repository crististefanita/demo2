package atf.api.reporting;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the semantic seam between the theme composition root and base CSS. */
class AtfReportThemeCompositionTest {
    @Test
    void baseCssOwnsSharedSurfaceAndTestStructureWithoutRendererMarkup() {
        String base = AtfReportThemeBaseCss.css();

        assertAll(
                () -> assertTrue(base.contains(".test-view .test-content-detail")),
                () -> assertTrue(base.contains("data-atf-scenario-id")),
                () -> assertTrue(base.contains("--atf-badge-height")),
                () -> assertTrue(base.contains(".atf-scenario-annotations")),
                () -> assertFalse(base.contains("<style")),
                () -> assertFalse(base.contains("<script")));
    }

    @Test
    void compositionKeepsBaseEvidenceAndProjectionLayersTogether() {
        String theme = AtfReportTheme.css();

        assertAll(
                () -> assertTrue(theme.indexOf("html,body,body.dark") < theme.indexOf("pre.code-block")),
                () -> assertTrue(theme.contains(".atf-ui-action-sequence")),
                () -> assertTrue(theme.contains(".category-view")),
                () -> assertTrue(theme.contains(".atf-secondary-overview")),
                () -> assertTrue(theme.contains(".atf-fails-view")));
    }

    @Test
    void compositionRootContainsOrderOnlyAndSemanticLayersOwnCssBlocks() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/atf/api/reporting/AtfReportTheme.java"));

        assertAll(
                () -> assertTrue(source.contains("AtfReportThemeNavigationCss.css()")),
                () -> assertTrue(source.contains("AtfReportThemeDetailCss.css()")),
                () -> assertFalse(source.contains(".category-view")),
                () -> assertFalse(source.contains(".atf-secondary-metadata")),
                () -> assertFalse(source.contains(".exception-view.atf-fails-view")));
    }

    @Test
    void semanticExtractionPreservesTheGoldenCssComposition() throws Exception {
        String css = AtfReportTheme.css();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(css.getBytes(StandardCharsets.UTF_8)));

        assertAll(
                () -> assertEquals(84622, css.length()),
                () -> assertEquals("a771f58bb5f32e4d3b544db98099a42992e2686becd1b7a84d61ce26710a2e35", hash));
    }
}
