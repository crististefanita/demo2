package atf.ui.tests.extent.section.validate;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Pure validator for the Tests section; it has no Page, Locator or JUnit dependency. */
public final class TestsSectionValidator {
    private final ExpectedReportProjectionValidator projectionValidator =
            new ExpectedReportProjectionValidator();

    public ValidationResult validateProjection(atf.ui.tests.extent.plan.ExpectedReportProjection projection,
                                               ReportAuditSnapshot.Tests snapshot) {
        return projectionValidator.validate(projection, snapshot);
    }

    public ValidationResult validate(ReportAuditSnapshot.Tests snapshot,
                                     Collection<String> expectedOwners) {
        Objects.requireNonNull(snapshot, "snapshot");
        Set<String> expected = new LinkedHashSet<>(Objects.requireNonNull(expectedOwners, "expectedOwners"));
        Set<String> actual = new LinkedHashSet<>(snapshot.classNames());
        boolean ownersValid = expected.isEmpty() ? !actual.isEmpty() : actual.equals(expected);
        return ValidationResult.of(List.of(
                ReportAuditChecks.check("Owners", expected.isEmpty() ? "> 0" : expected.toString(),
                        actual.toString(), ownersValid),
                ReportAuditChecks.check("Scenarios", "> 0", String.valueOf(snapshot.scenarioCount()),
                        snapshot.scenarioCount() > 0),
                ReportAuditChecks.check("Phases", ">= scenarios", String.valueOf(snapshot.phaseCount()),
                        snapshot.phaseCount() >= snapshot.scenarioCount()),
                ReportAuditChecks.check("Requests", "> 0", String.valueOf(snapshot.requestCount()),
                        snapshot.requestCount() > 0),
                ReportAuditChecks.check("Validation cards", "> 0", String.valueOf(snapshot.validationCount()),
                        snapshot.validationCount() > 0)));
    }

    /** Validates the structural contract and the setup/mock projection together. */
    public ValidationResult validate(ReportAuditSnapshot.Tests snapshot,
                                     Collection<String> expectedOwners,
                                     atf.ui.tests.extent.plan.ExpectedReportProjection projection) {
        return validate(snapshot, expectedOwners)
                .merge(projectionValidator.validate(projection, snapshot));
    }
}

