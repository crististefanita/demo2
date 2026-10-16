package atf.api.ui.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtfUiConfigurationLoaderTest {
    @Test
    void loadsTheUiSectionAndResolvesReportDirectoryRelativeToYaml() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-config-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, """
                ui:
                  browser: firefox
                  baseUrl: https://atf.local
                  timeout: PT4S
                  navigationTimeout: PT7S
                  viewport:
                    width: 1280
                    height: 720
                  report:
                    directory: artifacts/reports
                    filePrefix: report_
                  audit:
                    inputReport: artifacts/reports/input.html
                  apiSetup:
                    scenarioFamilies: [posts, requests]
                  screenValidation:
                    enabled: false
                    fullPage: true
                """);

        UiConfiguration configuration = AtfUiConfigurationLoader.load(yaml);

        assertEquals("firefox", configuration.browser().browser());
        assertEquals(1280, configuration.browser().viewportWidth());
        assertEquals(root.resolve("artifacts/reports").toAbsolutePath().normalize(),
                configuration.reportDirectory());
        assertEquals("report_", configuration.reportFilePrefix());
        assertEquals(root.resolve("artifacts/reports/input.html").toAbsolutePath().normalize(),
                configuration.auditInputReport().orElseThrow());
        assertEquals(java.util.List.of("posts", "requests"), configuration.apiScenarioFamilies());
        assertFalse(configuration.screenValidation().enabled());
        assertTrue(configuration.screenValidation().fullPage());
    }

    @Test
    void explicitConfigPathLoadsWithoutAnEnableSwitch() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-config-override-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  browser: chromium\n");

        UiConfiguration configuration = AtfUiConfigurationLoader.load(
                new AtfUiConfigurationContext(root, yaml.toString(), null));

        assertEquals("chromium", configuration.browser().browser());
        assertEquals("chromium", AtfUiConfigurationLoader.load(yaml).browser().browser());
    }

    @Test
    void explicitReportOverrideWinsOverYamlInputReport() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-report-override-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  audit:\n    inputReport: yaml.html\n");

        Path override = root.resolve("manual.html");
        UiConfiguration configuration = AtfUiConfigurationLoader.load(
                new AtfUiConfigurationContext(root, yaml.toString(), null, override.toString()));

        assertEquals(override.toAbsolutePath().normalize(), configuration.auditInputReport().orElseThrow());
    }

    @Test
    void relativeReportOverrideUsesYamlDirectoryNotSurefireModuleDirectory() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-report-base-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  browser: chromium\n");
        Path moduleWorkingDirectory = root.resolve("atf-tests");
        Files.createDirectories(moduleWorkingDirectory);

        UiConfiguration configuration = AtfUiConfigurationLoader.load(
                new AtfUiConfigurationContext(moduleWorkingDirectory, yaml.toString(), null,
                        "artifacts/reports/input.html"));

        assertEquals(root.resolve("artifacts/reports/input.html").toAbsolutePath().normalize(),
                configuration.auditInputReport().orElseThrow());
    }

    @Test
    void defaultsScenarioFamiliesWhenTheUiSetupIsOmitted() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-config-defaults-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  browser: chromium\n");

        assertEquals(java.util.List.of("posts", "users", "requests", "headers", "lifecycle"),
                AtfUiConfigurationLoader.load(yaml).apiScenarioFamilies());
        assertEquals(root.resolve("artifacts/reports/ui").toAbsolutePath().normalize(),
                AtfUiConfigurationLoader.load(yaml).reportDirectory());
        assertTrue(AtfUiConfigurationLoader.load(yaml).screenValidation().enabled());
        assertFalse(AtfUiConfigurationLoader.load(yaml).screenValidation().fullPage());
    }

    @Test
    void configuredReportPrefixProducesTimestampedNamesForParallelAudits() {
        Path root = Path.of("target/ui-config-prefix").toAbsolutePath().normalize();
        UiConfiguration configuration = new UiConfiguration(
                UiBrowserConfiguration.defaults(), root, "ui-audit_",
                List.of("posts"), UiScreenValidationConfiguration.defaults(),
                java.util.Optional.empty());

        assertEquals("ui-audit_existing_2026-10-16_10-00-00-123.html",
                configuration.timestampedReportFileName("existing", Clock.fixed(
                        Instant.parse("2026-10-16T10:00:00.123Z"), ZoneOffset.UTC)));
    }

    @Test
    void loadsDeclarativeAuditSuitesWithFamiliesTabsValidatorsDepthAndSource() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-suite-config-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, """
                ui:
                  audit:
                    suites:
                      users-posts:
                        scenarioFamilies: [users, posts]
                        tabs: [tests, overview, categories]
                        validators: [setup, request-contract, cross-tab]
                        depth: deep
                        source: yaml
                """);

        UiConfiguration configuration = AtfUiConfigurationLoader.load(yaml);
        UiAuditSuiteConfiguration suite = configuration.auditSuite("USERS_POSTS").orElseThrow();

        assertEquals(List.of("users", "posts"), suite.scenarioFamilies());
        assertEquals(List.of("tests", "overview", "categories"), suite.tabs());
        assertEquals(List.of("setup", "request-contract", "cross-tab"), suite.validators());
        assertEquals("deep", suite.depth());
        assertEquals("yaml", suite.source());
    }
}
