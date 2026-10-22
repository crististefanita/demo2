package atf.ui.tests.practiceshop.component.catalog;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.pages.PracticeShopBillingPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopBillingLookupTest {
    @Test
    void lookupEvidenceRequiresEveryApplicationGeneratedAddressField() {
        var address = new PracticeShopBillingPage.ObservedAddress("Wayne Lock", "Cartwrightmouth", "Alaska");

        assertEquals("Wayne Lock", address.street());
        assertEquals("Cartwrightmouth", address.city());
        assertEquals("Alaska", address.state());
    }

    @Test
    void blankLookupResultFailsInsteadOfProducingAnUnauditableBillingCheckpoint() {
        assertThrows(IllegalStateException.class,
                () -> new PracticeShopBillingPage.ObservedAddress("Wayne Lock", "  ", "Alaska"));
    }
}
