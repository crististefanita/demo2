package atf.api.tests.support;

import atf.api.core.config.ApiTransportMode;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiBootstrapConfigurationLoaderTest {
    @Test
    void loadsAllApiSettingsAndKeepsAcceptanceAsTheDefaultTransport() throws Exception {
        Path directory = Files.createTempDirectory("atf-api-bootstrap-");
        try {
            Path config = directory.resolve("atf.yaml");
            Files.writeString(config, """
                    api:
                      baseUri: https://configured.example
                      defaultAuthProfile: service
                      timeout: PT11S
                      retryStatuses: [408, 429]
                      maxAttempts: 3
                      transport: acceptance
                    """);

            var loaded = ApiBootstrapConfigurationLoader.load(config);

            assertEquals("https://configured.example", loaded.settings().baseUri().toString());
            assertEquals("service", loaded.settings().defaultAuthProfile());
            assertEquals(11, loaded.settings().timeout().toSeconds());
            assertEquals(java.util.Set.of(408, 429), loaded.settings().retryStatuses());
            assertEquals(3, loaded.settings().maxAttempts());
            assertEquals(ApiTransportMode.ACCEPTANCE, loaded.transportPolicy().mode());
        } finally {
            deleteTree(directory);
        }
    }

    @Test
    void liveTransportCannotBeSelectedWithoutAnExplicitOptIn() throws Exception {
        String previous = System.getProperty(ApiBootstrapConfigurationLoader.LIVE_OPT_IN_PROPERTY);
        Path directory = Files.createTempDirectory("atf-api-live-policy-");
        try {
            Path config = directory.resolve("atf.yaml");
            Files.writeString(config, "api:\n  transport: live\n");
            System.clearProperty(ApiBootstrapConfigurationLoader.LIVE_OPT_IN_PROPERTY);
            assertThrows(IllegalArgumentException.class,
                    () -> ApiBootstrapConfigurationLoader.load(config));

            System.setProperty(ApiBootstrapConfigurationLoader.LIVE_OPT_IN_PROPERTY, "true");
            var loaded = ApiBootstrapConfigurationLoader.load(config);
            assertEquals(ApiTransportMode.LIVE, loaded.transportPolicy().mode());
            assertEquals(ApiEnvironment.MOCK_BASE_URI.toString(), loaded.settings().baseUri().toString());
        } finally {
            if (previous == null) System.clearProperty(ApiBootstrapConfigurationLoader.LIVE_OPT_IN_PROPERTY);
            else System.setProperty(ApiBootstrapConfigurationLoader.LIVE_OPT_IN_PROPERTY, previous);
            deleteTree(directory);
        }
    }

    private static void deleteTree(Path root) throws Exception {
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); }
                catch (java.io.IOException error) { throw new RuntimeException(error); }
            });
        }
    }
}
