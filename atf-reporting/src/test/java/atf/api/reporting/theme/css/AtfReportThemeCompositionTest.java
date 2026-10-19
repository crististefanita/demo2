package atf.api.reporting.theme.css;
import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.theme.AtfReportTheme;
import atf.api.reporting.theme.css.AtfReportThemeBaseCss;
import atf.api.reporting.theme.css.AtfReportThemeDetailCss;
import atf.api.reporting.theme.css.AtfReportThemeNavigationCss;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the semantic seam between the theme composition root and base CSS. */
@AtfUnitTest
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
        String source = Files.readString(sourceFile("atf/api/reporting/theme/AtfReportTheme.java"));

        assertAll(
                () -> assertTrue(source.contains("AtfReportThemeNavigationCss.css()")),
                () -> assertTrue(source.contains("AtfReportThemeDetailCss.css()")),
                () -> assertFalse(source.contains(".category-view")),
                () -> assertFalse(source.contains(".atf-secondary-metadata")),
                () -> assertFalse(source.contains(".exception-view.atf-fails-view")));
    }

    @Test
    void semanticExtractionPreservesTheCssCompositionContract() {
        String css = AtfReportTheme.css();

        assertAll(
                () -> assertTrue(css.length() > 80_000),
                () -> assertTrue(css.contains(".atf-ui-action-table")),
                () -> assertTrue(css.contains(".atf-ui-action-operation")),
                () -> assertTrue(css.contains(".atf-ui-action-screenshot-link")),
                () -> assertFalse(css.contains("rgb(")),
                () -> assertFalse(css.contains("rgba(")));
    }

    private static Path sourceFile(String relativePath) {
        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            Path moduleSource = current.resolve("src/main/java").resolve(relativePath);
            if (Files.isRegularFile(moduleSource)) return moduleSource;
            Path reactorSource = current.resolve("atf-reporting/src/main/java").resolve(relativePath);
            if (Files.isRegularFile(reactorSource)) return reactorSource;
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate reporting source: " + relativePath);
    }
}
