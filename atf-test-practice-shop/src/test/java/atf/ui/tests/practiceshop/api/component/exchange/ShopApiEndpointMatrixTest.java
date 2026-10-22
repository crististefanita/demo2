package atf.ui.tests.practiceshop.api.component.exchange;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.ui.practiceshop.api.ShopApiHttpStatus;
import atf.ui.practiceshop.api.ShopApiRequestFactory;
import atf.ui.practiceshop.api.ShopApiRoutes;
import atf.ui.practiceshop.api.ShopPaymentMethod;
import atf.ui.practiceshop.support.ShopJsonEvidence;
import atf.ui.tests.practiceshop.api.support.ShopApiCallComponentTest;
import atf.ui.tests.practiceshop.api.support.ShopApiCallAuditContextTestSupport;
import atf.ui.tests.practiceshop.api.support.ShopApiComponentFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiScenarioFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Audited endpoint/status matrix against deterministic component transports. */
@ShopApiCallComponentTest
class ShopApiEndpointMatrixTest {
    private static final ShopApiRequestFactory REQUESTS = new ShopApiRequestFactory();

    @Test
    void endpointCasesUseConciseBusinessDisplayNames() {
        assertTrue(endpointCases().allMatch(scenario -> scenario.toString().equals(scenario.name())
                        && !scenario.toString().contains("method=")
                        && !scenario.toString().contains("requestSupplier=")),
                "parameterized report titles must show the business case, not record implementation fields");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpointCases")
    void endpointExchangeMatchesItsMethodRouteStatusAndJsonContract(
            ShopApiEndpointCase scenario) {
        ApiRequest request = scenario.requestSupplier().create();
        ShopApiCallAuditContextTestSupport.expectNext(scenario.name(), scenario.status());
        var execution = ShopApiComponentFixture.client().execute(request);
        var response = execution.response();

        assertEquals(scenario.method(), request.method(), scenario.name());
        assertEquals(scenario.path(), request.path(), scenario.name());
        assertTrue(scenario.status().matches(response.status()), scenario.name());
        assertTrue(execution.attempts() > 0, scenario.name());

        if (HttpMethod.POST.value().equals(scenario.method())
                || HttpMethod.PUT.value().equals(scenario.method())) {
            assertEquals(MediaTypes.APPLICATION_JSON,
                    request.headers().get(HttpHeader.CONTENT_TYPE.value()), scenario.name());
            assertNotNull(ShopJsonEvidence.parse(request.bodyText()), scenario.name());
        }
        if (ShopApiHttpStatus.NO_CONTENT.matches(response.status())) {
            assertTrue(response.body().isEmpty(), scenario.name());
        } else {
            assertTrue(response.header("content-type").contains(MediaTypes.APPLICATION_JSON),
                    scenario.name());
            assertNotNull(ShopJsonEvidence.parse(response.body()), scenario.name());
        }
    }

    private static Stream<ShopApiEndpointCase> endpointCases() {
        String cartId = ShopApiScenarioFixture.CART_ID;
        String mutationCartId = ShopApiScenarioFixture.MUTATION_CART_ID;
        String productId = ShopApiScenarioFixture.PRODUCT_ID;
        String mutationProductId = ShopApiScenarioFixture.MUTATION_PRODUCT_ID;

        return Stream.of(
                endpoint("login accepts configured fixture identity", HttpMethod.POST.value(),
                        ShopApiRoutes.LOGIN, ShopApiHttpStatus.OK,
                        () -> REQUESTS.login(ShopApiScenarioFixture.validCredentials())),
                endpoint("login rejects invalid fixture identity", HttpMethod.POST.value(),
                        ShopApiRoutes.LOGIN, ShopApiHttpStatus.UNAUTHORIZED,
                        () -> REQUESTS.login(ShopApiScenarioFixture.invalidCredentials())),
                endpoint("registration returns a created account", HttpMethod.POST.value(),
                        ShopApiRoutes.USER_REGISTER, ShopApiHttpStatus.CREATED,
                        () -> REQUESTS.register(ShopApiScenarioFixture.validCredentials())),
                endpoint("product search returns matching results", HttpMethod.GET.value(),
                        ShopApiRoutes.PRODUCT_SEARCH, ShopApiHttpStatus.OK,
                        () -> REQUESTS.search(ShopApiScenarioFixture.PRODUCT_NAME)),
                endpoint("product collection supports filters", HttpMethod.GET.value(),
                        ShopApiRoutes.PRODUCT_COLLECTION, ShopApiHttpStatus.OK,
                        () -> REQUESTS.products(null)),
                endpoint("categories are readable", HttpMethod.GET.value(),
                        ShopApiRoutes.CATEGORIES, ShopApiHttpStatus.OK, REQUESTS::categories),
                endpoint("product detail returns a known product", HttpMethod.GET.value(),
                        ShopApiRoutes.product(productId), ShopApiHttpStatus.OK,
                        () -> REQUESTS.product(productId, "")),
                endpoint("product detail rejects an unknown product", HttpMethod.GET.value(),
                        ShopApiRoutes.product(ShopApiScenarioFixture.MISSING_PRODUCT_ID),
                        ShopApiHttpStatus.NOT_FOUND,
                        () -> REQUESTS.product(ShopApiScenarioFixture.MISSING_PRODUCT_ID, "")),
                endpoint("cart creation returns a created resource", HttpMethod.POST.value(),
                        ShopApiRoutes.CARTS, ShopApiHttpStatus.CREATED, REQUESTS::createCart),
                endpoint("cart read returns its item collection", HttpMethod.GET.value(),
                        ShopApiRoutes.cart(cartId), ShopApiHttpStatus.OK,
                        () -> REQUESTS.readCart(cartId)),
                endpoint("cart add accepts a known product", HttpMethod.POST.value(),
                        ShopApiRoutes.cart(cartId), ShopApiHttpStatus.OK,
                        () -> REQUESTS.addToCart(cartId, productId, ShopApiScenarioFixture.QUANTITY)),
                endpoint("cart add rejects an unknown product", HttpMethod.POST.value(),
                        ShopApiRoutes.cart(cartId), ShopApiHttpStatus.UNPROCESSABLE_ENTITY,
                        () -> REQUESTS.addToCart(cartId, ShopApiScenarioFixture.INVALID_PRODUCT_ID,
                                ShopApiScenarioFixture.QUANTITY)),
                endpoint("cart quantity update accepts a known product", HttpMethod.PUT.value(),
                        ShopApiRoutes.updateCartQuantity(mutationCartId), ShopApiHttpStatus.OK,
                        () -> REQUESTS.updateCartQuantity(mutationCartId, mutationProductId,
                                ShopApiScenarioFixture.UPDATED_QUANTITY)),
                endpoint("cart quantity update rejects an unknown product", HttpMethod.PUT.value(),
                        ShopApiRoutes.updateCartQuantity(mutationCartId),
                        ShopApiHttpStatus.UNPROCESSABLE_ENTITY,
                        () -> REQUESTS.updateCartQuantity(mutationCartId,
                                ShopApiScenarioFixture.INVALID_PRODUCT_ID,
                                ShopApiScenarioFixture.UPDATED_QUANTITY)),
                endpoint("cart item removal returns no content", HttpMethod.DELETE.value(),
                        ShopApiRoutes.removeCartProduct(mutationCartId, mutationProductId),
                        ShopApiHttpStatus.NO_CONTENT,
                        () -> REQUESTS.removeFromCart(mutationCartId, mutationProductId)),
                endpoint("postcode lookup returns address JSON", HttpMethod.GET.value(),
                        ShopApiRoutes.POSTCODE_LOOKUP, ShopApiHttpStatus.OK,
                        () -> REQUESTS.postcodeLookup(ShopApiScenarioFixture.COUNTRY,
                                ShopApiScenarioFixture.POSTCODE, ShopApiScenarioFixture.HOUSE_NUMBER)),
                endpoint("payment check returns a JSON confirmation", HttpMethod.POST.value(),
                        ShopApiRoutes.PAYMENT_CHECK, ShopApiHttpStatus.OK,
                        () -> REQUESTS.payment(ShopPaymentMethod.CASH_ON_DELIVERY)),
                endpoint("authenticated invoice creation follows the OpenAPI success contract",
                        HttpMethod.POST.value(), ShopApiRoutes.INVOICE_CREATE,
                        ShopApiHttpStatus.OK,
                        () -> REQUESTS.invoice(ShopApiScenarioFixture.validInvoice(),
                                ShopApiScenarioFixture.BEARER_TOKEN)),
                endpoint("guest invoice creates an order from typed checkout data",
                        HttpMethod.POST.value(), ShopApiRoutes.GUEST_INVOICE_CREATE,
                        ShopApiHttpStatus.OK,
                        () -> REQUESTS.guestInvoice(ShopApiScenarioFixture.validGuestInvoice())),
                endpoint("guest invoice rejects missing required fields",
                        HttpMethod.POST.value(), ShopApiRoutes.GUEST_INVOICE_CREATE,
                        ShopApiHttpStatus.UNPROCESSABLE_ENTITY,
                        () -> REQUESTS.guestInvoice(ShopApiScenarioFixture.invalidGuestInvoice()))
        );
    }

    private static ShopApiEndpointCase endpoint(String name, String method, String path,
                                                ShopApiHttpStatus status,
                                                ShopApiRequestSupplier request) {
        return new ShopApiEndpointCase(name, method, path, status, request);
    }

    private record ShopApiEndpointCase(String name, String method, String path,
                                       ShopApiHttpStatus status,
                                       ShopApiRequestSupplier requestSupplier) {
        @Override
        public String toString() {
            return name;
        }
    }

    @FunctionalInterface
    private interface ShopApiRequestSupplier {
        ApiRequest create();
    }
}
