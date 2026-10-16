package atf.ui.tests.extent.audit;

import atf.api.core.http.HttpHeader;
import atf.api.core.http.MediaTypes;
import atf.api.mock.MockRequestBodyMode;
import atf.api.mock.MockRoute;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.ArrayList;
import java.util.List;

/** Owns POST/PUT request-body and request/response JSON body policy. */
final class MockRequestBodyValidator {
    ValidationResult validate(MockRoute expected, ApiJsonEvidenceAuditor.Result evidence,
                              MockReportContractAuditor.RenderedEvidence rendered) {
        if (!atf.api.core.http.JsonBodyPolicy.appliesTo(expected.method())) {
            return ValidationResult.empty();
        }
        List<String> violations = new ArrayList<>();
        String body = evidence == null ? "" : safe(evidence.requestBody());
        boolean hasPayload = MockContractValueSupport.hasPayload(body);
        boolean validJson = MockContractValueSupport.isJson(body);
        boolean requestHeadersRendered = rendered.requestFields().containsKey(
                MockReportEvidenceField.REQUEST_HEADERS.key());
        String renderedHeaders = MockContractValueSupport.headerValue(rendered.requestFields(),
                        MockReportEvidenceField.REQUEST_HEADERS.key())
                .toLowerCase(java.util.Locale.ROOT);
        String jsonHeader = (HttpHeader.CONTENT_TYPE.value() + "=" + MediaTypes.APPLICATION_JSON)
                .toLowerCase(java.util.Locale.ROOT);
        boolean jsonContentType = renderedHeaders.contains(jsonHeader);
        switch (MockRequestBodyMode.from(expected.requestBodyMode())) {
            case JSON -> {
                if (!hasPayload) violations.add(expected.key() + " must expose a non-empty JSON request body");
                else if (!validJson) violations.add(expected.key() + " must expose a valid JSON request body");
                if (requestHeadersRendered && !jsonContentType) {
                    violations.add(expected.key() + " must expose " + HttpHeader.CONTENT_TYPE.value()
                            + " " + MediaTypes.APPLICATION_JSON);
                }
            }
            case WRONG_CONTENT_TYPE -> {
                if (!hasPayload || !validJson) violations.add(expected.key()
                        + " must expose a valid JSON body for the wrong-content-type outcome");
                if (requestHeadersRendered && jsonContentType) {
                    violations.add(expected.key()
                            + " must expose a non-JSON Content-Type for the wrong-content-type outcome");
                }
            }
            default -> violations.add(expected.key() + " has unsupported request-body mode: "
                    + expected.requestBodyMode());
        }
        if (expected.expectedRequestBody() != null && validJson
                && !MockContractValueSupport.sameJson(expected.expectedRequestBody(), body)) {
            violations.add(expected.key() + " request JSON differs from mock YAML request-body");
        }
        if (!MockContractValueSupport.hasPayload(evidence == null ? null : evidence.responseBody())
                || !MockContractValueSupport.isJson(evidence.responseBody())) {
            violations.add(expected.key() + " must expose a non-empty JSON response body");
        }
        return ValidationResult.ofViolations(violations);
    }

    private static String safe(String value) { return value == null ? "" : value.trim(); }
}
