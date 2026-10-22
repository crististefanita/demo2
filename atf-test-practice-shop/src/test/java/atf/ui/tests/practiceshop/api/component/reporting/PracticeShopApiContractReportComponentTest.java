package atf.ui.tests.practiceshop.api.component.reporting;

import atf.api.console.AtfConsoleReporter;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.api.PracticeShopApiScreenFlow;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.support.PracticeShopApiReportPublisher;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiReportMetadataFixture;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture;
import atf.ui.tests.practiceshop.api.support.PracticeShopApiComponentFixture;
import atf.ui.tests.practiceshop.support.PracticeShopReportHtmlTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture.COUNTRY;
import static atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture.HOUSE_NUMBER;
import static atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture.POSTCODE;
import static atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture.PRODUCT_NAME;
import static atf.ui.tests.practiceshop.api.support.PracticeShopApiScenarioFixture.QUANTITY;

/** Publishes the same request/response/execution evidence without opening a browser. */
@AtfComponentTest
class PracticeShopApiContractReportComponentTest {
    @Test
    @DisplayName("Practice Shop API screen flow publishes standalone Extent evidence")
    void publishesStandaloneApiEvidence() throws Exception {
        PracticeShopApiClient client = PracticeShopApiComponentFixture.client();
        PracticeShopApiScreenFlow flow = new PracticeShopApiScreenFlow(client);
        var login = flow.login().establish(PracticeShopApiScenarioFixture.validCredentials());
        var catalog = flow.catalog().openProduct(PRODUCT_NAME);
        var cart = flow.cart().prepare(catalog.productId(), QUANTITY);
        var checkout = flow.checkout().confirm(COUNTRY, POSTCODE, HOUSE_NUMBER,
                PracticeShopPaymentMethod.CASH_ON_DELIVERY);

        List<ApiExecutionResult> executions = List.of(
                login.loginResult(), catalog.search(), catalog.detail(), cart.create(), cart.add(), cart.read(),
                checkout.addressLookup(), checkout.payment());
        assertWriteRequestsHaveJson(executions);
        assertResponsesHaveJson(executions);

        var configuration = PracticeShopConfigurationLoader.load();
        Path reportPath = configuration.apiReportDirectory().resolve(
                "api-atf-practice-shop-standalone_" + Instant.now().toString()
                        .replace(":", "-") + ".html");
        AtfConsoleSink console = AtfConsoleReporter.configuredStdout();
        Path report = PracticeShopApiReportPublisher.publishStandalone(reportPath,
                PracticeShopApiReportMetadataFixture.COMPONENT_TEST_CLASS,
                PracticeShopApiReportMetadataFixture.COMPONENT_TEST_PACKAGE,
                PracticeShopApiReportMetadataFixture.COMPONENT_SCENARIO,
                PracticeShopApiReportMetadataFixture.COMPONENT_TAGS,
                executions, console);

        String html = Files.readString(report);
        assertTrue(Files.isRegularFile(report));
        PracticeShopReportHtmlTestSupport.requireText(html, "REQUEST JSON", "request body disclosure");
        PracticeShopReportHtmlTestSupport.requireText(html, "RESPONSE JSON", "response body disclosure");
        PracticeShopReportHtmlTestSupport.requireText(html, "Execution evidence", "execution disclosure");
        PracticeShopReportHtmlTestSupport.requireText(html, PracticeShopApiRoutes.PRODUCT_SEARCH,
                "catalog route");
        PracticeShopReportHtmlTestSupport.requireText(html, PracticeShopApiRoutes.PAYMENT_CHECK,
                "payment route");
        PracticeShopReportHtmlTestSupport.requireInOrder(html, List.of(
                "POST /users/login — establish API session",
                "GET /products/search — find configured product",
                "GET /products/01product — read selected product",
                "POST /carts/01cart — add product to cart",
                "GET /carts/01cart — verify cart contents",
                "GET /postcode-lookup — resolve checkout address",
                "POST /payment/check — validate payment selection"),
                "business request order");
        PracticeShopReportHtmlTestSupport.requireAbsent(html, "QUERY /products/search",
                "semantic operation vocabulary must not regress");
        for (String header : List.of("OPERATION KIND", "ROUTE TEMPLATE", "AUTH TYPE",
                "REQUEST HEADERS", "CONTENT TYPE", "RESPONSE HEADERS")) {
            PracticeShopReportHtmlTestSupport.requireExactCell(html, header, "API report table");
        }
        for (String secret : List.of("atf@example.test", "token-1", "\"secret\"")) {
            PracticeShopReportHtmlTestSupport.requireAbsent(html, secret, "redaction contract");
        }
    }

    private static void assertWriteRequestsHaveJson(List<ApiExecutionResult> executions) {
        for (ApiExecutionResult execution : executions) {
            var request = execution.request();
            String method = request.method();
            boolean writeRequest = HttpMethod.POST.value().equalsIgnoreCase(method)
                    || HttpMethod.PUT.value().equalsIgnoreCase(method);
            if (writeRequest) {
                assertFalse(request.bodyText().isBlank(),
                        "POST/PUT must carry a JSON request body: " + request.path());
            }
        }
    }

    private static void assertResponsesHaveJson(List<ApiExecutionResult> executions) {
        for (ApiExecutionResult execution : executions) {
            var request = execution.request();
            var response = execution.response();
            assertFalse(response.body().isBlank(),
                    "API response body must be present for report evidence: " + request.path());
        }
    }
}
