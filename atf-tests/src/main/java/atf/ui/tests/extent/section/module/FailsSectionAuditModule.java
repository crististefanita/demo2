package atf.ui.tests.extent.section.module;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.flows.inspection.FailureFlowInspector;
import atf.ui.tests.extent.section.plan.ExtentJourney;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import atf.ui.tests.extent.section.result.SectionAuditResult;
import atf.ui.tests.extent.section.validate.FailsSectionValidator;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ValidationResult;
import com.microsoft.playwright.Page;


/** Browser adapter for Fails; causes and deep-links are observed, not invented. */
public final class FailsSectionAuditModule implements ExtentSectionAuditModule {
    private final FailsSectionValidator validator = new FailsSectionValidator();

    @Override
    public ExtentSection section() {
        return ExtentSection.FAILS;
    }

    @Override
    public SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec) {
        return fixture.audit().auditResult("Section Fails — causes and Tests deep-links", audit -> {
            var snapshot = fixture.exploration().inspectFiltersAndFailures();
            ReportAuditSnapshot.Failures failures = snapshot.failures();
            ValidationResult result = ValidationResult.empty();
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)) {
                result = fixture.setup() == null
                        ? validator.validate(failures)
                        : validator.validate(failures, fixture.expectedProjection());
            }
            Page page = snapshot.failsPage();
            if (spec.journey() == ExtentJourney.FAILURE_NAVIGATION) {
                if (failures.failureCount() == 0) {
                    result = result.merge(ValidationResult.of(java.util.List.of(
                            ReportAuditChecks.check("Failure navigation input", "> 0",
                                    "0", false))));
                } else {
                    Page testsPage = fixture.flow().openTests();
                    String expectedOwner = fixture.expectedOwnerNames().stream().findFirst()
                            .orElseGet(() -> fixture.flow().ownerNames(testsPage).stream()
                                    .findFirst().orElseThrow(() -> new IllegalStateException(
                                            "Failure navigation requires one rendered owner")));
                    FailureFlowInspector.Observation journey = new FailureFlowInspector(
                            fixture.audit(), fixture.flow().locators()).inspect(testsPage, expectedOwner);
                    result = result.merge(ValidationResult.of(java.util.List.of(
                            ReportAuditChecks.check("Tests deep-link", "Tests + scenario-index",
                                    String.valueOf(journey.failureDeepLinkWorks()),
                                    journey.failureDeepLinkWorks()),
                            ReportAuditChecks.check("Collapsed details", "0 expanded",
                                    journey.collapsedSummary(), journey.detailsCollapsed()),
                            ReportAuditChecks.check("Overview failure metric", "> 0",
                                    String.valueOf(journey.overviewFailureCount()),
                                    journey.overviewFailureCount() > 0))));
                    page = fixture.audit().openTab(ReportUiContract.Tab.FAILS.hash());
                }
            }
            java.util.EnumSet<atf.ui.tests.extent.section.plan.ExtentSectionCheck> executed =
                    java.util.EnumSet.noneOf(atf.ui.tests.extent.section.plan.ExtentSectionCheck.class);
            if (spec.hasCheck(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE)) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.STRUCTURE);
            }
            if (spec.journey() == ExtentJourney.FAILURE_NAVIGATION) {
                executed.add(atf.ui.tests.extent.section.plan.ExtentSectionCheck.CROSS_SECTION);
            }
            SectionAuditResult outcome = new SectionAuditResult(section(), spec.checks(), executed,
                    spec.journey(), result, java.util.List.of("Fails rows", "failure cause/deep-link journey"));
            audit.screen("Fails exposes an actionable empty state or failure navigation",
                    ReportUiContract.Tab.FAILS.hash(), fixture.audit().capture(page, "Fails section"),
                    outcome.assertions(), ReportAuditChecks.status(outcome.valid()));
            if (!outcome.valid()) throw new AssertionError(outcome.failureSummary());
            return outcome;
        });
    }
}

