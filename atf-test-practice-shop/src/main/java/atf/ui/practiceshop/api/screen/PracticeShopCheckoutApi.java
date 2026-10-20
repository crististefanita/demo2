package atf.ui.practiceshop.api.screen;

import atf.api.core.http.response.ApiExecutionResult;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;

/** Checkout-screen API boundary: advisory postcode lookup followed by payment. */
public final class PracticeShopCheckoutApi {
    private final PracticeShopApiClient client;

    public PracticeShopCheckoutApi(PracticeShopApiClient client) { this.client = client; }

    public CheckoutScreen confirm(String country, String postcode, String houseNumber,
                                  PracticeShopPaymentMethod method) {
        ApiExecutionResult address = client.execute(
                client.requests().postcodeLookup(country, postcode, houseNumber));
        ApiExecutionResult payment = client.execute(client.requests().payment(method));
        return new CheckoutScreen(address, payment, method,
                new RequestedAddress(country, postcode, houseNumber), AddressLookupPolicy.ADVISORY);
    }

    public record CheckoutScreen(ApiExecutionResult addressLookup,
                                 ApiExecutionResult payment,
                                 PracticeShopPaymentMethod method,
                                 RequestedAddress requestedAddress,
                                 AddressLookupPolicy addressLookupPolicy) { }

    public record RequestedAddress(String country, String postcode, String houseNumber) { }

    /** The lookup may enrich a form, but it never overwrites submitted billing values. */
    public enum AddressLookupPolicy { ADVISORY }
}
