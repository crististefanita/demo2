package atf.ui.tests.extent.audit;

import atf.api.mock.MockRoute;
import atf.ui.tests.extent.support.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/** Owns deep, order-independent JSON tree reconciliation against YAML. */
final class MockResponseJsonValidator {
    ValidationResult validate(MockRoute expected, String actualBody) {
        String expectedDocument = expected.body() == null || expected.body().isBlank()
                ? "null" : expected.body();
        String actualDocument = actualBody == null || actualBody.isBlank() ? "" : actualBody.trim();
        if (actualDocument.isBlank()) {
            return ValidationResult.ofViolations(List.of(expected.key()
                    + " rendered an empty response JSON document"));
        }
        try {
            JsonNode expectedTree = MockContractValueSupport.JSON.readTree(expectedDocument);
            JsonNode actualTree = MockContractValueSupport.JSON.readTree(actualDocument);
            if (!expectedTree.equals(actualTree)) {
                return ValidationResult.ofViolations(List.of(expected.key()
                        + " response JSON differs from mock YAML body [expected="
                        + MockContractValueSupport.compact(expectedTree) + ", actual="
                        + MockContractValueSupport.compact(actualTree) + "]"));
            }
            return ValidationResult.empty();
        } catch (Exception error) {
            return ValidationResult.ofViolations(List.of(expected.key()
                    + " response JSON cannot be compared: " + error.getMessage()));
        }
    }
}
