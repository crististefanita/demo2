package atf.ui.tests.practiceshop.api.integration;

import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiScreenFlow;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.api.testkit.AtfIntegrationTest;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** One opt-in live API journey; component tests carry the variation matrix. */
@Tag("api")
@AtfIntegrationTest
@EnabledIf("isLiveConfigured")
class PracticeShopApiScreenFlowIntegrationTest {
    private static PracticeShopConfiguration configuration;
    private PracticeShopApiClient client;

    @BeforeAll
    static void loadConfiguration() { configuration = PracticeShopConfigurationLoader.load(); }

    static boolean isLiveConfigured() {
        PracticeShopConfiguration value = PracticeShopConfigurationLoader.load();
        return value.api().mode() == atf.api.core.config.ApiTransportMode.LIVE
                && value.accountConfigured();
    }

    @AfterEach
    void closeClient() { client = null; }

    @Test
    void authenticatedLoginPreconditionFeedsEachScreenBoundary() {
        PracticeShopCredentials credentials = PracticeShopCredentials.load(configuration.accountFile());
        client = PracticeShopApiClient.live(configuration.api());
        var flow = new PracticeShopApiScreenFlow(client);
        var session = flow.login().establish(credentials);
        var catalog = flow.catalog().openProduct(configuration.productName());
        var cart = flow.cart().prepare(catalog.productId(), configuration.checkout().quantity());
        var checkout = flow.checkout().confirm(
                "RO", configuration.checkout().billing().postalCode(),
                configuration.checkout().billing().houseNumber(),
                PracticeShopPaymentMethod.from(configuration.checkout().paymentMethod()));

        assertEquals(true, session.authenticated());
        assertEquals(200, catalog.detail().response().status());
        assertEquals(200, cart.read().response().status());
        assertEquals(200, checkout.payment().response().status());
    }
}
