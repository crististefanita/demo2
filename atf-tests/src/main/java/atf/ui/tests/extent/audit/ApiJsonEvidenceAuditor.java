package atf.ui.tests.extent.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Locator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only browser audit for the two JSON payload disclosures of one API
 * request node. The caller owns navigation and disclosure clicks; this class
 * only evaluates the rendered evidence contract. A body-less HTTP exchange
 * is still compliant when the renderer publishes the explicit JSON document
 * {@code null}; absence of a payload is not absence of evidence.
 */
public final class ApiJsonEvidenceAuditor {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final ReportUiLocators locators;

    public ApiJsonEvidenceAuditor() {
        this(new ReportUiLocators());
    }

    public ApiJsonEvidenceAuditor(ReportUiLocators locators) {
        this.locators = locators;
    }

    public Result inspect(Locator requestNode) {
        List<String> violations = new ArrayList<>();
        String requestBody = body(locators.requestJsonBodyElements(requestNode), "request", violations);
        String responseBody = body(locators.responseJsonBodyElements(requestNode), "response", violations);
        validateJson(requestBody, "request", violations);
        validateJson(responseBody, "response", violations);
        return new Result(requestBody, responseBody, violations);
    }

    private static String body(Locator bodies, String side, List<String> violations) {
        // Some lifecycle cards add one structural wrapper around the request
        // collapse. The request locator is already scoped to one request, so
        // descendant lookup keeps the audit independent of that wrapper.
        if (bodies.count() != 1) {
            violations.add(side + " JSON body count must be exactly 1 but was " + bodies.count());
            return "";
        }
        Locator body = bodies.first();
        String tagName = String.valueOf(body.evaluate("element => element.tagName"));
        String value = "TEXTAREA".equalsIgnoreCase(tagName)
                ? body.inputValue().trim()
                : body.innerText().trim();
        if (value.isBlank()) violations.add(side + " JSON body must not be blank");
        return value;
    }

    private static void validateJson(String value, String side, List<String> violations) {
        if (value.isBlank()) return;
        try {
            JSON.readTree(value);
        } catch (Exception error) {
            violations.add(side + " body is not valid JSON: " + error.getMessage());
        }
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
