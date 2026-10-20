package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopBillingAddress;
import atf.ui.practiceshop.config.PracticeShopCheckoutConfiguration;
import atf.ui.practiceshop.config.PracticeShopGuest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopCheckoutConfigurationTest {
    @Test
    void checkoutInputsRemainTypedAndIndependentFromTheFlow() {
        var checkout = new PracticeShopCheckoutConfiguration(3,
                new PracticeShopGuest("guest@example.test", "Ada", "Lovelace"),
                new PracticeShopBillingAddress("Romania", "010101", "10", "Test Street",
                        "Bucharest", "Bucharest"),
                "Cash on Delivery");

        assertEquals(3, checkout.quantity());
        assertEquals("guest@example.test", checkout.guest().email());
        assertEquals("Romania", checkout.billing().country());
        assertEquals("Cash on Delivery", checkout.paymentMethod());
    }

    @Test
    void invalidCheckoutValuesFailBeforeTheBrowserStarts() {
        assertThrows(IllegalArgumentException.class, () -> new PracticeShopCheckoutConfiguration(0,
                new PracticeShopGuest("guest@example.test", "Ada", "Lovelace"),
                new PracticeShopBillingAddress("Romania", "010101", "10", "Test Street",
                        "Bucharest", "Bucharest"),
                "Cash on Delivery"));
    }
}
