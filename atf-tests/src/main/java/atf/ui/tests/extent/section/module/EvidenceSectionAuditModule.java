package atf.ui.tests.extent.section.module;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import atf.ui.tests.extent.section.result.SectionAuditResult;
import atf.ui.tests.extent.section.validate.EvidenceSectionValidator;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ValidationResult;


/** Evidence browser adapter; request/response JSON is validated as one contract. */
public final class EvidenceSectionAuditModule implements ExtentSectionAuditModule {
    private final EvidenceSectionValidator validator = new EvidenceSectionValidator();

    @Override public ExtentSection section() { return ExtentSection.EVIDENCE; }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        return fixture.audit().auditResult("Section Evidence — request, response and execution", audit -> {
            var page = fixture.flow().openTests();
            ReportAuditSnapshot.RequestEvidence snapshot =
                    fixture.exploration().inspectFirstRequestWithEvidence(page);
            ValidationResult result = (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)
                    || spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.JSON_CONTRACT))
                    ? validator.validate(snapshot) : ValidationResult.empty();
            if (fixture.setup() != null && spec.hasCheck(
                    atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION)) {
                result = result.merge(fixture.reconcileMockContracts(page));
            }
            java.util.EnumSet<atf.ui.tests.extent.section.plan.ExtentSectionCheck> executed =
                    java.util.EnumSet.noneOf(atf.ui.tests.extent.section.plan.ExtentSectionCheck.class);
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE);
            }
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.JSON_CONTRACT)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.JSON_CONTRACT);
            }
            if (fixture.setup() != null && spec.hasCheck(
                    atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.MOCK_RECONCILIATION);
            }
            SectionAuditResult outcome = new SectionAuditResult(section(), spec.checks(), executed,
                    spec.journey(), result, java.util.List.of("request JSON", "response JSON", "execution evidence"));
            audit.screen("Evidence exposes auditable request and response JSON",
                    ReportUiContract.Tab.TESTS.hash(), fixture.audit().capture(page, "Evidence section"),
                    outcome.assertions(), ReportAuditChecks.status(outcome.valid()));
            if (!outcome.valid()) throw new AssertionError(outcome.failureSummary());
            return outcome;
        });
    }
}
