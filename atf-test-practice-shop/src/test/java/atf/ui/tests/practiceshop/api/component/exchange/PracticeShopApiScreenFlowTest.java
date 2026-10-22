package atf.ui.tests.practiceshop.api.component.exchange;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopApiJsonField;
import atf.ui.practiceshop.api.PracticeShopApiScreenFlow;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.practiceshop.api.screen.PracticeShopCheckoutApi.AddressLookupPolicy;
import atf.ui.practiceshop.support.PracticeShopJsonEvidence;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiComponentFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies one reusable precondition and four screen boundaries without a browser. */
@AtfComponentTest
class PracticeShopApiScreenFlowTest {
    @Test
    void loginPreconditionFeedsCatalogCartAndCheckoutScreens() {
        PracticeShopApiClient client = PracticeShopApiComponentFixture.client();
        PracticeShopApiScreenFlow flow = new PracticeShopApiScreenFlow(client);
        String productName = PracticeShopApiScenarioFixture.PRODUCT_NAME;
        String postcode = PracticeShopApiScenarioFixture.POSTCODE;
        String country = PracticeShopApiScenarioFixture.COUNTRY;
        String houseNumber = PracticeShopApiScenarioFixture.HOUSE_NUMBER;
        int quantity = PracticeShopApiScenarioFixture.QUANTITY;

        var session = flow.login().establish(PracticeShopApiScenarioFixture.validCredentials());
        var catalog = flow.catalog().openProduct(productName);
        var cart = flow.cart().prepare(catalog.productId(), quantity);
        var checkout = flow.checkout().confirm(country, postcode, houseNumber,
                PracticeShopPaymentMethod.CASH_ON_DELIVERY);

        assertTrue(session.authenticated());
        assertEquals(PracticeShopApiScenarioFixture.PRODUCT_ID, catalog.productId());
        assertEquals(PracticeShopApiScenarioFixture.CART_ID, cart.cartId());
        var cartResponse = cart.read().response();
        var paymentResponse = checkout.payment().response();
        var addressResponse = checkout.addressLookup().response();
        var addressBody = PracticeShopJsonEvidence.parse(addressResponse.body());

        assertTrue(PracticeShopApiHttpStatus.OK.matches(cartResponse.status()));
        assertTrue(PracticeShopApiHttpStatus.OK.matches(paymentResponse.status()));
        assertEquals(AddressLookupPolicy.ADVISORY, checkout.addressLookupPolicy());
        assertEquals(postcode, checkout.requestedAddress().postcode());
        assertNotNull(addressBody);
        assertEquals(postcode, addressBody.path(PracticeShopApiJsonField.POSTCODE.wireValue()).asText());
    }
}
