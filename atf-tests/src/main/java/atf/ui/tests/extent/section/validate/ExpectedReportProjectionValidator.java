package atf.ui.tests.extent.section.validate;

import atf.ui.tests.extent.plan.ExpectedReportProjection;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.List;
import java.util.Objects;

/** Pure comparison between setup/mock expectations and an observed Tests snapshot. */
public final class ExpectedReportProjectionValidator {
    public ValidationResult validate(ExpectedReportProjection expected,
                                     ReportAuditSnapshot.Tests observed) {
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(observed, "observed");
        boolean matches = expected.matches(observed);
        return ValidationResult.of(List.of(
                ReportAuditChecks.check("Expected owners", expectedOwnerNames(expected).toString(),
                        observed.classNames().toString(), sameMultiset(observed.classNames(), expectedOwnerNames(expected))),
                ReportAuditChecks.check("Selected scenario count", String.valueOf(expected.scenarioCount()),
                        String.valueOf(observed.scenarioCount()), observed.scenarioCount() == expected.scenarioCount()),
                ReportAuditChecks.check("Selected scenario titles", expected.scenarioTitles().toString(),
                        observed.scenarioTitles().toString(), sameMultiset(observed.scenarioTitles(), expected.scenarioTitles())),
                ReportAuditChecks.check("Projected phases", ">= " + expected.phaseCount(),
                        String.valueOf(observed.phaseCount()), observed.phaseCount() >= expected.phaseCount()),
                ReportAuditChecks.check("Projected requests", ">= " + expected.requestCount(),
                        String.valueOf(observed.requestCount()), observed.requestCount() >= expected.requestCount()),
                ReportAuditChecks.check("Projected validations", ">= " + expected.requestCount(),
                        String.valueOf(observed.validationCount()), observed.validationCount() >= expected.requestCount()),
                ReportAuditChecks.check("ExpectedReportProjection", "owners + scenarios + counts match",
                        String.valueOf(matches), matches)));
    }

    private static boolean sameMultiset(List<String> actual, List<String> expected) {
        return actual.size() == expected.size()
                && actual.stream().sorted().toList().equals(expected.stream().sorted().toList());
    }

    private static List<String> expectedOwnerNames(ExpectedReportProjection expected) {
        return expected.owners().stream().map(ExpectedReportProjection.Owner::name).toList();
    }
}
