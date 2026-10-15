package atf.api.ui.config;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UiBrowserConfigurationTest {
    @Test
    void defaultsAreDeterministicAndVendorNeutral() {
        var configuration = UiBrowserConfiguration.defaults();
        assertEquals("chromium", configuration.browser());
        assertEquals(1440, configuration.viewportWidth());
        assertEquals(Duration.ofSeconds(10), configuration.timeout());
    }

    @Test
    void rejectsUnsupportedBrowserAndNonPositiveTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new UiBrowserConfiguration(
                URI.create("about:blank"), "safari", true, Duration.ofSeconds(1),
                Duration.ofSeconds(1), 100, 100, true));
        assertThrows(IllegalArgumentException.class, () -> new UiBrowserConfiguration(
                URI.create("about:blank"), "chromium", true, Duration.ZERO,
                Duration.ofSeconds(1), 100, 100, true));
    }
}
