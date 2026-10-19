package atf.api.reporting.api.contract.internal;

import atf.api.reporting.api.contract.ApiReportEvidenceField;
import atf.api.reporting.api.contract.ApiReportRouteContract;
import atf.api.reporting.api.contract.ApiReportContractInput;
import atf.api.reporting.audit.ValidationResult;

import java.util.ArrayList;
import java.util.List;

/** Validates execution evidence against the request card identity. */
public final class ApiReportExecutionEvidenceValidator {
    public ValidationResult validate(ApiReportRouteContract expected, ApiReportContractInput input) {
        List<String> violations = new ArrayList<>();
        String status = ApiReportContractValueSupport.value(input.executionFields(),
                ApiReportEvidenceField.RESPONSE_STATUS.key());
        if (!status.isBlank() && !String.valueOf(input.actualStatus()).equals(status)) {
            violations.add(expected.key() + " execution evidence response.status differs from report status");
        }
        String url = ApiReportContractValueSupport.value(input.executionFields(),
                ApiReportEvidenceField.RESPONSE_URL.key());
        if (!url.isBlank() && !input.requestUrl().isBlank() && !input.requestUrl().equals(url)) {
            violations.add(expected.key() + " execution evidence response.url differs from request URL");
        }
        return ValidationResult.ofViolations(violations);
    }
}
