package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.support.PracticeShopReportSystemMetadata;
import atf.api.reporting.configuration.ReportEnvironmentField;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
                () -> assertTrue(values.contains("headless")),
                () -> assertTrue(values.contains("zoom 70%")),
                () -> assertTrue(values.contains("UI + API evidence")),
                () -> assertTrue(values.contains("Transport mode=LIVE HTTP")),
                () -> assertTrue(values.contains("Precondition=API authentication: authenticated")),
                () -> assertTrue(values.contains("UI action timeout=20s")),
                () -> assertTrue(values.contains("Page navigation timeout=45s")),
                () -> assertTrue(values.contains("API request timeout=20s")),
                () -> assertTrue(values.contains("Network inspector")),
                () -> assertTrue(values.contains("Body preview")),
                () -> assertFalse(values.contains("PT20S")),
                () -> assertFalse(values.contains("PT45S")),
                () -> assertFalse(values.contains("Flow=")),
                () -> assertFalse(values.contains("Suite=")),
                () -> assertFalse(values.contains("Fixture=")),
                () -> assertFalse(values.contains("Test package")),
                () -> assertFalse(values.toLowerCase().contains("password")),
                () -> assertFalse(values.contains(".tmp")),
                () -> assertFalse(values.contains("practice.shop@example.test")));
    }

    @Test
    void exposesTheSameConsumerConfigurationThroughTheReportingAllowlist() {
        var snapshot = PracticeShopReportSystemMetadata.snapshot(
                PracticeShopConfigurationLoader.load(),
                atf.api.reporting.configuration.ReportingSettings.defaults());

        assertAll(
                () -> assertTrue(snapshot.values().containsKey(ReportEnvironmentField.PROFILE)),
                () -> assertTrue(snapshot.values().containsKey(ReportEnvironmentField.UI_HOST)),
                () -> assertTrue(snapshot.values().containsKey(ReportEnvironmentField.API_ENDPOINT)),
                () -> assertTrue(snapshot.values().containsKey(ReportEnvironmentField.BODY_PREVIEW)),
                () -> assertTrue(snapshot.values().containsKey(ReportEnvironmentField.SCREENSHOT_POLICY)),
                () -> assertEquals("20s", snapshot.displayValues().get(ReportEnvironmentField.UI_TIMEOUT.label())),
                () -> assertEquals("45s", snapshot.displayValues().get(ReportEnvironmentField.NAVIGATION_TIMEOUT.label())),
                () -> assertEquals("20s", snapshot.displayValues().get(ReportEnvironmentField.API_TIMEOUT.label())),
                () -> assertFalse(snapshot.values().containsKey(ReportEnvironmentField.FLOW)),
                () -> assertFalse(snapshot.values().containsKey(ReportEnvironmentField.FIXTURE)),
                () -> assertFalse(snapshot.values().containsKey(ReportEnvironmentField.SUITE)),
                () -> assertFalse(snapshot.values().containsKey(ReportEnvironmentField.TEST_PACKAGE)),
                () -> assertFalse(snapshot.displayValues().toString().contains("password")),
                () -> assertFalse(snapshot.displayValues().toString().contains(".tmp")));
    }
}
