package atf.ui.tests.practiceshop.component.configuration;

import atf.api.testkit.AtfComponentTest;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.ui.practiceshop.config.model.ShopAccountFixturePolicy;
import atf.ui.practiceshop.config.api.ShopApiCheckpoint;
import atf.ui.practiceshop.config.api.ShopApiConfiguration;
import atf.ui.practiceshop.config.api.ShopApiPrecondition;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import atf.ui.practiceshop.config.ShopReportConfiguration;
import atf.ui.practiceshop.config.ShopValidationProfile;
import atf.ui.practiceshop.routes.ShopUiRoutes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopConfigurationTest {
    @Test
    void yamlDefinesOwnedJourneyAndSyntheticFixtureInSourceResources() {
        var configuration = ShopConfigurationLoader.load();

        assertTrue(configuration.baseUrl().isAbsolute());
        assertTrue(configuration.baseUrl().getHost().endsWith("practicesoftwaretesting.com"));
        assertEquals("Pliers", configuration.productName());
        assertTrue(configuration.usesDynamicProductPath());
        assertTrue(configuration.matchesProductPath(ShopUiRoutes.product("current-external-fixture")));
        assertFalse(configuration.matchesProductPath(ShopUiRoutes.category("pliers")));
        assertEquals(2, configuration.checkout().quantity());
        assertEquals("Cash on Delivery", configuration.checkout().paymentMethod());
        assertEquals("United States of America (the)", configuration.checkout().billing().country());
        assertEquals("53703", configuration.checkout().billing().postalCode());
        assertEquals("Wayne Lock", configuration.checkout().billing().street());
        assertEquals("Cartwrightmouth", configuration.checkout().billing().city());
        assertEquals("Alaska", configuration.checkout().billing().state());
        assertEquals(70, configuration.browser().zoomPercent());
        assertTrue(configuration.browser().screenshotOnFailure());
        assertEquals(1, configuration.uiRetry().maxAttempts());
        assertFalse(configuration.uiRetry().enabled());
        assertFalse(configuration.browser().retryPolicy().enabled());
        assertEquals(ShopValidationProfile.UI_API, configuration.validationProfile());
        assertTrue(configuration.api().authenticatedPrecondition());
        assertEquals(6, configuration.api().evidenceBinding().checkpoints().size());
        assertTrue(configuration.api().evidenceBinding().includes(ShopApiCheckpoint.PRODUCT));
        assertTrue(configuration.api().evidenceBinding().includes(ShopApiCheckpoint.PAYMENT));
        assertTrue(configuration.api().evidenceBinding().includes(ShopApiCheckpoint.ORDER));
        assertTrue(configuration.uiReportDirectory().toString().endsWith("artifacts\\reports\\ui"));
        assertTrue(configuration.apiReportDirectory().toString().endsWith("artifacts\\reports\\api"));
        assertFalse(configuration.uiReportDirectory().equals(configuration.apiReportDirectory()));
        assertFalse(configuration.authenticatedE2eEnabled());
        assertFalse(configuration.authenticatedE2eAllowed());
        assertEquals(ShopAccountFixturePolicy.SOURCE_TEST_RESOURCE, configuration.accountPolicy());
        assertTrue(configuration.accountPolicy().expectedEvidence().contains("registered local account"));
        assertTrue(configuration.accountConfigured());
        assertEquals(ShopReportConfiguration.DEFAULT_SYSTEM_BODY_PREVIEW_CHARACTERS,
                configuration.reporting().systemBodyPreviewCharacters());
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT,
                configuration.reporting().screenshotPolicy());
        assertEquals("resources", configuration.accountFile().getParent().getFileName().toString(),
                "the authenticated fixture is intentionally owned by test resources");
        assertEquals("practice-software-testing-account.properties",
                configuration.accountFile().getFileName().toString());
    }

    @Test
    void resolvesRelativeUiRoutesFromTheYamlBaseUrl() {
        var configuration = ShopConfigurationLoader.load();

        assertEquals(configuration.baseUrl().resolve("product/1"),
                configuration.uri(ShopUiRoutes.product("1")));
        assertEquals(configuration.baseUrl().resolve("checkout"),
                configuration.uri(ShopUiRoutes.CHECKOUT));
    }

    @Test
    void explicitYamlRetainsTheExactProductNameInsteadOfReplacingItWithTheDefault(@TempDir Path temp)
            throws Exception {
        Path yaml = temp.resolve("practice-shop.yaml");
        for (String product : List.of("Pliers", "Combination Pliers")) {
            Files.writeString(yaml, """
                    practiceShop:
                      baseUrl: https://practicesoftwaretesting.com
                      productName: "%s"
                    """.formatted(product));
            assertEquals(product, ShopConfigurationLoader.load(yaml).productName());
        }
    }

    @Test
    void uiOnlyHarnessProfileKeepsRuntimeConfigurationButDisablesApiEvidence() {
        var configured = ShopConfigurationLoader.load();
        var uiOnly = configured.withValidationProfile(ShopValidationProfile.UI_ONLY);

        assertEquals(ShopValidationProfile.UI_ONLY, uiOnly.validationProfile());
        assertTrue(uiOnly.api().evidenceBinding().checkpoints().isEmpty());
        assertEquals(configured.baseUrl(), uiOnly.baseUrl());
        assertEquals(configured.productName(), uiOnly.productName());
        assertEquals(configured.checkout(), uiOnly.checkout());
        assertEquals(configured.browser(), uiOnly.browser());
        assertEquals(configured.uiReportDirectory(), uiOnly.uiReportDirectory());
    }

    @Test
    void authenticatedRunCannotDeclareAnAnonymousApiPrecondition() {
        assertThrows(IllegalArgumentException.class, () -> new atf.ui.practiceshop.config.ShopConfiguration(
                configuration().baseUrl(), configuration().accountFile(), configuration().productName(),
                configuration().productPath(), configuration().checkout(), configuration().browser(),
                configuration().validationProfile(),
                new ShopApiConfiguration(configuration().api().endpoint(),
                        configuration().api().mode(), ShopApiPrecondition.ANONYMOUS,
                        configuration().api().timeout()),
                configuration().uiReportDirectory(), configuration().apiReportDirectory(), true));
    }

    @Test
    void annotationStyleAuthProfilePromotesYamlToHeadedLiveE2e(@TempDir Path temp) throws Exception {
        var base = configuration();
        Path sourceFixture = temp.resolve("src/test/resources/account.properties");
        Files.createDirectories(sourceFixture.getParent());
        Files.writeString(sourceFixture,
                "email=customer@practicesoftwaretesting.com\npassword=secret\n");

        var configured = base.forAuthenticatedE2e(sourceFixture, false, 70);

        assertTrue(configured.authenticatedE2eEnabled());
        assertTrue(configured.authenticatedE2eAllowed());
        assertEquals(atf.api.core.config.ApiTransportMode.LIVE, configured.api().mode());
        assertEquals(ShopApiPrecondition.AUTHENTICATED, configured.api().precondition());
        assertFalse(configured.browser().headless());
        assertEquals(70, configured.browser().zoomPercent());
    }

    @Test
    void authenticatedGateRequiresReadableSourceFixture(@TempDir Path temp) throws Exception {
        var base = configuration();
        Path nonFixture = temp.resolve("external.properties");
        Files.writeString(nonFixture,
                "email=customer@practicesoftwaretesting.com\npassword=secret\n");
        Path sourceFixture = temp.resolve("src/test/resources/account.properties");
        Files.createDirectories(sourceFixture.getParent());
        Files.writeString(sourceFixture,
                "email=customer@practicesoftwaretesting.com\npassword=secret\n");

        assertFalse(withAccountFile(base, nonFixture).authenticatedE2eAllowed());
        assertTrue(withAccountFile(base, sourceFixture).authenticatedE2eAllowed());
    }

    @Test
    void rejectsAnExternalAccountOverride(@TempDir Path temp) {
        String property = "atf.practice.account-file";
        String previous = System.getProperty(property);
        System.setProperty(property, temp.resolve("external.properties").toString());
        try {
            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    ShopConfigurationLoader::load);
            assertTrue(failure.getCause() instanceof IllegalArgumentException);
            assertTrue(failure.getCause().getMessage().contains("src/test/resources"));
        } finally {
            if (previous == null) System.clearProperty(property);
            else System.setProperty(property, previous);
        }
    }

    @Test
    void directConfigurationAcceptsTheOwnedSourceResourceFixture(@TempDir Path temp) throws Exception {
        var base = configuration();
        Path sourceFixture = temp.resolve("src/test/resources/account.properties");
        Files.createDirectories(sourceFixture.getParent());
        Files.writeString(sourceFixture,
                "email=customer@practicesoftwaretesting.com\npassword=secret\n");

        var configured = withAccountFile(base, sourceFixture);

        assertTrue(configured.accountConfigured(),
                "the typed direct configuration must honor the same source fixture policy");
        assertTrue(configured.authenticatedE2eAllowed());
    }

    @Test
    void rejectsAnUnboundedSystemPreviewPolicy() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShopReportConfiguration(0));
    }

    private static atf.ui.practiceshop.config.ShopConfiguration configuration() {
        return ShopConfigurationLoader.load();
    }

    private static atf.ui.practiceshop.config.ShopConfiguration withAccountFile(
            atf.ui.practiceshop.config.ShopConfiguration base, Path accountFile) {
        return new atf.ui.practiceshop.config.ShopConfiguration(base.baseUrl(), accountFile,
                base.productName(), base.productPath(), base.checkout(), base.browser(), base.uiRetry(),
                base.validationProfile(), base.api(), base.uiReportDirectory(), base.apiReportDirectory(), true);
    }
}
