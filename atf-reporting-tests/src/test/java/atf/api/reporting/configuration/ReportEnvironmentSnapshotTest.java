package atf.api.reporting.configuration;

import atf.api.reporting.security.SensitiveDataRedactor;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit contract for the operational Overview/System projection boundary. */
@AtfUnitTest
class ReportEnvironmentSnapshotTest {
    @Test
    void keepsRunContextTypedButPublishesOnlyOperationalSystemFields() {
        var snapshot = ReportEnvironmentSnapshot.builder()
                .put(ReportEnvironmentField.ENVIRONMENT, "ACCEPTANCE")
                .put(ReportEnvironmentField.UI_HOST, "https://ui.example")
                .put(ReportEnvironmentField.SUITE, "secret test suite")
                .put(ReportEnvironmentField.TEST_PACKAGE, "atf.tests.internal")
                .put(ReportEnvironmentField.FLOW, "business checkout")
                .put(ReportEnvironmentField.FIXTURE, "private-fixture")
                .put(ReportEnvironmentField.RETRY_POLICY, "disabled")
                .build();

        var published = ReportEnvironment.with(snapshot, new SensitiveDataRedactor());

        assertAll(
                () -> assertTrue(snapshot.displayValues().containsKey("Suite")),
                () -> assertTrue(published.containsKey("Environment")),
                () -> assertTrue(published.containsKey("UI host")),
                () -> assertTrue(published.containsKey("Retry policy")),
                () -> assertFalse(published.containsKey("Suite")),
                () -> assertFalse(published.containsKey("Test package")),
                () -> assertFalse(published.containsKey("Flow")),
                () -> assertFalse(published.containsKey("Fixture")),
                () -> assertFalse(published.values().stream()
                        .anyMatch(value -> value.contains("private-fixture"))));
    }
}
