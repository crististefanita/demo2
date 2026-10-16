package atf.ui.tests.extent.plan;

import atf.api.mock.AtfMockConfiguration;
import atf.api.ui.config.UiAuditSuiteConfiguration;
import atf.api.ui.config.UiBrowserConfiguration;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.config.UiScreenValidationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiReportAuditSpecTest {
    @AfterEach
    void clearSelectionProperties() {
        System.clearProperty(UiReportAuditSpec.RULES_PROPERTY);
        System.clearProperty(UiReportAuditSpec.DEPTH_PROPERTY);
        System.clearProperty(UiReportAuditSpec.SUITE_PROPERTY);
    }

    @Test
    void deepSpecIsDataOnlyAndUsesClosedRuleVocabulary() {
        UiReportAuditSpec spec = UiReportAuditSpec.legacy(
                "users-posts", List.of("users", "posts"), List.of("ui", "api"),
                "setup", "failure", "report");

        assertEquals(List.of("users", "posts"), spec.apiFamilies());
        assertTrue(spec.includes(UiReportAuditRule.REQUEST_CONTRACT));
        assertTrue(spec.includes(UiReportAuditRule.CROSS_TAB));
    }

    @Test
    void ruleNamesAreStableAndParseableForManualSelection() {
        assertEquals(UiReportAuditRule.REQUEST_CONTRACT,
                UiReportAuditRule.parse("request-contract"));
        assertEquals(UiReportAuditRule.RESPONSIVE,
                UiReportAuditRule.parse("responsive"));
    }

    @Test
    void yamlSuiteProvidesFamiliesAndRuleSelectionWithoutManualProperties() {
        UiConfiguration configuration = new UiConfiguration(
                UiBrowserConfiguration.defaults(), Path.of("target/ui-suite"), "ui-",
                List.of("posts"), UiScreenValidationConfiguration.defaults(), Optional.empty(),
                Map.of("users-posts", new UiAuditSuiteConfiguration("users-posts",
                        List.of("users", "posts"), List.of("tests"),
                        List.of("setup", "request-contract", "cross-tab"), "deep", "yaml")));

        System.setProperty(UiReportAuditSpec.SUITE_PROPERTY, "users-posts");
        UiReportAuditSpec spec = UiReportAuditSpec.resolve(UiReportAuditSpecTest.class, configuration);

        assertEquals(List.of("users", "posts"), spec.apiFamilies());
        assertTrue(spec.includes(UiReportAuditRule.REQUEST_CONTRACT));
        assertTrue(spec.includes(UiReportAuditRule.CROSS_TAB));
        assertTrue(spec.includes(UiReportAuditRule.SETUP));
        assertEquals(3, spec.rules().size());
    }

    @Test
    void explicitJvmDepthOverridesYamlDepthWithoutChangingFamilySelection() {
        UiConfiguration configuration = new UiConfiguration(
                UiBrowserConfiguration.defaults(), Path.of("target/ui-suite"), "ui-",
                List.of("posts"), UiScreenValidationConfiguration.defaults(), Optional.empty(),
                Map.of("users-posts", new UiAuditSuiteConfiguration("users-posts",
                        List.of("users", "posts"), List.of("tests"),
                        List.of("setup", "tests", "request-contract", "categories"), "deep", "yaml")));

        System.setProperty(UiReportAuditSpec.SUITE_PROPERTY, "users-posts");
        System.setProperty(UiReportAuditSpec.DEPTH_PROPERTY, "smoke");
        UiReportAuditSpec spec = UiReportAuditSpec.resolve(UiReportAuditSpecTest.class, configuration);

        assertEquals(List.of("users", "posts"), spec.apiFamilies());
        assertEquals(4, spec.rules().size());
        assertTrue(spec.includes(UiReportAuditRule.SETUP));
        assertTrue(spec.includes(UiReportAuditRule.TESTS));
        assertTrue(spec.includes(UiReportAuditRule.REQUEST_CONTRACT));
        assertTrue(spec.includes(UiReportAuditRule.OVERVIEW));
        assertFalse(spec.includes(UiReportAuditRule.CATEGORIES));
    }
}
