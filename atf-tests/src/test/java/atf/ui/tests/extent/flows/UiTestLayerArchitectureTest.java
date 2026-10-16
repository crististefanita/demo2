package atf.ui.tests.extent.flows;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guardrail for the UI test layer: selector knowledge belongs to locators and
 * semantic flows, never to a JUnit intent class.
 */
class UiTestLayerArchitectureTest {
    @Test
    void uiTestsDoNotContainDirectPlaywrightLocators() throws Exception {
        Path root = Path.of("src", "test", "java", "atf", "ui", "tests");
        List<String> forbidden = List.of(
                "import com.microsoft.playwright.Locator",
                "com.microsoft.playwright.Locator",
                ".locator(", "getByRole(", "getByText(", "page.locator(");
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("\\locators\\"))
                    .filter(path -> !path.getFileName().toString().endsWith("ArchitectureTest.java"))
                    .filter(path -> !path.getFileName().toString().endsWith("ArchitectureGuardTest.java"))
                    .filter(path -> !path.getFileName().toString().equals("ReportUiLocatorsTest.java"))
                    .toList()) {
                String text = Files.readString(file);
                for (String token : forbidden) {
                    assertTrue(!text.contains(token),
                            () -> "UI test layer contains direct locator token '" + token + "': " + file);
                }
            }
        }
    }
}
