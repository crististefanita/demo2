package atf.ui.tests.extent.exploratory;

import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.extent.audit.ExistingReportStructureAuditor;
import atf.ui.tests.extent.audit.OverviewScreenValidator;
import atf.ui.tests.extent.audit.ReportStyleValidator;
import atf.ui.tests.extent.flows.ExistingReportUiFlow;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ReportAuditContext;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static atf.ui.tests.extent.support.ReportAuditChecks.check;
import static atf.ui.tests.extent.support.ReportAuditChecks.status;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gherkin intent only; all report DOM mechanics live in {@link ExistingReportUiFlow}. */
@EnabledIf("isConfigured")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Existing Extent report exploratory audit")
class ExistingExtentReportExploratoryTest {
    private UiConfiguration configuration;
    private Path inputReport;
    private Path auditReport;
    private ReportAuditContext auditContext;
    private UiAuditFlow auditFlow;
    private ExistingReportUiFlow reportFlow;
    private ExistingReportStructureAuditor structureAuditor;
    private OverviewScreenValidator overviewValidator;

    static boolean isConfigured() { return AtfUiConfigurationLoader.load().auditInputReport().isPresent(); }

    @BeforeAll
    void openConfiguredReport() {
        configuration = AtfUiConfigurationLoader.load();
        inputReport = configuration.auditInputReport().orElseThrow(() ->
                new IllegalStateException("No existing Extent report configured; set -Datf.ui.report or ui.audit.inputReport in atf.yaml"));
        assertTrue(Files.isRegularFile(inputReport), "Configured Extent report does not exist: " + inputReport);
        auditContext = ReportAuditContext.open(configuration, inputReport, "existing");
        auditFlow = new UiAuditFlow(auditContext, configuration,
                "ExistingExtentReportExploratoryTest", "atf.ui.tests.extent.exploratory",
                List.of("ui", "exploratory", "existing-report"),
                "the configured Extent report is opened as read-only input",
                "Existing report audit failure", "configured Extent report");
        structureAuditor = new ExistingReportStructureAuditor(auditFlow, inputReport);
        reportFlow = new ExistingReportUiFlow(auditFlow, structureAuditor);
        overviewValidator = new OverviewScreenValidator();
        auditReport = auditContext.reportPath();
    }

