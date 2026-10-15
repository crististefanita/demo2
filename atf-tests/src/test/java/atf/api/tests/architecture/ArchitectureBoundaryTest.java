package atf.api.tests.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps renderer/test dependencies outside renderer-neutral production modules. */
class ArchitectureBoundaryTest {
    private static final int PACKAGE_COHESION_LIMIT = 8;
    private static final Map<String, String> REVIEWED_COHESION_EXCEPTIONS = Map.of(
            "atf.api.core.http", "transport primitives share one renderer-neutral boundary",
            "atf.api.core.reporting", "status, assertion and console ports share the core reporting contract",
            "atf.api.reporting", "Extent, fallback and published-artifact projections still share the reporting facade");

    @Test
    void coreAndExecutionDoNotImportRendererOrTestFrameworks() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        assertNoForbidden(root.resolve("atf-core/src/main/java"),
                List.of("com.aventstack", "org.junit", "atf.api.execution"), violations);
        assertNoForbidden(root.resolve("atf-mock/src/main/java"),
                List.of("com.aventstack", "org.junit", "atf.api.execution",
                        "atf.api.validation", "atf.api.reporting"), violations);
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
                () -> assertDependencyAbsent(root, "atf-mock", "atf-reporting"),
                () -> assertDependencyPresent(root, "atf-mock", "atf-core"),
                () -> assertDependencyAbsent(root, "atf-reporting", "atf-console"),
                () -> assertDependencyAbsent(root, "atf-validation", "atf-execution"),
                () -> assertDependencyAbsent(root, "atf-validation", "atf-reporting"),
                () -> assertDependencyAbsent(root, "atf-execution", "atf-reporting"),
                () -> assertDependencyPresent(root, "atf-validation", "atf-core"),
                () -> assertDependencyPresent(root, "atf-execution", "atf-validation"),
                () -> assertDependencyPresent(root, "atf-console", "atf-core"),
                () -> assertDependencyPresent(root, "atf-reporting", "atf-core"),
                () -> assertDependencyPresent(root, "atf-tests", "atf-reporting"));
    }

    @Test
    void reportingProductionCodeDoesNotReachIntoExecutionOrBusinessValidation() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        assertNoForbidden(root.resolve("atf-reporting/src/main/java"),
                List.of("atf.api.execution", "atf.api.validation", "atf.api.console",
                        "org.junit", "org.junit.jupiter"),
                violations);
        assertNoForbidden(root.resolve("atf-console/src/main/java"),
                List.of("atf.api.reporting", "com.aventstack"),
                violations);
        assertTrue(violations.isEmpty(), () -> "Reporting boundary violations: " + violations);
    }

    @Test
    void productionModulesDoNotDeclareMutableStaticFields() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        for (String module : List.of("atf-core", "atf-mock", "atf-validation", "atf-execution",
                "atf-console", "atf-reporting")) {
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

    @Test
    void productionPackageCohesionUsesAnExplicitReviewedExceptionList() throws Exception {
        Path root = locateProjectRoot();
        Map<String, Integer> counts = productionPackageCounts(root);
        Map<String, Integer> overLimit = new TreeMap<>();
        counts.forEach((packageName, count) -> {
            if (count > PACKAGE_COHESION_LIMIT) overLimit.put(packageName, count);
        });

        assertTrue(!counts.isEmpty(), "Production package inventory must not be empty");
        assertTrue(overLimit.keySet().stream().allMatch(REVIEWED_COHESION_EXCEPTIONS::containsKey),
                () -> "New package cohesion hotspot requires an explicit review: " + overLimit);
        assertTrue(REVIEWED_COHESION_EXCEPTIONS.keySet().stream()
                        .filter(counts::containsKey)
                        .allMatch(overLimit::containsKey),
                () -> "Reviewed package allow-list is stale: " + overLimit);
    }

    @Test
    void atfTestsKeepReusableScenariosInMainAndExecutableTestsInTest() throws Exception {
        Path root = locateProjectRoot();
        Path module = root.resolve("atf-tests");
        Path main = module.resolve("src/main/java/atf/api/tests");
        Path tests = module.resolve("src/test/java/atf/api/tests");

        assertAll(
                () -> assertTrue(Files.isRegularFile(main.resolve("api/users/UsersApiScenarios.java")),
                        "UsersApiScenarios belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(main.resolve("api/posts/PostsApiScenarios.java")),
                        "PostsApiScenarios belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(main.resolve("support/ApiAcceptancePublisher.java")),
                        "ApiAcceptancePublisher belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(tests.resolve("api/users/UsersApiTests.java")),
                        "UsersApiTests belongs to src/test/java"),
                () -> assertTrue(Files.isRegularFile(tests.resolve("api/posts/PostsApiTests.java")),
                        "PostsApiTests belongs to src/test/java"),
                () -> assertFalse(containsJavaFileNamed(tests, "ApiScenarios.java"),
                        "reusable *ApiScenarios classes must not be hidden in src/test/java"));
    }

    private static boolean containsJavaFileNamed(Path root, String suffix) throws Exception {
        if (!Files.isDirectory(root)) return false;
        try (var files = Files.walk(root)) {
            return files.anyMatch(path -> path.toString().endsWith(suffix));
        }
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

    private static Map<String, Integer> productionPackageCounts(Path root) throws Exception {
        Map<String, Integer> counts = new TreeMap<>();
        Pattern declaration = Pattern.compile("(?m)^package\\s+([^;]+);");
        for (String module : List.of("atf-core", "atf-validation", "atf-execution",
                "atf-console", "atf-reporting")) {
            Path sourceRoot = root.resolve(module).resolve("src/main/java");
            if (!Files.isDirectory(sourceRoot)) continue;
            try (var files = Files.walk(sourceRoot)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                    var match = declaration.matcher(Files.readString(file));
                    if (match.find()) {
                        counts.merge(match.group(1), 1, Integer::sum);
                    }
                }
            }
        }
        return counts;
    }
}
