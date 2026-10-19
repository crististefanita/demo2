package atf.api.tests.architecture;

import atf.api.core.config.module.AtfModuleIds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArchitectureTestSupport {
    static final int PACKAGE_COHESION_LIMIT = 8;
    static final List<String> PRODUCTION_MODULES = AtfModuleIds.SOURCE_GOVERNED_MODULES;
    /** Reviewed exceptions are explicit seams, never a blanket package-size waiver. */
    static final Map<String, String> REVIEWED_COHESION_EXCEPTIONS = Map.of(
            "atf.api.reporting.runtime.core",
            "runtime core fragments share the single generated-script publication boundary",
            "atf.api.reporting.runtime.section",
            "Extent runtime section facades and their typed JS fragments share one publication boundary");

    private ArchitectureTestSupport() {
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

    static boolean containsJavaFileNamed(Path root, String suffix) throws Exception {
        if (!Files.isDirectory(root)) return false;
        try (var files = Files.walk(root)) {
            return files.anyMatch(path -> path.toString().endsWith(suffix));
        }
    }

    static void assertNoPackagePrefix(Path sourceRoot, String forbiddenPrefix)
            throws Exception {
        if (!Files.isDirectory(sourceRoot)) return;
        try (var files = Files.walk(sourceRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                assertFalse(source.contains("package " + forbiddenPrefix + ".")
                                || source.contains("import " + forbiddenPrefix + "."),
                        () -> file + " contains forbidden Java namespace " + forbiddenPrefix);
            }
        }
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

    static void assertNoForbidden(Path sourceRoot, List<String> forbidden,
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

    static void assertNoForbiddenReportingCore(Path sourceRoot, List<String> forbidden,
                                                List<String> violations) throws Exception {
        if (!Files.isDirectory(sourceRoot)) {
            violations.add("missing source root: " + sourceRoot);
            return;
        }
        Path acceptanceAdapter = sourceRoot.resolve("atf/api/reporting/api/acceptance");
        try (var files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
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

    static Map<String, Integer> productionPackageCounts(Path root) throws Exception {
        Map<String, Integer> counts = new TreeMap<>();
        Pattern declaration = Pattern.compile("(?m)^package\\s+([^;]+);");
        for (String module : PRODUCTION_MODULES) {
            Path sourceRoot = root.resolve(module).resolve(
                    module.equals(AtfModuleIds.TESTS) ? "src/main" : "src/main/java");
            if (!Files.isDirectory(sourceRoot)) continue;
            try (var files = Files.walk(sourceRoot)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                    var match = declaration.matcher(Files.readString(file));
                    if (match.find()) counts.merge(match.group(1), 1, Integer::sum);
                }
            }
        }
        return counts;
    }

    static Map<String, Integer> testPackageCounts(Path sourceRoot) throws Exception {
        Map<String, Integer> counts = new TreeMap<>();
        Pattern declaration = Pattern.compile("(?m)^package\\s+([^;]+);");
        if (!Files.isDirectory(sourceRoot)) return counts;
        try (var files = Files.walk(sourceRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                var match = declaration.matcher(Files.readString(file));
                if (match.find()) counts.merge(match.group(1), 1, Integer::sum);
            }
        }
        return counts;
    }
}
