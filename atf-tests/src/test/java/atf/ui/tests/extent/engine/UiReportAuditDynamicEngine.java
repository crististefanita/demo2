package atf.ui.tests.extent.engine;

import atf.api.core.reporting.AtfReportStatus;
import atf.ui.tests.extent.audit.CrossTabReconciler;
import atf.ui.tests.extent.audit.ReportStyleValidator;
import atf.ui.tests.extent.flows.ApiReportExplorationFlow;
import atf.ui.tests.extent.flows.ApiReportUiFlow;
import atf.ui.tests.extent.flows.UiReportAuditFixture;
import atf.ui.tests.extent.flows.inspection.ReportProjectionMatrixInspector;
import atf.ui.tests.extent.flows.inspection.FailureFlowInspector;
import atf.ui.tests.extent.flows.inspection.FailureDetailsDisplayInspector;
import atf.ui.tests.extent.plan.UiReportAuditRule;
import atf.ui.tests.extent.plan.UiReportAuditSpec;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.validate.OverviewSectionValidator;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.ValidationResult;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Composes one dynamic JUnit container from a declarative audit spec.
 * Browser mechanics stay in flows; this class only maps a rule to the already
 * existing flow/validator and publishes the result as Extent evidence.
 */
public final class UiReportAuditDynamicEngine {
    private UiReportAuditDynamicEngine() { }

    public static DynamicNode container(UiReportAuditFixture fixture) {
        UiReportAuditSpec spec = fixture.spec();
        Stream<DynamicNode> nodes = Arrays.stream(UiReportAuditRule.values())
                .filter(spec::includes)
                .map(rule -> DynamicTest.dynamicTest(rule.displayName(),
                        () -> run(fixture, rule)));
        return DynamicContainer.dynamicContainer(spec.id(), nodes);
    }

    private static void run(UiReportAuditFixture fixture, UiReportAuditRule rule) {
        switch (rule) {
            case SETUP -> setup(fixture);
            case TESTS -> tests(fixture);
            case TESTS_DEEP -> testsDeep(fixture);
            case REQUEST_CONTRACT -> requestContract(fixture);
            case OVERVIEW -> overview(fixture);
            case OVERVIEW_DEEP -> overviewDeep(fixture);
            case CATEGORIES, FAILS -> filtersAndFailures(fixture, rule);
            case DURATIONS -> durations(fixture);
            case RESPONSIVE -> responsive(fixture);
            case STYLE -> style(fixture);
            case EVIDENCE -> evidence(fixture);
            case CROSS_TAB -> crossTab(fixture);
            case FAILURE_FLOW -> failureFlow(fixture);
            case FAILURE_DETAILS -> failureDetails(fixture);
        }
    }

    private static void setup(UiReportAuditFixture fixture) {
        fixture.audit().audit("Setup — API collection, mock and manifest", audit -> {
            var setup = fixture.setup();
            boolean valid = setup.report().toFile().isFile()
                    && setup.manifest().families().equals(fixture.spec().apiFamilies())
                    && setup.manifest().expectedOwnerNames().size() == fixture.spec().apiFamilies().size()
                    && setup.manifest().writeRoutesHaveJsonResponses();
            audit.step("THEN", "setup produces the expected read-only API input",
                    "setup → API → mock → Extent", null,
                    List.of(
                            ReportAuditChecks.check("Selected families", fixture.spec().apiFamilies().toString(),
                                    setup.manifest().families().toString(), setup.manifest().families().equals(fixture.spec().apiFamilies())),
                            ReportAuditChecks.check("Expected owners", "> 0",
                                    String.valueOf(setup.manifest().expectedOwnerNames().size()),
                                    !setup.manifest().expectedOwnerNames().isEmpty()),
                            ReportAuditChecks.check("POST/PUT JSON responses", "valid and non-empty",
                                    String.valueOf(setup.manifest().writeRouteCount()),
                                    setup.manifest().writeRoutesHaveJsonResponses())),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, "Setup manifest does not match the selected API collection");
        });
    }

