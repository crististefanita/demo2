package atf.ui.tests.practiceshop.api.integration;

import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiScreenFlow;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.api.testkit.AtfIntegrationTest;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import atf.ui.tests.practiceshop.support.PracticeShopTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One opt-in live API journey; component tests carry the variation matrix. */
@Tag("api")
@AtfIntegrationTest
@PracticeShopTestSupport.RequiresLiveAuthenticatedApi
class PracticeShopApiScreenFlowIntegrationTest {
    private static PracticeShopConfiguration configuration;

    @BeforeAll
    static void loadConfiguration() { configuration = PracticeShopConfigurationLoader.load(); }

    @Test
    void authenticatedLoginPreconditionFeedsEachScreenBoundary() {
        PracticeShopCredentials credentials = PracticeShopCredentials.load(configuration.accountFile());
        try (PracticeShopApiClient client = PracticeShopApiClient.live(configuration.api())) {
            var flow = new PracticeShopApiScreenFlow(client);
            var session = flow.login().establish(credentials);
            var catalog = flow.catalog().openProduct(configuration.productName());
            var cart = flow.cart().prepare(catalog.productId(), configuration.checkout().quantity());
            var checkout = flow.checkout().confirm(
                    configuration.checkout().billing().country(),
                    configuration.checkout().billing().postalCode(),
                    configuration.checkout().billing().houseNumber(),
                    PracticeShopPaymentMethod.from(configuration.checkout().paymentMethod()));

            var detailResponse = catalog.detail().response();
            var cartResponse = cart.read().response();
            var paymentResponse = checkout.payment().response();

            assertTrue(session.authenticated());
            assertTrue(PracticeShopApiHttpStatus.OK.matches(detailResponse.status()));
            assertTrue(PracticeShopApiHttpStatus.OK.matches(cartResponse.status()));
            assertTrue(PracticeShopApiHttpStatus.OK.matches(paymentResponse.status()));
        }
    }
}
