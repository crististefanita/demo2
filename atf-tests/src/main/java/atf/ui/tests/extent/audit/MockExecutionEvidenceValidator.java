package atf.ui.tests.extent.audit;

import atf.api.mock.MockRoute;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Owns reconciliation of execution status and response URL evidence. */
final class MockExecutionEvidenceValidator {
    ValidationResult validate(MockRoute expected, String requestUrl, int actualStatus,
                              Map<String, String> fields) {
        List<String> violations = new ArrayList<>();
        String status = fields.getOrDefault("response.status", "");
        if (!status.isBlank() && !String.valueOf(actualStatus).equals(status)) {
            violations.add(expected.key() + " execution evidence response.status differs from report status");
        }
        String url = fields.getOrDefault("response.url", "");
        if (!url.isBlank() && requestUrl != null && !requestUrl.isBlank() && !requestUrl.equals(url)) {
            violations.add(expected.key() + " execution evidence response.url differs from request URL");
        }
        return ValidationResult.ofViolations(violations);
    }
}
