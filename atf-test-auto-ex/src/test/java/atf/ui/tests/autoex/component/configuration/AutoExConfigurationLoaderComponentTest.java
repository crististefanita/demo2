package atf.ui.tests.autoex.component.configuration;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.config.AutoExConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protects the AutoEx YAML contract and path override without starting a browser or public-site call. */
@AtfComponentTest
class AutoExConfigurationLoaderComponentTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void packagedTestYamlDefinesTheSafeAndCompleteDefaultProfile() {
        AutoExConfiguration configuration = AutoExConfigurationLoader.load();

        assertAll(
                () -> assertEquals("https://automationexercise.com", configuration.baseUrl().toString()),
                () -> assertFalse(configuration.e2eEnabled(), "live E2E must remain opt-in by default"),
                () -> assertEquals("chromium", configuration.browser().browser()),
                () -> assertEquals(20, configuration.browser().timeout().toSeconds()),
                () -> assertEquals(45, configuration.browser().navigationTimeout().toSeconds()),
                () -> assertEquals(1440, configuration.browser().viewportWidth()),
                () -> assertEquals(1000, configuration.browser().viewportHeight()),
                () -> assertEquals(Path.of("artifacts/reports/ui"), configuration.uiReportBaseDirectory()),
                () -> assertEquals("Blue Top", configuration.catalog().productName()),
                () -> assertEquals(2, configuration.catalog().quantity()),
                () -> assertEquals("ATF AutoEx", configuration.registration().displayName()),
                () -> assertEquals("Rs. 1000", configuration.expectedCartTotal()));
    }

    @Test
    void explicitYamlPathOverridesPackagedDefaults() throws IOException {
        String yaml = Files.readString(defaultYaml())
                .replace("e2eEnabled: false", "e2eEnabled: true")
                .replace("searchTerm: Blue Top", "searchTerm: Green Top")
                .replace("productName: Blue Top", "productName: Green Top");
        Path override = temporaryDirectory.resolve("autoex-local.yaml");
        Files.writeString(override, yaml);

        AutoExConfiguration configuration = AutoExConfigurationLoader.load(override);

        assertTrue(configuration.e2eEnabled());
        assertEquals("Green Top", configuration.catalog().searchTerm());
        assertEquals("Green Top", configuration.catalog().productName());
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    void configuredSystemPropertySelectsTheExternalYamlForDirectIdeRuns() throws IOException {
        String previous = System.getProperty(AutoExConfigurationLoader.CONFIG_PROPERTY);
        try {
            String yaml = Files.readString(defaultYaml()).replace("quantity: 2", "quantity: 3");
            Path override = temporaryDirectory.resolve("ide-profile.yaml");
            Files.writeString(override, yaml);
            System.setProperty(AutoExConfigurationLoader.CONFIG_PROPERTY, override.toString());

            assertEquals(3, AutoExConfigurationLoader.load().catalog().quantity());
        } finally {
            if (previous == null) System.clearProperty(AutoExConfigurationLoader.CONFIG_PROPERTY);
            else System.setProperty(AutoExConfigurationLoader.CONFIG_PROPERTY, previous);
        }
    }

    @Test
    void rejectsInvalidBaseUrlAndEscapingReportDirectory() throws IOException {
        Path invalidUrl = writeVariant("baseUrl: https://automationexercise.com",
                "baseUrl: ftp://automationexercise.com");
        Path escapingReports = writeVariant("uiBaseDirectory: artifacts/reports/ui",
                "uiBaseDirectory: ../outside");
        Path misspelledSetting = writeVariant("navigationTimeout: PT45S", "navigationTimout: PT30S");
        Path invalidTimeout = writeVariant("timeout: PT20S", "timeout: twenty seconds");

        assertThrows(IllegalStateException.class, () -> AutoExConfigurationLoader.load(invalidUrl));
        assertThrows(IllegalStateException.class,
                () -> AutoExConfigurationLoader.load(escapingReports));
        assertThrows(IllegalStateException.class,
                () -> AutoExConfigurationLoader.load(misspelledSetting),
                "unknown YAML keys must not silently fall back to defaults");
        assertThrows(IllegalStateException.class,
                () -> AutoExConfigurationLoader.load(invalidTimeout));
    }

    @Test
    void redactsRegistrationAndDemoPaymentWhenConfigurationIsRenderedAsText() {
        String rendered = AutoExConfigurationLoader.load().toString();

        assertAll(
                () -> assertFalse(rendered.contains("5550100100")),
                () -> assertFalse(rendered.contains("4242424242424242")),
                () -> assertFalse(rendered.contains("123")),
                () -> assertFalse(rendered.contains("10 Test Avenue")),
                () -> assertEquals("Payment[configured=true]", AutoExConfigurationLoader.load()
                        .payment().toString()),
                () -> assertEquals("Registration[configured=true]", AutoExConfigurationLoader.load()
                        .registration().toString()));
    }

    private Path writeVariant(String original, String replacement) throws IOException {
        String yaml = Files.readString(defaultYaml()).replace(original, replacement);
        Path file = temporaryDirectory.resolve("variant-" + Math.abs(replacement.hashCode()) + ".yaml");
        Files.writeString(file, yaml);
        return file;
    }

    private static Path defaultYaml() {
        try {
            var resource = AutoExConfigurationLoaderComponentTest.class.getClassLoader()
                    .getResource(AutoExConfigurationLoader.DEFAULT_RESOURCE);
            if (resource == null) throw new IllegalStateException("AutoEx test YAML is not on the test classpath");
            return Path.of(resource.toURI());
        } catch (URISyntaxException error) {
            throw new IllegalStateException("Cannot resolve AutoEx test YAML resource", error);
        }
    }
}
