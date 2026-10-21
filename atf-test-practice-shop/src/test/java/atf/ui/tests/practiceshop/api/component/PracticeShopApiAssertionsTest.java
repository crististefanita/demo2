package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.request.HttpMethod;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiOperation;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.support.PracticeShopApiAssertions;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopApiAssertionsTest {
    @Test
    void nearMissProductIdentityFailsPathAssertions() {
        var search = exchange(PracticeShopApiOperation.SEARCH.wireValue(), PracticeShopApiRoutes.PRODUCT_SEARCH,
                PracticeShopApiHttpStatus.OK,
                "{\"q\":\"Pliers\"}",
                "{\"data\":[{\"id\":\"p-1\",\"name\":\"Pliers\"}]}");
        var detail = exchange(HttpMethod.GET.value(), PracticeShopApiRoutes.product("p-1"),
                PracticeShopApiHttpStatus.OK, "",
                "{\"id\":\"p-1\",\"name\":\"Pliers near miss\"}");

        var assertions = PracticeShopApiAssertions.product(search, detail, "Pliers", "p-1");

        assertTrue(assertions.stream().anyMatch(a -> a.name().contains("catalog search") && a.passed()));
        assertFalse(assertions.stream().anyMatch(a -> a.name().contains("product identity") && a.passed()));
    }

    @Test
    void unauthorizedCatalogSearchFailsThePreconditionInsteadOfPassingByShape() {
        var search = exchange(PracticeShopApiOperation.SEARCH.wireValue(), PracticeShopApiRoutes.PRODUCT_SEARCH,
                PracticeShopApiHttpStatus.UNAUTHORIZED,
                "", "{\"message\":\"Unauthorized\"}");
        var detail = exchange(HttpMethod.GET.value(), PracticeShopApiRoutes.product("p-1"),
                PracticeShopApiHttpStatus.OK, "", "{\"id\":\"p-1\",\"name\":\"Pliers\"}");

        var assertions = PracticeShopApiAssertions.product(search, detail, "Pliers", "p-1");

        assertFalse(assertions.stream().anyMatch(a ->
                a.name().equals("API precondition · catalog search") && a.passed()));
    }

    @Test
    void loginWithoutCurrentUserDoesNotPassWhenTheObservedJsonTransportIsInvalid() {
        var login = exchange(HttpMethod.POST.value(), PracticeShopApiRoutes.LOGIN,
                PracticeShopApiHttpStatus.OK,
                "{\"email\":\"shop@example.test\",\"password\":\"secret\"}",
                "{}",
                Map.of("content-type", atf.api.core.http.request.MediaTypes.APPLICATION_JSON,
                        "content-length", "0"));

        var assertions = PracticeShopApiAssertions.login(login, null, "Practice Shop User");

        assertFalse(assertions.stream().anyMatch(a ->
                a.name().equals("API session establishment") && a.passed()));
    }

    @Test
    void orderRequiresServerInvoiceToMatchTheVisibleOrderConfirmation() {
        var order = exchange(HttpMethod.POST.value(), PracticeShopApiRoutes.GUEST_INVOICE_CREATE,
                PracticeShopApiHttpStatus.OK, "{}",
                "{\"id\":\"order-1\",\"invoice_number\":\"INV-2026000007\",\"total\":24.02}");

        var passing = PracticeShopApiAssertions.order(order,
                "Thanks for your order! Your invoice number is INV-2026000007.");
        var mismatched = PracticeShopApiAssertions.order(order,
                "Thanks for your order! Your invoice number is INV-OTHER.");

        assertTrue(passing.stream().allMatch(a -> a.passed()));
        assertFalse(mismatched.stream().filter(a -> a.name().equals("UI/API order correlation"))
                .findFirst().orElseThrow().passed());
    }

    private static PracticeShopApiNetworkJournal.Exchange exchange(String method, String path,
                                                                     PracticeShopApiHttpStatus status,
                                                                     String requestBody, String responseBody) {
        return exchange(method, path, status, requestBody, responseBody,
                Map.of("content-type", atf.api.core.http.request.MediaTypes.APPLICATION_JSON));
    }

    private static PracticeShopApiNetworkJournal.Exchange exchange(String method, String path,
                                                                     PracticeShopApiHttpStatus status,
                                                                     String requestBody, String responseBody,
                                                                     Map<String, String> responseHeaders) {
        return new PracticeShopApiNetworkJournal.Exchange(method, path,
                PracticeShopApiScenarioFixture.API_ENDPOINT + path, status.code(),
                requestBody, responseBody, Map.of(), responseHeaders);
    }
}
