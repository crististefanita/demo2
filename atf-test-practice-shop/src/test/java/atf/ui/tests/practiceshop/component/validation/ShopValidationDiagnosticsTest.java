package atf.ui.tests.practiceshop.component.validation;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.ShopValidationDiagnostics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopValidationDiagnosticsTest {
    @Test
    void keepsExceptionTypeAndCauseMessageInACompactFailureObservation() {
        String diagnostic = ShopValidationDiagnostics.unavailable(
                new IllegalStateException("selector did not resolve\nfor the configured product"));

        assertEquals("unavailable: IllegalStateException — selector did not resolve for the configured product",
                diagnostic);
    }

    @Test
    void boundsVeryLongPlaywrightMessages() {
        String diagnostic = ShopValidationDiagnostics.unavailable(
                new IllegalArgumentException("x".repeat(500)));

        assertTrue(diagnostic.length() <= 210);
        assertTrue(diagnostic.endsWith("…"));
    }
}
