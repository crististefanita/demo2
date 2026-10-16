package atf.ui.tests.extent.section.module;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import atf.ui.tests.extent.section.result.SectionAuditResult;
import atf.ui.tests.extent.section.validate.ScreenValidationSectionValidator;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ValidationResult;


/** Screen-validation browser adapter for the compact three-zone evidence layout. */
public final class ScreenValidationSectionAuditModule implements ExtentSectionAuditModule {
    private final ScreenValidationSectionValidator validator = new ScreenValidationSectionValidator();

    @Override public ExtentSection section() { return ExtentSection.SCREEN_VALIDATION; }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        return fixture.audit().auditResult("Section Screen validation — compact evidence zones", audit -> {
            var page = fixture.flow().openTests();
            var owners = fixture.flow().ownerNames(page);
            ReportAuditSnapshot.ScreenValidation snapshot = null;
            for (String ownerName : owners) {
                var owner = fixture.flow().openOwner(page, ownerName);
                fixture.flow().expandAll(owner.detail());
                ReportAuditSnapshot.ScreenValidation candidate = fixture.exploration()
                        .inspectScreenValidation(page);
                if (snapshot == null || candidate.screenshotLinkCount() > snapshot.screenshotLinkCount()) {
                    snapshot = candidate;
                }
                if (candidate.inlineScreenshot() && candidate.actionSequenceCount() > 0
                        && candidate.observationCount() > 0 && candidate.assertionTableCount() > 0) {
                    break;
                }
            }
            if (snapshot == null) {
                snapshot = new ReportAuditSnapshot.ScreenValidation(0, 0, 0, 0, 0,
                        0, 0, false, false, false, false, false,
                        java.util.List.of("no API owner is rendered in the Tests tab"));
            }
            ValidationResult result = (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)
                    || spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.SCREENSHOT_LINK)
                    || spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.INLINE_ALIGNMENT))
                    ? validator.validate(snapshot) : ValidationResult.empty();
            java.util.EnumSet<atf.ui.tests.extent.section.plan.ExtentSectionCheck> executed =
                    java.util.EnumSet.noneOf(atf.ui.tests.extent.section.plan.ExtentSectionCheck.class);
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE);
            }
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.SCREENSHOT_LINK)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.SCREENSHOT_LINK);
            }
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.INLINE_ALIGNMENT)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.INLINE_ALIGNMENT);
            }
            SectionAuditResult outcome = new SectionAuditResult(section(), spec.checks(), executed,
                    spec.journey(), result, java.util.List.of("action sequence", "audit observations", "assertions", "inline screenshot"));
            audit.screen("Screen validation keeps action, observations and assertions contiguous",
                    ReportUiContract.Tab.TESTS.hash(), fixture.audit().capture(page, "Screen validation section"),
                    outcome.assertions(), ReportAuditChecks.status(outcome.valid()));
            if (!outcome.valid()) throw new AssertionError(outcome.failureSummary());
            return outcome;
        });
    }
}