    private static void tests(UiReportAuditFixture fixture) {
        fixture.audit().audit("Tests — dynamic owners, scenarios, phases and requests", audit -> {
            Page page = fixture.flow().openTests();
            ReportAuditSnapshot.Tests snapshot = fixture.exploration()
                    .inspectEveryTestClass(page);
            Set<String> expected = new LinkedHashSet<>(fixture.setup().manifest().expectedOwnerNames());
            Set<String> actual = new LinkedHashSet<>(snapshot.classNames());
            boolean valid = !actual.isEmpty() && actual.equals(expected)
                    && snapshot.scenarioCount() > 0 && snapshot.phaseCount() >= snapshot.scenarioCount()
                    && snapshot.requestCount() > 0 && snapshot.validationCount() > 0;
            audit.screen("Tests exposes the complete dynamic report tree", ReportUiContract.Tab.TESTS.hash(),
                    fixture.audit().capture(page, "Tests dynamic structure"), List.of(
                            ReportAuditChecks.check("Owners", expected.toString(), actual.toString(), actual.equals(expected)),
                            ReportAuditChecks.check("Scenarios", "> 0", String.valueOf(snapshot.scenarioCount()), snapshot.scenarioCount() > 0),
                            ReportAuditChecks.check("Phases", ">= scenarios", String.valueOf(snapshot.phaseCount()), snapshot.phaseCount() >= snapshot.scenarioCount()),
                            ReportAuditChecks.check("Requests", "> 0", String.valueOf(snapshot.requestCount()), snapshot.requestCount() > 0),
                            ReportAuditChecks.check("Validation cards", "> 0", String.valueOf(snapshot.validationCount()), snapshot.validationCount() > 0)),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, "Tests tab does not match the dynamic API manifest");
        });
    }

    private static void testsDeep(UiReportAuditFixture fixture) {
        fixture.audit().audit("Tests — selected API scenarios and evidence shape", audit -> {
            Page page = fixture.flow().openTests();
            var snapshot = fixture.exploration().inspectEveryTestClass(page);
            var selected = fixture.setup().manifest().selectedTests();
            var expectedProjection = fixture.setup().manifest().expectedProjection();
            boolean valid = snapshot.requestCount() > 0;
            List<String> findings = new ArrayList<>();
            if (selected.isEmpty()) {
                findings.add("family selection (no exact class.method refs)");
            } else {
                valid &= expectedProjection.matches(snapshot);
                findings.add("selected=" + selected.stream()
                        .map(selection -> selection.reference() + " → " + selection.scenarioTitle())
                        .toList());
                for (var selection : selected) {
                    ApiReportUiFlow.Owner owner = fixture.flow().openOwner(page, selection.reportOwner());
                    List<String> titles = new ArrayList<>();
                    var scenarios = fixture.flow().locators().scenarios(owner.detail());
                    for (int index = 0; index < scenarios.count(); index++) {
                        titles.add(fixture.flow().locators().nodeTitle(scenarios.nth(index)));
                    }
                    boolean titleFound = titles.contains(selection.scenarioTitle());
                    valid &= titleFound;
                    findings.add(selection.reference() + " rendered=" + titleFound
                            + " titles=" + titles);
                    fixture.flow().expandAll(owner.detail());
                }
            }
            audit.screen("Each selected API test renders a traceable TEST BODY",
                    ReportUiContract.Tab.TESTS.hash(), fixture.audit().capture(page, "selected test evidence"),
                    List.of(
                            ReportAuditChecks.check("Selected scenarios", selected.isEmpty()
                                            ? "family mode" : String.valueOf(selected.size()),
                                    selected.isEmpty() ? "family mode" : String.valueOf(expectedProjection.scenarioCount()),
                                    selected.isEmpty() || expectedProjection.matches(snapshot)),
                            ReportAuditChecks.check("Scenario identity", "every selected class.method",
                                    findings.toString(), valid)),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, () -> "Selected scenario evidence failed: " + findings);
        });
    }

