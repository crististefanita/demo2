package atf.api.validation.evaluation;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfReportStatus;

import java.util.List;

public record ValidationDecision(
        List<AtfAssertion> rows,
        AtfReportStatus status,
        String message,
        boolean shouldThrow,
        ValidationTable table) {
    public ValidationDecision(List<AtfAssertion> rows, AtfReportStatus status, String message, boolean shouldThrow) {
        this(rows, status, message, shouldThrow, null);
    }

    public ValidationDecision {
        rows = List.copyOf(rows);
    }

    /** Returns the compact explanation for the failed contract rows. */
    public ValidationFailureEvidence failureEvidence() {
        if (status != AtfReportStatus.FAIL) {
            throw new IllegalStateException("failure evidence is available only for failed decisions");
        }
        return ValidationFailureEvidence.from(rows);
    }
}
