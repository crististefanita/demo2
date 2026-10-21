package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopApiConfiguration;
import atf.ui.practiceshop.config.PracticeShopApiPrecondition;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

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
        assertEquals(1, configuration.uiRetry().maxAttempts());
        assertFalse(configuration.uiRetry().enabled());
        assertFalse(configuration.browser().retryPolicy().enabled());
        assertEquals(PracticeShopValidationProfile.UI_API, configuration.validationProfile());
        assertTrue(configuration.api().authenticatedPrecondition());
        assertEquals(4, configuration.api().evidenceBinding().checkpoints().size());
        assertTrue(configuration.api().evidenceBinding().includes(PracticeShopApiCheckpoint.PRODUCT));
        assertTrue(configuration.api().evidenceBinding().includes(PracticeShopApiCheckpoint.PAYMENT));
        assertTrue(configuration.uiReportDirectory().toString().endsWith("artifacts\\reports\\ui"));
        assertTrue(configuration.apiReportDirectory().toString().endsWith("artifacts\\reports\\api"));
        assertFalse(configuration.uiReportDirectory().equals(configuration.apiReportDirectory()));
        assertFalse(configuration.authenticatedE2eAllowed());
        assertFalse(configuration.accountConfigured(),
                "a tracked example.com fixture must never enable an authenticated live run");
        // The external account path remains configurable without committing a real account.
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

    @Test
    void authenticatedGateRequiresReadableNonReservedAccountCandidate(@TempDir Path temp) throws Exception {
        var base = configuration();
        Path example = temp.resolve("example.properties");
        Files.writeString(example, "email=customer@example.com\npassword=secret\n");
        Path externalCandidate = temp.resolve("external.properties");
        Files.writeString(externalCandidate,
                "email=customer@practicesoftwaretesting.com\npassword=secret\n");

        assertFalse(withAccountFile(base, example).authenticatedE2eAllowed());
        assertTrue(withAccountFile(base, externalCandidate).authenticatedE2eAllowed());
    }

    private static atf.ui.practiceshop.config.PracticeShopConfiguration configuration() {
        return PracticeShopConfigurationLoader.load();
    }

    private static atf.ui.practiceshop.config.PracticeShopConfiguration withAccountFile(
            atf.ui.practiceshop.config.PracticeShopConfiguration base, Path accountFile) {
        return new atf.ui.practiceshop.config.PracticeShopConfiguration(base.baseUrl(), accountFile,
                base.productName(), base.productPath(), base.checkout(), base.browser(), base.uiRetry(),
                base.validationProfile(), base.api(), base.uiReportDirectory(), base.apiReportDirectory(), true);
    }
}


