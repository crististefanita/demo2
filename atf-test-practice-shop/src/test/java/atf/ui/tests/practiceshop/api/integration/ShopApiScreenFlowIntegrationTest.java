package atf.ui.tests.practiceshop.api.integration;

import atf.ui.practiceshop.api.ShopApiClient;
import atf.ui.practiceshop.api.ShopApiScreenFlow;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.practiceshop.api.ShopPaymentMethod;
import atf.api.testkit.AtfIntegrationTest;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import atf.ui.practiceshop.config.ShopCredentials;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import atf.ui.tests.practiceshop.support.ShopTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One opt-in live API journey; component tests carry the variation matrix. */
@Tag("api")
@AtfIntegrationTest
@ShopTestSupport.RequiresLiveAuthenticatedApi
class ShopApiScreenFlowIntegrationTest {
    private static ShopConfiguration configuration;

    @BeforeAll
    static void loadConfiguration() { configuration = ShopConfigurationLoader.load(); }

    @Test
    void authenticatedLoginPreconditionFeedsEachScreenBoundary() {
        ShopCredentials credentials = ShopCredentials.load(configuration.accountFile());
        try (ShopApiClient client = ShopApiClient.live(configuration.api())) {
            var flow = new ShopApiScreenFlow(client);
            var session = flow.login().establish(credentials);
            var catalog = flow.catalog().openProduct(configuration.productName());
            var cart = flow.cart().prepare(catalog.productId(), configuration.checkout().quantity());
            var checkout = flow.checkout().confirm(
                    configuration.checkout().billing().country(),
                    configuration.checkout().billing().postalCode(),
                    configuration.checkout().billing().houseNumber(),
                    ShopPaymentMethod.from(configuration.checkout().paymentMethod()));

            var detailResponse = catalog.detail().response();
            var cartResponse = cart.read().response();
            var paymentResponse = checkout.payment().response();

            assertTrue(session.authenticated());
            assertTrue(ShopApiHttpStatus.OK.matches(detailResponse.status()));
            assertTrue(ShopApiHttpStatus.OK.matches(cartResponse.status()));
            assertTrue(ShopApiHttpStatus.OK.matches(paymentResponse.status()));
        }
    }
}
