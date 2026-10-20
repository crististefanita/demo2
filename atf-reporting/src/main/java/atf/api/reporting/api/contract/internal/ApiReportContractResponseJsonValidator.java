package atf.api.reporting.api.contract.internal;

import atf.api.reporting.api.contract.ApiReportRouteContract;
import atf.api.reporting.audit.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/** Compares response JSON as a tree, independent of whitespace and field order. */
public final class ApiReportContractResponseJsonValidator {
    public ValidationResult validate(ApiReportRouteContract expected, String actualBody) {
        String expectedDocument = expected.responseBody().isBlank() ? "null" : expected.responseBody();
        String actualDocument = actualBody == null ? "" : actualBody.trim();
        if (actualDocument.isBlank()) {
            return ValidationResult.ofViolations(List.of(expected.key()
                    + " rendered an empty response JSON document"));
        }
        try {
            JsonNode expectedTree = ApiReportContractValueSupport.JSON.readTree(expectedDocument);
            JsonNode actualTree = ApiReportContractValueSupport.JSON.readTree(actualDocument);
            if (!expectedTree.equals(actualTree)) {
                return ValidationResult.ofViolations(List.of(expected.key()
                        + " response JSON differs from configured catalog body [expected="
                        + ApiReportContractValueSupport.compact(expectedTree) + ", actual="
                        + ApiReportContractValueSupport.compact(actualTree) + "]"));
            }
            return ValidationResult.empty();
        } catch (Exception error) {
            return ValidationResult.ofViolations(List.of(expected.key()
                    + " response JSON cannot be compared: " + error.getMessage()));
        }
    }
}
