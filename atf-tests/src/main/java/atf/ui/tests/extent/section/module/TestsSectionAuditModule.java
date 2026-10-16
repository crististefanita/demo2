package atf.ui.tests.extent.section.module;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.ExtentSectionCheck;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.result.SectionAuditResult;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import atf.ui.tests.extent.section.validate.TestsSectionValidator;
import atf.ui.tests.extent.section.validate.SectionAuditCheckPolicy;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ValidationResult;
import com.microsoft.playwright.Page;

import java.util.EnumSet;
import java.util.List;

/** Browser adapter for Tests; expectations remain in the pure validator. */
public final class TestsSectionAuditModule implements ExtentSectionAuditModule {
    private final TestsSectionValidator validator = new TestsSectionValidator();

    @Override
    public ExtentSection section() {
        return ExtentSection.TESTS;
    }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        return fixture.audit().auditResult("Section Tests — dynamic report structure", audit -> {
            Page page = fixture.flow().openTests();
            ReportAuditSnapshot.Tests snapshot = fixture.exploration().inspectEveryTestClass(page);
            ValidationResult result = fixture.setup() == null
                    || fixture.setup().selectedTests().isEmpty()
                    ? validator.validate(snapshot, fixture.expectedOwnerNames())
                    : validator.validate(snapshot, fixture.expectedOwnerNames(),
                            fixture.expectedProjection());
            result = SectionAuditCheckPolicy.select(section(), spec.checks(), result);
            EnumSet<ExtentSectionCheck> executed = EnumSet.noneOf(ExtentSectionCheck.class);
            if (spec.hasCheck(ExtentSectionCheck.STRUCTURE)) {
                executed.add(ExtentSectionCheck.STRUCTURE);
            }
            if (spec.hasCheck(ExtentSectionCheck.CROSS_SECTION)
                    && fixture.setup() != null
                    && !fixture.setup().manifest().selectedTests().isEmpty()) {
                executed.add(ExtentSectionCheck.CROSS_SECTION);
            }
            SectionAuditResult outcome = new SectionAuditResult(section(), spec.checks(), executed,
                    spec.journey(), result,
                    List.of("owners", "scenarios", "phases", "request evidence"));
            audit.screen("Tests section exposes owners, scenarios, phases and requests",
                    ReportUiContract.Tab.TESTS.hash(), fixture.audit().capture(page, "Tests section"),
                    outcome.assertions(), ReportAuditChecks.status(outcome.valid()));
            if (!outcome.valid()) throw new AssertionError(outcome.failureSummary());
            return outcome;
        });
    }
}

