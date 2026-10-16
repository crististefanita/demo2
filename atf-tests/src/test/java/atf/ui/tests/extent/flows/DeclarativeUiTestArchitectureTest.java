package atf.ui.tests.extent.flows;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps annotation-driven test classes declarative. Browser mechanics belong
 * to production flows/modules and selector knowledge belongs to locators.
 */
class DeclarativeUiTestArchitectureTest {
    @Test
    void declarativeTestsDoNotOwnPlaywrightOrLocatorTypes() throws Exception {
        Path testRoot = Path.of(DeclarativeUiTestArchitectureTest.class
                .getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("../../src/test/java/atf/ui/tests/extent").normalize();
        List<String> forbidden = List.of(
                "import com.microsoft.playwright.Page",
                "import com.microsoft.playwright.Locator",
                "com.microsoft.playwright.Page",
                "com.microsoft.playwright.Locator",
                "ReportUiLocators",
                ".locator(", "getByRole(", "getByText(", "getByLabel(");
        try (Stream<Path> files = Files.walk(testRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().endsWith("ArchitectureTest.java"))
                    .filter(path -> !path.getFileName().toString().endsWith("ArchitectureGuardTest.java"))
                    .filter(this::isDeclarativeSource)
                    .toList()) {
                String text = Files.readString(file);
                for (String token : forbidden) {
                    assertTrue(!text.contains(token),
                            () -> "Declarative UI test contains forbidden token '"
                                    + token + "': " + file);
                }
            }
        }
    }

    private boolean isDeclarativeSource(Path file) {
        try {
            String text = Files.readString(file);
            String normalized = file.toString().replace('\\', '/');
            boolean declarativePackage = normalized.contains("/configurable/")
                    || normalized.contains("/section/");
            return (declarativePackage && (text.contains("@ApiScenarioSetup")
                    || text.contains("@UiExtentAudit")
                    || text.contains("@UiExtentSectionAudit")))
                    || file.getFileName().toString().equals("AbstractApiReportUiContractTest.java");
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot inspect declarative source " + file, failure);
        }
    }

}
