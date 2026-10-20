package atf.ui.tests.practiceshop.api.integration;

import atf.api.console.AtfConsoleReporter;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.testkit.AtfIntegrationTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiScreenFlow;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.api.PracticeShopApiSession;
import atf.ui.practiceshop.support.PracticeShopApiAssertions;
import atf.ui.practiceshop.support.PracticeShopApiExecutionExchange;
import atf.ui.practiceshop.support.PracticeShopApiReportPublisher;
import atf.ui.practiceshop.support.PracticeShopApiReportValidation;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiReportMetadataFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

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
@EnabledIf("isLiveConfigured")
class PracticeShopStandaloneApiReportTest {
    private static final String REPORT_PREFIX = "api-atf-practice-shop-standalone_";
    private static final String AUTHENTICATION_PHASE = "authentication";
    private static final String CATALOG_PHASE = "catalog";
    private static final String CART_PHASE = "cart";
    private static final String CHECKOUT_PHASE = "checkout";

    static boolean isLiveConfigured() {
        PracticeShopConfiguration configuration = PracticeShopConfigurationLoader.load();
        return configuration.authenticatedE2eAllowed()
                && configuration.api().mode() == atf.api.core.config.ApiTransportMode.LIVE;
    }

    @Test
    @DisplayName("Practice Shop live API business flow publishes real HTTP evidence")
    void publishesLiveApiBusinessEvidence() throws Exception {
        PracticeShopConfiguration configuration = PracticeShopConfigurationLoader.load();
        PracticeShopCredentials credentials = PracticeShopCredentials.load(configuration.accountFile());
        PracticeShopPaymentMethod paymentMethod = PracticeShopPaymentMethod.from(
                configuration.checkout().paymentMethod());
        PracticeShopApiClient client = PracticeShopApiClient.live(configuration.api());
        PracticeShopApiScreenFlow flow = new PracticeShopApiScreenFlow(client);

        PracticeShopApiSession login = client.login(credentials);
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

        var loginExchange = PracticeShopApiExecutionExchange.from(
                login.loginResult(), 1, AUTHENTICATION_PHASE);
        var searchExchange = PracticeShopApiExecutionExchange.from(
                catalog.search(), 2, CATALOG_PHASE);
        var detailExchange = PracticeShopApiExecutionExchange.from(
                catalog.detail(), 3, CATALOG_PHASE);
        var addExchange = PracticeShopApiExecutionExchange.from(
                cart.add(), 5, CART_PHASE);
        var readExchange = PracticeShopApiExecutionExchange.from(
                cart.read(), 6, CART_PHASE);
        var paymentExchange = PracticeShopApiExecutionExchange.from(
                checkout.payment(), 8, CHECKOUT_PHASE);
        List<PracticeShopApiReportValidation> validations = List.of(
                new PracticeShopApiReportValidation("Authentication business contract",
                        PracticeShopApiAssertions.login(loginExchange, null, credentials.displayName())),
                new PracticeShopApiReportValidation("Catalog business contract",
                        PracticeShopApiAssertions.product(searchExchange, detailExchange,
                                configuration.productName(), catalog.productId())),
                new PracticeShopApiReportValidation("Cart business contract",
                        PracticeShopApiAssertions.cart(addExchange, readExchange,
                                catalog.productId(), configuration.checkout().quantity())),
                new PracticeShopApiReportValidation("Checkout business contract",
                        PracticeShopApiAssertions.payment(paymentExchange, paymentMethod.wireValue())));

        Path reportPath = configuration.apiReportDirectory().resolve(
                REPORT_PREFIX + Instant.now().toString().replace(":", "-") + ".html");
        AtfConsoleSink console = AtfConsoleReporter.configuredStdout();
        Path report = PracticeShopApiReportPublisher.publishLiveStandalone(reportPath,
                PracticeShopApiReportMetadataFixture.LIVE_TEST_CLASS,
                PracticeShopApiReportMetadataFixture.LIVE_TEST_PACKAGE,
                PracticeShopApiReportMetadataFixture.LIVE_SCENARIO,
                PracticeShopApiReportMetadataFixture.LIVE_TAGS,
                executions, validations, console);

        assertTrue(Files.isRegularFile(report), "live API report must be written");
        String html = Files.readString(report);
        assertTrue(html.contains("LIVE / HTTP"), "report must disclose live provenance");
        assertTrue(html.contains("Authentication business contract"));
        assertTrue(html.contains("Catalog business contract"));
        assertTrue(html.contains("Cart business contract"));
        assertTrue(html.contains("Checkout business contract"));
        assertTrue(html.contains("REQUEST JSON"));
        assertTrue(html.contains("RESPONSE JSON"));
        assertTrue(html.contains("Payment was successful") || html.contains("payment_method"));
        assertTrue(html.contains(HttpMethod.POST.value()), "write operations must remain visible");
    }

    private static void publishLiveLoginFailure(PracticeShopConfiguration configuration,
                                                 PracticeShopApiSession login,
                                                 PracticeShopCredentials credentials) throws Exception {
        var exchange = PracticeShopApiExecutionExchange.from(login.loginResult(), 1, AUTHENTICATION_PHASE);
        List<PracticeShopApiReportValidation> validations = List.of(
                new PracticeShopApiReportValidation("Authentication business contract",
                        PracticeShopApiAssertions.login(exchange, null, credentials.displayName())));
        Path reportPath = configuration.apiReportDirectory().resolve(
                REPORT_PREFIX + Instant.now().toString().replace(":", "-") + ".html");
        Path report = PracticeShopApiReportPublisher.publishLiveStandalone(reportPath,
                PracticeShopApiReportMetadataFixture.LIVE_TEST_CLASS,
                PracticeShopApiReportMetadataFixture.LIVE_TEST_PACKAGE,
                PracticeShopApiReportMetadataFixture.LIVE_SCENARIO,
                PracticeShopApiReportMetadataFixture.LIVE_TAGS,
                List.of(login.loginResult()), validations, AtfConsoleReporter.configuredStdout());
        assertTrue(Files.isRegularFile(report), "failed live precondition must still publish evidence");
    }
}
