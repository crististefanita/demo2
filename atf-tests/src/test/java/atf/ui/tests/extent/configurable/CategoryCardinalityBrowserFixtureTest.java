package atf.ui.tests.extent.configurable;

import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.audit.CategoryFilterAuditor;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ReportAuditContext;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Browser proof for Categories with zero, one and many generated filters.
 *
 * <p>The fixture publishes a real standalone Extent artifact for each
 * cardinality and audits it through the same locator boundary used by the
 * production report explorations. This keeps the cardinality contract
 * independent from the API publisher tag vocabulary.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class CategoryCardinalityBrowserFixtureTest {
    private static final UiConfiguration CONFIGURATION = AtfUiConfigurationLoader.load();

    @TestFactory
    Stream<DynamicTest> generatedCategoryCardinalities() {
        return Stream.of(
                Fixture.ZERO,
                Fixture.ONE,
                Fixture.MANY)
                .map(fixture -> DynamicTest.dynamicTest(
                        "Categories — " + fixture.name().toLowerCase(),
                        () -> verifyFixture(fixture)));
    }

    private void verifyFixture(Fixture fixture) throws Exception {
        Path root = Files.createTempDirectory("atf-category-cardinality-");
        Path report = publishFixture(root, fixture);
        try (ReportAuditContext context = ReportAuditContext.open(CONFIGURATION, report,
                "category-cardinality-" + fixture.name().toLowerCase())) {
            UiAuditFlow audit = new UiAuditFlow(context, CONFIGURATION,
                    getClass().getSimpleName(), getClass().getPackageName(),
                    List.of("ui", "category-fixture"),
                    "generated " + fixture.name().toLowerCase() + " category fixture is loaded",
                    "Category cardinality fixture failed", ReportUiContract.Tab.CATEGORIES.hash());
            Page page = audit.openTab(ReportUiContract.Tab.CATEGORIES.hash());
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
            if (!Boolean.getBoolean("atf.keep.category.fixtures")) deleteTree(root);
        }
    }

    private static Path publishFixture(Path root, Fixture fixture) {
        try (UiExtentReportSession session = UiExtentReportSession.openConfigured(
                root, "categories-" + fixture.name().toLowerCase() + ".html",
                AtfConsoleSink.noOp())) {
            var scenario = session.startScenario("CategoryCardinalityFixture",
                    "atf.ui.tests.extent.configurable", "generated category fixture",
                    fixture.tags());
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.step("THEN", "generated category fixture is valid",
                    ReportUiContract.Tab.CATEGORIES.hash(), null, AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
            return session.reportPath();
        }
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception failure) {
                    throw new RuntimeException(failure);
                }
            });
        }
    }

    private enum Fixture {
        ZERO(0, List.of()),
        ONE(1, List.of("category-one")),
        MANY(3, List.of("category-a", "category-b", "category-c"));

        private final int expectedCategoryCount;
        private final List<String> tags;

        Fixture(int expectedCategoryCount, List<String> tags) {
            this.expectedCategoryCount = expectedCategoryCount;
            this.tags = tags;
        }

        int expectedCategoryCount() { return expectedCategoryCount; }

        List<String> tags() { return tags; }
    }
}
