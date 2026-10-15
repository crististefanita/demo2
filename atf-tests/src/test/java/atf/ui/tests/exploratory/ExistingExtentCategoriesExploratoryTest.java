package atf.ui.tests.exploratory;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.support.ReportAuditChecks;
import atf.ui.tests.support.ReportAuditContext;
import com.microsoft.playwright.Locator;
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

import static atf.ui.tests.support.ReportAuditChecks.check;
import static atf.ui.tests.support.ReportAuditChecks.number;
import static atf.ui.tests.support.ReportAuditChecks.status;
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
    private PlaywrightActionSequence activeActions;

    static boolean isConfigured() {
        UiConfiguration loaded = AtfUiConfigurationLoader.load();
        return loaded.enabled() && loaded.auditInputReport().isPresent();
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
        auditContext = ReportAuditContext.open(configuration, inputReport,
                "existing-ui-categories-atf-extent-report.html");
        auditReport = auditContext.reportPath();
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
            Page page = openTab("#category-view");
            Locator categories = page.locator("#category-view .test-list .test-item");
            int categoryCount = categories.count();
            assertTrue(categoryCount > 0, "Categories tab has no filter items");

            List<CategoryObservation> observations = new ArrayList<>();
            for (int index = 0; index < categoryCount; index++) {
                page = openTab("#category-view");
                categories = page.locator("#category-view .test-list .test-item");
                Locator item = categories.nth(index);
                String selector = "#category-view .test-list .test-item:nth-of-type(" + (index + 1) + ")";
                String name = item.locator(".test-detail .name").innerText().trim();
                int declaredCount = number(item.locator(".duration").innerText());

                activeActions.click(item, selector, "category filter selects " + name);
                Locator detail = page.locator("#category-view .test-content-detail:visible");
                activeActions.waitForVisible(detail, "#category-view .test-content-detail:visible");
                String selectedTitle = detail.locator("[data-atf-category-title='true'], h4").first()
                        .innerText().trim();
                Locator links = detail.locator("a.linked:visible");
                Locator rows = detail.locator("tr.tag-test-status");
                int linkCount = links.count();
                int rowCount = rows.count();
                boolean selected = item.getAttribute("class").contains("active");
                boolean valid = selected && name.equalsIgnoreCase(selectedTitle)
                        && declaredCount == linkCount && declaredCount == rowCount
                        && allLinksHaveIds(links);

                CategoryObservation observation = new CategoryObservation(
                        name, declaredCount, linkCount, rowCount, selected, valid);
                observations.add(observation);

                UiScreenValidation.Evidence evidence = capture(page,
                        "Category filter — " + name);
                String stepTitle = "Category \"" + name + "\" reconciles declared tests="
                        + declaredCount + " with rendered links";
                audit.screenMedia(stepTitle, selector, evidence, List.of(
                                check("Selected filter", "active", selected ? "active" : "not active", selected),
                                check("Selected category", name, selectedTitle, name.equalsIgnoreCase(selectedTitle)),
                                check("Declared test count", String.valueOf(declaredCount),
                                        String.valueOf(linkCount), declaredCount == linkCount),
                                check("Rendered status rows", String.valueOf(declaredCount),
                                        String.valueOf(rowCount), declaredCount == rowCount),
                                check("Linked test ids", "all present", allLinksHaveIds(links) ? "present" : "missing",
                                        allLinksHaveIds(links))), status(valid));
                assertTrue(valid, "Category filter is inconsistent: " + observation);
            }

            CategoryObservation navigationCandidate = observations.stream()
                    .filter(CategoryObservation::valid)
                    .filter(observation -> observation.linkCount() > 0)
                    .findFirst().orElseThrow(() ->
                            new AssertionError("No category exposes a navigable linked test"));
            page = openTab("#category-view");
            Locator candidate = page.locator("#category-view .test-list .test-item")
                    .filter(new Locator.FilterOptions().setHasText(navigationCandidate.name()));
            activeActions.click(candidate.first(), "#category-view category=" + navigationCandidate.name(),
                    "selected category is ready for navigation");
            Locator link = page.locator("#category-view .test-content-detail:visible a.linked:visible").first();
            activeActions.click(link, "#category-view a.linked:first-of-type", "linked scenario opens in Tests");
            activeActions.waitForVisible(page.locator("#test-view"), "#test-view");
            assertTrue(page.locator("#test-view").isVisible(),
                    "A linked category scenario did not navigate to Tests");
            boolean testViewVisible = page.locator("#test-view").isVisible();

            boolean allReconciled = observations.size() == categoryCount
                    && observations.stream().allMatch(CategoryObservation::valid);
            audit.screenMedia("Category links navigate back to the Tests tab",
                    "#category-view a.linked → #test-view", capture(page,
                            "Category linked scenario navigation"), List.of(
                            check("Category filters explored", String.valueOf(categoryCount),
                                    String.valueOf(observations.size()), observations.size() == categoryCount),
                            check("Category count reconciliation", "all filters consistent",
                                    allReconciled ? "consistent" : "mismatch", allReconciled),
                            check("Linked navigation", "#test-view visible", String.valueOf(page.locator("#test-view").isVisible()),
                                    testViewVisible)), status(allReconciled));
            assertAll(
                    () -> assertTrue(allReconciled, "At least one category filter is inconsistent"),
                    () -> assertTrue(testViewVisible));
        });
    }

    private boolean allLinksHaveIds(Locator links) {
        for (int index = 0; index < links.count(); index++) {
            String id = links.nth(index).getAttribute("test-id");
            if (id == null || id.isBlank() || !id.matches("\\d+")) return false;
        }
        return links.count() > 0;
    }

    private Page openTab(String hash) {
        activeActions = auditContext.newActionSequence();
        return auditContext.openTab(hash, activeActions);
    }

    private UiScreenValidation.Evidence capture(Page page, String title) {
        UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                page, configuration.screenValidation(), title);
        System.out.println("[ATF UI][CATEGORIES] screenshot title=" + title
                + " base64Chars=" + evidence.base64Png().length());
        activeActions.observation("Screenshot", "current page", "visual state captured",
                "Base64 media link");
        return evidence;
    }

    private void audit(String title, AuditAction action) {
        UiAudit audit = beginAudit(title);
        try {
            action.run(audit);
            audit.pass();
        } catch (AssertionError | RuntimeException failure) {
            audit.fail();
            throw failure;
        }
    }

    private UiAudit beginAudit(String title) {
        activeActions = auditContext.newActionSequence();
        UiExtentReportSession.UiScenario scenario = auditContext.startScenario(
                "ExistingExtentCategoriesExploratoryTest", "atf.ui.tests.exploratory", title,
                List.of("ui", "exploratory", "existing-report", "categories"));
        UiExtentReportSession.UiPhase setup = scenario.startPhase("SETUP");
        setup.step("GIVEN", "the configured API Extent report is loaded read-only",
                inputReport.getFileName().toString(), null,
                List.of(check("Input report", "published HTML", inputReport.toString(),
                        Files.isRegularFile(inputReport))), AtfReportStatus.PASS);
        setup.complete(AtfReportStatus.PASS);
        return new UiAudit(scenario, scenario.startPhase("TEST BODY"));
    }

    @FunctionalInterface
    private interface AuditAction { void run(UiAudit audit); }

    private final class UiAudit {
        private final UiExtentReportSession.UiScenario scenario;
        private final UiExtentReportSession.UiPhase body;
        private boolean bodyCompleted;

        private UiAudit(UiExtentReportSession.UiScenario scenario, UiExtentReportSession.UiPhase body) {
            this.scenario = scenario;
            this.body = body;
        }

        private void screenMedia(String title, String target, UiScreenValidation.Evidence evidence,
                                 List<AtfAssertion> assertions, AtfReportStatus status) {
            body.screenValidationAsMedia(title, target, evidence.base64Png(), assertions,
                    activeActions == null ? List.of() : activeActions.drain(), status);
        }

        private void pass() {
            if (!bodyCompleted) {
                body.complete(AtfReportStatus.PASS);
                bodyCompleted = true;
            }
            scenario.complete(AtfReportStatus.PASS);
        }

        private void fail() {
            if (!bodyCompleted) {
                body.complete(AtfReportStatus.FAIL);
                bodyCompleted = true;
            }
            scenario.complete(AtfReportStatus.FAIL);
        }
    }

    private record CategoryObservation(String name, int declaredCount, int linkCount,
                                       int rowCount, boolean selected, boolean valid) { }
}
