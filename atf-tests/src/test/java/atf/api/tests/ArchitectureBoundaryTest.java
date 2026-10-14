package atf.api.tests;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps renderer/test dependencies outside renderer-neutral production modules. */
class ArchitectureBoundaryTest {
    @Test
    void coreAndExecutionDoNotImportRendererOrTestFrameworks() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        assertNoForbidden(root.resolve("atf-core/src/main/java"),
                List.of("com.aventstack", "org.junit", "atf.api.execution"), violations);
        assertNoForbidden(root.resolve("atf-execution/src/main/java"),
                List.of("com.aventstack", "org.junit", "<style", "<script",
                        "atf.api.validation"), violations);
        assertNoForbidden(root.resolve("atf-validation/src/main/java"),
                List.of("com.aventstack", "org.junit", "<style", "<script",
                        "atf.api.reporting"), violations);
        assertTrue(violations.isEmpty(), () -> "Architecture boundary violations: " + violations);
    }

    private static Path locateProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("atf-core"))
                    && Files.isDirectory(current.resolve("atf-execution"))
                    && Files.isDirectory(current.resolve("atf-validation"))
                    && Files.isRegularFile(current.resolve("pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
    }

    private static void assertNoForbidden(Path sourceRoot, List<String> forbidden,
                                          List<String> violations) throws Exception {
        if (!Files.isDirectory(sourceRoot)) {
            violations.add("missing source root: " + sourceRoot);
            return;
        }
        try (var files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path);
                    forbidden.stream().filter(source::contains).forEach(token ->
                            violations.add(path + " contains " + token));
                } catch (Exception error) {
                    violations.add("cannot read " + path + ": " + error.getMessage());
                }
            });
        }
    }
}
