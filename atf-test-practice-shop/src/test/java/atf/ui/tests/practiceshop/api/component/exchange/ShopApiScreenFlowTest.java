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
import atf.ui.tests.practiceshop.api.support.ShopApiCallAuditContextTestSupport;
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

        ShopApiCallAuditContextTestSupport.expectNext("login establishes a valid session",
                ShopApiHttpStatus.OK);
        ShopApiCallAuditContextTestSupport.expectNext("catalog search returns the configured product",
                ShopApiHttpStatus.OK);
        ShopApiCallAuditContextTestSupport.expectNext("catalog detail returns the selected product",
                ShopApiHttpStatus.OK);
        ShopApiCallAuditContextTestSupport.expectNext("cart creation returns a new cart",
                ShopApiHttpStatus.CREATED);
        ShopApiCallAuditContextTestSupport.expectNext("cart add accepts the configured product",
                ShopApiHttpStatus.OK);
        ShopApiCallAuditContextTestSupport.expectNext("cart read returns the prepared cart",
                ShopApiHttpStatus.OK);
        ShopApiCallAuditContextTestSupport.expectNext("postcode lookup returns address details",
                ShopApiHttpStatus.OK);
        ShopApiCallAuditContextTestSupport.expectNext("payment check confirms the selected method",
                ShopApiHttpStatus.OK);

        var session = flow.login().establish(ShopApiScenarioFixture.validCredentials());
        var catalog = flow.catalog().openProduct(productName);
        var cart = flow.cart().prepare(catalog.productId(), quantity);
        var checkout = flow.checkout().confirm(country, postcode, houseNumber,
                ShopPaymentMethod.CASH_ON_DELIVERY);

        assertTrue(session.authenticated());
        assertTrue(ShopApiHttpStatus.OK.matches(session.loginResult().response().status()));
        assertEquals(ShopApiScenarioFixture.PRODUCT_ID, catalog.productId());
        assertTrue(ShopApiHttpStatus.OK.matches(catalog.search().response().status()));
        assertTrue(ShopApiHttpStatus.OK.matches(catalog.detail().response().status()));
        assertEquals(ShopApiScenarioFixture.CART_ID, cart.cartId());
        assertTrue(ShopApiHttpStatus.CREATED.matches(cart.create().response().status()));
        assertTrue(ShopApiHttpStatus.OK.matches(cart.add().response().status()));
        var cartResponse = cart.read().response();
        var paymentResponse = checkout.payment().response();
        var addressResponse = checkout.addressLookup().response();
        var addressBody = ShopJsonEvidence.parse(addressResponse.body());

        assertTrue(ShopApiHttpStatus.OK.matches(cartResponse.status()));
        assertTrue(ShopApiHttpStatus.OK.matches(paymentResponse.status()));
        assertTrue(ShopApiHttpStatus.OK.matches(addressResponse.status()));
        assertEquals(AddressLookupPolicy.ADVISORY, checkout.addressLookupPolicy());
        assertEquals(postcode, checkout.requestedAddress().postcode());
        assertNotNull(addressBody);
        assertEquals(postcode, addressBody.path(ShopApiJsonField.POSTCODE.wireValue()).asText());
    }
}
