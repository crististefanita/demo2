package atf.ui.tests.practiceshop.api.component.exchange;

import atf.api.core.http.request.HttpMethod;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.ShopApiOperation;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.practiceshop.api.ShopApiRoutes;
import atf.ui.practiceshop.support.ShopApiAssertions;
import atf.ui.practiceshop.support.ShopApiNetworkJournal;
import atf.ui.tests.practiceshop.api.support.ShopApiScenarioFixture;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopApiAssertionsTest {
    @Test
    void nearMissProductIdentityFailsPathAssertions() {
        var search = exchange(ShopApiOperation.SEARCH.wireValue(), ShopApiRoutes.PRODUCT_SEARCH,
                ShopApiHttpStatus.OK,
                "{\"q\":\"Pliers\"}",
                "{\"data\":[{\"id\":\"p-1\",\"name\":\"Pliers\"}]}");
        var detail = exchange(HttpMethod.GET.value(), ShopApiRoutes.product("p-1"),
                ShopApiHttpStatus.OK, "",
                "{\"id\":\"p-1\",\"name\":\"Pliers near miss\"}");

        var assertions = ShopApiAssertions.product(search, detail, "Pliers", "p-1");

        assertTrue(assertions.stream().anyMatch(a -> a.name().contains("catalog search") && a.passed()));
        assertFalse(assertions.stream().anyMatch(a -> a.name().contains("product identity") && a.passed()));
    }

    @Test
    void catalogQueryUsesSharedUriDecoderForPlusAndPercentEncodedValues() {
        var search = new ShopApiNetworkJournal.Exchange(
                "GET", ShopApiRoutes.PRODUCT_SEARCH,
                ShopApiScenarioFixture.API_ENDPOINT + ShopApiRoutes.PRODUCT_SEARCH
                        + "?q=Pliers+Pro&unused=a%2Bb",
                ShopApiHttpStatus.OK.code(), "",
                "{\"data\":[{\"id\":\"p-1\",\"name\":\"Pliers Pro\"}]}",
                Map.of(), Map.of("content-type", atf.api.core.http.request.MediaTypes.APPLICATION_JSON));
        var detail = exchange(HttpMethod.GET.value(), ShopApiRoutes.product("p-1"),
                ShopApiHttpStatus.OK, "", "{\"id\":\"p-1\",\"name\":\"Pliers Pro\"}");

        var assertions = ShopApiAssertions.product(search, detail, "Pliers Pro", "p-1");

        assertTrue(assertions.stream().filter(a -> a.name().equals("API request query · catalog search"))
                .findFirst().orElseThrow().passed());
    }

    @Test
    void unauthorizedCatalogSearchFailsThePreconditionInsteadOfPassingByShape() {
        var search = exchange(ShopApiOperation.SEARCH.wireValue(), ShopApiRoutes.PRODUCT_SEARCH,
                ShopApiHttpStatus.UNAUTHORIZED,
                "", "{\"message\":\"Unauthorized\"}");
        var detail = exchange(HttpMethod.GET.value(), ShopApiRoutes.product("p-1"),
                ShopApiHttpStatus.OK, "", "{\"id\":\"p-1\",\"name\":\"Pliers\"}");

        var assertions = ShopApiAssertions.product(search, detail, "Pliers", "p-1");

        assertFalse(assertions.stream().anyMatch(a ->
                a.name().equals("API precondition · catalog search") && a.passed()));
    }

    @Test
    void loginDoesNotPassWhenTheObservedJsonTransportIsInvalid() {
        var login = exchange(HttpMethod.POST.value(), ShopApiRoutes.LOGIN,
                ShopApiHttpStatus.OK,
                "{\"email\":\"shop@example.test\",\"password\":\"secret\"}",
                "{}",
                Map.of("content-type", atf.api.core.http.request.MediaTypes.APPLICATION_JSON,
                        "content-length", "0"));

        var assertions = ShopApiAssertions.login(login);

        assertFalse(assertions.stream().anyMatch(a ->
                a.name().equals("API login response JSON") && a.passed()));
    }

    @Test
    void orderRequiresServerInvoiceToMatchTheVisibleOrderConfirmation() {
        var order = exchange(HttpMethod.POST.value(), ShopApiRoutes.GUEST_INVOICE_CREATE,
                ShopApiHttpStatus.OK, "{}",
                "{\"id\":\"order-1\",\"invoice_number\":\"INV-2026000007\",\"total\":24.02}");

        var passing = ShopApiAssertions.order(order,
                "Thanks for your order! Your invoice number is INV-2026000007.");
        var mismatched = ShopApiAssertions.order(order,
                "Thanks for your order! Your invoice number is INV-OTHER.");

        assertTrue(passing.stream().allMatch(a -> a.passed()));
        assertFalse(mismatched.stream().filter(a -> a.name().equals("UI/API order correlation"))
                .findFirst().orElseThrow().passed());
    }

    private static ShopApiNetworkJournal.Exchange exchange(String method, String path,
                                                                     ShopApiHttpStatus status,
                                                                     String requestBody, String responseBody) {
        return exchange(method, path, status, requestBody, responseBody,
                Map.of("content-type", atf.api.core.http.request.MediaTypes.APPLICATION_JSON));
    }

    private static ShopApiNetworkJournal.Exchange exchange(String method, String path,
                                                                     ShopApiHttpStatus status,
                                                                     String requestBody, String responseBody,
                                                                     Map<String, String> responseHeaders) {
        return new ShopApiNetworkJournal.Exchange(method, path,
                ShopApiScenarioFixture.API_ENDPOINT + path, status.code(),
                requestBody, responseBody, Map.of(), responseHeaders);
    }
}
