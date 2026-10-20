package atf.api.reporting.html;
import atf.api.testkit.AtfUnitTest;
import atf.api.core.http.response.ApiAttemptOutcome;
import atf.api.core.http.response.ApiAttemptRecord;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.response.ApiResponse;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.model.ReportField;
import atf.api.reporting.support.ApiReportTestUrls;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class HtmlReportViewsTest {
    @Test
    void projectionsReadTheSameSemanticDocumentWithoutRebuildingVerdicts() {
        var document = new HtmlReportSemanticDocument();
        var scenario = document.startScenario("UsersTests", "user contract", List.of("api", "contract"));
        var phase = new HtmlReportSemanticDocument.PhaseModel("TEST BODY");
        var request = new HtmlReportSemanticDocument.RequestModel("item", "GET", ApiReportTestUrls.GENERIC_USER_ONE);
        request.status = AtfReportStatus.FAIL;
        var validation = new HtmlReportSemanticDocument.ValidationModel("Validate user");
        validation.assertions = List.of(new AtfAssertion("HTTP status", "201 Created", "200 OK", false));
        validation.status = AtfReportStatus.FAIL;
        phase.children.add(request);
        phase.children.add(validation);
        phase.status = AtfReportStatus.FAIL;
        scenario.phases.add(phase);
        scenario.status = AtfReportStatus.FAIL;

        var scenarios = document.snapshot();
        var tests = new HtmlReportTestsView().render(scenarios);
        var category = new HtmlReportCategoryView().render(scenarios);
        var fails = new HtmlReportFailsView().render(scenarios);
        var overview = new HtmlReportOverviewView().render(scenarios);

        assertAll(
                () -> assertTrue(tests.indexOf("class=\"request\"") < tests.indexOf("class=\"validation\"")),
                () -> assertTrue(category.contains("class=\"tag\">api</span>")),
                () -> assertTrue(fails.contains("HTTP status") && fails.contains("200 OK")),
                () -> assertTrue(overview.contains("<strong>1</strong>")));
    }

    @Test
    void executionEvidenceUsesTheSameRedactionBoundaryAsTheOtherRequestEvidence() {
        var document = new HtmlReportSemanticDocument();
        var scenario = document.startScenario("RetryTests", "retry evidence", List.of("api"));
        var phase = new HtmlReportSemanticDocument.PhaseModel("TEST BODY");
        var request = new HtmlReportSemanticDocument.RequestModel("retry", "GET", ApiReportTestUrls.GENERIC_USERS);
        request.execution(new ApiExecutionResult(
                new ApiResponse(200, "ignored", Map.of(), Duration.ofMillis(301),
                        ApiReportTestUrls.example(ApiReportTestUrls.GENERIC_USERS
                                + "?token=raw-url-token&mode=full")),
                2,
                Duration.ofMillis(18),
                List.of(),
                List.of(
                        new ApiAttemptRecord(1, 429, Duration.ofMillis(5),
                                ApiAttemptOutcome.RETRYABLE_RESPONSE, "retry token=raw-retry-token"),
                        new ApiAttemptRecord(2, 200, Duration.ofMillis(12),
                                ApiAttemptOutcome.SUCCESS, "completed"))));
        phase.children.add(request);
        scenario.phases.add(phase);

        String html = new HtmlReportTestsView().render(document.snapshot());

        assertAll(
                () -> assertTrue(html.contains("execution-evidence")),
                () -> assertTrue(html.contains("retry-history-table")),
                () -> assertTrue(html.contains("response.duration.ms")),
                () -> assertTrue(html.contains("<th>response.duration.ms</th><td>0.301 s</td>")),
                () -> assertTrue(html.contains("<td>1</td><td>429</td><td>5 ms</td><td>RETRYABLE_RESPONSE</td>")),
                () -> assertTrue(html.contains("<td>2</td><td>200</td><td>12 ms</td><td>SUCCESS</td>")),
                () -> assertTrue(html.contains("***REDACTED***")),
                () -> assertTrue(html.contains("mode=full")),
                () -> assertFalse(html.contains("raw-url-token")),
                () -> assertFalse(html.contains("raw-retry-token")));
    }

    @Test
    void fallbackPublishesPhaseFailureCauseWithTheSameRedactionBoundary() {
        var document = new HtmlReportSemanticDocument();
        var scenario = document.startScenario("FailureTests", "redacted failure", List.of("api"));
        var phase = new HtmlReportSemanticDocument.PhaseModel("TEST BODY");
        phase.status = AtfReportStatus.FAIL;
        phase.details.add(new ReportField("Failure cause", "ApiValidationException: token=raw-failure-token; mode=full"));
        scenario.phases.add(phase);
        scenario.status = AtfReportStatus.FAIL;

        String html = new HtmlReportTestsView().render(document.snapshot());

        assertAll(
                () -> assertTrue(html.contains("phase-details")),
                () -> assertTrue(html.contains("Failure cause")),
                () -> assertTrue(html.contains("token=***REDACTED***")),
                () -> assertTrue(html.contains("mode=full")),
                () -> assertFalse(html.contains("raw-failure-token")));
    }
}
