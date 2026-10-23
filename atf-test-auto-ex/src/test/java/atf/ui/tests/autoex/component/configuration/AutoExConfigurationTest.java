package atf.ui.tests.autoex.component.configuration;

import atf.api.testkit.AtfUnitTest;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.config.AutoExConfigurationLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class AutoExConfigurationTest {
    @Test
    void readsConsumerOwnedSettingsAndKeepsLiveExecutionOptIn() {
        AutoExConfiguration configuration = AutoExConfigurationLoader.load();

        assertEquals("https://automationexercise.com", configuration.baseUrl().toString());
        assertEquals("Blue Top", configuration.catalog().productName());
        assertEquals("Rs. 1000", configuration.expectedCartTotal());
        assertEquals(Path.of("artifacts/reports/ui"), configuration.uiReportBaseDirectory());
        assertFalse(configuration.e2eEnabled());
        assertTrue(configuration.browser().headless());
    }

    @Test
    void rejectsReportBaseThatCouldEscapeTheProjectRoot() {
        assertThrows(IllegalArgumentException.class, () -> new AutoExConfiguration(
                java.net.URI.create("https://automationexercise.com"), false,
                atf.api.ui.config.UiBrowserConfiguration.defaults(), Path.of("../outside"),
                new AutoExConfiguration.Catalog("shirt", "1", "shirt", "Rs. 1", 1),
                new AutoExConfiguration.Registration("Mr", "1", "May", "1990", "A", "B",
                        "Company", "Address", "Address 2", "United States", "State", "City",
                        "00000", "5550100100"),
                new AutoExConfiguration.Payment("Demo", "4242424242424242", "123", "12", "2030")));
    }
}
