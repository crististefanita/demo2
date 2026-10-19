package atf.api.reporting.ui.audit.section;

import atf.api.reporting.audit.ReportAuditChecks;
import atf.api.reporting.audit.ReportAuditSnapshot;
import atf.api.reporting.audit.ValidationResult;
import atf.api.reporting.ui.audit.plan.ReportProjection;
import atf.api.core.reporting.contract.AtfReportStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Pure contract for the Fails projection, including its empty-state branch. */
public final class FailsSectionValidator {
    public ValidationResult validate(ReportAuditSnapshot.Failures snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        boolean emptyState = snapshot.failureCount() == 0
                && snapshot.primaryCauseCount() == 0
                && snapshot.openLinkCount() == 0
                && snapshot.emptyState();
        boolean actionable = snapshot.failureCount() > 0
                && snapshot.primaryCauseCount() > 0
                && snapshot.openLinkCount() > 0;
        return ValidationResult.of(List.of(
                ReportAuditChecks.check("Failure rows", "0 or > 0", String.valueOf(snapshot.failureCount()),
                        emptyState || actionable),
                ReportAuditChecks.check("Primary causes", snapshot.failureCount() == 0 ? "0" : "> 0",
                        String.valueOf(snapshot.primaryCauseCount()), snapshot.failureCount() == 0
                                ? snapshot.primaryCauseCount() == 0 : snapshot.primaryCauseCount() > 0),
                ReportAuditChecks.check("Open in Tests", snapshot.failureCount() == 0 ? "0" : "> 0",
                        String.valueOf(snapshot.openLinkCount()), snapshot.failureCount() == 0
                                ? snapshot.openLinkCount() == 0 : snapshot.openLinkCount() > 0),
                ReportAuditChecks.check("Empty state", snapshot.failureCount() == 0 ? "true" : "false",
                        String.valueOf(snapshot.emptyState()), snapshot.failureCount() == 0
                                ? snapshot.emptyState() : !snapshot.emptyState())));
    }

    public ValidationResult validate(ReportAuditSnapshot.Failures snapshot, ReportProjection projection) {
        ValidationResult structural = validate(snapshot);
        boolean tabProjected = projection.tabs().stream().anyMatch(tab -> "Fails".equalsIgnoreCase(tab));
        List<atf.api.core.reporting.contract.AtfAssertion> checks = new ArrayList<>(List.of(
                ReportAuditChecks.check("Expected Fails tab", "declared by projection",
                        projection.tabs().toString(), tabProjected)));
        if (projection.hasStatusExpectation()) {
            long expectedFailures = projection.expectedStatusCount(AtfReportStatus.FAIL);
            checks.add(ReportAuditChecks.check("Projected failure rows", String.valueOf(expectedFailures),
                    String.valueOf(snapshot.failureCount()), expectedFailures == snapshot.failureCount()));
        }
        return structural.merge(ValidationResult.of(checks));
    }
}
