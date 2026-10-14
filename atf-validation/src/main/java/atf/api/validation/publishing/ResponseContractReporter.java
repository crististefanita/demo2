package atf.api.validation.publishing;

import atf.api.core.http.ApiResponse;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.ValidationSink;
import atf.api.validation.contract.ResponseContract;
import atf.api.validation.evaluation.ApiValidationException;
import atf.api.validation.evaluation.ValidationDecision;
import atf.api.validation.evaluation.ValidationEvaluator;

/**
 * Publishes one response contract as one validation sibling of its request.
 * The evaluator is independent of Extent; this class only crosses the
 * renderer-neutral reporting port.
 */
public final class ResponseContractReporter {
    private final ValidationEvaluator evaluator;

    public ResponseContractReporter() {
        this(new ValidationEvaluator());
    }

    public ResponseContractReporter(ValidationEvaluator evaluator) {
        this.evaluator = java.util.Objects.requireNonNull(evaluator, "evaluator");
    }

    public ValidationDecision validate(ValidationSink request, String title,
                                       ApiResponse response, ResponseContract contract,
                                       boolean warningMode) {
        if (request == null || title == null || title.isBlank()) throw new IllegalArgumentException("request/title");
        var validation = request.startValidation(title);
        try {
            var decision = evaluator.evaluate(contract, response, warningMode);
            validation.assertions(decision.rows());
            validation.complete(decision.status());
            if (decision.shouldThrow()) {
                throw new ApiValidationException(decision.message());
            }
            return decision;
        } catch (RuntimeException failure) {
            try {
                validation.assertions(java.util.List.of(new atf.api.core.reporting.AtfAssertion(
                        "Validation execution", "rule evaluation completes", failure.getMessage(), false,
                        AtfReportStatus.FAIL)));
                validation.complete(AtfReportStatus.FAIL);
            } catch (RuntimeException ignored) {
                // Preserve the functional failure; reporting is best effort at this boundary.
            }
            throw failure;
        } finally {
            validation.close();
        }
    }
}
