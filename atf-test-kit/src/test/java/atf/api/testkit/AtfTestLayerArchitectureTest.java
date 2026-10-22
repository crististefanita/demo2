package atf.api.testkit;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Enforces one executable test hierarchy across every ATF module. */
@AtfComponentTest
class AtfTestLayerArchitectureTest {
    private static final Set<String> LAYER_ANNOTATIONS = Set.of(
            "@AtfUnitTest", "@AtfComponentTest", "@AtfIntegrationTest", "@AtfE2eTest");
    private static final Pattern TEST_DECLARATION = Pattern.compile(
            "(?m)^\\s*@(Test|ParameterizedTest|TestFactory|TestTemplate|RepeatedTest|Suite|SelectPackages)\\b");
    private static final String STANDALONE_REPORTING_LAYER = "standalone-independent";
    private static final Set<String> STANDALONE_REPORTING_TEST_NAMES = Set.of(
            "ReportingStandaloneContractTest.java", "ReportingStandaloneDependencyTest.java",
            "ReportingStandaloneEvidenceLayoutTest.java",
            "ReportingStandaloneEnvironmentSnapshotTest.java");

    @Test
    void everyConcreteExecutableTestDeclaresExactlyOneLayer() throws Exception {
        Path projectRoot = locateProjectRoot();
        for (Path file : testSources(projectRoot)) {
            String source = Files.readString(file);
            if (isStandaloneReportingTest(file, projectRoot)) continue;
            if (!isConcreteExecutable(source)) continue;

            List<String> declared = declaredLayers(source);
            assertEquals(1, declared.size(),
                    () -> "Executable test must declare exactly one layer: " + file);
            assertNoForbiddenImports(file, projectRoot, source, declared.getFirst());
        }
    }

    @Test
    void supportDoesNotBecomeAHiddenExecutionLayer() throws Exception {
        Path projectRoot = locateProjectRoot();
        for (Path file : testSources(projectRoot)) {
            if (!file.toString().replace('\\', '/').contains("/support/")) continue;
            String source = Files.readString(file);
            if (!isConcreteExecutable(source)) continue;
            assertFalse(source.contains("@AtfE2eTest")
                            || source.contains("@AtfIntegrationTest"),
                    () -> "Support must not own browser/module execution: " + file);
        }
    }

    @Test
    void standaloneReportingUsesAFormalIndependentPolicy() throws Exception {
        Path projectRoot = locateProjectRoot();
        String pom = Files.readString(projectRoot.resolve("atf-reporting/pom.xml"));
        List<Path> standaloneTests = testSources(projectRoot).stream()
                .filter(file -> isStandaloneReportingTest(file, projectRoot))
                .toList();

        assertAll(
                () -> assertFalse(pom.contains("<parent>"),
                        "standalone reporting must not inherit the whole reactor"),
                () -> assertFalse(pom.matches("(?s).*<artifactId>atf-(core|mock|execution|validation|console|ui|debug-ui|test-kit)</artifactId>.*"),
                        "standalone reporting must not depend on an ATF module"),
                () -> assertEquals(STANDALONE_REPORTING_TEST_NAMES,
                        standaloneTests.stream()
                                .map(path -> path.getFileName().toString())
                                .collect(java.util.stream.Collectors.toSet()),
                        "standalone reporting tests must remain an explicit, finite exception"));

        for (Path file : standaloneTests) {
            String source = Files.readString(file);
            assertFalse(LAYER_ANNOTATIONS.stream().anyMatch(source::contains),
                    () -> "standalone reporting exception must not silently claim a reactor layer: " + file);
            assertTrue(source.contains("atf.api.reporting"),
                    () -> "standalone reporting test must exercise the public reporting API: " + file);
            assertFalse(source.lines().anyMatch(line -> line.matches(
                            "\\s*import\\s+atf\\.api\\.(core|execution|validation|mock|console|ui|debugui|testkit)\\..*")),
                    () -> "Standalone reporting test imports another ATF module: " + file);
        }
    }

    @Test
    void moduleLayerMatrixIsDerivedFromDeclaredAnnotations() throws Exception {
        Path projectRoot = locateProjectRoot();
        Map<String, Set<String>> matrix = new TreeMap<>();
        for (Path file : testSources(projectRoot)) {
            String source = Files.readString(file);
            if (!isConcreteExecutable(source)) continue;
            String relative = projectRoot.relativize(file).toString().replace('\\', '/');
            String module = relative.substring(0, relative.indexOf('/'));
            String layer = isStandaloneReportingTest(file, projectRoot)
                    ? STANDALONE_REPORTING_LAYER
                    : declaredLayers(source).getFirst();
            matrix.computeIfAbsent(module, ignored -> new TreeSet<>()).add(layer);
        }

        assertFalse(matrix.isEmpty(), "the live reactor must expose an executable test matrix");
        assertEquals(Set.of(STANDALONE_REPORTING_LAYER), matrix.get("atf-reporting"),
                () -> "atf-reporting must be represented as an independent test surface: " + matrix);
        assertTrue(matrix.values().stream().allMatch(layers -> !layers.isEmpty()),
                () -> "matrix contains an empty module layer: " + matrix);
    }

    private static boolean isConcreteExecutable(String source) {
        return TEST_DECLARATION.matcher(source).find()
                && !source.matches("(?s).*\\babstract\\s+class\\s+\\w+.*");
    }

    private static List<String> declaredLayers(String source) {
        return source.lines()
                .map(String::trim)
                .filter(LAYER_ANNOTATIONS::contains)
                .toList();
    }

    private static void assertNoForbiddenImports(Path file, Path projectRoot,
                                                  String source, String layer) {
        if ("@AtfUnitTest".equals(layer)) {
            assertFalse(source.lines().anyMatch(line -> line.matches(
                            "\\s*import\\s+com\\.microsoft\\.playwright\\..*")),
                    () -> "Unit test imports Playwright: " + file);
        }
        if ("@AtfComponentTest".equals(layer)) {
            String relative = projectRoot.relativize(file).toString().replace('\\', '/');
            boolean browserOwner = relative.startsWith("atf-ui/")
                    || relative.startsWith("atf-test-practice-shop/")
                    || relative.contains("/atf/ui/tests/extent/");
            if (!browserOwner) {
                assertFalse(source.lines().anyMatch(line -> line.matches(
                                "\\s*import\\s+com\\.microsoft\\.playwright\\..*")),
                        () -> "Non-UI component test imports Playwright: " + file);
            }
        }
    }

    private static List<Path> testSources(Path projectRoot) throws Exception {
        try (Stream<Path> files = Files.walk(projectRoot)) {
            return files.filter(AtfTestLayerArchitectureTest::isTestSource).toList();
        }
    }

    private static boolean isTestSource(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return normalized.matches(".*/src/test/java/.*\\.java$")
                && !normalized.contains("/target/");
    }

    private static boolean isStandaloneReportingTest(Path file, Path projectRoot) {
        String relative = projectRoot.relativize(file).toString().replace('\\', '/');
        return relative.startsWith("atf-reporting/src/test/java/atf/api/reporting/standalone/");
    }

    private static Path locateProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("atf-reporting"))
                    && Files.isDirectory(current.resolve("atf-core"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate ATF project root");
    }
}
