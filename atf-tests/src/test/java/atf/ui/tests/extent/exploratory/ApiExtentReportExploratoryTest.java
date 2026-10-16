package atf.ui.tests.extent.exploratory;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.tests.support.ApiScenarioSetup;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.audit.CrossTabReconciler;
import atf.ui.tests.extent.flows.ApiReportExplorationFlow;
import atf.ui.tests.extent.flows.UiReportAuditFixture;
import atf.ui.tests.extent.plan.UiReportAuditSpec;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import atf.ui.tests.extent.support.ValidationResult;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden end-to-end wiring check for the reusable Extent audit harness.
 *
 * <p>The configurable rule matrix lives in
 * {@code configurable/UiReportAuditDynamicTest}. This class intentionally
 * keeps one small replacement test: it proves that the generated API report
 * can be opened, structurally inspected and reconciled across the primary
 * projections without re-owning selectors, setup or rule orchestration.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("API Extent report golden exploratory wiring")
@ApiScenarioSetup
class ApiExtentReportExploratoryTest {
    private UiReportAuditFixture fixture;
    private UiAuditFlow auditFlow;

    @BeforeAll
    @DisplayName("GIVEN a resolved UI audit spec WHEN setup runs THEN one read-only report fixture is opened")
    void openGoldenFixture() {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        UiReportAuditSpec spec = UiReportAuditSpec.resolve(getClass(), configuration);
        fixture = UiReportAuditFixture.open(getClass(), configuration, spec);
        auditFlow = fixture.audit();
        assertTrue(Files.isRegularFile(fixture.setup().report()),
                "Golden wiring requires the generated API report artifact");
        assertTrue(fixture.setup().manifest().writeRoutesHaveJsonResponses(),
                "Golden wiring requires JSON responses for every write route");
    }

    @AfterAll
    void closeGoldenFixture() {
        if (fixture != null) fixture.close();
    }

    @Test
    @DisplayName("GIVEN the generated artifact WHEN primary projections are traversed THEN the shared manifest remains reconciled")
    void goldenArtifactWiringReconcilesPrimaryProjections() {
        auditFlow.audit("Golden E2E — generated report wiring and cross-tab reconciliation", audit -> {
            Page testsPage = fixture.flow().openTests();
            ReportAuditSnapshot.Tests tests = fixture.exploration()
                    .inspectEveryTestClass(testsPage);
            Page overviewPage = fixture.audit().openTab(ReportUiContract.Tab.OVERVIEW.hash());
            var overview = fixture.exploration().inspectOverviewSnapshot(overviewPage, true);
            var filters = fixture.exploration().inspectFiltersAndFailures();

            List<String> expectedOwners = fixture.setup().manifest().expectedOwnerNames();
            boolean ownersMatch = new LinkedHashSet<>(expectedOwners)
                    .equals(new LinkedHashSet<>(tests.classNames()));
            ValidationResult crossTab = new CrossTabReconciler().reconcile(
                    expectedOwners,
                    tests.classNames(),
                    overview.classCount(),
                    filters.annotations().linkedScenarioCount());
            boolean valid = ownersMatch && tests.scenarioCount() > 0
                    && tests.requestCount() > 0 && tests.validationCount() > 0
                    && overview.complete() && crossTab.valid();

            audit.screen("Golden report wiring is structurally reconciled",
                    "Tests ↔ Overview ↔ Categories ↔ Fails",
                    fixture.audit().capture(overviewPage, "golden wiring reconciliation"),
                    List.of(
                            ReportAuditChecks.check("Manifest owners", expectedOwners.toString(),
                                    tests.classNames().toString(), ownersMatch),
                            ReportAuditChecks.check("Tests scenarios", "> 0",
                                    String.valueOf(tests.scenarioCount()), tests.scenarioCount() > 0),
                            ReportAuditChecks.check("Tests requests", "> 0",
                                    String.valueOf(tests.requestCount()), tests.requestCount() > 0),
                            ReportAuditChecks.check("Overview complete", "true",
                                    String.valueOf(overview.complete()), overview.complete()),
                            ReportAuditChecks.check("Cross-tab findings", "0",
                                    String.valueOf(crossTab.violations()), crossTab.valid())),
                    valid ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
            assertTrue(valid, () -> "Golden report wiring failed: owners=" + tests.classNames()
                    + ", overview=" + overview + ", crossTab=" + crossTab.failureSummary());
        });
    }
}
