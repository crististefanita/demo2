package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiScreenFlow;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies one reusable precondition and four screen boundaries without a browser. */
@AtfComponentTest
class PracticeShopApiScreenFlowTest {
    @Test
    void loginPreconditionFeedsCatalogCartAndCheckoutScreens() {
        PracticeShopApiClient client = PracticeShopApiClient.forTransport(
                URI.create("https://api.practicesoftwaretesting.com"), transport());
        PracticeShopApiScreenFlow flow = new PracticeShopApiScreenFlow(client);

        var session = flow.login().establish(new PracticeShopCredentials(
                "atf@example.test", "secret", "ATF", "Practice"));
        var catalog = flow.catalog().openProduct("Combination Pliers");
        var cart = flow.cart().prepare(catalog.productId(), 2);
        var checkout = flow.checkout().confirm("RO", "010101", "10",
                PracticeShopPaymentMethod.CASH_ON_DELIVERY);

        assertTrue(session.authenticated());
        assertEquals("01product", catalog.productId());
        assertEquals("01cart", cart.cartId());
        assertEquals(200, cart.read().response().status());
        assertEquals(200, checkout.payment().response().status());
        assertEquals(atf.ui.practiceshop.api.screen.PracticeShopCheckoutApi.AddressLookupPolicy.ADVISORY,
                checkout.addressLookupPolicy());
        assertEquals("010101", checkout.requestedAddress().postcode());
        assertTrue(checkout.addressLookup().response().body().contains("010101"));
    }

    private static ApiTransport transport() {
        return (request, url, auth) -> {
            String body;
            int status = 200;
            switch (request.path()) {
                case "/users/login" -> body = "{\"access_token\":\"token-1\"}";
                case "/products/search" -> body = "{\"data\":[{\"id\":\"01product\",\"name\":\"Combination Pliers\"}]}";
                case "/products/01product" -> body = "{\"id\":\"01product\",\"name\":\"Combination Pliers\"}";
                case "/carts" -> body = "{\"id\":\"01cart\"}";
                case "/carts/01cart" -> body = request.method().equals("GET")
                        ? "{\"cart_items\":[{\"product_id\":\"01product\",\"quantity\":2}]}"
                        : "{\"result\":\"item added or updated\"}";
                case "/postcode-lookup" -> body = "{\"country\":\"RO\",\"postcode\":\"010101\"}";
                case "/payment/check" -> body = "{\"message\":\"Payment was successful\"}";
                default -> { status = 500; body = "{\"message\":\"unexpected route\"}"; }
            }
            return CompletableFuture.completedFuture(new ApiResponse(status, body,
                    Map.of("content-type", "application/json"), Duration.ZERO, url));
        };
    }
}
