package atf.api.reporting.api.contract;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable projection of one rendered API request card for contract auditing. */
public record ApiReportContractInput(
        String requestTitle,
        String requestUrl,
        int actualStatus,
        String requestBody,
        String responseBody,
        List<String> evidenceViolations,
        Map<String, String> requestFields,
        Map<String, String> responseFields,
        Map<String, String> executionFields) {

    public ApiReportContractInput {
        requestTitle = requestTitle == null ? "" : requestTitle;
        requestUrl = requestUrl == null ? "" : requestUrl;
        requestBody = requestBody == null ? "" : requestBody;
        responseBody = responseBody == null ? "" : responseBody;
        evidenceViolations = evidenceViolations == null ? List.of() : List.copyOf(evidenceViolations);
        requestFields = copy(requestFields);
        responseFields = copy(responseFields);
        executionFields = copy(executionFields);
    }

    public static ApiReportContractInput of(String requestTitle, String requestUrl,
                                            int actualStatus, String requestBody,
                                            String responseBody) {
        return new ApiReportContractInput(requestTitle, requestUrl, actualStatus,
                requestBody, responseBody, List.of(), Map.of(), Map.of(), Map.of());
    }

    public boolean hasRenderedMetadata() {
        return !requestFields.isEmpty() || !responseFields.isEmpty() || !executionFields.isEmpty();
    }

    private static Map<String, String> copy(Map<String, String> values) {
        return values == null ? Map.of() : Map.copyOf(Objects.requireNonNull(values));
    }
}
