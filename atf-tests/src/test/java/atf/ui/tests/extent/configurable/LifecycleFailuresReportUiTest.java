package atf.ui.tests.extent.configurable;

import atf.api.tests.support.ApiScenarioSetup;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.engine.UiReportAuditDynamicEngine;
import atf.ui.tests.extent.flows.UiReportAuditFixture;
import atf.ui.tests.extent.plan.UiExtentAudit;
import atf.ui.tests.extent.plan.UiReportAuditRule;
import atf.ui.tests.extent.plan.UiReportAuditSpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.stream.Stream;

/**
 * Dedicated real-failure contract for the LifecycleApiScenarios report.
 * The family is selected programmatically; YAML remains only the mock source.
 */
@ApiScenarioSetup(families = {"lifecycle"})
@UiExtentAudit(rules = {
        UiReportAuditRule.SETUP,
        UiReportAuditRule.TESTS,
        UiReportAuditRule.TESTS_DEEP,
        UiReportAuditRule.REQUEST_CONTRACT,
        UiReportAuditRule.OVERVIEW,
        UiReportAuditRule.OVERVIEW_DEEP,
        UiReportAuditRule.CATEGORIES,
        UiReportAuditRule.FAILS,
        UiReportAuditRule.DURATIONS,
        UiReportAuditRule.EVIDENCE,
        UiReportAuditRule.RESPONSIVE,
        UiReportAuditRule.STYLE,
        UiReportAuditRule.CROSS_TAB,
        UiReportAuditRule.FAILURE_FLOW,
        UiReportAuditRule.FAILURE_DETAILS
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Lifecycle API failures — deep Extent UI audit")
class LifecycleFailuresReportUiTest {
    private UiReportAuditFixture fixture;

    @BeforeAll
    void openLifecycleFailureAudit() {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        UiReportAuditSpec spec = UiReportAuditSpec.resolve(getClass(), configuration);
        fixture = UiReportAuditFixture.open(getClass(), configuration, spec);
    }

    @AfterAll
    void closeLifecycleFailureAudit() {
        if (fixture != null) fixture.close();
    }

    @TestFactory
    Stream<DynamicNode> lifecycleFailureRules() {
        return Stream.of(UiReportAuditDynamicEngine.container(fixture));
    }
}
