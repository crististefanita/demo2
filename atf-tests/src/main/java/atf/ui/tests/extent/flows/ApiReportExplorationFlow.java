package atf.ui.tests.extent.flows;

import atf.ui.tests.extent.audit.ApiJsonEvidenceAuditor;
import atf.ui.tests.extent.flows.inspection.ApiReportDurationInspector;
import atf.ui.tests.extent.flows.inspection.ApiReportFilterInspector;
import atf.ui.tests.extent.flows.inspection.ApiReportRequestEvidenceInspector;
import atf.ui.tests.extent.flows.inspection.ApiReportStructureInspector;
import atf.ui.tests.extent.flows.inspection.ReportProjectionMatrixInspector;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.extract.ScreenValidationSectionExtractor;
import atf.ui.tests.extent.section.extract.OverviewSectionExtractor;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.Objects;

/**
 * Semantic facade for auditing an API Extent report through the ATF debug UI.
 *
 * <p>The facade exposes only flows and validators. Immutable observations live
 * in {@link ReportAuditSnapshot}, so another renderer or report adapter can
 * consume the same facts without depending on this browser facade.</p>
 */
public final class ApiReportExplorationFlow {
    private final ApiReportStructureInspector structureInspector;
    private final ApiReportDurationInspector durationInspector;
    private final ApiReportFilterInspector filterInspector;
    private final ApiReportRequestEvidenceInspector requestEvidenceInspector;
    private final ReportProjectionMatrixInspector projectionMatrixInspector;
    private final ScreenValidationSectionExtractor screenValidationExtractor;
    private final OverviewSectionExtractor overviewSectionExtractor;

    public ApiReportExplorationFlow(UiAuditFlow auditFlow) {
        this(auditFlow, new ReportUiLocators());
    }

    public ApiReportExplorationFlow(UiAuditFlow auditFlow, ReportUiLocators locators) {
        Objects.requireNonNull(auditFlow, "auditFlow");
        this.structureInspector = new ApiReportStructureInspector(auditFlow, locators);
        this.durationInspector = new ApiReportDurationInspector(auditFlow, locators);
        this.filterInspector = new ApiReportFilterInspector(auditFlow, structureInspector, locators);
        this.requestEvidenceInspector = new ApiReportRequestEvidenceInspector(auditFlow, locators);
        this.projectionMatrixInspector = new ReportProjectionMatrixInspector(locators);
        this.screenValidationExtractor = new ScreenValidationSectionExtractor(auditFlow, locators);
        this.overviewSectionExtractor = new OverviewSectionExtractor(locators);
    }

    public ReportAuditSnapshot.Tests inspectEveryTestClass(Page page) {
        return structureInspector.inspectEveryTestClass(page);
    }

    public ReportAuditSnapshot.Duration selectOwnerAndInspectDuration(Page page, int index) {
        return durationInspector.selectOwnerAndInspectDuration(page, index);
    }

    public int ownerCount(Page page) {
        return structureInspector.ownerCount(page);
    }

    public ReportAuditSnapshot.Duration inspectDurationContract(Page page) {
        return durationInspector.inspectDurationContract(page);
    }

    public List<ReportAuditSnapshot.DurationProjection> inspectDurationProjections(Page page) {
        return durationInspector.inspectDurationProjections(page);
    }

    public FiltersSnapshot inspectFiltersAndFailures() {
        return filterInspector.inspectFiltersAndFailures();
    }

    public boolean openFirstFailureInTests(FiltersSnapshot snapshot) {
        return filterInspector.openFirstFailureInTests(snapshot);
    }

    public ReportAuditSnapshot.Annotations inspectAnnotations(Page category) {
        return structureInspector.inspectAnnotations(category);
    }

    public ReportAuditSnapshot.Failures inspectFailures(Page fails) {
        return structureInspector.inspectFailures(fails);
    }

    public ReportAuditSnapshot.RequestEvidence inspectFirstRequestWithEvidence(Page page) {
        return requestEvidenceInspector.inspectFirstRequestWithEvidence(page);
    }

    public ReportAuditSnapshot.ScreenValidation inspectScreenValidation(Page page) {
        return screenValidationExtractor.inspect(page);
    }

    public ReportAuditSnapshot.Overview inspectOverviewSnapshot(Page page, boolean includeHierarchy) {
        return overviewSectionExtractor.inspect(page, includeHierarchy);
    }

    public ReportProjectionMatrixInspector.Observation inspectProjection(Page page,
            atf.ui.tests.extent.support.ReportUiContract.Tab tab) {
        return projectionMatrixInspector.inspect(page, tab);
    }

    public ReportAuditSnapshot.Duration combine(ReportAuditSnapshot.Duration left,
                                                ReportAuditSnapshot.Duration right) {
        return left.plus(right);
    }

    /** Compares status-badge geometry at normal and narrow viewports. */
    public ReportAuditSnapshot.BadgeGeometry compareStatusBadgeGeometry(Page page,
            int normalWidth, int normalHeight, int narrowWidth, int narrowHeight) {
        return durationInspector.compareStatusBadgeGeometry(page, normalWidth, normalHeight,
                narrowWidth, narrowHeight);
    }

    /** Page handles remain flow concerns; the observations inside are neutral snapshots. */
    public record FiltersSnapshot(Page categoryPage, Page failsPage,
                                  ReportAuditSnapshot.Annotations annotations,
                                  ReportAuditSnapshot.Failures failures,
                                  boolean tagLinks, boolean failureLinks) { }
}
