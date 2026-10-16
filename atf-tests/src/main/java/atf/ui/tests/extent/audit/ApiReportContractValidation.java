package atf.ui.tests.extent.audit;

import atf.api.core.reporting.AtfAssertion;
import atf.ui.tests.extent.flows.ApiReportUiFlow;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ValidationResult;
import com.microsoft.playwright.Locator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Read-only semantic validation of API request cards rendered by Extent.
 * Selectors stay in {@link ReportUiLocators}; this class only turns DOM
 * evidence into contract results and never performs navigation.
 */
public final class ApiReportContractValidation {
    private final ReportUiLocators locators;
    private final ApiJsonEvidenceAuditor jsonAuditor;
    private final MockReportContractAuditor mockAuditor;

    public ApiReportContractValidation(ReportUiLocators locators,
                                       MockReportContractAuditor mockAuditor) {
        this.locators = Objects.requireNonNull(locators, "locators");
        this.jsonAuditor = new ApiJsonEvidenceAuditor();
        this.mockAuditor = Objects.requireNonNull(mockAuditor, "mockAuditor");
    }

    public ApiReportContractValidation(atf.ui.tests.extent.flows.ApiReportUiFlow flow,
                                       MockReportContractAuditor mockAuditor) {
        this(Objects.requireNonNull(flow, "flow").locators(), mockAuditor);
    }

    public Result inspect(ApiReportUiFlow.Owner owner) {
        List<String> violations = new ArrayList<>();
        List<RequestAudit> requestsAudited = new ArrayList<>();
        int requestCount = 0;
        Locator scenarios = locators.scenarios(owner.detail());
        for (int scenarioIndex = 0; scenarioIndex < scenarios.count(); scenarioIndex++) {
            Locator scenario = scenarios.nth(scenarioIndex);
            for (int phaseIndex = 0; phaseIndex < locators.phases(scenario).count(); phaseIndex++) {
                Locator phase = locators.phases(scenario).nth(phaseIndex);
                Locator requests = locators.requestsInPhase(phase);
                for (int requestIndex = 0; requestIndex < requests.count(); requestIndex++) {
                    Locator request = requests.nth(requestIndex);
                    requestCount++;
                    String title = locators.requestTitle(request);
                    String url = locators.responseUrl(request);
                    ApiJsonEvidenceAuditor.Result json = jsonAuditor.inspect(request);
                    MockReportContractAuditor.Result mock = mockAuditor.inspect(
                            title, url,
                            locators.responseStatus(request), json,
                            new MockReportContractAuditor.RenderedEvidence(
                                    locators.requestFields(request),
                                    locators.responseFields(request),
                                    locators.executionFields(request)));
                    if (!json.compliant()) {
                        violations.add(owner.name() + " / " + title
                                + " JSON: " + json.violations());
                    }
                    if (!mock.compliant()) {
                        violations.add(owner.name() + " / " + title
                                + " mock: " + mock.violations());
                    }
                    requestsAudited.add(new RequestAudit(title, url,
                            locators.responseStatus(request), json.requestHasPayload(),
                            json.responseHasPayload(), json.compliant() && mock.compliant()));
                }
            }
        }
        if (requestCount == 0) {
            violations.add(owner.name() + " rendered no visible API request cards");
        }
        return new Result(owner.name(), requestCount, requestsAudited, violations);
    }

    public List<AtfAssertion> assertions(Result result) {
        return List.of(ReportAuditChecks.check(
                result.ownerName() + " API requests",
                "all rendered requests match mock status and JSON trees",
                result.requestCount() + " requests; "
                        + (result.violations().isEmpty() ? "no violations" : result.violations()),
                result.compliant()));
    }

    public record Result(String ownerName, int requestCount, List<RequestAudit> requestsAudited,
                         List<String> violations) {
        public Result(String ownerName, int requestCount, List<String> violations) {
            this(ownerName, requestCount, List.of(), violations);
        }

        public Result {
            ownerName = Objects.requireNonNull(ownerName, "ownerName");
            requestsAudited = List.copyOf(Objects.requireNonNull(requestsAudited, "requestsAudited"));
            violations = List.copyOf(Objects.requireNonNull(violations, "violations"));
        }

        public boolean compliant() { return requestCount > 0 && violations.isEmpty(); }

        public ValidationResult validation() {
            return ValidationResult.ofViolations(violations);
        }

        public String routeSummary() {
            return requestsAudited.stream().map(RequestAudit::summary).toList().toString();
        }
    }

    public record RequestAudit(String title, String url, int status,
                               boolean requestHasPayload, boolean responseHasPayload,
                               boolean compliant) {
        public String summary() {
            String identity = (title == null || title.isBlank()) ? "<untitled request>" : title;
            return identity + " -> HTTP " + status
                    + " [requestJson=" + (requestHasPayload ? "present" : "null")
                    + ", responseJson=" + (responseHasPayload ? "present" : "null")
                    + ", " + (compliant ? "PASS" : "FAIL") + "]";
        }
    }
}