    @AfterAll
    void closeAudit() throws Exception {
        if (auditContext != null) {
            auditContext.close();
            System.out.println("[ATF UI][EXISTING-REPORT] audit-report=" + auditReport);
        }
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN Overview and Tests are reconciled THEN static sections and dynamic totals agree")
    void overviewAndTestsReconcileDynamically() {
        audit("Existing report — Overview ↔ Tests reconciliation", audit -> {
            var tests = structureAuditor.discoverTests(auditFlow.openTab(ReportUiContract.Tab.TESTS.hash()));
            var page = auditFlow.openTab(ReportUiContract.Tab.OVERVIEW.hash());
            var screenTests = new OverviewScreenValidator.ScreenTestsSnapshot(
                    tests.ownerCount(), tests.scenarioCount(), 0, List.of());
            var result = overviewValidator.validate(page, screenTests,
                    OverviewScreenValidator.fromScreenTests(screenTests));
            UiScreenValidation.Evidence evidence = capture(page, "Overview and Tests reconciliation");
            audit.screen("Overview contains the complete static contract and reconciles dynamic totals",
                    ReportUiContract.Tab.OVERVIEW.hash() + " ↔ " + ReportUiContract.Tab.TESTS.hash(), evidence, List.of(
                            check("Overview screen contract", "reusable validator passes", result.failureSummary(), result.valid()),
                            check("Test owners", "> 0", String.valueOf(tests.ownerCount()), tests.ownerCount() > 0),
                            check("Scenario total", "> 0", String.valueOf(tests.scenarioCount()), tests.scenarioCount() > 0)), status(result.valid()));
            assertAll(() -> assertTrue(result.valid(), result.failureSummary()),
                    () -> assertTrue(tests.ownerCount() > 0), () -> assertTrue(tests.scenarioCount() > 0));
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN every discovered class and phase is expanded THEN UI nodes remain auditable")
    void testsTreeIsDynamicallyExpandable() {
        audit("Existing report — dynamic Tests tree", audit -> {
            var page = auditFlow.openTab(ReportUiContract.Tab.TESTS.hash());
            var snapshot = structureAuditor.discoverTests(page);
            var expansion = structureAuditor.expandEveryOwnerScenarioAndPhase(page);
            UiScreenValidation.Evidence evidence = capture(page, "Dynamic Tests tree after expansion");
            boolean valid = snapshot.ownerCount() > 0 && snapshot.scenarioCount() > 0
                    && snapshot.phaseCount() >= snapshot.scenarioCount()
                    && snapshot.validationCount() > 0 && snapshot.disclosureCount() > 0;
            boolean exhaustive = expansion.ownerCount() == snapshot.ownerCount()
                    && expansion.scenarioCount() == snapshot.scenarioCount()
                    && expansion.phaseCount() == snapshot.phaseCount()
                    && expansion.expandedScenarioCount() == snapshot.scenarioCount()
                    && expansion.expandedPhaseCount() == snapshot.phaseCount();
            audit.screen("Every discovered class, scenario and phase is expandable", ReportUiContract.Tab.TESTS.hash(), evidence,
                    List.of(check("Class owners", "> 0", String.valueOf(snapshot.ownerCount()), snapshot.ownerCount() > 0),
                            check("Scenarios", "> 0", String.valueOf(snapshot.scenarioCount()), snapshot.scenarioCount() > 0),
                            check("Phases", ">= scenarios", String.valueOf(snapshot.phaseCount()), snapshot.phaseCount() >= snapshot.scenarioCount()),
                            check("Validation nodes", "> 0", String.valueOf(snapshot.validationCount()), snapshot.validationCount() > 0),
                            check("Disclosure controls", "> 0", String.valueOf(snapshot.disclosureCount()), snapshot.disclosureCount() > 0),
                            check("All discovered nodes expanded", "owners/scenarios/phases reconciled", expansion.toString(), exhaustive)), status(valid && exhaustive));
            assertAll(() -> assertTrue(valid), () -> assertTrue(exhaustive));
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN categories and Fails are explored THEN navigation is dynamic and actionable")
    void categoriesAndFailsAreActionable() {
        audit("Existing report — category and failure navigation", audit -> {
            var snapshot = reportFlow.inspectCategoryAndFailures(auditFlow.openTab(ReportUiContract.Tab.CATEGORIES.hash()));
            boolean valid = snapshot.categoryCount() > 0 && snapshot.linkedScenarios() > 0 && snapshot.themed()
                    && snapshot.failsPresent() && snapshot.failureStateValid();
            UiScreenValidation.Evidence evidence = capture(snapshot.failsPage(), "Categories and Fails navigation");
            audit.screen("Categories remain linked and Fails remains a valid optional view",
                    ReportUiContract.Tab.CATEGORIES.hash() + " → " + ReportUiContract.Tab.FAILS.hash(), evidence, List.of(
                            check("Categories", "> 0", String.valueOf(snapshot.categoryCount()), snapshot.categoryCount() > 0),
                            check("Linked scenarios", "> 0", String.valueOf(snapshot.linkedScenarios()), snapshot.linkedScenarios() > 0),
                            check("Dark report surface", "styled", snapshot.themed() ? "styled" : "missing", snapshot.themed()),
                            check("Fails view", "present", String.valueOf(snapshot.failsPresent()), snapshot.failsPresent()),
                            check("Failure rows", "zero or dynamic", String.valueOf(snapshot.failureRows()), snapshot.failureRows() >= 0),
                            check("Failure navigation contract", "valid", String.valueOf(snapshot.failureStateValid()), snapshot.failureStateValid())), status(valid));
            assertTrue(valid);
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN Fails is opened THEN list and detail stay on one horizontal row")
    void failsListAndDetailStayOnTheSameRow() {
        audit("Existing report — Fails list/detail split layout", audit -> {
            var page = auditFlow.openTab(ReportUiContract.Tab.FAILS.hash());
            var result = new ReportStyleValidator(new ReportUiLocators()).inspect(page);
            UiScreenValidation.Evidence evidence = capture(page, "Fails list/detail split layout");
            audit.screen("Fails keeps the failure list left and the detail pane right on the same row",
                    ReportUiContract.Tab.FAILS.hash(), evidence,
                    List.of(check("Fails split layout", "flex row, nowrap, detail right and top aligned",
                            result.summary(), result.valid())), status(result.valid()));
            assertTrue(result.valid(), result.summary());
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN visual evidence is expanded THEN Base64 screenshot links and assertions are grouped")
    void visualEvidenceIsLinkedAndGrouped() {
        audit("Existing report — grouped screen evidence", audit -> {
            var page = auditFlow.openTab(ReportUiContract.Tab.TESTS.hash());
            var snapshot = reportFlow.inspectVisualEvidence(page);
            UiScreenValidation.Evidence evidence = capture(page, "Base64 screenshot links and grouped validations");
            boolean inputHasUiActions = snapshot.actionSequenceCount() > 0;
            boolean valid = snapshot.validationCount() > 0 && snapshot.disclosureCount() > 0
                    && (!inputHasUiActions || snapshot.screenshotLinkCount() > 0)
                    && (!inputHasUiActions || snapshot.fixedActionTable())
                    && snapshot.nestedScreenshotImages() == 0
                    && snapshot.directScreenshotLinks() == snapshot.screenshotLinkCount()
                    && snapshot.featherlightScreenshotLinks() == snapshot.screenshotLinkCount()
                    && (!inputHasUiActions || snapshot.screenshotAnchorCount() == snapshot.screenshotLinkCount())
                    && (!inputHasUiActions || snapshot.canonicalScreenshotLinks() == snapshot.screenshotLinkCount())
                    && evidence.hasScreenshotPayload();
            audit.screen("One visual state carries one Base64 screenshot link and grouped validation evidence", ReportUiContract.Tab.TESTS.hash(), evidence,
                    List.of(check("Validation tables", "> 0", String.valueOf(snapshot.validationCount()), snapshot.validationCount() > 0),
                            check("Collapsible evidence", "> 0", String.valueOf(snapshot.disclosureCount()), snapshot.disclosureCount() > 0),
                            check("Base64 screenshot links", "direct PNG data URI", String.valueOf(snapshot.directScreenshotLinks()), snapshot.directScreenshotLinks() == snapshot.screenshotLinkCount()),
                            check("Lightbox contract", "data-featherlight=image", String.valueOf(snapshot.featherlightScreenshotLinks()), snapshot.featherlightScreenshotLinks() == snapshot.screenshotLinkCount()),
                            check("Inline image cardinality", "0 nested images", String.valueOf(snapshot.nestedScreenshotImages()), snapshot.nestedScreenshotImages() == 0),
                            check("Playwright table geometry", "fixed audit columns; dynamic table min-width 760px", String.valueOf(snapshot.fixedActionTable()), !inputHasUiActions || snapshot.fixedActionTable()),
                            check("Observed action text", "row/cell boundaries preserved", snapshot.observedActionText(), !inputHasUiActions || snapshot.observedTextIsStructured()),
                            check("New Base64 screenshot", "present", String.valueOf(evidence.hasScreenshotPayload()), evidence.hasScreenshotPayload())), status(valid));
            assertTrue(valid);
        });
    }

    @Test
    @DisplayName("GIVEN an existing report WHEN visual evidence is expanded THEN the Base64 link is actionable")
    void screenshotLinkIsActionable() {
        audit("Existing report — actionable Base64 screenshot link", audit -> {
            var page = auditFlow.openTab(ReportUiContract.Tab.TESTS.hash());
            var snapshot = reportFlow.inspectActionableScreenshot(page);
            UiScreenValidation.Evidence evidence = capture(page, "Actionable Base64 screenshot link");
            audit.screen("Base64 screenshot link remains safe and actionable", "a.atf-ui-action-screenshot-link", evidence,
                    List.of(check("Screenshot payload", "valid PNG data URI in href", String.valueOf(snapshot.validPngPayload()), snapshot.validPngPayload()),
                            check("New-page target", "_blank", snapshot.target(), "_blank".equals(snapshot.target())),
                            check("Safe opener policy", "rel contains noopener", snapshot.rel(), snapshot.rel().contains("noopener")),
                            check("Lightbox contract", "data-featherlight=image", snapshot.featherlight(), "image".equals(snapshot.featherlight())),
                            check("Duplicate payload policy", "one unique payload per visual state", String.valueOf(snapshot.distinctPayloadCount()), snapshot.distinctPayloadCount() == 1),
                            check("Accessible marker", "📷 base64 img", snapshot.markerText(), snapshot.markerText().contains("📷 base64 img")),
                            check("Vertical alignment", "centered; transform none; overflow visible", snapshot.geometry(), snapshot.centeredVertically()),
                            check("Image navigation", "Featherlight overlay shows data:image/png;base64,...", String.valueOf(snapshot.openedBase64Image()), snapshot.openedBase64Image()),
                            check("Inline image cardinality", "0 nested images", String.valueOf(snapshot.inlineImageCount()), snapshot.inlineImageCount() == 0)), status(snapshot.actionable()));
            assertTrue(snapshot.actionable());
        });
    }

    private UiScreenValidation.Evidence capture(com.microsoft.playwright.Page page, String title) {
        UiScreenValidation.Evidence evidence = auditFlow.capture(page, title);
        System.out.println("[ATF UI][EXISTING-REPORT] screen-validation title=" + title + " base64Link=" + evidence.hasScreenshotPayload());
        return evidence;
    }

    private void audit(String title, UiAuditFlow.AuditAction action) { auditFlow.audit(title, action); }
}
