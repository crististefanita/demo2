package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopValidationProfileTest {
    @Test
    void uiApiIsTheDefaultTypedProfileAndUiOnlyIsAnExplicitFallback() {
        assertEquals(PracticeShopValidationProfile.UI_API,
                PracticeShopValidationProfile.from("ui-api"));
        assertEquals(PracticeShopValidationProfile.UI_ONLY,
                PracticeShopValidationProfile.from("ui-only"));
        assertEquals(PracticeShopValidationProfile.UI_API,
                PracticeShopValidationProfile.from(""));
        assertEquals(true, PracticeShopValidationProfile.UI_API.apiEvidenceRequired());
        assertEquals(false, PracticeShopValidationProfile.UI_ONLY.apiEvidenceRequired());
    }

    @Test
    void unsupportedProfilesFailBeforeTheBrowserStarts() {
        assertThrows(IllegalArgumentException.class,
                () -> PracticeShopValidationProfile.from("ui-api-and-random-switches"));
    }
}
