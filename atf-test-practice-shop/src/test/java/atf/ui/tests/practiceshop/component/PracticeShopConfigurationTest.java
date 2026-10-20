package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopApiConfiguration;
import atf.ui.practiceshop.config.PracticeShopApiPrecondition;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopConfigurationTest {
    @Test
    void yamlDefinesTheExternalJourneyWithoutAnInRepoSecret() {
        var configuration = PracticeShopConfigurationLoader.load();

        assertTrue(configuration.baseUrl().isAbsolute());
        assertTrue(configuration.baseUrl().getHost().endsWith("practicesoftwaretesting.com"));
        assertEquals("Pliers", configuration.productName());
        assertTrue(configuration.usesDynamicProductPath());
        assertTrue(configuration.matchesProductPath(PracticeShopUiRoutes.product("current-external-fixture")));
        assertFalse(configuration.matchesProductPath(PracticeShopUiRoutes.category("pliers")));
        assertEquals(2, configuration.checkout().quantity());
        assertEquals("Cash on Delivery", configuration.checkout().paymentMethod());
        assertEquals("Romania", configuration.checkout().billing().country());
        assertEquals(70, configuration.browser().zoomPercent());
        assertTrue(configuration.browser().screenshotOnFailure());
        assertEquals(PracticeShopValidationProfile.UI_API, configuration.validationProfile());
        assertTrue(configuration.api().authenticatedPrecondition());
        assertTrue(configuration.uiReportDirectory().toString().endsWith("artifacts\\reports\\ui"));
        assertTrue(configuration.apiReportDirectory().toString().endsWith("artifacts\\reports"));
        assertFalse(configuration.authenticatedE2eAllowed());
        // The default target path is intentionally absent until a local/CI account is injected.
        assertTrue(configuration.accountFile().toString().contains("practice-software-testing-account.properties"));
    }

    @Test
    void resolvesRelativeUiRoutesFromTheYamlBaseUrl() {
        var configuration = PracticeShopConfigurationLoader.load();

        assertEquals(configuration.baseUrl().resolve("product/1"),
                configuration.uri(PracticeShopUiRoutes.product("1")));
        assertEquals(configuration.baseUrl().resolve("checkout"),
                configuration.uri(PracticeShopUiRoutes.CHECKOUT));
    }

    @Test
    void authenticatedRunCannotDeclareAnAnonymousApiPrecondition() {
        assertThrows(IllegalArgumentException.class, () -> new atf.ui.practiceshop.config.PracticeShopConfiguration(
                configuration().baseUrl(), configuration().accountFile(), configuration().productName(),
                configuration().productPath(), configuration().checkout(), configuration().browser(),
                configuration().validationProfile(),
                new PracticeShopApiConfiguration(configuration().api().endpoint(),
                        configuration().api().mode(), PracticeShopApiPrecondition.ANONYMOUS,
                        configuration().api().timeout()),
                configuration().uiReportDirectory(), configuration().apiReportDirectory(), true));
    }

    private static atf.ui.practiceshop.config.PracticeShopConfiguration configuration() {
        return PracticeShopConfigurationLoader.load();
    }
}


