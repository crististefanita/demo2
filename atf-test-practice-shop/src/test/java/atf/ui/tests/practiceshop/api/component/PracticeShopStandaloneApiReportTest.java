package atf.ui.tests.practiceshop.api.component;

import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiScreenFlow;
import atf.ui.practiceshop.api.PracticeShopPaymentMethod;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.support.PracticeShopApiReportPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Publishes the same request/response/execution evidence without opening a browser. */
@AtfComponentTest
class PracticeShopStandaloneApiReportTest {
    @Test
    @DisplayName("Practice Shop API screen flow publishes standalone Extent evidence")
    void publishesStandaloneApiEvidence() throws Exception {
        PracticeShopApiClient client = PracticeShopApiClient.forTransport(
                URI.create("https://api.practicesoftwaretesting.com"), transport());
        PracticeShopApiScreenFlow flow = new PracticeShopApiScreenFlow(client);
        var login = flow.login().establish(new PracticeShopCredentials(
                "atf@example.test", "secret", "ATF", "Practice"));
        var catalog = flow.catalog().openProduct("Combination Pliers");
        var cart = flow.cart().prepare(catalog.productId(), 2);
        var checkout = flow.checkout().confirm("RO", "010101", "10",
                PracticeShopPaymentMethod.CASH_ON_DELIVERY);

        List<atf.api.core.http.response.ApiExecutionResult> executions = List.of(
                login.loginResult(), catalog.search(), catalog.detail(), cart.create(), cart.add(), cart.read(),
                checkout.addressLookup(), checkout.payment());
        java.nio.file.Path report;
        try (ExtentReportSession session = ExtentReportSession.openConfigured()) {
            report = session.reportPath();
            AtfReportPorts.Scenario scenario = session.startScenario(
                    "PracticeShopStandaloneApiReportTest", "atf.ui.tests.practiceshop.api.component",
                    "Practice Shop API screen flow", List.of("api", "component", "practice-shop"));
            AtfReportPorts.Phase phase = scenario.startPhase("TEST BODY");
            try {
                for (var execution : executions) PracticeShopApiReportPublisher.publish(phase, execution);
                phase.complete(AtfReportStatus.PASS);
                scenario.complete(AtfReportStatus.PASS);
            } finally {
                phase.close();
                scenario.close();
            }
        }

        String html = Files.readString(report);
        assertTrue(Files.isRegularFile(report));
        assertTrue(html.contains("REQUEST JSON"));
        assertTrue(html.contains("RESPONSE JSON"));
        assertTrue(html.contains("Execution evidence"));
        assertTrue(html.contains("/products/search"));
        assertTrue(html.contains("/payment/check"));
        assertTrue(!html.contains("atf@example.test"));
        assertTrue(!html.contains("token-1"));
        assertTrue(!html.contains("\"secret\""));
    }

    private static ApiTransport transport() {
        return (request, url, auth) -> {
            String body;
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
                default -> body = "{\"message\":\"unexpected route\"}";
            }
            return CompletableFuture.completedFuture(new ApiResponse(200, body,
                    Map.of("content-type", "application/json"), Duration.ZERO, url));
        };
    }
}
