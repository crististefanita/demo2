package atf.ui.practiceshop.support.api;

import atf.api.core.http.request.HttpMethod;
import atf.api.reporting.api.ApiManualReplay;
import atf.api.reporting.api.ReportRequestSignature;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.api.ShopApiOperation;
import atf.ui.practiceshop.api.ShopApiRoutes;
import atf.ui.tests.practiceshop.support.ShopReportHtmlTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.time.Duration;

import static atf.ui.practiceshop.support.api.ShopApiMethodFidelityFixture.QUERY;
import static atf.ui.practiceshop.support.api.ShopApiMethodFidelityFixture.REQUEST_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

@AtfComponentTest
class ShopApiMethodFidelityTest {
    @Test
    void browserTimingAndUnavailablePayloadAreNotManufacturedAsZeroOrJsonNull() {
        var unknown = ShopApiMethodFidelityFixture.browserEvidence(QUERY);
        assertNull(unknown.response().duration());
        assertNull(unknown.totalDuration());
        assertNull(unknown.attemptHistory().getFirst().duration());
        var unavailable = ShopApiMethodFidelityFixture.exchange(QUERY).withResponseBody(
                ShopApiBodyEvidence.unavailable("Browser", "body not exposed"));
        assertEquals(atf.api.reporting.api.evidence.ReportBodyAvailability.NOT_CAPTURED,
                ShopApiEvidenceMapper.toExecution(unavailable).response().bodyAvailability());
    }
    @Test
    void capturedMethodsRemainFaithfulInModelSignatureAndReplay() throws Exception {
        var json = new ObjectMapper();
        for (String method : ShopApiMethodFidelityFixture.METHODS) {
            var evidence = ShopApiMethodFidelityFixture.browserEvidence(method);
            var request = evidence.request();
            assertEquals(method, request.method());
            assertEquals(json.readTree(REQUEST_JSON), json.readTree(request.bodyText()));
            assertEquals(ReportRequestSignature.forRequest(request), evidence.trace().requestSignature());
            assertTrue(evidence.trace().requestSignature().startsWith(method + " "));
            String replay = ApiManualReplay.describe(evidence.provenance(), request,
                    evidence.response().url(), request.bodyText());
            assertTrue(replay.contains("--request '" + method + "'"), replay);
            assertTrue(replay.contains(request.bodyText()), replay);
        }
        assertNotEquals(ShopApiMethodFidelityFixture.browserEvidence(QUERY).trace().requestSignature(),
                ShopApiMethodFidelityFixture.browserEvidence(HttpMethod.GET.value()).trace().requestSignature());
        assertEquals(ShopApiOperation.SEARCH,
                ShopApiOperation.resolve(QUERY, ShopApiRoutes.PRODUCT_SEARCH));
    }

    @Test
    void directCoreAdapterPreservesMeasuredResponseAndTotalDurations() {
        var evidence = ShopApiEvidenceMapper.toExecution(ShopApiMethodFidelityFixture.directExecution(QUERY));
        assertEquals(QUERY, evidence.request().method());
        assertEquals(Duration.ofMillis(17), evidence.response().duration());
        assertEquals(Duration.ofMillis(43), evidence.totalDuration());
    }

    @Test
    void bothShopPublishersAndUiEvidenceRenderCapturedQueryRatherThanGet() throws Exception {
        var reports = ShopApiMethodFidelityFixture.publish();
        assertEquals(6, reports.apiRequests().size());
        for (int index = 0; index < reports.apiRequests().size(); index++) {
            var request = reports.apiRequests().get(index);
            String expectedMethod = ShopApiMethodFidelityFixture.METHODS.get(index / 2);
            assertEquals(expectedMethod, request.method(), request.title());
            assertTrue(request.title().startsWith(expectedMethod + " "), request.title());
        }
        String api = Files.readString(reports.api());
        String ui = Files.readString(reports.ui());
        ShopReportHtmlTestSupport.requireText(api,
                "QUERY /products/search — find configured product", "observed and standalone request title");
        ShopReportHtmlTestSupport.requireText(ui,
                "QUERY /products/search", "nested browser-captured request title");
        ShopReportHtmlTestSupport.requireText(ui, "REQUEST JSON", "request body");
        ShopReportHtmlTestSupport.requireText(ui, "RESPONSE JSON", "response body");
        ShopReportHtmlTestSupport.requireText(ui, "Execution evidence", "execution evidence");
    }
}
