package atf.ui.tests.extent.section.module;

import atf.ui.tests.extent.plan.ExpectedReportProjection;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import atf.ui.tests.extent.section.result.SectionAuditResult;
import atf.ui.tests.extent.section.validate.OverviewSectionValidator;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ValidationResult;
import java.util.List;


/** Overview browser adapter; the projection expectation is built before DOM observation. */
public final class OverviewSectionAuditModule implements ExtentSectionAuditModule {
    private final OverviewSectionValidator validator = new OverviewSectionValidator();

    @Override public ExtentSection section() { return ExtentSection.OVERVIEW; }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        return fixture.audit().auditResult("Section Overview — totals and selected projection", audit -> {
            var testsPage = fixture.flow().openTests();
            ReportAuditSnapshot.Tests tests = fixture.exploration().inspectEveryTestClass(testsPage);
            var overview = fixture.audit().openTab(ReportUiContract.Tab.OVERVIEW.hash());
            ReportAuditSnapshot.Overview snapshot = fixture.exploration()
                    .inspectOverviewSnapshot(overview, true);
            OverviewSectionValidator.Expected expected = fixture.setup() == null
                    ? new OverviewSectionValidator.Expected(List.of(), 0, java.util.Set.of())
                    : new OverviewSectionValidator.Expected(fixture.setup().families(),
                    fixture.setup().mock().routes().size(), java.util.Set.copyOf(fixture.expectedOwnerNames()));
            ValidationResult result = spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)
                    ? validator.validate(snapshot, tests, expected) : ValidationResult.empty();
            if (fixture.setup() != null && !fixture.setup().manifest().selectedTests().isEmpty()) {
                ExpectedReportProjection projection = fixture.setup().manifest().expectedProjection();
                result = result.merge(ValidationResult.of(List.of(
                        ReportAuditChecks.check("Expected report projection", "selected API scenarios",
                                tests.scenarioCount() + " scenarios", projection.matches(tests)))));
            }
            java.util.EnumSet<atf.ui.tests.extent.section.plan.ExtentSectionCheck> executed =
                    java.util.EnumSet.noneOf(atf.ui.tests.extent.section.plan.ExtentSectionCheck.class);
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE);
            }
            if (fixture.setup() != null && spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION);
                result = result.merge(fixture.reconcileMockContracts(fixture.flow().openTests()));
            }
            SectionAuditResult outcome = new SectionAuditResult(section(), spec.checks(), executed,
                    spec.journey(), result, List.of("Overview totals", "Tests projection"));
            audit.screen("Overview reconciles with the selected Tests projection",
                    ReportUiContract.Tab.OVERVIEW.hash(), fixture.audit().capture(overview, "Overview section"),
                    outcome.assertions(), ReportAuditChecks.status(outcome.valid()));
            if (!outcome.valid()) throw new AssertionError(outcome.failureSummary());
            return outcome;
        });
    }
}
