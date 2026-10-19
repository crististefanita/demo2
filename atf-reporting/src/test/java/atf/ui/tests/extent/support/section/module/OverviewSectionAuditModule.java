package atf.ui.tests.extent.support.section.module;
import atf.api.reporting.ui.audit.runtime.SectionAuditCheckExecutor;
import atf.api.reporting.audit.ReportAuditChecks;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.api.reporting.audit.ValidationResult;

import atf.api.reporting.ui.audit.plan.ReportProjection;
import atf.api.reporting.audit.ReportAuditSnapshot;
import atf.api.reporting.ui.audit.plan.ExtentSection;
import atf.api.reporting.ui.audit.plan.ExtentSectionCheck;
import atf.api.reporting.ui.audit.plan.SectionAuditSpec;
import atf.ui.tests.extent.support.section.runtime.SectionAuditFixture;
import atf.api.reporting.ui.audit.result.SectionAuditResult;
import atf.api.reporting.ui.audit.section.OverviewSectionValidator;
import java.util.List;
import java.util.Set;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** Overview browser adapter; the projection expectation is built before DOM observation. */
public final class OverviewSectionAuditModule implements ExtentSectionAuditModule {
    private final OverviewSectionValidator validator = new OverviewSectionValidator();
    private final SectionAuditCheckExecutor checkExecutor = new SectionAuditCheckExecutor();

    @Override public ExtentSection section() { return ExtentSection.OVERVIEW; }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        var testsPage = fixture.flow().openTests();
        ReportAuditSnapshot.Tests tests = fixture.exploration().inspectEveryTestClass(testsPage);
        var overview = fixture.audit().openTab(ReportUiContract.Tab.OVERVIEW.hash());
        ReportAuditSnapshot.Overview snapshot = fixture.exploration()
                .inspectOverviewSnapshot(overview, true);
        OverviewSectionValidator.Expected expected = fixture.setup() == null
                ? new OverviewSectionValidator.Expected(List.of(), 0, Set.of())
                : new OverviewSectionValidator.Expected(fixture.setup().families(),
                fixture.setup().mock().routes().size(), Set.copyOf(fixture.expectedOwnerNames()));
        ValidationResult result = spec.hasCheck(ExtentSectionCheck.STRUCTURE)
                ? validator.validate(snapshot, tests, expected) : ValidationResult.empty();
        if (fixture.setup() != null && !fixture.setup().manifest().selectedTests().isEmpty()) {
            ReportProjection projection = fixture.setup().manifest().expectedProjection();
            result = result.merge(ValidationResult.of(List.of(
                    check("Expected report projection",
                            "selected API scenarios", tests.scenarioCount() + " scenarios",
                            projection.matches(tests)))));
        }
        SectionAuditCheckExecutor.Execution execution = checkExecutor.execute(section(), spec, result);
        return new SectionAuditResult(section(), spec.checks(), execution.executedChecks(),
                spec.journey(), execution.validation(), List.of("Overview totals", "Tests projection"));
    }
}
