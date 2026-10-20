package atf.ui.tests.extent.e2e.configurable;
import atf.api.testkit.AtfE2eTest;
import atf.ui.tests.extent.support.audit.screen.CategoryFilterAuditor;
import atf.ui.tests.extent.support.audit.ReportAuditContext;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.ui.tests.extent.support.lifecycle.UiAuditFlow;

import atf.ui.tests.extent.support.config.ReportingTestResources;
import atf.ui.tests.extent.support.config.UiConfiguration;
import atf.ui.tests.extent.support.configurable.CategoryCardinalityFixture;
import atf.ui.tests.extent.support.configurable.CategoryCardinalityReportFixtures;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import atf.api.reporting.ui.audit.vocabulary.UiAuditGherkin;
import atf.api.core.config.module.AtfModule;
import atf.api.reporting.test.AtfModuleTest;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Browser proof for Categories with zero, one and many generated filters.
 *
 * <p>The fixture publisher is isolated under {@code configurable.fixtures};
 * this class only composes the dynamic test and assertions. Each case publishes a real standalone Extent artifact for each
 * cardinality and audits it through the same locator boundary used by the
 * production report explorations. This keeps the cardinality contract
 * independent from the API publisher tag vocabulary.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@AtfModuleTest(requires = {AtfModule.REPORTING, AtfModule.UI})
@AtfE2eTest
class CategoryCardinalityBrowserFixtureTest {
    private static final UiConfiguration CONFIGURATION = ReportingTestResources.uiConfiguration();

    @TestFactory
    Stream<DynamicTest> generatedCategoryCardinalities() {
        return Stream.of(
                CategoryCardinalityFixture.ZERO,
                CategoryCardinalityFixture.ONE,
                CategoryCardinalityFixture.MANY)
                .map(fixture -> DynamicTest.dynamicTest(
                        fixture.gherkinProposition(),
                        () -> verifyFixture(fixture)));
    }

    private void verifyFixture(CategoryCardinalityFixture fixture) throws Exception {
        Path root = java.nio.file.Files.createTempDirectory("atf-category-cardinality-");
        Path report = CategoryCardinalityReportFixtures.publish(root, fixture);
        try (ReportAuditContext context = ReportAuditContext.open(CONFIGURATION, report,
                "category-cardinality-" + fixture.name().toLowerCase())) {
            UiAuditFlow audit = new UiAuditFlow(context, CONFIGURATION,
                    getClass().getSimpleName(), getClass().getPackageName(),
                    List.of("ui", "category-fixture"),
                    UiAuditGherkin.categoryFixtureReady(fixture.name()),
                    "Category cardinality fixture failed", ReportUiContract.Tab.CATEGORIES.hash());
            Page page = audit.openTab(ReportUiContract.Tab.TESTS.hash());
            ReportUiLocators locators = new ReportUiLocators();
            if (fixture == CategoryCardinalityFixture.ZERO) {
                assertEquals(0, locators.navTab(page, ReportUiContract.Tab.CATEGORIES).count(),
                        "The Categories navigation must not be rendered when no category exists");
                assertEquals(1, locators.categoryView(page).count(),
                        "The Categories view shell must remain available for the empty state");
                assertTrue(!locators.categoryView(page).isVisible(),
                        "The Categories view must remain hidden when no category exists");
                return;
            }

            page = audit.openTab(ReportUiContract.Tab.CATEGORIES.hash());
            CategoryFilterAuditor auditor = new CategoryFilterAuditor(audit);
            List<CategoryFilterAuditor.CategoryOption> options = auditor.discover(page);
            assertEquals(fixture.expectedCategoryCount(), options.size(),
                    "Generated category count must match the fixture");

            for (CategoryFilterAuditor.CategoryOption option : options) {
                CategoryFilterAuditor.FilterSnapshot selected = auditor.select(page, option);
                assertTrue(selected.valid(), () -> "Category filter did not reconcile: " + selected);
                assertTrue(auditor.navigateToFirstLinkedScenario(page, option),
                        () -> "Category link did not navigate for " + option.name());
                page = audit.openTab(ReportUiContract.Tab.CATEGORIES.hash());
            }
        } finally {
            if (!Boolean.getBoolean("atf.keep.category.fixtures")) {
                CategoryCardinalityReportFixtures.deleteTree(root);
            }
        }
    }
}
