package atf.ui.tests.extent.flows;

import atf.ui.tests.extent.audit.ExistingReportStructureAuditor;
import atf.ui.tests.extent.audit.FailsScreenValidator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.extract.CategoriesSectionExtractor;
import atf.ui.tests.extent.section.extract.VisualEvidenceSectionExtractor;
import atf.ui.tests.extent.section.validate.CategoriesSectionValidator;
import atf.ui.tests.extent.section.validate.VisualEvidenceValidator;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Page;

/** Semantic flow for the read-only audit of an already published Extent file. */
public final class ExistingReportUiFlow {
    private final UiAuditFlow auditFlow;
    private final ExistingReportStructureAuditor structureAuditor;
    private final CategoriesSectionExtractor categoriesExtractor;
    private final CategoriesSectionValidator categoriesValidator;
    private final FailsScreenValidator failsValidator;
    private final VisualEvidenceSectionExtractor visualEvidenceExtractor;
    private final VisualEvidenceValidator visualEvidenceValidator;

    public ExistingReportUiFlow(UiAuditFlow auditFlow, ExistingReportStructureAuditor structureAuditor) {
        this(auditFlow, structureAuditor, new ReportUiLocators());
    }

    public ExistingReportUiFlow(UiAuditFlow auditFlow, ExistingReportStructureAuditor structureAuditor,
                                ReportUiLocators locators) {
        this.auditFlow = auditFlow;
        this.structureAuditor = structureAuditor;
        this.categoriesExtractor = new CategoriesSectionExtractor(auditFlow, locators);
        this.categoriesValidator = new CategoriesSectionValidator();
        this.failsValidator = new FailsScreenValidator(auditFlow, locators);
        this.visualEvidenceExtractor = new VisualEvidenceSectionExtractor(auditFlow, structureAuditor, locators);
        this.visualEvidenceValidator = new VisualEvidenceValidator();
    }

    public NavigationSnapshot inspectCategoryAndFailures(Page categoryPage) {
        ReportAuditSnapshot.Annotations categories = categoriesExtractor.extract(categoryPage);
        if (categories.linkedScenarioCount() > 0) categoriesExtractor.navigateToFirstLinkedScenario(categoryPage);
        var categoryValidation = categoriesValidator.validate(categories);
        if (!categoryValidation.valid()) {
            throw new AssertionError(categoryValidation.failureSummary());
        }
        Page failsPage = auditFlow.openTab(ReportUiContract.Tab.FAILS.hash());
        FailsScreenValidator.Observation failures = failsValidator.inspect(failsPage);
        return new NavigationSnapshot(categoryPage, failsPage, categories.categoryCount(),
                categories.linkedScenarioCount(), categories.styled(), failures.failureRows(),
                failures.hasFailures(), failures.emptyState(), failures.openLinkCount(),
                failures.primaryCauseCount(), failures.valid(), failures.viewPresent());
    }

    public VisualEvidenceSnapshot inspectVisualEvidence(Page page) {
        ReportAuditSnapshot.VisualEvidence observation = visualEvidenceExtractor.extract(page);
        var visualValidation = visualEvidenceValidator.validate(observation);
        if (!visualValidation.valid()) {
            throw new AssertionError(visualValidation.failureSummary());
        }
        return new VisualEvidenceSnapshot(observation.validationCount(), observation.disclosureCount(),
                observation.actionSequenceCount(), observation.screenshotLinkCount(),
                observation.duplicateScreenshotMarkers(), observation.nestedScreenshotImages(),
                observation.directScreenshotLinks(), observation.featherlightScreenshotLinks(),
                observation.screenshotAnchorCount(), observation.canonicalScreenshotLinks(),
                observation.observedActionText(), observation.observedTextIsStructured(),
                observation.fixedActionTable());
    }

    public ScreenshotSnapshot inspectActionableScreenshot(Page page) {
        ReportAuditSnapshot.ScreenshotEvidence observation = visualEvidenceExtractor.extractActionableScreenshot(page);
        return new ScreenshotSnapshot(observation.actionable(), observation.validPngPayload(),
                observation.target(), observation.rel(), observation.featherlight(), observation.markerText(),
                observation.geometry(), observation.centeredVertically(), observation.openedBase64Image(),
                observation.distinctPayloadCount(), observation.inlineImageCount());
    }

    public record NavigationSnapshot(Page categoryPage, Page failsPage, int categoryCount,
                                     int linkedScenarios, boolean themed, int failureRows,
                                     boolean hasFailures, boolean emptyState, int failureLinks,
                                     int failureCauseCount,
                                     boolean failureStateValid, boolean failsPresent) { }

    public record VisualEvidenceSnapshot(int validationCount, int disclosureCount,
                                         int actionSequenceCount, int screenshotLinkCount,
                                         int duplicateScreenshotMarkers, int nestedScreenshotImages,
                                         int directScreenshotLinks, int featherlightScreenshotLinks,
                                         int screenshotAnchorCount, int canonicalScreenshotLinks,
                                         String observedActionText, boolean observedTextIsStructured,
                                         boolean fixedActionTable) { }

    public record ScreenshotSnapshot(boolean actionable, boolean validPngPayload, String target,
                                     String rel, String featherlight, String markerText,
                                     String geometry, boolean centeredVertically,
                                     boolean openedBase64Image, int distinctPayloadCount,
                                     int inlineImageCount) {
        public static ScreenshotSnapshot notAvailable() {
            return new ScreenshotSnapshot(false, false, "", "", "", "", "missing",
                    false, false, 0, 0);
        }
    }
}
