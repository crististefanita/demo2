package atf.ui.tests.extent.section;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps annotation-driven composition roots free of browser implementation details. */
class SectionAuditArchitectureGuardTest {
    @Test
    void generatedAnnotationTestDependsOnlyOnCompositionApi() throws Exception {
        Path source = Path.of("src/test/java/atf/ui/tests/extent/section/GeneratedTestsSectionReportUiTest.java");
        assertTrue(Files.isRegularFile(source), "generated section test source must be present");
        String text = Files.readString(source);
        assertFalse(text.contains("ReportUiLocators"));
        assertFalse(text.contains("com.microsoft.playwright.Page"));
        assertFalse(text.contains("com.microsoft.playwright.Locator"));
        assertFalse(text.contains(".locator("));
        assertTrue(text.contains("@UiExtentSectionAudit"));
        assertTrue(text.contains("SectionAuditDynamicEngine"));
    }

    @Test
    void sectionModulesReturnRendererNeutralResultsAndDoNotAssertWithJUnit() throws Exception {
        Path root = Path.of("src/main/java/atf/ui/tests/extent/section/module");
        try (var files = Files.list(root)) {
            files.filter(path -> path.getFileName().toString().endsWith("SectionAuditModule.java"))
                    .forEach(path -> {
                        try {
                            String text = Files.readString(path);
                            assertTrue(text.contains("SectionAuditResult audit("), path.toString());
                            assertFalse(text.contains("org.junit.jupiter.api.Assertions"), path.toString());
                            assertFalse(text.contains("assertTrue("), path.toString());
                        } catch (Exception error) {
                            throw new AssertionError(error);
                        }
                    });
        }
    }
}
