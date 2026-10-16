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
 * Focused contract for the failed-phase presentation of LifecycleApiScenarios.
 * The input API report remains real and intentionally contains both a failed
 * TEST BODY and a failed TEARDOWN; this class audits only the rendered UI
 * contract for those failures.
 */
@ApiScenarioSetup(families = {"lifecycle"})
@UiExtentAudit(rules = {
        UiReportAuditRule.SETUP,
        UiReportAuditRule.TESTS,
        UiReportAuditRule.FAILURE_DETAILS
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Lifecycle API failure details — focused Extent UI audit")
class LifecycleFailureDetailsReportUiTest {
    private UiReportAuditFixture fixture;

    @BeforeAll
    void openLifecycleFailureDetailsAudit() {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        UiReportAuditSpec spec = UiReportAuditSpec.resolve(getClass(), configuration);
        fixture = UiReportAuditFixture.open(getClass(), configuration, spec);
    }

    @AfterAll
    void closeLifecycleFailureDetailsAudit() {
        if (fixture != null) fixture.close();
    }

    @TestFactory
    Stream<DynamicNode> lifecycleFailureDetailsRules() {
        return Stream.of(UiReportAuditDynamicEngine.container(fixture));
    }
}
