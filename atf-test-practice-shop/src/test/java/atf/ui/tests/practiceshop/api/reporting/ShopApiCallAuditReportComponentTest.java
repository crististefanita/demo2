package atf.ui.tests.practiceshop.api.reporting;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.semantic.AtfReportSemanticSnapshot;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.ShopApiReportPublisher;
import atf.ui.tests.practiceshop.api.support.ShopApiCallAuditContextTestSupport;
import atf.ui.tests.practiceshop.api.support.ShopApiComponentFixture;
import atf.ui.tests.practiceshop.api.support.ShopApiScenarioFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protects consumer-owned API-call report composition, not renderer internals. */
@AtfComponentTest
class ShopApiCallAuditReportComponentTest {
    private static final String TEST_BODY = "TEST BODY";
    private static final String AUDIT_DETAIL = "API audit completeness";

    @Test
    void actualExchangeAndJUnitVerdictRemainDistinctReportFacts() throws Exception {
        List<ApiExecutionResult> exchanges = new ArrayList<>();
        ShopApiCallAuditContextTestSupport.recordWith(exchanges::add);
        var result = ShopApiComponentFixture.client().login(
                ShopApiScenarioFixture.validCredentials());
        assertTrue(result.authenticated());
        assertEquals(1, exchanges.size());

        var output = Files.createTempDirectory("shop-api-audit-component-").resolve("audit.html");
        try (var report = new ExtentReportSession(output)) {
            var scenario = report.startScenario("ShopApiClientContractTest", "api.reporting",
                    "local login publishes the observed exchange", List.of("api", "component"));
            var phase = scenario.startPhase(TEST_BODY);
            ShopApiReportPublisher.publishObserved(phase, exchanges.getFirst(), 1,
                    "test method passed", exchanges.size(), AtfReportStatus.PASS);
            ShopApiCallAuditTestSupport.publishAuditDetails(phase, null, exchanges.size());
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
            var snapshot = report.semanticSnapshot();
            assertEquals(1, snapshot.requestCount());
            assertEquals(1, snapshot.validationCount());
            var request = (AtfReportSemanticSnapshot.RequestSnapshot) snapshot.classes().getFirst()
                    .scenarios().getFirst().phases().getFirst().nodes().getFirst();
            assertEquals("POST", request.method());
            assertEquals("/users/login", request.path());
            assertEquals(List.of("Contract validation"),
                    request.validations().stream()
                            .map(AtfReportSemanticSnapshot.ValidationSnapshot::title).toList());
            assertEquals(List.of("HTTP status", "Request JSON", "Content-Type",
                            "Response JSON", "Response Content-Type", "JUnit method outcome",
                            "API exchanges captured"),
                    request.validations().getFirst().assertionNames());
            assertEquals(List.of("HTTP status", "Request JSON", "Content-Type",
                            "Response JSON", "Response Content-Type", "JUnit method outcome",
                            "API exchanges captured"),
                    request.validations().getFirst().assertionNames());
        } finally {
            ShopApiCallAuditContextTestSupport.clear();
        }

        var html = Files.readString(output);
        assertTrue(html.contains("POST /users/login"));
        assertTrue(html.contains("API AUDIT COMPLETENESS"));
        assertTrue(html.contains("1 actual API exchange captured"));
        assertFalse(html.contains("JUnit result"));
        assertFalse(html.contains("Test method outcome"));
        assertTrue(html.contains("Contract validation"));
        assertTrue(html.contains("all component assertions pass"));
        int requestIndex = html.indexOf("<span>POST /users/login — establish API session</span>");
        int validationIndex = html.indexOf("<span>Contract validation</span>", requestIndex);
        assertTrue(divDepthAt(html, validationIndex) > divDepthAt(html, requestIndex),
                "validation disclosure must follow the standalone report's nested layout");
    }

    @Test
    void missingExchangeDoesNotRewritePassingJUnitVerdictOrInventAContract() throws Exception {
        var output = Files.createTempDirectory("shop-api-audit-empty-").resolve("audit.html");
        try (var report = new ExtentReportSession(output)) {
            var scenario = report.startScenario("ShopApiClientContractTest", "api.reporting",
                    "passing test with no captured exchange", List.of("api", "component"));
            var phase = scenario.startPhase(TEST_BODY);
            ShopApiCallAuditTestSupport.publishAuditDetails(phase, null, 0);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);

            var snapshot = report.semanticSnapshot();
            var scenarioSnapshot = snapshot.classes().getFirst().scenarios().getFirst();
            assertEquals(AtfReportStatus.PASS, scenarioSnapshot.status());
            assertEquals(0, snapshot.requestCount());
            assertEquals(0, snapshot.validationCount());
            assertEquals(List.of(AUDIT_DETAIL), scenarioSnapshot.phases().getFirst().detailNames());
        }

        var html = Files.readString(output);
        assertTrue(html.contains("0 actual API exchanges captured"));
    }

    @Test
    void failedJUnitOutcomeRemainsScenarioFailureWithItsActualMessage() throws Exception {
        var output = Files.createTempDirectory("shop-api-audit-failure-").resolve("audit.html");
        try (var report = new ExtentReportSession(output)) {
            var scenario = report.startScenario("ShopApiClientContractTest", "api.reporting",
                    "failed assertion", List.of("api", "component"));
            var phase = scenario.startPhase(TEST_BODY);
            ShopApiCallAuditTestSupport.publishAuditDetails(
                    phase, new AssertionError("observed status mismatch"), 1);
            phase.complete(AtfReportStatus.FAIL);
            scenario.complete(AtfReportStatus.FAIL);

            var scenarioSnapshot = report.semanticSnapshot().classes().getFirst().scenarios().getFirst();
            assertEquals(AtfReportStatus.FAIL, scenarioSnapshot.status());
            assertEquals(List.of(AUDIT_DETAIL, "JUnit failure"),
                    scenarioSnapshot.phases().getFirst().detailNames());
        }

        var html = Files.readString(output);
        assertTrue(html.contains("AssertionError: observed status mismatch"));
        assertFalse(html.contains("Test method outcome"));
    }

    private static int divDepthAt(String html, int index) {
        var tags = java.util.regex.Pattern.compile("(?i)</?div\\b[^>]*>");
        var matcher = tags.matcher(html.substring(0, index));
        int depth = 0;
        while (matcher.find()) {
            depth += matcher.group().startsWith("</") ? -1 : 1;
        }
        return depth;
    }
}
