package atf.ui.tests.extent.audit;

import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.MockRoute;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Stable facade for the mock-backed report contract.
 *
 * <p>Route identity, request-body policy, request metadata, response metadata,
 * execution evidence and deep JSON comparison are separate collaborators.
 * Existing callers keep this facade and the same result vocabulary.</p>
 */
public final class MockReportContractAuditor {
    private final MockRouteResolver routeResolver;
    private final MockRequestBodyValidator requestBodyValidator = new MockRequestBodyValidator();
    private final MockRequestMetadataValidator requestMetadataValidator = new MockRequestMetadataValidator();
    private final MockResponseMetadataValidator responseMetadataValidator = new MockResponseMetadataValidator();
    private final MockExecutionEvidenceValidator executionEvidenceValidator = new MockExecutionEvidenceValidator();
    private final MockResponseJsonValidator responseJsonValidator = new MockResponseJsonValidator();

    public MockReportContractAuditor(AtfMockConfiguration mock) {
        routeResolver = new MockRouteResolver(Objects.requireNonNull(mock, "mock"));
    }

    public Result inspect(String requestTitle, int actualStatus,
                          ApiJsonEvidenceAuditor.Result evidence) {
        return inspect(requestTitle, null, actualStatus, evidence);
    }

    public Result inspect(String requestTitle, String requestUrl, int actualStatus,
                          ApiJsonEvidenceAuditor.Result evidence) {
        return inspect(requestTitle, requestUrl, actualStatus, evidence, RenderedEvidence.empty());
    }

    public Result inspect(String requestTitle, String requestUrl, int actualStatus,
                          ApiJsonEvidenceAuditor.Result evidence, RenderedEvidence rendered) {
        MockRouteResolver.Resolution resolution = routeResolver.resolve(requestTitle, requestUrl, actualStatus);
        ValidationResult validation = resolution.validation();
        if (resolution.route().isEmpty()) {
            return new Result(requestTitle, Optional.empty(), validation.violations());
        }
        MockRoute expected = resolution.route().orElseThrow();
        validation = validation.merge(requestBodyValidator.validate(expected, evidence, rendered));
        if (!rendered.requestFields().isEmpty()) {
            validation = validation.merge(requestMetadataValidator.validate(expected, requestUrl,
                    rendered.requestFields()));
        }
        if (!rendered.responseFields().isEmpty()) {
            validation = validation.merge(responseMetadataValidator.validate(expected, actualStatus, rendered));
        }
        if (!rendered.executionFields().isEmpty()) {
            validation = validation.merge(executionEvidenceValidator.validate(expected, requestUrl,
                    actualStatus, rendered.executionFields()));
        }
        validation = validation.merge(responseJsonValidator.validate(expected,
                evidence == null ? null : evidence.responseBody()));
        return new Result(requestTitle, Optional.of(expected), validation.violations());
    }

    public record Result(String requestTitle, Optional<MockRoute> route, List<String> violations) {
        public Result {
            requestTitle = requestTitle == null ? "" : requestTitle;
            route = route == null ? Optional.empty() : route;
            violations = violations == null ? List.of() : List.copyOf(violations);
        }

        public boolean compliant() { return violations.isEmpty(); }

        public ValidationResult validation() { return ValidationResult.ofViolations(violations); }
    }

    public record RenderedEvidence(Map<String, String> requestFields,
                                   Map<String, String> responseFields,
                                   Map<String, String> executionFields) {
        public RenderedEvidence {
            requestFields = requestFields == null ? Map.of() : Map.copyOf(requestFields);
            responseFields = responseFields == null ? Map.of() : Map.copyOf(responseFields);
            executionFields = executionFields == null ? Map.of() : Map.copyOf(executionFields);
        }

        public static RenderedEvidence empty() {
            return new RenderedEvidence(Map.of(), Map.of(), Map.of());
        }
    }
}
