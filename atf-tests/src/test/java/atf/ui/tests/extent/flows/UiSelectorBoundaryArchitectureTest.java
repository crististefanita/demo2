package atf.ui.tests.extent.flows;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ensures flow/auditor classes consume the locator facade instead of composing selectors. */
class UiSelectorBoundaryArchitectureTest {
    @Test
    void flowAndAuditorLayersDoNotComposePlaywrightSelectors() throws Exception {
        Path root = Path.of(UiSelectorBoundaryArchitectureTest.class
                .getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("../../src/main/java/atf/ui/tests").normalize();
        List<String> forbidden = List.of(
                "page.locator(", "categoryPage.locator(", "failsPage.locator(",
                "locator(\"", "getByRole(", "getByText(", "getByLabel(");
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("\\flows\\")
                            || path.toString().contains("\\audit\\"))
                    .toList()) {
                String text = Files.readString(file);
                for (String token : forbidden) {
                    assertTrue(!text.contains(token),
                            () -> "Selector token '" + token + "' escaped the locator boundary: " + file);
                }
            }
        }
    }
}
