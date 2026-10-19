package atf.api.console;
import atf.api.testkit.AtfUnitTest;


import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest

class AtfConsoleConfigurationLoaderTest {
    @Test
    void loadsOnlyConsolePolicyFromYaml() throws Exception {
        Path config = Files.createTempFile("atf-console-", ".yaml");
        try {
            Files.writeString(config, """
                    reporting:
                      assertionMaxDisplayCharacters: 128
                      extent:
                        outputDirectory: reports
                      console:
                        enabled: false
                        detailsEnabled: true
                    """);

            var loaded = AtfConsoleConfigurationLoader.load(config);

            assertFalse(loaded.settings().enabled());
            assertTrue(loaded.settings().detailsEnabled());
            assertEquals(config.toAbsolutePath().normalize(), loaded.source().orElseThrow());
        } finally {
            Files.deleteIfExists(config);
        }
    }

    @Test
    void discoversConfigurationFromParentAndKeepsDefaultsWhenAbsent() throws Exception {
        Path project = Files.createTempDirectory("atf-console-discovery-");
        Path nested = project.resolve("module/target");
        Files.createDirectories(nested);
        Path config = project.resolve("atf.yml");
        Files.writeString(config, "reporting:\n  console:\n    detailsEnabled: true\n");

        assertTrue(AtfConsoleConfigurationLoader.load(
                new AtfConsoleConfigurationContext(nested, null, null))
                .settings().detailsEnabled());
        assertTrue(AtfConsoleConfigurationLoader.load(
                new AtfConsoleConfigurationContext(project.resolve("missing"), null, null))
                .settings().enabled());
    }

    @Test
    void explicitPropertyContextWinsOverEnvironmentContext() throws Exception {
        Path root = Files.createTempDirectory("atf-console-context-");
        Path property = root.resolve("property.yaml");
        Path environment = root.resolve("environment.yaml");
        Files.writeString(property, "reporting:\n  console:\n    enabled: false\n");
        Files.writeString(environment, "reporting:\n  console:\n    enabled: true\n");

        var loaded = AtfConsoleConfigurationLoader.load(new AtfConsoleConfigurationContext(
                root, property.toString(), environment.toString()));

        assertFalse(loaded.settings().enabled());
    }

    @Test
    void rejectsNonBooleanConsoleValues() throws Exception {
        Path config = Files.createTempFile("atf-console-invalid-", ".yaml");
        try {
            Files.writeString(config, "reporting:\n  console:\n    enabled: not-a-boolean\n");
            assertThrows(IllegalStateException.class,
                    () -> AtfConsoleConfigurationLoader.load(config));
        } finally {
            Files.deleteIfExists(config);
        }
    }
}
