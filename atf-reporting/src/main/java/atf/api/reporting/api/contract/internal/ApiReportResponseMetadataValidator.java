package atf.api.reporting.api.contract.internal;

import atf.api.core.http.request.HttpHeader;
import atf.api.reporting.api.contract.ApiReportEvidenceField;
import atf.api.reporting.api.contract.ApiReportRouteContract;
import atf.api.reporting.api.contract.ApiReportContractInput;
import atf.api.reporting.audit.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Validates response status, content type and response headers. */
public final class ApiReportResponseMetadataValidator {
    public ValidationResult validate(ApiReportRouteContract expected, ApiReportContractInput input) {
        List<String> violations = new ArrayList<>();
        Map<String, String> actualHeaders = ApiReportContractValueSupport.parseMap(
                input.responseFields().get(ApiReportEvidenceField.RESPONSE_HEADERS.key()));
        expected.responseHeaders().entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase(HttpHeader.CONTENT_TYPE.value()))
                .forEach(entry -> {
                    String actual = ApiReportContractValueSupport.caseInsensitiveValue(actualHeaders, entry.getKey());
                    if (!entry.getValue().equals(actual)) {
                        violations.add(expected.key() + " expected response header " + entry.getKey()
                                + "=" + entry.getValue() + " but report rendered "
                                + (actual == null ? "<missing>" : actual));
                    }
                });
        String contentType = ApiReportContractValueSupport.value(input.responseFields(),
                ApiReportEvidenceField.CONTENT_TYPE.key());
        String expectedContentType = ApiReportContractValueSupport.caseInsensitiveValue(
                expected.responseHeaders(), HttpHeader.CONTENT_TYPE.value());
        if (expectedContentType != null && !expectedContentType.equals(contentType)) {
            violations.add(expected.key() + " expected CONTENT-TYPE " + expectedContentType
                    + " but report rendered " + (contentType.isBlank() ? "<missing>" : contentType));
        }
        String renderedStatus = ApiReportContractValueSupport.value(input.responseFields(),
                ApiReportEvidenceField.HTTP_STATUS.key());
        if (!renderedStatus.isBlank() && !String.valueOf(input.actualStatus()).equals(renderedStatus)) {
            violations.add(expected.key() + " response evidence status differs from execution status");
        }
        return ValidationResult.ofViolations(violations);
    }
}
