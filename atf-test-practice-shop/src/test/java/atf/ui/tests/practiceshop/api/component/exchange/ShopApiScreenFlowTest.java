package atf.ui.tests.practiceshop.api.component.exchange;

import atf.ui.practiceshop.api.ShopApiClient;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.practiceshop.api.ShopApiJsonField;
import atf.ui.practiceshop.api.ShopApiScreenFlow;
import atf.ui.practiceshop.api.ShopPaymentMethod;
import atf.ui.practiceshop.api.screen.ShopCheckoutApi.AddressLookupPolicy;
import atf.ui.practiceshop.support.ShopJsonEvidence;
import atf.ui.tests.practiceshop.api.support.ShopApiScenarioFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiComponentFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiCallComponentTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies one reusable precondition and four screen boundaries without a browser. */
@ShopApiCallComponentTest
class ShopApiScreenFlowTest {
    @Test
    void loginPreconditionFeedsCatalogCartAndCheckoutScreens() {
        ShopApiClient client = ShopApiComponentFixture.client();
        ShopApiScreenFlow flow = new ShopApiScreenFlow(client);
        String productName = ShopApiScenarioFixture.PRODUCT_NAME;
        String postcode = ShopApiScenarioFixture.POSTCODE;
        String country = ShopApiScenarioFixture.COUNTRY;
        String houseNumber = ShopApiScenarioFixture.HOUSE_NUMBER;
        int quantity = ShopApiScenarioFixture.QUANTITY;

        var session = flow.login().establish(ShopApiScenarioFixture.validCredentials());
        var catalog = flow.catalog().openProduct(productName);
        var cart = flow.cart().prepare(catalog.productId(), quantity);
        var checkout = flow.checkout().confirm(country, postcode, houseNumber,
                ShopPaymentMethod.CASH_ON_DELIVERY);

        assertTrue(session.authenticated());
        assertEquals(ShopApiScenarioFixture.PRODUCT_ID, catalog.productId());
        assertEquals(ShopApiScenarioFixture.CART_ID, cart.cartId());
        var cartResponse = cart.read().response();
        var paymentResponse = checkout.payment().response();
        var addressResponse = checkout.addressLookup().response();
        var addressBody = ShopJsonEvidence.parse(addressResponse.body());

        assertTrue(ShopApiHttpStatus.OK.matches(cartResponse.status()));
        assertTrue(ShopApiHttpStatus.OK.matches(paymentResponse.status()));
        assertEquals(AddressLookupPolicy.ADVISORY, checkout.addressLookupPolicy());
        assertEquals(postcode, checkout.requestedAddress().postcode());
        assertNotNull(addressBody);
        assertEquals(postcode, addressBody.path(ShopApiJsonField.POSTCODE.wireValue()).asText());
    }
}
