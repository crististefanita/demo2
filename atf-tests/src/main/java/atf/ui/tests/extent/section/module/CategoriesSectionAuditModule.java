package atf.ui.tests.extent.section.module;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import atf.ui.tests.extent.section.result.SectionAuditResult;
import atf.ui.tests.extent.section.validate.CategoriesSectionValidator;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ValidationResult;


/** Categories browser adapter; cardinality and styling are validated purely. */
public final class CategoriesSectionAuditModule implements ExtentSectionAuditModule {
    private final CategoriesSectionValidator validator = new CategoriesSectionValidator();

    @Override public ExtentSection section() { return ExtentSection.CATEGORIES; }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        return fixture.audit().auditResult("Section Categories — dynamic filters and links", audit -> {
            var filters = fixture.exploration().inspectFiltersAndFailures();
            ReportAuditSnapshot.Annotations snapshot = filters.annotations();
            ValidationResult result = spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)
                    ? validator.validate(snapshot) : ValidationResult.empty();
            if (fixture.setup() != null && spec.hasCheck(
                    atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION)) {
                result = result.merge(fixture.reconcileMockContracts(fixture.flow().openTests()));
            }
            java.util.Set<atf.ui.tests.extent.section.plan.ExtentSectionCheck> executed =
                    spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)
                            ? java.util.Set.of(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)
                            : java.util.Set.of();
            if (fixture.setup() != null && spec.hasCheck(
                    atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION)) {
                var expanded = java.util.EnumSet.noneOf(
                        atf.ui.tests.extent.section.plan.ExtentSectionCheck.class);
                expanded.addAll(executed);
                expanded.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION);
                executed = expanded;
            }
            SectionAuditResult outcome = new SectionAuditResult(section(), spec.checks(), executed,
                    spec.journey(), result, java.util.List.of("category cardinalities", "filter surface"));
            audit.screen("Categories exposes styled dynamic filters and linked scenarios",
                    ReportUiContract.Tab.CATEGORIES.hash(),
                    fixture.audit().capture(filters.categoryPage(), "Categories section"),
                    outcome.assertions(), ReportAuditChecks.status(outcome.valid()));
            if (!outcome.valid()) throw new AssertionError(outcome.failureSummary());
            return outcome;
        });
    }
}
