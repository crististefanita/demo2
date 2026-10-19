package atf.api.tests.governance;

import atf.api.core.config.module.AtfModuleIds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shared filesystem and Maven assertions for the whole-ATF governance suite. */
final class AtfGovernanceTestSupport {
    private static final String JAVA_SUFFIX = ".java";
    private AtfGovernanceTestSupport() {
    }

    static Path locateProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve(AtfModuleIds.CORE))
                    && Files.isDirectory(current.resolve(AtfModuleIds.EXECUTION))
                    && Files.isDirectory(current.resolve(AtfModuleIds.VALIDATION))
                    && Files.isRegularFile(current.resolve("pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
    }

    static Path moduleSource(Path root, String module, boolean tests) {
        return sourceRoots(root, module, tests).getFirst();
    }

    /** Resolves both conventional Maven roots and the legacy atf-tests layout. */
    static List<Path> sourceRoots(Path root, String module, boolean tests) {
        String sourceSet = tests ? "src/test" : "src/main";
        Path sourceSetRoot = root.resolve(module).resolve(sourceSet);
        Path conventional = sourceSetRoot.resolve("java");
        return Files.isDirectory(conventional) ? List.of(conventional) : List.of(sourceSetRoot);
    }

    static void assertDependencyPresent(Path root, String module, String dependency)
            throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        assertTrue(pom.contains("<artifactId>" + dependency + "</artifactId>"),
                () -> module + " must depend on " + dependency);
    }

    static void assertDependencyAbsent(Path root, String module, String dependency)
            throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        assertFalse(pom.contains("<artifactId>" + dependency + "</artifactId>"),
                () -> module + " must not depend on " + dependency);
    }

    static void assertDependencyScope(Path root, String module, String dependency, String scope)
            throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        String marker = "<artifactId>" + dependency + "</artifactId>";
        int dependencyStart = pom.indexOf(marker);
        assertTrue(dependencyStart >= 0,
                () -> module + " must declare dependency " + dependency);
        int dependencyEnd = pom.indexOf("</dependency>", dependencyStart);
        assertTrue(dependencyEnd >= 0,
                () -> module + " has an unterminated dependency block for " + dependency);
        String block = pom.substring(dependencyStart, dependencyEnd);
        assertTrue(block.contains("<scope>" + scope + "</scope>"),
                () -> module + " -> " + dependency + " must use " + scope + " scope");
    }

    static void assertNoForbidden(Path sourceRoot, List<String> forbidden,
                                  List<String> violations) throws Exception {
        if (!Files.isDirectory(sourceRoot)) {
            violations.add("missing source root: " + sourceRoot);
            return;
        }
        try (var files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(JAVA_SUFFIX)).forEach(path -> {
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

    static void assertNoForbiddenReportingCore(Path sourceRoot, List<String> forbidden,
                                                List<String> violations) throws Exception {
        if (!Files.isDirectory(sourceRoot)) {
            violations.add("missing source root: " + sourceRoot);
            return;
        }
        Path acceptanceAdapter = sourceRoot.resolve("atf/api/reporting/api/acceptance");
        try (var files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(JAVA_SUFFIX))
                    .filter(path -> !path.startsWith(acceptanceAdapter))
                    .forEach(path -> {
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
