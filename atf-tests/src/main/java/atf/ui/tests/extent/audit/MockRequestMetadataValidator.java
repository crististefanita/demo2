package atf.ui.tests.extent.audit;

import atf.api.core.http.ApiQueryParameter;
import atf.api.mock.MockRoute;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Owns auth, query and required-request-header reconciliation. */
final class MockRequestMetadataValidator {
    ValidationResult validate(MockRoute expected, String requestUrl, Map<String, String> fields) {
        List<String> violations = new ArrayList<>();
        String actualProfile = fields.getOrDefault(MockReportEvidenceField.AUTH_PROFILE.key(), "").trim();
        if (!"*".equals(expected.authProfile()) && !expected.authProfile().equals(actualProfile)) {
            violations.add(expected.key() + " expected AUTH PROFILE " + expected.authProfile()
                    + " but report rendered " + (actualProfile.isBlank() ? "<missing>" : actualProfile));
        }
        var query = MockContractValueSupport.queryFromUrl(requestUrl);
        if (!query.containsAll(expected.queryParameters())) {
            String queryNames = expected.queryParameters().values().stream().map(ApiQueryParameter::name)
                    .distinct().collect(java.util.stream.Collectors.joining(","));
            violations.add(expected.key() + " expected query " + queryNames + " parameters "
                    + expected.queryParameters().display() + " but report rendered " + query.display());
        }
        Map<String, String> actualHeaders = MockContractValueSupport.parseMap(
                fields.get(MockReportEvidenceField.REQUEST_HEADERS.key()));
        expected.requiredHeaders().forEach((key, value) -> {
            String actual = MockContractValueSupport.caseInsensitiveValue(actualHeaders, key);
            if (!value.equals(actual)) {
                violations.add(expected.key() + " expected request header " + key + "=" + value
                        + " but report rendered " + (actual == null ? "<missing>" : actual));
            }
        });
        return ValidationResult.ofViolations(violations);
    }
}
