package atf.ui.tests.practiceshop.component.evidence;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopApiEvidenceBinding;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiEvidenceBindingTest {
    @Test
    void missingSelectionEnablesAllCheckpointsOnlyForUiApi() {
        var all = PracticeShopApiEvidenceBinding.forProfile(PracticeShopValidationProfile.UI_API, null);
        var none = PracticeShopApiEvidenceBinding.forProfile(PracticeShopValidationProfile.UI_ONLY, null);

        assertEquals("cart, login, order, payment, product, registration", all.displayValue());
        assertTrue(all.includes(PracticeShopApiCheckpoint.REGISTRATION));
        assertTrue(all.includes(PracticeShopApiCheckpoint.PRODUCT));
        assertTrue(all.includes(PracticeShopApiCheckpoint.PAYMENT));
        assertTrue(all.includes(PracticeShopApiCheckpoint.ORDER));
        assertFalse(none.includes(PracticeShopApiCheckpoint.PRODUCT));
        assertEquals("", none.displayValue());
    }

    @Test
    void explicitSelectionIsTypedAndDoesNotEnableUnselectedCheckpoints() {
        var binding = PracticeShopApiEvidenceBinding.fromCsv(
                PracticeShopValidationProfile.UI_API, "product, payment");

        assertTrue(binding.includes(PracticeShopApiCheckpoint.PRODUCT));
        assertTrue(binding.includes(PracticeShopApiCheckpoint.PAYMENT));
        assertFalse(binding.includes(PracticeShopApiCheckpoint.LOGIN));
        assertEquals("payment, product", binding.displayValue());
    }

    @Test
    void unknownCheckpointFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> PracticeShopApiEvidenceBinding.fromCsv(
                PracticeShopValidationProfile.UI_API, "product,unknown"));
        assertThrows(IllegalArgumentException.class, () -> PracticeShopApiCheckpoint.from(""));
    }
}
