package atf.ui.tests.extent.audit;

import atf.api.core.http.HttpHeader;
import atf.api.mock.MockRoute;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Owns response status, media type and response-header reconciliation. */
final class MockResponseMetadataValidator {
    ValidationResult validate(MockRoute expected, int actualStatus,
                              MockReportContractAuditor.RenderedEvidence rendered) {
        List<String> violations = new ArrayList<>();
        Map<String, String> actualHeaders = MockContractValueSupport.parseMap(
                rendered.responseFields().get("RESPONSE HEADERS"));
        expected.headers().entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase(HttpHeader.CONTENT_TYPE.value()))
                .forEach(entry -> {
                    String actual = MockContractValueSupport.caseInsensitiveValue(actualHeaders, entry.getKey());
                    if (!entry.getValue().equals(actual)) {
                        violations.add(expected.key() + " expected response header " + entry.getKey()
                                + "=" + entry.getValue() + " but report rendered "
                                + (actual == null ? "<missing>" : actual));
                    }
                });
        String contentType = rendered.responseFields().getOrDefault("CONTENT-TYPE", "");
        String expectedContentType = MockContractValueSupport.caseInsensitiveValue(
                expected.headers(), HttpHeader.CONTENT_TYPE.value());
        if (expectedContentType != null && !expectedContentType.equals(contentType)) {
            violations.add(expected.key() + " expected CONTENT-TYPE " + expectedContentType
                    + " but report rendered " + (contentType.isBlank() ? "<missing>" : contentType));
        }
        String renderedStatus = rendered.responseFields().getOrDefault("HTTP STATUS", "");
        if (!renderedStatus.isBlank() && !String.valueOf(actualStatus).equals(renderedStatus)) {
            violations.add(expected.key() + " response evidence status differs from execution status");
        }
        return ValidationResult.ofViolations(violations);
    }
}
