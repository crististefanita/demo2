package atf.ui.tests.practiceshop.api.integration;

import atf.api.console.AtfConsoleReporter;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.testkit.AtfIntegrationTest;
import atf.ui.practiceshop.api.ShopApiClient;
import atf.ui.practiceshop.api.ShopApiScreenFlow;
import atf.ui.practiceshop.api.contract.ShopPaymentMethod;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import atf.ui.practiceshop.config.model.ShopCredentials;
import atf.ui.practiceshop.api.ShopApiSession;
import atf.ui.practiceshop.support.api.ShopApiAssertions;
import atf.ui.practiceshop.support.api.ShopApiExecutionExchange;
import atf.ui.practiceshop.support.reporting.ShopApiReportPublisher;
import atf.ui.practiceshop.support.api.ShopApiReportValidation;
import atf.ui.tests.practiceshop.api.support.ShopApiReportMetadataFixture;
import atf.ui.tests.practiceshop.support.ShopReportHtmlTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import atf.ui.tests.practiceshop.support.ShopTestSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Authoritative live business artifact. The component report remains the
 * deterministic contract matrix; this test is the only business PASS source.
 */
@Tag("api")
@AtfIntegrationTest
@ShopTestSupport.RequiresLiveAuthenticatedApi
class ShopStandaloneApiReportTest {
    private static final String REPORT_PREFIX = "api-atf-practice-shop-standalone_";
    private static final String AUTHENTICATION_PHASE = "authentication";
    private static final String CATALOG_PHASE = "catalog";
    private static final String CART_PHASE = "cart";
    private static final String CHECKOUT_PHASE = "checkout";

    @Test
    @DisplayName("Practice Shop live API business flow publishes real HTTP evidence")
    void publishesLiveApiBusinessEvidence() throws Exception {
        ShopConfiguration configuration = ShopConfigurationLoader.load();
        ShopCredentials credentials = ShopCredentials.load(configuration.accountFile());
        ShopPaymentMethod paymentMethod = ShopPaymentMethod.from(
                configuration.checkout().paymentMethod());
        try (ShopApiClient client = ShopApiClient.live(configuration.api())) {
            ShopApiScreenFlow flow = new ShopApiScreenFlow(client);

            ShopApiSession login = client.login(credentials);
        if (!login.authenticated()) {
            publishLiveLoginFailure(configuration, login, credentials);
            assertTrue(login.authenticated(), "live API login precondition must be authenticated");
                return;
        }
        var catalog = flow.catalog().openProduct(configuration.productName());
        var cart = flow.cart().prepare(catalog.productId(), configuration.checkout().quantity());
        var checkout = flow.checkout().confirm(
                configuration.checkout().billing().country(),
                configuration.checkout().billing().postalCode(),
                configuration.checkout().billing().houseNumber(),
                paymentMethod);

        List<ApiExecutionResult> executions = List.of(
                login.loginResult(), catalog.search(), catalog.detail(), cart.create(), cart.add(), cart.read(),
                checkout.addressLookup(), checkout.payment());
        assertTrue(executions.stream().allMatch(execution ->
                        execution.provenance() == ApiTransportProvenance.LIVE_HTTP),
                "business report must never be built from acceptance/mock transport");

        var loginExchange = ShopApiExecutionExchange.from(
                login.loginResult(), 1, AUTHENTICATION_PHASE);
        var searchExchange = ShopApiExecutionExchange.from(
                catalog.search(), 2, CATALOG_PHASE);
        var detailExchange = ShopApiExecutionExchange.from(
                catalog.detail(), 3, CATALOG_PHASE);
        var addExchange = ShopApiExecutionExchange.from(
                cart.add(), 5, CART_PHASE);
        var readExchange = ShopApiExecutionExchange.from(
                cart.read(), 6, CART_PHASE);
        var paymentExchange = ShopApiExecutionExchange.from(
                checkout.payment(), 8, CHECKOUT_PHASE);
        List<ShopApiReportValidation> validations = List.of(
                new ShopApiReportValidation("Authentication business contract",
                        ShopApiAssertions.login(loginExchange)),
                new ShopApiReportValidation("Catalog business contract",
                        ShopApiAssertions.product(searchExchange, detailExchange,
                                configuration.productName(), catalog.productId())),
                new ShopApiReportValidation("Cart business contract",
                        ShopApiAssertions.cart(addExchange, readExchange,
                                catalog.productId(), configuration.checkout().quantity())),
                new ShopApiReportValidation("Checkout business contract",
                        ShopApiAssertions.payment(paymentExchange, paymentMethod.wireValue())));

        Path reportPath = configuration.apiReportDirectory().resolve(
                REPORT_PREFIX + Instant.now().toString().replace(":", "-") + ".html");
        AtfConsoleSink console = AtfConsoleReporter.configuredStdout();
        Path report = ShopApiReportPublisher.publishLiveStandalone(reportPath,
                ShopApiReportMetadataFixture.LIVE_TEST_CLASS,
                ShopApiReportMetadataFixture.LIVE_TEST_PACKAGE,
                ShopApiReportMetadataFixture.LIVE_SCENARIO,
                ShopApiReportMetadataFixture.LIVE_TAGS,
                executions, validations, console);

            assertTrue(Files.isRegularFile(report), "live API report must be written");
            String html = Files.readString(report);
            ShopReportHtmlTestSupport.requireText(html, "LIVE / HTTP", "live provenance");
            for (String contract : List.of("Authentication business contract", "Catalog business contract",
                    "Cart business contract", "Checkout business contract")) {
                ShopReportHtmlTestSupport.requireText(html, contract, "business validation");
            }
            ShopReportHtmlTestSupport.requireText(html, "REQUEST JSON", "request body disclosure");
            ShopReportHtmlTestSupport.requireText(html, "RESPONSE JSON", "response body disclosure");
            assertTrue(html.contains("Payment was successful") || html.contains("payment_method"),
                    "payment business evidence must remain visible");
            ShopReportHtmlTestSupport.requireText(html, HttpMethod.POST.value(),
                    "write operations must remain visible");
        }
    }

    private static void publishLiveLoginFailure(ShopConfiguration configuration,
                                                 ShopApiSession login,
                                                 ShopCredentials credentials) throws Exception {
        var exchange = ShopApiExecutionExchange.from(login.loginResult(), 1, AUTHENTICATION_PHASE);
        List<ShopApiReportValidation> validations = List.of(
                new ShopApiReportValidation("Authentication business contract",
                        ShopApiAssertions.login(exchange)));
        Path reportPath = configuration.apiReportDirectory().resolve(
                REPORT_PREFIX + Instant.now().toString().replace(":", "-") + ".html");
        Path report = ShopApiReportPublisher.publishLiveStandalone(reportPath,
                ShopApiReportMetadataFixture.LIVE_TEST_CLASS,
                ShopApiReportMetadataFixture.LIVE_TEST_PACKAGE,
                ShopApiReportMetadataFixture.LIVE_SCENARIO,
                ShopApiReportMetadataFixture.LIVE_TAGS,
                List.of(login.loginResult()), validations, AtfConsoleReporter.configuredStdout());
        assertTrue(Files.isRegularFile(report), "failed live precondition must still publish evidence");
    }
}
