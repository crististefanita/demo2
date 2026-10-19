package atf.api.tests.governance;

import atf.api.core.config.module.AtfModuleIds;

import java.util.List;

/** Test-only policy catalog for whole-ATF source and Maven boundaries. */
final class AtfGovernanceRules {
    static final List<SourceRule> PRODUCTION_SOURCE_RULES = List.of(
            source(AtfModuleIds.CORE, "com.aventstack", "org.junit", "atf.api.execution"),
            source(AtfModuleIds.MOCK, "com.aventstack", "org.junit", "atf.api.execution",
                    "atf.api.validation", "atf.api.reporting"),
            source(AtfModuleIds.EXECUTION, "com.aventstack", "org.junit", "<style", "<script",
                    "atf.api.validation"),
            source(AtfModuleIds.VALIDATION, "com.aventstack", "org.junit", "<style", "<script",
                    "atf.api.reporting"),
            source(AtfModuleIds.UI, "atf.api.execution", "atf.api.validation", "atf.api.reporting",
                    "atf.api.mock", "org.junit", "<style", "<script"),
            source(AtfModuleIds.DEBUG_UI, "atf.api.execution", "atf.api.validation", "atf.api.reporting",
                    "atf.api.mock", "org.junit", "<style", "<script"));

    static final List<DependencyRule> MODULE_DEPENDENCY_RULES = List.of(
            dependency(AtfModuleIds.CORE, AtfModuleIds.VALIDATION, false),
            dependency(AtfModuleIds.CORE, AtfModuleIds.EXECUTION, false),
            dependency(AtfModuleIds.CORE, AtfModuleIds.REPORTING, false),
            dependency(AtfModuleIds.MOCK, AtfModuleIds.REPORTING, false),
            dependency(AtfModuleIds.MOCK, AtfModuleIds.CORE, true),
            dependency(AtfModuleIds.REPORTING, AtfModuleIds.CONSOLE, true),
            dependency(AtfModuleIds.VALIDATION, AtfModuleIds.EXECUTION, false),
            dependency(AtfModuleIds.VALIDATION, AtfModuleIds.REPORTING, false),
            dependency(AtfModuleIds.EXECUTION, AtfModuleIds.REPORTING, false),
            dependency(AtfModuleIds.VALIDATION, AtfModuleIds.CORE, true),
            dependency(AtfModuleIds.EXECUTION, AtfModuleIds.VALIDATION, true),
            dependency(AtfModuleIds.CONSOLE, AtfModuleIds.CORE, true),
            dependency(AtfModuleIds.REPORTING, AtfModuleIds.CORE, true),
            dependency(AtfModuleIds.REPORTING, AtfModuleIds.VALIDATION, true),
            dependency(AtfModuleIds.REPORTING, AtfModuleIds.EXECUTION, true),
            dependency(AtfModuleIds.UI, AtfModuleIds.CORE, true),
            dependency(AtfModuleIds.TESTS, AtfModuleIds.REPORTING, false));

    private AtfGovernanceRules() {
    }

    private static SourceRule source(String module, String... forbidden) {
        return new SourceRule(module, List.of(forbidden));
    }

    private static DependencyRule dependency(String module, String dependency, boolean required) {
        return new DependencyRule(module, dependency, required);
    }

    record SourceRule(String module, List<String> forbiddenTokens) {
    }

    record DependencyRule(String module, String dependency, boolean required) {
    }
}
