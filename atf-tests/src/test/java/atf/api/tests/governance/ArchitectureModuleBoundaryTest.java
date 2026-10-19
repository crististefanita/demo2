package atf.api.tests.governance;

import atf.api.core.config.module.AtfModuleIds;
import atf.api.testkit.AtfComponentTest;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static atf.api.tests.governance.AtfGovernanceTestSupport.*;

/** Whole-ATF guard for source imports, Maven direction and process-state ownership. */
@AtfComponentTest
class ArchitectureModuleBoundaryTest {
    @Test
    void rootPomExposesTheCanonicalReactorModuleCatalog() throws Exception {
        Path root = locateProjectRoot();
        String pom = Files.readString(root.resolve("pom.xml"));
        assertTrue(AtfModuleIds.REACTOR_MODULES.stream()
                        .allMatch(module -> pom.contains("<module>" + module + "</module>")),
                () -> "Root POM must expose every canonical reactor module: "
                        + AtfModuleIds.REACTOR_MODULES);
    }

    @Test
    void productionModulesDoNotImportForbiddenLayersOrMarkup() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        for (AtfGovernanceRules.SourceRule rule : AtfGovernanceRules.PRODUCTION_SOURCE_RULES) {
            assertNoForbidden(moduleSource(root, rule.module(), false),
                    rule.forbiddenTokens(), violations);
        }
        assertTrue(violations.isEmpty(), () -> "Architecture boundary violations: " + violations);
    }

    @Test
    void modulePomsKeepRendererNeutralDependencyDirection() throws Exception {
        Path root = locateProjectRoot();
        assertAll(AtfGovernanceRules.MODULE_DEPENDENCY_RULES.stream()
                .map(rule -> () -> {
                    if (rule.required()) {
                        assertDependencyPresent(root, rule.module(), rule.dependency());
                    } else {
                        assertDependencyAbsent(root, rule.module(), rule.dependency());
                    }
                }));
    }

    @Test
    void reportingProductionCodeDoesNotReachIntoExecutionOrBusinessValidation() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        assertNoForbiddenReportingCore(moduleSource(root, AtfModuleIds.REPORTING, false),
                List.of("atf.api.execution", "atf.api.validation", "atf.api.console",
                        "org.junit", "org.junit.jupiter"), violations);
        assertNoForbidden(moduleSource(root, AtfModuleIds.CONSOLE, false),
                List.of("atf.api.reporting", "com.aventstack"), violations);
        assertTrue(violations.isEmpty(), () -> "Reporting boundary violations: " + violations);
    }

    @Test
    void executionAndReportingRuntimeGraphsAreCoreOnly() throws Exception {
        Path root = locateProjectRoot();
        assertDependencyScope(root, AtfModuleIds.EXECUTION, AtfModuleIds.VALIDATION, "test");
        assertDependencyScope(root, AtfModuleIds.REPORTING, AtfModuleIds.VALIDATION, "test");
        assertDependencyScope(root, AtfModuleIds.REPORTING, AtfModuleIds.EXECUTION, "test");
    }

    @Test
    void processStreamOutputBelongsOnlyToTheConsoleModule() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        String stdoutToken = "System." + "out";
        String stderrToken = "System." + "err";
        String stackTraceToken = "print" + "StackTrace";
        for (String module : AtfModuleIds.SOURCE_GOVERNED_MODULES) {
            if (module.equals(AtfModuleIds.CONSOLE)) continue;
            for (Path sourceRoot : List.of(
                    sourceRoots(root, module, false).getFirst(),
                    sourceRoots(root, module, true).getFirst())) {
                if (!Files.isDirectory(sourceRoot)) continue;
                try (var files = Files.walk(sourceRoot)) {
                    files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                        try {
                            String source = Files.readString(path);
                            if (source.contains(stdoutToken)) violations.add(path + " owns direct stdout output");
                            if (source.contains(stderrToken)) violations.add(path + " owns direct stderr output");
                            if (source.contains(stackTraceToken)) violations.add(path + " leaks a direct stack trace");
                        } catch (Exception error) {
                            violations.add("cannot read " + path + ": " + error.getMessage());
                        }
                    });
                }
            }
        }
        assertTrue(violations.isEmpty(), () ->
                "Only atf-console may own process stream output: " + violations);
    }

    @Test
    void productionModulesDoNotDeclareMutableStaticFields() throws Exception {
        Path root = locateProjectRoot();
        var violations = new ArrayList<String>();
        for (String module : AtfModuleIds.SOURCE_GOVERNED_MODULES) {
            Path sourceRoot = sourceRoots(root, module, false).getFirst();
            if (!Files.isDirectory(sourceRoot)) continue;
            try (var files = Files.walk(sourceRoot)) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                    try {
                        int lineNumber = 0;
                        for (String line : Files.readAllLines(path)) {
                            lineNumber++;
                            String trimmed = line.trim();
                            if (trimmed.startsWith("//") || trimmed.startsWith("*")
                                    || trimmed.contains(" static final ") || trimmed.startsWith("static final ")) continue;
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
}
