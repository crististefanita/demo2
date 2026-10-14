package atf.api.core;

import atf.api.core.config.ApiSettings;
import atf.api.core.config.AtfConfiguration;
import atf.api.core.config.ReportingSettings;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationContractTest {
    @Test
    void settingsSnapshotIsImmutableAndCopiesRetryStatuses() {
        var statuses = new java.util.HashSet<>(Set.of(429));
        var settings = new ApiSettings(URI.create("https://example.test"), "default",
                Duration.ofSeconds(3), statuses, 3);
        var snapshot = settings.snapshot();
        statuses.add(503);

        assertEquals(Set.of(429), snapshot.retryStatuses());
        assertEquals(Duration.ofSeconds(3), snapshot.timeout());
        assertEquals(3, snapshot.maxAttempts());
    }

    @Test
    void configurationRejectsNullBoundariesAndValidatesDisplayLimit() {
        var configuration = new AtfConfiguration(
                new ApiSettings(URI.create("https://example.test"), "none"),
                ReportingSettings.defaults());
        assertDoesNotThrow(configuration::validate);
        assertThrows(NullPointerException.class, () -> new AtfConfiguration(null, ReportingSettings.defaults()));
    }
}
