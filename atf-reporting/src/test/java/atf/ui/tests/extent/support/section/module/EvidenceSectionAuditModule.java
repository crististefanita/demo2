package atf.ui.tests.extent.support.section.module;
import atf.api.reporting.ui.audit.runtime.SectionAuditCheckExecutor;
import atf.api.reporting.audit.ValidationResult;

import atf.api.reporting.audit.ReportAuditSnapshot;
import atf.api.reporting.ui.audit.plan.ExtentSection;
import atf.api.reporting.ui.audit.plan.SectionAuditSpec;
import atf.ui.tests.extent.support.section.runtime.SectionAuditFixture;
import atf.api.reporting.ui.audit.result.SectionAuditResult;
import atf.api.reporting.ui.audit.section.EvidenceSectionValidator;


/** Evidence browser adapter; request/response JSON is validated as one contract. */
public final class EvidenceSectionAuditModule implements ExtentSectionAuditModule {
    private final EvidenceSectionValidator validator = new EvidenceSectionValidator();
    private final SectionAuditCheckExecutor checkExecutor = new SectionAuditCheckExecutor();

    @Override public ExtentSection section() { return ExtentSection.EVIDENCE; }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        var page = fixture.flow().openTests();
        ReportAuditSnapshot.RequestEvidence snapshot =
                fixture.exploration().inspectFirstRequestWithEvidence(page);
        ValidationResult result = (spec.hasCheck(atf.api.reporting.ui.audit.plan.ExtentSectionCheck.STRUCTURE)
                || spec.hasCheck(atf.api.reporting.ui.audit.plan.ExtentSectionCheck.JSON_CONTRACT))
                ? validator.validate(snapshot) : ValidationResult.empty();
        if (fixture.setup() != null && spec.hasCheck(
                atf.api.reporting.ui.audit.plan.ExtentSectionCheck.MOCK_RECONCILIATION)) {
            result = result.merge(fixture.reconcileMockContracts(page));
        }
        SectionAuditCheckExecutor.Execution execution = checkExecutor.execute(section(), spec, result);
        return new SectionAuditResult(section(), spec.checks(), execution.executedChecks(),
                spec.journey(), execution.validation(),
                java.util.List.of("request JSON", "response JSON", "execution evidence"));
    }
}
