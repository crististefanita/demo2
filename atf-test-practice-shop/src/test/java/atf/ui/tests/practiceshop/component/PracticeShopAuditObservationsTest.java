package atf.ui.tests.practiceshop.component;

import atf.api.reporting.api.ReportAttemptRecord;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.testkit.AtfComponentTest;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.practiceshop.support.PracticeShopAuditObservations;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import atf.ui.practiceshop.config.PracticeShopApiConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopAuditObservationsTest {
    private static final PracticeShopConfiguration CONFIGURATION = PracticeShopConfigurationLoader.load();

    @Test
    void recordsOneScreenAuditAndOneObservationPerApiExchange() {
        PlaywrightActionSequence actions = new PlaywrightActionSequence();
        Page page = page(CONFIGURATION.uri(PracticeShopUiRoutes.product("1")).toString());
        var checkpoint = new PracticeShopPurchaseCheckpoint(
                AtfGherkinKeyword.GIVEN,
                "product screen displays configured fields",
                List.of(new AtfAssertion("Product", "ready", "ready", true)),
                List.of(execution("GET", "/products/1", 200)));

        PracticeShopAuditObservations.recordCheckpoint(actions, page, checkpoint,
                new UiScreenValidation.Evidence("product", "", false));

        var observations = actions.snapshot();
        assertEquals(2, observations.size());
        assertTrue(observations.stream().allMatch(row -> row.source().name().equals("OBSERVE")));
        assertEquals("Audit screen contract", observations.get(0).operation());
        assertEquals("Audit API exchange #1", observations.get(1).operation());
        assertTrue(observations.get(1).observed().contains("status=200"));
        assertTrue(observations.get(1).observed().contains("transport=LIVE / HTTP"));
    }

    @Test
    void ignoresMalformedApiEvidenceWithoutInventingAnAuditExchange() {
        PlaywrightActionSequence actions = new PlaywrightActionSequence();
        Page page = page(CONFIGURATION.uri(PracticeShopUiRoutes.CHECKOUT).toString());
        var checkpoint = new PracticeShopPurchaseCheckpoint(
                AtfGherkinKeyword.THEN,
                "confirmation screen is displayed",
                List.of(new AtfAssertion("Confirmation", "visible", "visible", true)),
                List.of(new ReportExecutionEvidence(null, 1, Duration.ZERO, List.of(), List.of(), null,
                        ReportTransportProvenance.LIVE_HTTP)));

        PracticeShopAuditObservations.recordCheckpoint(actions, page, checkpoint,
                new UiScreenValidation.Evidence("confirmation", "", false));

        assertEquals(1, actions.snapshot().size());
        assertEquals("Audit screen contract", actions.snapshot().get(0).operation());
    }

    @Test
    void recordsScreenAuditForTheAccountCatalogCompatibilityPath() {
        PlaywrightActionSequence actions = new PlaywrightActionSequence();

        PracticeShopAuditObservations.recordScreenContract(
                actions,
                page(CONFIGURATION.uri(PracticeShopUiRoutes.ACCOUNT).toString()),
                "account screen is displayed",
                List.of(
                        new AtfAssertion("Account route", PracticeShopUiRoutes.ACCOUNT,
                                PracticeShopUiRoutes.ACCOUNT, true),
                        new AtfAssertion("Account marker", "visible", "visible", true)),
                new UiScreenValidation.Evidence("account", "", false),
                true);

        assertEquals(1, actions.snapshot().size());
        assertEquals("Audit screen contract", actions.snapshot().get(0).operation());
        assertTrue(actions.snapshot().get(0).observed().contains("assertions=2"));
        assertTrue(actions.snapshot().get(0).observed().contains("url="
                + CONFIGURATION.uri(PracticeShopUiRoutes.ACCOUNT)));
    }

    private static ReportExecutionEvidence execution(String method, String path, int status) {
        var request = new ReportRequest(method, path, Map.of(),
                Map.of("Content-Type", "application/json"), "bearer", "");
        var response = new ReportResponse(status, "{}",
                Map.of("Content-Type", "application/json"), Duration.ofMillis(3),
                PracticeShopApiConfiguration.defaults().endpoint().resolve(path.substring(1)).toString());
        return new ReportExecutionEvidence(response, 1, Duration.ofMillis(3), List.of(),
                List.of(new ReportAttemptRecord(1, status, Duration.ofMillis(3), "SUCCESS", "final response")),
                request, ReportTransportProvenance.LIVE_HTTP);
    }

    private static Page page(String url) {
        return (Page) Proxy.newProxyInstance(
                Page.class.getClassLoader(),
                new Class<?>[]{Page.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "url" -> url;
                    case "toString" -> "page(" + url + ")";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
