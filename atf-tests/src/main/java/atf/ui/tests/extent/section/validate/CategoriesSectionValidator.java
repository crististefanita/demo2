package atf.ui.tests.extent.section.validate;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.List;
import java.util.Objects;

/** Pure contract for Categories; cardinality remains data-driven. */
public final class CategoriesSectionValidator {
    public ValidationResult validate(ReportAuditSnapshot.Annotations snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        boolean cardinalities = snapshot.hasConsistentLinkCardinality()
                && snapshot.linkCardinalities().stream().allMatch(value -> value >= 0);
        return ValidationResult.of(List.of(
                ReportAuditChecks.check("Categories", "> 0", String.valueOf(snapshot.categoryCount()),
                        snapshot.categoryCount() > 0),
                ReportAuditChecks.check("Linked scenarios", "> 0", String.valueOf(snapshot.linkedScenarioCount()),
                        snapshot.linkedScenarioCount() > 0),
                ReportAuditChecks.check("Category surface", "styled", snapshot.filterSurface(),
                        snapshot.styled()),
                ReportAuditChecks.check("Link cardinality", "dynamic zero/one/many",
                        snapshot.linkCardinalities().toString(), cardinalities)));
    }
}