    private static void requestContract(UiReportAuditFixture fixture) {
        fixture.audit().audit("Tests — every rendered request reconciles with mock JSON", audit -> {
            Page page = fixture.flow().openTests();
            List<String> actualOwners = fixture.flow().ownerNames(page);
            List<atf.api.core.reporting.AtfAssertion> assertions = new ArrayList<>();
            boolean valid = new LinkedHashSet<>(actualOwners)
                    .equals(new LinkedHashSet<>(fixture.setup().manifest().expectedOwnerNames()));
            for (int index = 0; index < actualOwners.size(); index++) {
                String ownerName = actualOwners.get(index);
                if (index > 0) fixture.flow().confirmTestsTabVisible(page, ownerName);
                ApiReportUiFlow.Owner owner = fixture.flow().openOwner(page, ownerName);
                int expanded = fixture.flow().expandOwnerRequests(owner);
                var result = fixture.validation().inspect(owner);
                assertions.addAll(fixture.validation().assertions(result));
                valid &= expanded == result.requestCount() && result.compliant();
                audit.screen(ownerName + " request/response JSON contract", ownerName,
                        fixture.audit().capture(page, ownerName + " contract evidence"),
                        fixture.validation().assertions(result),
                        result.compliant() ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
                assertTrue(expanded == result.requestCount(),
                        () -> ownerName + " expanded request count differs from inspected count");
                assertTrue(result.compliant(), () -> ownerName + ": " + result.violations());
            }
            assertTrue(valid, "At least one rendered owner does not match the API mock contract");
        });
    }

    private static void overview(UiReportAuditFixture fixture) {
        fixture.audit().audit("Overview — dynamic totals reconcile with Tests", audit -> {
            Page testsPage = fixture.flow().openTests();
            ReportAuditSnapshot.Tests snapshot = fixture.exploration()
                    .inspectEveryTestClass(testsPage);
            Page overviewPage = fixture.audit().openTab(ReportUiContract.Tab.OVERVIEW.hash());
            ReportAuditSnapshot.Overview overview = fixture.exploration()
                    .inspectOverviewSnapshot(overviewPage, true);
            ValidationResult result = new OverviewSectionValidator().validate(overview, snapshot,
                    new OverviewSectionValidator.Expected(fixture.setup().families(),
                            fixture.setup().mock().routes().size(),
                            java.util.Set.copyOf(fixture.setup().manifest().expectedOwnerNames())));
            audit.screen("Overview reconciles with the discovered Tests population",
                    ReportUiContract.Tab.OVERVIEW.hash(), fixture.audit().capture(overviewPage, "Overview reconciliation"),
                    result.assertions(), ReportAuditChecks.status(result.valid()));
            assertTrue(result.valid(), result::failureSummary);
        });
    }

    private static void overviewDeep(UiReportAuditFixture fixture) {
        fixture.audit().audit("Overview — selected projection and display contract", audit -> {
            Page testsPage = fixture.flow().openTests();
            var tests = fixture.exploration().inspectEveryTestClass(testsPage);
            Page overviewPage = fixture.audit().openTab(ReportUiContract.Tab.OVERVIEW.hash());
            var overview = fixture.exploration().inspectOverviewSnapshot(overviewPage, true);
            var result = new OverviewSectionValidator().validate(overview, tests,
                    new OverviewSectionValidator.Expected(fixture.setup().families(),
                            fixture.setup().mock().routes().size(),
                            java.util.Set.copyOf(fixture.setup().manifest().expectedOwnerNames())));
            boolean selectedProjection = fixture.setup().manifest().selectedTests().isEmpty()
                    || tests.scenarioCount() == fixture.setup().manifest().selectedTests().size();
            boolean valid = result.valid() && selectedProjection;
            audit.screen("Overview mirrors the selected Tests projection",
                    ReportUiContract.Tab.OVERVIEW.hash(), fixture.audit().capture(overviewPage, "selected overview"),
                    result.assertions(), ReportAuditChecks.status(valid));
            assertTrue(valid, () -> result.failureSummary()
                    + "; selected projection=" + selectedProjection);
        });
    }

    private static void filtersAndFailures(UiReportAuditFixture fixture, UiReportAuditRule rule) {
        fixture.audit().audit(rule.displayName(), audit -> {
            var snapshot = fixture.exploration().inspectFiltersAndFailures();
            boolean categoryValid = snapshot.annotations().categoryCount() > 0
                    && snapshot.annotations().linkedScenarioCount() > 0
                    && snapshot.annotations().styled() && snapshot.tagLinks();
            boolean failureValid = snapshot.failures().failureCount() == 0 || snapshot.failureLinks();
            boolean valid = rule == UiReportAuditRule.CATEGORIES ? categoryValid : failureValid;
            Page page = rule == UiReportAuditRule.CATEGORIES
                    ? snapshot.categoryPage() : snapshot.failsPage();
            audit.screen(rule.displayName(), rule == UiReportAuditRule.CATEGORIES
                            ? ReportUiContract.Tab.CATEGORIES.hash() : ReportUiContract.Tab.FAILS.hash(),
                    fixture.audit().capture(page, rule.name().toLowerCase() + " validation"),
                    List.of(
                            ReportAuditChecks.check("Categories", "> 0", String.valueOf(snapshot.annotations().categoryCount()), categoryValid),
                            ReportAuditChecks.check("Failure navigation or empty state", "actionable or empty",
                                    String.valueOf(snapshot.failures().failureCount()), failureValid)),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, () -> rule + " validation failed");
        });
    }

    private static void durations(UiReportAuditFixture fixture) {
        fixture.audit().audit("Tests — every dynamic duration is numeric and reconciled", audit -> {
            Page page = fixture.flow().openTests();
            int owners = fixture.exploration().ownerCount(page);
            ReportAuditSnapshot.Duration total =
                    new ReportAuditSnapshot.Duration(0, 0, 0, 0, 0, List.of());
            for (int index = 0; index < owners; index++) {
                total = fixture.exploration().combine(total,
                        fixture.exploration().selectOwnerAndInspectDuration(page, index));
            }
            boolean valid = owners > 0 && total.nodeCount() > 0
                    && total.durationMarkerCount() == total.nodeCount()
                    && total.formattedBadgeCount() == total.nodeCount()
                    && total.aggregatedNodeCount() > 0 && total.violations().isEmpty();
            List<ReportAuditSnapshot.DurationProjection> projections =
                    fixture.exploration().inspectDurationProjections(page);
            ValidationResult projectionResult = new atf.ui.tests.extent.flows.inspection.ApiReportDurationProjectionPolicy()
                    .validate(projections);
            boolean projectionValid = projectionResult.valid();
            valid = valid && projectionValid;
            audit.screen("Dynamic durations are present on every node", ReportUiContract.Tab.TESTS.hash(),
                    fixture.audit().capture(page, "duration contract"), List.of(
                            ReportAuditChecks.check("Owners", "> 0", String.valueOf(owners), owners > 0),
                            ReportAuditChecks.check("Duration markers", String.valueOf(total.nodeCount()),
                                    String.valueOf(total.durationMarkerCount()), total.durationMarkerCount() == total.nodeCount()),
                            ReportAuditChecks.check("Formatted badges", String.valueOf(total.nodeCount()),
                                    String.valueOf(total.formattedBadgeCount()), total.formattedBadgeCount() == total.nodeCount()),
                            ReportAuditChecks.check("Violations", "0", total.violations().toString(), total.violations().isEmpty()),
                            ReportAuditChecks.check("Duration projections", "all rendered surfaces valid",
                                    projections.toString(), projectionValid)),
                    ReportAuditChecks.status(valid));
            String durationViolations = total.violations().toString();
            assertTrue(valid, () -> "Duration contract failed: " + durationViolations);
        });
    }

    private static void responsive(UiReportAuditFixture fixture) {
        fixture.audit().audit("Tests — status badge geometry is stable at narrow width", audit -> {
            Page page = fixture.flow().openTests();
            var geometry = fixture.exploration().compareStatusBadgeGeometry(page,
                    1440, 900, 520, 900);
            boolean valid = geometry.normal().count() > 0
                    && geometry.normal().signature().equals(geometry.narrow().signature())
                    && geometry.normal().labels().equals(geometry.narrow().labels());
            audit.screen("Status badge geometry remains stable", ReportUiContract.Tab.TESTS.hash(),
                    fixture.audit().capture(page, "responsive badge geometry"), List.of(
                            ReportAuditChecks.check("Badge count", "> 0", String.valueOf(geometry.normal().count()), geometry.normal().count() > 0),
                            ReportAuditChecks.check("Computed signature", geometry.normal().signature(), geometry.narrow().signature(),
                                    geometry.normal().signature().equals(geometry.narrow().signature()))),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, "Status badge geometry drifted between viewports");
        });
    }

    private static void style(UiReportAuditFixture fixture) {
        fixture.audit().audit("All tabs — CSS surface and layout contract", audit -> {
            Page testsPage = fixture.flow().openTests();
            ReportStyleValidator.Result tests = new ReportStyleValidator(fixture.flow().locators())
                    .inspect(testsPage);
            Page overviewPage = fixture.audit().openTab(ReportUiContract.Tab.OVERVIEW.hash());
            var metadata = fixture.flow().locators().overviewExecutionMetadata(
                    fixture.flow().locators().overviewView(overviewPage));
            if (metadata.count() > 0 && !"true".equals(metadata.getAttribute("open"))) {
                fixture.audit().actions().click(
                        fixture.flow().locators().overviewExecutionMetadataAction(overviewPage),
                        "open Overview execution metadata for style validation");
            }
            ReportStyleValidator.Result overview = new ReportStyleValidator(fixture.flow().locators())
                    .inspect(overviewPage);
            Page categoriesPage = fixture.audit().openTab(ReportUiContract.Tab.CATEGORIES.hash());
            ReportStyleValidator.Result categories = new ReportStyleValidator(fixture.flow().locators())
                    .inspect(categoriesPage);
            Page failsPage = fixture.audit().openTab(ReportUiContract.Tab.FAILS.hash());
            ReportStyleValidator.Result fails = new ReportStyleValidator(fixture.flow().locators())
                    .inspect(failsPage);
            ReportProjectionMatrixInspector matrix = new ReportProjectionMatrixInspector(
                    fixture.flow().locators());
            List<String> matrixViolations = new ArrayList<>();
            for (ReportUiContract.Tab tab : ReportUiContract.Tab.values()) {
                var observation = matrix.inspect(fixture.audit().openTab(tab.hash()), tab);
                matrixViolations.addAll(observation.violations());
            }
            List<String> violations = new ArrayList<>(tests.violations());
            violations.addAll(overview.violations());
            violations.addAll(categories.violations());
            violations.addAll(fails.violations());
            violations.addAll(matrixViolations);
            boolean valid = violations.isEmpty();
            audit.screen("Important report surfaces remain visible, sized and styled",
                    ReportUiContract.Tab.OVERVIEW.hash() + " ↔ " + ReportUiContract.Tab.TESTS.hash(),
                    fixture.audit().capture(overviewPage, "style contract"),
                    List.of(ReportAuditChecks.check("CSS/layout violations", "0", violations.toString(), valid)),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, () -> "CSS/layout contract failed: " + violations);
        });
    }

    private static void evidence(UiReportAuditFixture fixture) {
        fixture.audit().audit("Tests — request, response and execution evidence are expandable", audit -> {
            Page page = fixture.flow().openTests();
            var evidence = fixture.exploration().inspectFirstRequestWithEvidence(page);
            var json = evidence.jsonEvidence();
            boolean valid = evidence.tablesVisible() && json.compliant();
            audit.screen("API evidence channels remain distinct and auditable",
                    ReportUiContract.Tab.TESTS.hash(), fixture.audit().capture(page, "request evidence"), List.of(
                            ReportAuditChecks.check("Request details", "> 0", String.valueOf(evidence.requestTableCount()), evidence.requestTableCount() > 0),
                            ReportAuditChecks.check("Response details", "> 0", String.valueOf(evidence.responseTableCount()), evidence.responseTableCount() > 0),
                            ReportAuditChecks.check("Execution evidence", "> 0", String.valueOf(evidence.executionTableCount()), evidence.executionTableCount() > 0),
                            ReportAuditChecks.check("JSON contract", "compliant", json.violations().toString(), json.compliant())),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, () -> "Evidence contract failed: " + json.violations());
        });
    }

    private static void crossTab(UiReportAuditFixture fixture) {
        fixture.audit().audit("Cross-tab — Tests, Overview, Categories and Fails share one projection", audit -> {
            Page testsPage = fixture.flow().openTests();
            List<String> owners = fixture.flow().ownerNames(testsPage);
            var filters = fixture.exploration().inspectFiltersAndFailures();
            Page overview = fixture.audit().openTab(ReportUiContract.Tab.OVERVIEW.hash());
            var observation = fixture.exploration().inspectOverviewSnapshot(overview, true);
            ValidationResult result = new CrossTabReconciler().reconcile(
                    fixture.setup().manifest().expectedOwnerNames(), owners,
                    observation.classCount(), filters.annotations().linkedScenarioCount());
            audit.screen("Every report tab reconciles to the same dynamic setup", "Tests ↔ Overview ↔ Categories ↔ Fails",
                    fixture.audit().capture(overview, "cross-tab reconciliation"),
                    result.assertions(), ReportAuditChecks.status(result.valid()));
            assertTrue(result.valid(), () -> "Report tabs do not reconcile to the same manifest: "
                    + result.failureSummary());
        });
    }

    private static void failureFlow(UiReportAuditFixture fixture) {
        fixture.audit().audit("Fails — Tests deep-link, causes and collapsed disclosures", audit -> {
            Page testsPage = fixture.flow().openTests();
            List<String> owners = fixture.setup().manifest().expectedOwnerNames();
            String expectedOwner = owners.stream().findFirst().orElseThrow(
                    () -> new IllegalStateException("Failure flow requires one expected API owner"));
            FailureFlowInspector.Observation observation = new FailureFlowInspector(
                    fixture.audit(), fixture.flow().locators()).inspect(testsPage, expectedOwner);
            boolean valid = observation.valid();
            audit.screen("Fails opens the real failure in Tests and keeps disclosures collapsed",
                    ReportUiContract.Tab.FAILS.hash(), fixture.audit().capture(
                            observation.overviewPage(), "failure flow overview"),
                    List.of(
                            ReportAuditChecks.check("Failure rows", "> 0",
                                    String.valueOf(observation.failureCount()), observation.failureCount() > 0),
                            ReportAuditChecks.check("Primary causes", "> 0",
                                    String.valueOf(observation.primaryCauseCount()), observation.primaryCauseCount() > 0),
                            ReportAuditChecks.check("Tests deep-link", "Tests + scenario-index",
                                    String.valueOf(observation.failureDeepLinkWorks()), observation.failureDeepLinkWorks()),
                            ReportAuditChecks.check("Technical descendants collapsed", "true",
                                    observation.collapsedSummary(), observation.detailsCollapsed())),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, () -> "Failure flow failed: " + observation.violations());
        });
    }

    private static void failureDetails(UiReportAuditFixture fixture) {
        fixture.audit().audit("Tests — failed phases are compact, collapsed and actionable", audit -> {
            Page page = fixture.flow().openTests();
            String expectedOwner = fixture.setup().manifest().expectedOwnerNames().stream()
                    .findFirst().orElseThrow(() -> new IllegalStateException(
                            "Failure details require one expected API owner"));
            FailureDetailsDisplayInspector.Observation observation =
                    new FailureDetailsDisplayInspector(fixture.audit(), fixture.flow().locators())
                            .inspect(page, expectedOwner);
            boolean valid = observation.valid();
            audit.screen("Failed TEST BODY and TEARDOWN phases explain the cause on demand",
                    ReportUiContract.Tab.TESTS.hash(), fixture.audit().capture(page,
                            "failed phase details and collapsed trace"),
                    List.of(
                            ReportAuditChecks.check("Failed scenarios", "> 0",
                                    String.valueOf(observation.failedScenarioCount()),
                                    observation.failedScenarioCount() > 0),
                            ReportAuditChecks.check("Failed phases", "> 0",
                                    String.valueOf(observation.failedPhaseCount()),
                                    observation.failedPhaseCount() > 0),
                            ReportAuditChecks.check("Compact disclosures", 
                                    String.valueOf(observation.failedPhaseCount()),
                                    String.valueOf(observation.failureDisclosureCount()),
                                    observation.failureDisclosureCount() == observation.failedPhaseCount()),
                            ReportAuditChecks.check("Readable causes", 
                                    String.valueOf(observation.failedPhaseCount()),
                                    String.valueOf(observation.readableCauseCount()),
                                    observation.readableCauseCount() == observation.failedPhaseCount()),
                            ReportAuditChecks.check("Semantic causes", 
                                    String.valueOf(observation.failedPhaseCount()),
                                    String.valueOf(observation.semanticCauseCount()),
                                    observation.semanticCauseCount() == observation.failedPhaseCount()),
                            ReportAuditChecks.check("Assertion expected/actual", "> 0",
                                    observation.assertionEvidenceCount() + "/"
                                            + observation.expectedActualPairCount(),
                                    observation.assertionEvidenceCount() > 0
                                            && observation.expectedActualPairCount() > 0),
                            ReportAuditChecks.check("Trace hidden on arrival", "0",
                                    String.valueOf(observation.visibleTraceTablesOnArrival()),
                                    observation.visibleTraceTablesOnArrival() == 0),
                            ReportAuditChecks.check("Final Tests state", "collapsed",
                                    String.valueOf(observation.finalStateCollapsed()),
                                    observation.finalStateCollapsed())),
                    ReportAuditChecks.status(valid));
            assertTrue(valid, () -> "Failed phase display contract failed: "
                    + observation.violations());
        });
    }
}
