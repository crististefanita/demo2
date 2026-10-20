package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopConfigurationTest {
    @Test
    void yamlDefinesTheExternalJourneyWithoutAnInRepoSecret() {
        var configuration = PracticeShopConfigurationLoader.load();

        assertEquals("https://practicesoftwaretesting.com", configuration.baseUrl().toString());
        assertEquals("Pliers", configuration.productName());
        assertTrue(configuration.usesDynamicProductPath());
        assertTrue(configuration.matchesProductPath("/product/current-external-fixture"));
        assertFalse(configuration.matchesProductPath("/categories/pliers"));
        assertEquals(2, configuration.checkout().quantity());
        assertEquals("Cash on Delivery", configuration.checkout().paymentMethod());
        assertEquals("Romania", configuration.checkout().billing().country());
        assertEquals(70, configuration.browser().zoomPercent());
        assertEquals(PracticeShopValidationProfile.UI_API, configuration.validationProfile());
        // The default target path is intentionally absent until a local/CI account is injected.
        assertTrue(configuration.accountFile().toString().contains("practice-software-testing-account.properties"));
    }
}


