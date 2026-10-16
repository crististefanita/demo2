package atf.ui.tests.extent.configurable;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.audit.ApiReportContractValidation;
import atf.ui.tests.extent.flows.ApiReportExplorationFlow;
import atf.ui.tests.extent.flows.UiReportAuditFixture;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.plan.UiReportAuditRule;
import atf.ui.tests.extent.plan.UiReportAuditSpec;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Data-driven browser matrix for the reusable report contract.
 *
 * <p>Each case owns one generated, read-only API artifact and one UI audit
 * context. The cases deliberately exercise empty and failed Fails states,
 * GET/POST/PUT evidence and both viewport geometries without duplicating
 * selector or report orchestration code.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Configurable Extent UI browser matrix")
class UiReportAuditBrowserMatrixTest {
    private static final List<String> ALL_FAMILIES = List.of(
            "posts", "users", "requests", "headers", "flows", "negative", "lifecycle");
    private static final Set<UiReportAuditRule> MATRIX_RULES = EnumSet.of(
            UiReportAuditRule.SETUP, UiReportAuditRule.TESTS,
            UiReportAuditRule.REQUEST_CONTRACT, UiReportAuditRule.CATEGORIES,
            UiReportAuditRule.FAILS, UiReportAuditRule.RESPONSIVE,
            UiReportAuditRule.DURATIONS, UiReportAuditRule.CROSS_TAB);

    private UiConfiguration configuration;

    @BeforeAll
    void loadConfiguration() {
        configuration = AtfUiConfigurationLoader.load();
    }

    @TestFactory
    Stream<DynamicTest> browserMatrix() {
        Stream<MatrixCase> cases = Stream.of(
                new MatrixCase("empty-users-posts-normal", List.of("users", "posts"),
                        false, FailState.EMPTY, false),
                new MatrixCase("failure-lifecycle-narrow", List.of("lifecycle"),
                        true, FailState.FAILURE, false),
                new MatrixCase("write-methods-all-api-normal", ALL_FAMILIES,
                        false, FailState.FAILURE, true),
                new MatrixCase("write-methods-all-api-narrow", ALL_FAMILIES,
                        true, FailState.FAILURE, true));
        String selected = System.getProperty("atf.ui.matrix.case", "").trim();
        if (!selected.isBlank()) cases = cases.filter(testCase -> testCase.id().equals(selected));
        return cases
                .map(testCase -> DynamicTest.dynamicTest(testCase.id(),
                        () -> runCase(testCase)));
    }

