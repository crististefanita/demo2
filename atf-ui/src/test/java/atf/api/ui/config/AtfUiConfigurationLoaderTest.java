package atf.api.ui.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

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
                  enabled: true
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

        assertTrue(configuration.enabled());
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
    void explicitProcessOverrideCanEnableUiWithoutMutatingYaml() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-config-override-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  enabled: false\n");

        UiConfiguration configuration = AtfUiConfigurationLoader.load(
                new AtfUiConfigurationContext(root, yaml.toString(), null, "true"));

        assertTrue(configuration.enabled());
        assertFalse(AtfUiConfigurationLoader.load(yaml).enabled());
    }

    @Test
    void explicitReportOverrideWinsOverYamlInputReport() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-report-override-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  audit:\n    inputReport: yaml.html\n");

        Path override = root.resolve("manual.html");
        UiConfiguration configuration = AtfUiConfigurationLoader.load(
                new AtfUiConfigurationContext(root, yaml.toString(), null, null, override.toString()));

        assertEquals(override.toAbsolutePath().normalize(), configuration.auditInputReport().orElseThrow());
    }

    @Test
    void relativeReportOverrideUsesYamlDirectoryNotSurefireModuleDirectory() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-report-base-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  enabled: true\n");
        Path moduleWorkingDirectory = root.resolve("atf-tests");
        Files.createDirectories(moduleWorkingDirectory);

        UiConfiguration configuration = AtfUiConfigurationLoader.load(
                new AtfUiConfigurationContext(moduleWorkingDirectory, yaml.toString(), null,
                        "true", "artifacts/reports/input.html"));

        assertEquals(root.resolve("artifacts/reports/input.html").toAbsolutePath().normalize(),
                configuration.auditInputReport().orElseThrow());
    }

    @Test
    void defaultsScenarioFamiliesWhenTheUiSetupIsOmitted() throws Exception {
        Path root = Files.createTempDirectory("atf-ui-config-defaults-");
        Path yaml = root.resolve("atf.yaml");
        Files.writeString(yaml, "ui:\n  enabled: true\n");

        assertEquals(java.util.List.of("posts", "users", "requests", "headers", "lifecycle"),
                AtfUiConfigurationLoader.load(yaml).apiScenarioFamilies());
        assertTrue(AtfUiConfigurationLoader.load(yaml).screenValidation().enabled());
        assertFalse(AtfUiConfigurationLoader.load(yaml).screenValidation().fullPage());
    }
}
