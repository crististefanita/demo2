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
    private static final List<String> PRODUCTION_MODULES = List.of(
            "atf-core", "atf-mock", "atf-validation", "atf-execution",
            "atf-console", "atf-reporting", "atf-ui", "atf-debug-ui", "atf-tests");
    private static final Map<String, String> REVIEWED_COHESION_EXCEPTIONS = Map.of(
            "atf.api.core.http", "transport primitives share one renderer-neutral boundary",
            "atf.api.core.reporting", "status, assertion and console ports share the core reporting contract",
            "atf.api.reporting", "Extent, fallback and published-artifact projections still share the reporting facade",
            "atf.api.reporting.artifact", "artifact identity, structure, evidence, navigation and theme contracts form one artifact-audit boundary",
            "atf.api.tests.support", "typed API setup, selection and acceptance publication form one API test composition boundary",
            "atf.ui.tests.extent.flows", "navigation, disclosure and report-flow adapters share one UI traversal boundary",
            "atf.ui.tests.extent.audit", "reusable Overview, Categories, Fails, Evidence and API contract validators form one UI audit boundary",
            "atf.ui.tests.extent.flows.inspection", "flow-specific DOM inspectors share one renderer audit boundary",
            "atf.ui.tests.extent.support", "UI audit lifecycle, input and closed report vocabulary form one semantic support boundary");

    @Test
    void productionModulesDoNotImportForbiddenLayersOrMarkup() throws Exception {
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
        assertNoForbidden(root.resolve("atf-ui/src/main/java"),
                List.of("atf.api.execution", "atf.api.validation", "atf.api.reporting",
                        "atf.api.mock", "org.junit", "<style", "<script"), violations);
        assertNoForbidden(root.resolve("atf-debug-ui/src/main/java"),
                List.of("atf.api.execution", "atf.api.validation", "atf.api.reporting",
                        "atf.api.mock", "org.junit", "<style", "<script"), violations);
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
                () -> assertDependencyPresent(root, "atf-ui", "atf-core"),
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
        for (String module : PRODUCTION_MODULES) {
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
        Path uiSupport = module.resolve("src/main/java/atf/ui/tests/extent/support");
        Path uiAudit = module.resolve("src/main/java/atf/ui/tests/extent/audit");
        Path tests = module.resolve("src/test/java/atf/api/tests");
        Path junitSupport = tests.resolve("support/junit");

        assertAll(
                () -> assertTrue(Files.isRegularFile(main.resolve("api/users/UsersApiScenarios.java")),
                        "UsersApiScenarios belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(main.resolve("api/posts/PostsApiScenarios.java")),
                        "PostsApiScenarios belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(main.resolve("support/ApiAcceptancePublisher.java")),
                        "ApiAcceptancePublisher belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(junitSupport.resolve("ApiResourceTestBase.java")),
                        "JUnit fixtures belong to the explicit support.junit namespace"),
                () -> assertTrue(Files.isRegularFile(junitSupport.resolve("RecordingApiTransport.java")),
                        "test transport belongs to the explicit support.junit namespace"),
                () -> assertFalse(Files.isRegularFile(tests.resolve("support/ApiResourceTestBase.java")),
                        "JUnit infrastructure must not remain in the reusable support namespace"),
                () -> assertTrue(Files.isRegularFile(uiSupport.resolve("ReportAuditContext.java")),
                        "ReportAuditContext belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(uiSupport.resolve("UiAuditLifecycle.java")),
                        "UiAuditLifecycle belongs to src/main/java"),
                () -> assertTrue(Files.isRegularFile(uiAudit.resolve("ExistingReportStructureAuditor.java")),
                        "Existing report DOM auditing belongs to the reusable audit package"),
                () -> assertFalse(containsJavaFileNamed(module.resolve("src/test/java/atf/ui/tests"),
                                "ReportAuditContext.java"),
                        "UI audit infrastructure must not remain in src/test/java"),
                () -> assertTrue(Files.isRegularFile(tests.resolve("api/users/UsersApiTests.java")),
                        "UsersApiTests belongs to src/test/java"),
                () -> assertTrue(Files.isRegularFile(tests.resolve("api/posts/PostsApiTests.java")),
                        "PostsApiTests belongs to src/test/java"),
                () -> assertFalse(containsJavaFileNamed(tests, "ApiScenarios.java"),
                        "reusable *ApiScenarios classes must not be hidden in src/test/java"));
    }

    @Test
    void reportUiAuditsUseOneSharedFlowBoundary() throws Exception {
        Path root = locateProjectRoot().resolve("atf-tests/src/test/java/atf/ui/tests/extent");
        List<String> auditTests = List.of(
                "exploratory/ApiExtentReportExploratoryTest.java",
                "exploratory/ExistingExtentReportExploratoryTest.java",
                "exploratory/ExistingExtentCategoriesExploratoryTest.java",
                "flows/ReportNavigationAggregationFlowTest.java");
        var violations = new ArrayList<String>();
        for (String relative : auditTests) {
            Path source = root.resolve(relative);
            if (!Files.isRegularFile(source)) {
                violations.add("missing UI audit test: " + source);
                continue;
            }
            String content = Files.readString(source);
            if (!content.contains("UiAuditFlow")) {
                violations.add(relative + " must use UiAuditFlow");
            }
            if (content.contains("UiAuditLifecycle") || content.contains("beginAudit")
                    || content.contains("class UiAudit")) {
                violations.add(relative + " still owns duplicated audit lifecycle");
            }
        }
        assertTrue(violations.isEmpty(), () -> "UI audit flow boundary violations: " + violations);
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
        for (String module : PRODUCTION_MODULES) {
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


