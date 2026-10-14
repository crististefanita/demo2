package atf.api.tests;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
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

    @Test
    void modulePomsKeepRendererNeutralDependencyDirection() throws Exception {
        Path root = locateProjectRoot();
        assertAll(
                () -> assertDependencyAbsent(root, "atf-core", "atf-validation"),
                () -> assertDependencyAbsent(root, "atf-core", "atf-execution"),
                () -> assertDependencyAbsent(root, "atf-core", "atf-reporting"),
                () -> assertDependencyAbsent(root, "atf-validation", "atf-execution"),
                () -> assertDependencyAbsent(root, "atf-validation", "atf-reporting"),
                () -> assertDependencyAbsent(root, "atf-execution", "atf-reporting"),
                () -> assertDependencyPresent(root, "atf-validation", "atf-core"),
                () -> assertDependencyPresent(root, "atf-execution", "atf-validation"),
                () -> assertDependencyPresent(root, "atf-reporting", "atf-core"),
                () -> assertDependencyPresent(root, "atf-tests", "atf-reporting"));
    }

    @Test
    void reportingProductionCodeDoesNotReachIntoExecutionOrBusinessValidation() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        assertNoForbidden(root.resolve("atf-reporting/src/main/java"),
                List.of("atf.api.execution", "atf.api.validation", "org.junit", "org.junit.jupiter"),
                violations);
        assertTrue(violations.isEmpty(), () -> "Reporting boundary violations: " + violations);
    }

    @Test
    void productionModulesDoNotDeclareMutableStaticFields() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        for (String module : List.of("atf-core", "atf-validation", "atf-execution", "atf-reporting")) {
            Path sourceRoot = root.resolve(module).resolve("src/main/java");
            if (!Files.isDirectory(sourceRoot)) continue;
            try (var files = Files.walk(sourceRoot)) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                    try {
                        int lineNumber = 0;
                        for (String line : Files.readAllLines(path)) {
                            lineNumber++;
                            String trimmed = line.trim();
                            if (trimmed.startsWith("//") || trimmed.startsWith("*")
                                    || trimmed.contains(" static final ") || trimmed.startsWith("static final ")) {
                                continue;
                            }
                            if (trimmed.matches(".*\\bstatic\\s+(volatile\\s+)?[A-Za-z_$][\\w$<>?, .\\[\\]]*\\s+[A-Za-z_$][\\w$]*\\s*(=.*)?;.*")) {
                                violations.add(path + ":" + lineNumber + " mutable static field");
                            }
                        }
                    } catch (Exception error) {
                        violations.add("cannot read " + path + ": " + error.getMessage());
                    }
                });
            }
        }
        assertTrue(violations.isEmpty(), () -> "Mutable static state found: " + violations);
    }

    private static void assertDependencyPresent(Path root, String module, String dependency)
            throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        assertTrue(pom.contains("<artifactId>" + dependency + "</artifactId>"),
                () -> module + " must depend on " + dependency);
    }

    private static void assertDependencyAbsent(Path root, String module, String dependency)
            throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        assertFalse(pom.contains("<artifactId>" + dependency + "</artifactId>"),
                () -> module + " must not depend on " + dependency);
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
