package atf.ui.tests.extent.flows;

import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.extent.section.validate.OverviewSectionValidator;
import atf.ui.tests.extent.support.ReportAuditContext;
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
import java.util.ArrayList;
import java.util.List;

import static atf.ui.tests.extent.support.ReportAuditChecks.check;
import static atf.ui.tests.extent.support.ReportAuditChecks.status;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gherkin intent only; selector mechanics are delegated to {@link ReportNavigationFlow}. */
@EnabledIf("isConfigured")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Extent report navigation and aggregation flow")
class ReportNavigationAggregationFlowTest {
    private UiConfiguration configuration;
    private Path inputReport;
    private Path auditReport;
    private ReportAuditContext auditContext;
    private UiAuditFlow auditFlow;
    private ReportNavigationFlow reportFlow;

    static boolean isConfigured() { return AtfUiConfigurationLoader.load().auditInputReport().isPresent(); }

    @BeforeAll
    void openConfiguredReport() {
        configuration = AtfUiConfigurationLoader.load();
        inputReport = configuration.auditInputReport().orElseThrow(() -> new IllegalStateException("No existing Extent report configured"));
        assertTrue(Files.isRegularFile(inputReport), "Configured Extent report does not exist: " + inputReport);
        auditContext = ReportAuditContext.open(configuration, inputReport, "navigation-flow");
        auditFlow = new UiAuditFlow(auditContext, configuration,
                "ReportNavigationAggregationFlowTest", "atf.ui.tests.extent.flows",
                List.of("ui", "flow", "navigation", "aggregation"),
                "the configured Extent report is opened as read-only input",
                "Navigation flow audit failure", "configured Extent report");
        reportFlow = new ReportNavigationFlow(auditFlow);
        auditReport = auditContext.reportPath();
    }

    @AfterAll
    void closeAudit() { if (auditContext != null) auditContext.close(); }

    @Test
    @DisplayName("GIVEN a report WHEN Tests, filters, Categories and Overview are traversed THEN the artifact remains consistent")
    void testsFiltersCategoriesAndOverviewReconcile() {
        audit("Tests → status/tag filters → Clear → Categories → Overview", audit -> {
            ReportNavigationFlow.NavigationSnapshot snapshot = reportFlow.traverse();
            UiScreenValidation.Evidence evidence = capture(snapshot.overviewPage(), "Tests, filters, Categories and Overview reconciled");
            var baseline = snapshot.baseline();
            var category = snapshot.category();
            var overview = new OverviewSectionValidator().validate(snapshot.overview(), baseline,
                    new OverviewSectionValidator.Expected(List.of(), 0, java.util.Set.of()));
            boolean valid = snapshot.passSelected() && snapshot.passScenarioCount() >= 0
                    && snapshot.combinedScenarioCount() >= 0 && snapshot.tagCombinationValid()
                    && snapshot.clearRestoresBaseline() && category.valid() && overview.valid()
                    && evidence.hasScreenshotPayload();
            List<atf.api.core.reporting.AtfAssertion> assertions = new ArrayList<>(overview.assertions());
            assertions.add(0, check("Tests baseline", "owners/scenarios > 0", baseline.classCount() + "/" + baseline.scenarioCount(), baseline.classCount() > 0 && baseline.scenarioCount() > 0));
            assertions.add(check("Pass filter", "selected and non-negative", snapshot.passSelected() + " / " + snapshot.passScenarioCount(), snapshot.passSelected() && snapshot.passScenarioCount() >= 0));
            assertions.add(check("Dynamic tag combination", "selected tags reconcile", snapshot.activeTagCount() + " active after " + snapshot.tagSelectionSteps() + " selections", snapshot.tagCombinationValid()));
            assertions.add(check("Clear", String.valueOf(baseline.scenarioCount()), String.valueOf(snapshot.clearedScenarioCount()), snapshot.clearRestoresBaseline()));
            assertions.add(check("Categories", "selected category has coherent rows/links", category.toString(), category.valid()));
            assertions.add(check("Inline Base64 screenshot", "valid PNG payload", String.valueOf(evidence.hasScreenshotPayload()), evidence.hasScreenshotPayload()));
            audit.screen("Overview reconciles the unfiltered Tests baseline after filter exploration",
                    "#test-view → #category-view → #dashboard-view", evidence, assertions, status(valid));
            assertAll(() -> assertTrue(snapshot.passSelected()), () -> assertTrue(snapshot.tagCombinationValid()),
                    () -> assertTrue(snapshot.clearRestoresBaseline()), () -> assertTrue(category.valid()),
                    () -> assertTrue(overview.valid(), overview.failureSummary()));
        });
    }

    private UiScreenValidation.Evidence capture(com.microsoft.playwright.Page page, String title) {
        return auditFlow.capture(page, title);
    }

    private void audit(String title, UiAuditFlow.AuditAction action) { auditFlow.audit(title, action); }
}
