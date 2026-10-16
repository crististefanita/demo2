package atf.ui.tests.extent.section;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prevents the retired DOM-inspector/validator seam from being silently restored. */
class DomInspectorMigrationBoundaryTest {
    @Test
    void migratedExtractorsAndPureValidatorsAreTheActiveBoundary() {
        Path root = projectRoot();
        Path main = root.resolve("atf-tests/src/main/java/atf/ui/tests/extent");
        assertTrue(Files.isRegularFile(main.resolve("section/extract/EvidenceSectionExtractor.java")));
        assertTrue(Files.isRegularFile(main.resolve("section/extract/ApiReportStructureExtractor.java")));
        assertTrue(Files.isRegularFile(main.resolve("section/extract/FilterSectionExtractor.java")));
        assertTrue(Files.isRegularFile(main.resolve("section/validate/JsonEvidenceValidator.java")));
        assertTrue(Files.isRegularFile(main.resolve("section/validate/VisualEvidenceValidator.java")));
        assertFalse(Files.exists(main.resolve("flows/inspection/ApiReportRequestEvidenceInspector.java")));
        assertFalse(Files.exists(main.resolve("flows/inspection/ApiReportStructureInspector.java")));
        assertFalse(Files.exists(main.resolve("flows/inspection/ApiReportFilterInspector.java")));
        assertFalse(Files.exists(main.resolve("audit/CategoryScreenValidator.java")));
        assertFalse(Files.exists(main.resolve("audit/EvidenceScreenValidator.java")));
    }

    private static Path projectRoot() {
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("atf-tests"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("ATF project root not found");
    }
}
