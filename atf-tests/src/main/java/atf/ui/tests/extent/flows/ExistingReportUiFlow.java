package atf.ui.tests.extent.flows;

import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.extent.audit.CategoryScreenValidator;
import atf.ui.tests.extent.audit.ExistingReportStructureAuditor;
import atf.ui.tests.extent.audit.EvidenceScreenValidator;
import atf.ui.tests.extent.audit.FailsScreenValidator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Page;

/** Semantic flow for the read-only audit of an already published Extent file. */
public final class ExistingReportUiFlow {
    private final UiAuditFlow auditFlow;
    private final ExistingReportStructureAuditor structureAuditor;
    private final ReportUiLocators locators;
    private final CategoryScreenValidator categoryValidator;
    private final FailsScreenValidator failsValidator;
    private final EvidenceScreenValidator evidenceValidator;

    public ExistingReportUiFlow(UiAuditFlow auditFlow, ExistingReportStructureAuditor structureAuditor) {
        this(auditFlow, structureAuditor, new ReportUiLocators());
    }

    public ExistingReportUiFlow(UiAuditFlow auditFlow, ExistingReportStructureAuditor structureAuditor,
                                ReportUiLocators locators) {
        this.auditFlow = auditFlow;
        this.structureAuditor = structureAuditor;
        this.locators = locators;
        this.categoryValidator = new CategoryScreenValidator(auditFlow, locators);
        this.failsValidator = new FailsScreenValidator(auditFlow, locators);
        this.evidenceValidator = new EvidenceScreenValidator(auditFlow, structureAuditor, locators);
    }

    public NavigationSnapshot inspectCategoryAndFailures(Page categoryPage) {
        CategoryScreenValidator.Observation categories = categoryValidator.inspect(categoryPage);
        if (categories.linkedScenarioCount() > 0) categoryValidator.navigateToFirstLinkedScenario(categoryPage);
        Page failsPage = auditFlow.openTab(ReportUiContract.Tab.FAILS.hash());
        FailsScreenValidator.Observation failures = failsValidator.inspect(failsPage);
        return new NavigationSnapshot(categoryPage, failsPage, categories.categoryCount(),
                categories.linkedScenarioCount(), categories.styled(), failures.failureRows(),
                failures.hasFailures(), failures.emptyState(), failures.openLinkCount(),
                failures.primaryCauseCount(), failures.valid(), failures.viewPresent());
    }

    public VisualEvidenceSnapshot inspectVisualEvidence(Page page) {
        EvidenceScreenValidator.VisualObservation observation = evidenceValidator.inspectVisual(page);
        return new VisualEvidenceSnapshot(observation.validationCount(), observation.disclosureCount(),
                observation.actionSequenceCount(), observation.screenshotLinkCount(),
                observation.duplicateScreenshotMarkers(), observation.nestedScreenshotImages(),
                observation.directScreenshotLinks(), observation.featherlightScreenshotLinks(),
                observation.screenshotAnchorCount(), observation.canonicalScreenshotLinks(),
                observation.observedActionText(), observation.observedTextIsStructured(),
                observation.fixedActionTable());
    }

    public ScreenshotSnapshot inspectActionableScreenshot(Page page) {
        EvidenceScreenValidator.ScreenshotObservation observation = evidenceValidator.inspectActionableScreenshot(page);
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
