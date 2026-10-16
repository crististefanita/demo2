package atf.ui.tests.extent.section.validate;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ValidationResult;

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
                        String.valueOf(snapshot.primaryCauseCount()),
                        snapshot.failureCount() == 0 ? snapshot.primaryCauseCount() == 0 : snapshot.primaryCauseCount() > 0),
                ReportAuditChecks.check("Open in Tests", snapshot.failureCount() == 0 ? "0" : "> 0",
                        String.valueOf(snapshot.openLinkCount()),
                        snapshot.failureCount() == 0 ? snapshot.openLinkCount() == 0 : snapshot.openLinkCount() > 0),
                ReportAuditChecks.check("Empty state", snapshot.failureCount() == 0 ? "true" : "false",
                        String.valueOf(snapshot.emptyState()),
                        snapshot.failureCount() == 0 ? snapshot.emptyState() : !snapshot.emptyState())));
    }

    /**
     * Keeps Fails tied to the same setup projection as Tests. The failure
     * payload may legitimately be empty, but the selected report must still
     * declare Fails as a supported tab.
     */
    public ValidationResult validate(ReportAuditSnapshot.Failures snapshot,
                                     atf.ui.tests.extent.plan.ExpectedReportProjection projection) {
        ValidationResult structural = validate(snapshot);
        boolean tabProjected = projection.tabs().stream()
                .anyMatch(tab -> "Fails".equalsIgnoreCase(tab));
        return structural.merge(ValidationResult.of(List.of(
                ReportAuditChecks.check("Expected Fails tab", "declared by projection",
                        projection.tabs().toString(), tabProjected))));
    }
}

