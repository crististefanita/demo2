package atf.api.tests.governance;

import atf.api.core.config.module.AtfModuleIds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shared filesystem and Maven assertions for the whole-ATF governance suite. */
public final class AtfGovernanceTestSupport {
    private static final String JAVA_SUFFIX = ".java";
    static final int PACKAGE_COHESION_LIMIT = 8;
    static final List<String> PRODUCTION_MODULES = AtfModuleIds.SOURCE_GOVERNED_MODULES;
    /** Reviewed exceptions are explicit seams, never a blanket package-size waiver. */
    static final Map<String, String> REVIEWED_COHESION_EXCEPTIONS = Map.of(
            "atf.api.core.http.response",
            "core HTTP response vocabulary and typed operation descriptors share one response boundary",
            "atf.api.reporting.api",
            "standalone reporting contracts, evidence adapters and JSON policy share the public reporting boundary",
            "atf.api.reporting.runtime.core",
            "runtime core fragments share the single generated-script publication boundary",
            "atf.api.reporting.runtime.section",
            "Extent runtime section facades and their typed JS fragments share one publication boundary",
            "atf.api.reporting.evidence",
            "typed evidence table normalization and HTML evidence rendering share one stable reporting boundary",
            "atf.api.reporting.html",
            "HTML shell, semantic snapshot, overview, tests and fails views share one artifact projection boundary");
    /** The whole-reactor suite is intentionally kept together as one readable gate. */
    static final Map<String, String> REVIEWED_TEST_COHESION_EXCEPTIONS = Map.of();
    private AtfGovernanceTestSupport() {
    }

    public static Path locateProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve(AtfModuleIds.CORE))
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
            for (Path file : files.filter(path -> path.toString().endsWith(JAVA_SUFFIX)).toList()) {
                String source = Files.readString(file);
                assertFalse(source.contains("package " + forbiddenPrefix + ".")
                                || source.contains("import " + forbiddenPrefix + "."),
                        () -> file + " contains forbidden Java namespace " + forbiddenPrefix);
            }
        }
    }

    static Map<String, Integer> productionPackageCounts(Path root) throws Exception {
        Map<String, Integer> counts = new TreeMap<>();
        Pattern declaration = Pattern.compile("(?m)^package\\s+([^;]+);");
        for (String module : AtfModuleIds.SOURCE_GOVERNED_MODULES) {
            Path sourceRoot = root.resolve(module).resolve(
                    module.equals(AtfModuleIds.TESTS) ? "src/main" : "src/main/java");
            mergePackageCounts(sourceRoot, declaration, counts);
        }
        return counts;
    }

    static Map<String, Integer> testPackageCounts(Path sourceRoot) throws Exception {
        Map<String, Integer> counts = new TreeMap<>();
        mergePackageCounts(sourceRoot, Pattern.compile("(?m)^package\\s+([^;]+);"), counts);
        return counts;
    }

    private static void mergePackageCounts(Path sourceRoot, Pattern declaration,
                                           Map<String, Integer> counts) throws Exception {
        if (!Files.isDirectory(sourceRoot)) return;
        try (var files = Files.walk(sourceRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(JAVA_SUFFIX)).toList()) {
                var match = declaration.matcher(Files.readString(file));
                if (match.find()) counts.merge(match.group(1), 1, Integer::sum);
            }
        }
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

    static void assertProductionDependencyAbsent(Path root, String module, String dependency)
            throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        String marker = "<artifactId>" + dependency + "</artifactId>";
        int start = pom.indexOf(marker);
        if (start < 0) return;
        int end = pom.indexOf("</dependency>", start);
        assertTrue(end >= 0, () -> module + " has an unterminated dependency block for " + dependency);
        String block = pom.substring(start, end);
        assertTrue(block.contains("<scope>test</scope>"),
                () -> module + " must not have a production dependency on " + dependency);
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

    static void assertAllAtfDependenciesAreTestScoped(Path root, String module)
            throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        var violations = new java.util.ArrayList<String>();
        int cursor = 0;
        while ((cursor = pom.indexOf("<dependency>", cursor)) >= 0) {
            int end = pom.indexOf("</dependency>", cursor);
            if (end < 0) break;
            String block = pom.substring(cursor, end);
            if (block.contains("<groupId>atf.api</groupId>")) {
                String artifact = extractXmlValue(block, "artifactId");
                if (artifact != null && !block.contains("<scope>test</scope>")) {
                    violations.add(artifact + " is not test-scoped");
                }
            }
            cursor = end + "</dependency>".length();
        }
        assertTrue(violations.isEmpty(), () -> module
                + " must keep every ATF dependency test-only: " + violations);
    }

    static void assertNoAtfDependencies(Path root, String module) throws Exception {
        String pom = Files.readString(root.resolve(module).resolve("pom.xml"));
        var violations = new java.util.ArrayList<String>();
        int cursor = 0;
        while ((cursor = pom.indexOf("<dependency>", cursor)) >= 0) {
            int end = pom.indexOf("</dependency>", cursor);
            if (end < 0) break;
            String block = pom.substring(cursor, end);
            if (block.contains("<groupId>atf.api</groupId>")) {
                String artifact = extractXmlValue(block, "artifactId");
                violations.add(artifact == null ? "unknown ATF dependency" : artifact);
            }
            cursor = end + "</dependency>".length();
        }
        assertTrue(violations.isEmpty(), () -> module
                + " must not declare ATF module dependencies: " + violations);
    }

    private static String extractXmlValue(String block, String element) {
        String open = "<" + element + ">";
        String close = "</" + element + ">";
        int start = block.indexOf(open);
        if (start < 0) return null;
        start += open.length();
        int end = block.indexOf(close, start);
        return end < 0 ? null : block.substring(start, end).trim();
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
