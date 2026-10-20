package atf.ui.tests.extent.support.audit.api;
import atf.api.reporting.audit.ValidationResult;

import com.microsoft.playwright.Locator;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import atf.ui.tests.extent.support.section.extract.ApiJsonEvidenceExtractor;
import atf.api.reporting.ui.audit.section.JsonEvidenceValidator;

import java.util.List;

/**
 * Read-only browser audit for the two JSON payload disclosures of one API
 * request node. The caller owns navigation and disclosure clicks; this class
 * only evaluates the rendered evidence contract. A body-less HTTP exchange
 * is still compliant when the renderer publishes the explicit JSON document
 * {@code null}; absence of a payload is not absence of evidence.
 */
public final class ApiJsonEvidenceAuditor {
    private final ApiJsonEvidenceExtractor extractor;
    private final JsonEvidenceValidator validator = new JsonEvidenceValidator();

    public ApiJsonEvidenceAuditor() {
        this(new ReportUiLocators());
    }

    public ApiJsonEvidenceAuditor(ReportUiLocators locators) {
        this.extractor = new ApiJsonEvidenceExtractor(locators);
    }

    public Result inspect(Locator requestNode) {
        var snapshot = extractor.extract(requestNode);
        ValidationResult validation = validator.validate(snapshot);
        return new Result(snapshot.requestBody(), snapshot.responseBody(), validation.violations());
    }

    public record Result(String requestBody, String responseBody, List<String> violations) {
        public Result {
            violations = violations == null ? List.of() : List.copyOf(violations);
        }

        public boolean compliant() {
            return isJsonDocument(requestBody)
                    && isJsonDocument(responseBody) && violations.isEmpty();
        }

        public ValidationResult validation() {
            return ValidationResult.ofViolations(violations);
        }

        public boolean requestHasPayload() {
            return !isExplicitEmpty(requestBody);
        }

        public boolean responseHasPayload() {
            return !isExplicitEmpty(responseBody);
        }

        private static boolean isJsonDocument(String body) {
            return body != null && !body.isBlank();
        }

        private static boolean isExplicitEmpty(String body) {
            return "null".equals(body == null ? "" : body.trim());
        }
    }
}
