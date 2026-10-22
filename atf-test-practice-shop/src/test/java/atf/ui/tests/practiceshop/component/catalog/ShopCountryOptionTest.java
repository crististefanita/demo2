package atf.ui.tests.practiceshop.component.catalog;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.ShopCountryOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopCountryOptionTest {
    private static final ShopCountryOption ROMANIA =
            new ShopCountryOption("RO", "Romania", "RO");

    @Test
    void matchesConfiguredCountryByHumanLabelOrCountryCode() {
        assertTrue(ROMANIA.matches("Romania"));
        assertTrue(ROMANIA.matches("RO"));
        assertTrue(ROMANIA.matches(" ro "));
        assertFalse(ROMANIA.matches("Bulgaria"));
    }

    @Test
    void selectsByHumanLabelEvenWhenConfigurationUsesCountryCode() {
        assertEquals("Romania", ROMANIA.selectionLabel());
    }

    @Test
    void displaysBothCountryNameAndCodeForUsefulDiagnostics() {
        assertEquals("Romania (RO)", ROMANIA.displayValue());
    }

    @Test
    void fallsBackToOptionTextWhenNoExplicitLabelExists() {
        var option = new ShopCountryOption("RO", null, "RO");

        assertTrue(option.matches("RO"));
        assertEquals("RO", option.selectionLabel());
        assertEquals("RO", option.displayValue());
    }
}
