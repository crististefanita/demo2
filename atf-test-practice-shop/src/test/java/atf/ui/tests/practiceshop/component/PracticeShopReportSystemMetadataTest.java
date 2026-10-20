package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.support.PracticeShopReportSystemMetadata;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies that the Practice Shop report exposes useful configuration without secrets or temp paths. */
@AtfComponentTest
class PracticeShopReportSystemMetadataTest {
    @Test
    void publishesTypedConsumerConfigurationAsSafeOverviewMetadata() {
        Map<String, String> metadata = PracticeShopReportSystemMetadata.from(
                PracticeShopConfigurationLoader.load());
        String values = metadata.toString();

        assertAll(
                () -> assertTrue(values.contains("chromium")),
                () -> assertTrue(values.contains("headless=true")),
                () -> assertTrue(values.contains("zoom=70%")),
                () -> assertTrue(values.contains("Validation profile=ui_api")),
                () -> assertTrue(values.contains("API mode=LIVE")),
                () -> assertTrue(values.contains("API precondition=authenticated")),
                () -> assertTrue(values.contains("quantity=2")),
                () -> assertTrue(values.contains("Cash on Delivery")),
                () -> assertTrue(values.contains("configured UI report directory")),
                () -> assertFalse(values.toLowerCase().contains("password")),
                () -> assertFalse(values.contains(".tmp")),
                () -> assertFalse(values.contains("practice.shop@example.test")));
    }
}