    private void runCase(MatrixCase testCase) {
        UiReportAuditSpec spec = new UiReportAuditSpec(
                testCase.id(), testCase.families(), MATRIX_RULES,
                List.of("ui", "browser-matrix", testCase.id()),
                "the " + testCase.id() + " API setup report is loaded read-only",
                "Browser matrix case failed", "generated API matrix report");
        try (UiReportAuditFixture fixture = UiReportAuditFixture.open(getClass(), configuration, spec)) {
            fixture.audit().audit("Browser matrix — " + testCase.id(), audit -> {
                var setup = fixture.setup();
                var testsPage = fixture.flow().openTests();
                ReportAuditSnapshot.Tests tests = fixture.exploration().inspectEveryTestClass(testsPage);
                Set<String> actualOwners = new LinkedHashSet<>(tests.classNames());
                Set<String> expectedOwners = new LinkedHashSet<>(setup.manifest().expectedOwnerNames());
                boolean ownersValid = actualOwners.equals(expectedOwners);

                Set<String> renderedMethods = new LinkedHashSet<>();
                List<ApiReportContractValidation.Result> contracts = new java.util.ArrayList<>();
                for (String ownerName : fixture.flow().ownerNames(testsPage)) {
                    var owner = fixture.flow().openOwner(testsPage, ownerName);
                    int expanded = fixture.flow().expandOwnerRequests(owner);
                    ApiReportContractValidation.Result result = fixture.validation().inspect(owner);
                    contracts.add(result);
                    result.requestsAudited().forEach(request -> renderedMethods.add(method(request.title())));
                    assertTrue(expanded == result.requestCount(),
                            () -> ownerName + " expanded=" + expanded + " inspected=" + result.requestCount());
                    assertTrue(result.compliant(), () -> ownerName + " contract: " + result.violations());
                }
                boolean jsonAndMockValid = !contracts.isEmpty()
                        && contracts.stream().allMatch(ApiReportContractValidation.Result::compliant);
                boolean methodCoverage = !testCase.requireWriteMethods()
                        || renderedMethods.containsAll(Set.of("GET", "POST", "PUT"));

                var filters = fixture.exploration().inspectFiltersAndFailures();
                boolean failureShape = testCase.failState().matches(filters.failures());
                boolean categoryShape = filters.annotations().categoryCount() > 0
                        && filters.annotations().linkedScenarioCount() > 0
                        && filters.annotations().styled()
                        && filters.annotations().linkCardinalities().size()
                        == filters.annotations().categoryCount()
                        && filters.annotations().linkCardinalities().stream()
                        .allMatch(value -> value >= 0);
                String cardinalityShape = cardinalityShape(filters.annotations());
                boolean responsive = true;
                if (testCase.narrowViewport()) {
                    // Contract inspection may leave the selected owner detail
                    // active. Responsive validation starts from a fresh Tests
                    // projection so a previous disclosure cannot hide it.
                    var responsivePage = fixture.flow().openTests();
                    var geometry = fixture.exploration().compareStatusBadgeGeometry(
                            responsivePage, configuration.browser().viewportWidth(),
                            configuration.browser().viewportHeight(), 520, 900);
                    responsive = geometry.normal().count() > 0
                            && geometry.normal().labels().equals(geometry.narrow().labels())
                            && geometry.normal().signature().equals(geometry.narrow().signature());
                }
                boolean valid = ownersValid && tests.scenarioCount() > 0
                        && tests.requestCount() > 0 && jsonAndMockValid && methodCoverage
                        && failureShape && categoryShape && responsive;
                System.out.println("[ATF MATRIX] case=" + testCase.id()
                        + " owners=" + ownersValid + " requests=" + tests.requestCount()
                        + " contracts=" + jsonAndMockValid + " methods=" + renderedMethods
                        + " fails=" + filters.failures() + " failureShape=" + failureShape
                        + " categories=" + categoryShape + " cardinality=" + cardinalityShape
                        + " responsive=" + responsive);
                audit.screen("Matrix case reconciles setup, transport and UI projections",
                        ReportUiContract.Tab.TESTS.hash() + " ↔ " + ReportUiContract.Tab.FAILS.hash(),
                        fixture.audit().capture(testsPage, testCase.id() + " evidence"),
                        List.of(
                                ReportAuditChecks.check("Manifest owners", expectedOwners.toString(),
                                        actualOwners.toString(), ownersValid),
                                ReportAuditChecks.check("Rendered methods", testCase.requireWriteMethods()
                                                ? "GET, POST, PUT" : "configured methods",
                                        renderedMethods.toString(), methodCoverage),
                                ReportAuditChecks.check("Request/mock contracts", "all compliant",
                                        contracts.size() + " owners", jsonAndMockValid),
                                ReportAuditChecks.check("Fails state", testCase.failState().name(),
                                        filters.failures().toString(), failureShape),
                                ReportAuditChecks.check("Categories", "> 0 linked and styled",
                                        filters.annotations().toString(), categoryShape),
                                ReportAuditChecks.check("Category cardinality", "dynamic zero/one/many",
                                        cardinalityShape, categoryShape),
                                ReportAuditChecks.check("Viewport", testCase.narrowViewport()
                                                ? "normal = narrow geometry" : "configured viewport",
                                        String.valueOf(responsive), responsive)),
                        valid ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
                assertTrue(valid, () -> "Matrix case failed: " + testCase.id()
                        + ", methods=" + renderedMethods + ", fails=" + filters.failures());
            });
        }
    }

    private static String method(String title) {
        if (title == null || title.isBlank()) return "";
        return title.trim().split("\\s+", 2)[0].toUpperCase(Locale.ROOT);
    }

    private static String cardinalityShape(ReportAuditSnapshot.Annotations annotations) {
        StringBuilder shape = new StringBuilder();
        if (annotations.hasZeroLinks()) shape.append("zero ");
        if (annotations.hasOneLink()) shape.append("one ");
        if (annotations.hasManyLinks()) shape.append("many ");
        return shape.isEmpty() ? "none" : shape.toString().trim();
    }


    private enum FailState {
        EMPTY {
            @Override boolean matches(ReportAuditSnapshot.Failures snapshot) {
                return snapshot.failureCount() == 0 && snapshot.emptyState();
            }
        },
        FAILURE {
            @Override boolean matches(ReportAuditSnapshot.Failures snapshot) {
                return snapshot.failureCount() > 0 && snapshot.primaryCauseCount() > 0
                        && snapshot.openLinkCount() > 0;
            }
        };

        abstract boolean matches(ReportAuditSnapshot.Failures snapshot);
    }

    private record MatrixCase(String id, List<String> families, boolean narrowViewport,
                              FailState failState, boolean requireWriteMethods) {
        private MatrixCase {
            id = id.toLowerCase(Locale.ROOT);
            families = List.copyOf(families);
        }
    }
}
