package atf.ui.tests.extent.exploratory;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.extent.audit.CategoryFilterAuditor;
import atf.ui.tests.extent.support.ReportAuditContext;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Page;
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

/**
 * Exploratory, read-only audit of the Categories tab of an existing Extent
 * API report. A category selection is treated as a real filter operation: its
 * selected title, status rows, linked tests and declared count must reconcile.
 * The input report is never changed; this test creates a separate UI audit
 * report and publishes one Base64 media link per explored filter state.
 */
@EnabledIf("isConfigured")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Existing Extent Categories exploratory audit")
class ExistingExtentCategoriesExploratoryTest {
    private UiConfiguration configuration;
    private Path inputReport;
    private Path auditReport;
    private ReportAuditContext auditContext;
    private UiAuditFlow auditFlow;
    private CategoryFilterAuditor categoryAuditor;

    static boolean isConfigured() {
        UiConfiguration loaded = AtfUiConfigurationLoader.load();
        return loaded.auditInputReport().isPresent();
    }

    @BeforeAll
    @DisplayName("GIVEN an existing API Extent report WHEN debug UI starts THEN Categories is opened read-only")
    void openConfiguredReport() {
        configuration = AtfUiConfigurationLoader.load();
        inputReport = configuration.auditInputReport().orElseThrow(() ->
                new IllegalStateException("No existing Extent report configured; set -Datf.ui.report "
                        + "or ui.audit.inputReport in atf.yaml"));
        assertTrue(Files.isRegularFile(inputReport),
                "Configured Extent report does not exist: " + inputReport);
        auditContext = ReportAuditContext.open(configuration, inputReport, "categories");
        auditFlow = new UiAuditFlow(
                auditContext, configuration,
                "ExistingExtentCategoriesExploratoryTest",
                "atf.ui.tests.extent.exploratory",
                List.of("ui", "exploratory", "categories"),
                "the configured Extent report is opened as read-only input",
                "Categories audit failure",
                "configured category report");
        auditReport = auditContext.reportPath();
        categoryAuditor = new CategoryFilterAuditor(auditFlow);
        System.out.println("[ATF UI][CATEGORIES] input=" + inputReport);
    }

    @AfterAll
    void closeAudit() throws Exception {
        if (auditContext != null) {
            auditContext.close();
            System.out.println("[ATF UI][CATEGORIES] audit-report=" + auditReport);
        }
    }

    @Test
    @DisplayName("GIVEN Categories WHEN every category filter is selected THEN counts, rows and links reconcile")
    void everyCategoryFilterReconcilesDynamically() {
        audit("Categories — dynamic filter reconciliation", audit -> {
            Page page = openTab(ReportUiContract.Tab.CATEGORIES.hash());
            List<CategoryFilterAuditor.CategoryOption> options = categoryAuditor.discover(page);
            int categoryCount = options.size();
            assertTrue(categoryCount > 0, "Categories tab has no filter items");

            List<CategoryObservation> observations = new ArrayList<>();
            for (CategoryFilterAuditor.CategoryOption option : options) {
                page = openTab(ReportUiContract.Tab.CATEGORIES.hash());
                CategoryFilterAuditor.FilterSnapshot snapshot = categoryAuditor.select(page, option);
                String selector = snapshot.selector();

                CategoryObservation observation = new CategoryObservation(
                        option.name(), option.declaredCount(), snapshot.linkCount(), snapshot.rowCount(),
                        snapshot.selected(), snapshot.valid());
                observations.add(observation);

                UiScreenValidation.Evidence evidence = capture(page,
                        "Category filter — " + option.name());
                String stepTitle = "Category \"" + option.name() + "\" reconciles declared tests="
                        + option.declaredCount() + " with rendered links";
                audit.screen(stepTitle, selector, evidence, List.of(
                                check("Selected filter", "active", snapshot.selected() ? "active" : "not active", snapshot.selected()),
                                check("Selected category", option.name(), snapshot.selectedTitle(),
                                        option.name().equalsIgnoreCase(snapshot.selectedTitle())),
                                check("Declared test count", String.valueOf(option.declaredCount()),
                                        String.valueOf(snapshot.linkCount()), option.declaredCount() == snapshot.linkCount()),
                                check("Rendered status rows", String.valueOf(option.declaredCount()),
                                        String.valueOf(snapshot.rowCount()), option.declaredCount() == snapshot.rowCount()),
                                check("Linked test ids", option.declaredCount() == 0 ? "empty" : "all present",
                                        snapshot.linkedIdsValid() ? "valid" : "missing", snapshot.linkedIdsValid())),
                        status(snapshot.valid()));
                assertTrue(snapshot.valid(), "Category filter is inconsistent: " + observation);
            }

            CategoryObservation navigationCandidate = observations.stream()
                    .filter(CategoryObservation::valid)
                    .filter(observation -> observation.linkCount() > 0)
                    .findFirst().orElse(null);
            boolean testViewVisible;
            if (navigationCandidate != null) {
                page = openTab(ReportUiContract.Tab.CATEGORIES.hash());
                int navigationIndex = observations.indexOf(navigationCandidate);
                testViewVisible = categoryAuditor.navigateToFirstLinkedScenario(
                        page, options.get(navigationIndex));
            } else {
                testViewVisible = observations.stream()
                        .allMatch(observation -> observation.declaredCount() == 0
                                && observation.linkCount() == 0);
                auditFlow.actions().observation("Empty category state", "#category-view",
                        "zero-result filters remain empty",
                        testViewVisible ? "all filters empty" : "unexpected category links");
            }

            boolean allReconciled = observations.size() == categoryCount
                    && observations.stream().allMatch(CategoryObservation::valid);
            audit.screen("Category links navigate back to the Tests tab",
                    ReportUiContract.Tab.CATEGORIES.hash() + " a.linked → " + ReportUiContract.Tab.TESTS.hash(), capture(page,
                            "Category linked scenario navigation"), List.of(
                            check("Category filters explored", String.valueOf(categoryCount),
                                    String.valueOf(observations.size()), observations.size() == categoryCount),
                            check("Category count reconciliation", "all filters consistent",
                                    allReconciled ? "consistent" : "mismatch", allReconciled),
                            check("Linked navigation", navigationCandidate == null ? "zero-result state" : "#test-view visible",
                                    navigationCandidate == null ? "zero-result state" : String.valueOf(testViewVisible),
                                    testViewVisible)), status(allReconciled));
            assertAll(
                    () -> assertTrue(allReconciled, "At least one category filter is inconsistent"),
                    () -> assertTrue(testViewVisible));
        });
    }

    private Page openTab(String hash) {
        Page page = auditFlow.openTab(hash);
        return page;
    }

    private UiScreenValidation.Evidence capture(Page page, String title) {
        UiScreenValidation.Evidence evidence = auditFlow.capture(page, title);
        System.out.println("[ATF UI][CATEGORIES] screenshot title=" + title
                + " base64Chars=" + evidence.base64Png().length());
        return evidence;
    }

    private void audit(String title, UiAuditFlow.AuditAction action) {
        auditFlow.audit(title, audit -> {
            action.run(audit);
        });
    }

    private record CategoryObservation(String name, int declaredCount, int linkCount,
                                       int rowCount, boolean selected, boolean valid) { }
}


