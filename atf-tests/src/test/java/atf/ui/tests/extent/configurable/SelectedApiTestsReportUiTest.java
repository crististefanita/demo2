package atf.ui.tests.extent.configurable;

import atf.api.tests.api.posts.PostsApiScenarios;
import atf.api.tests.api.users.UsersApiScenarios;
import atf.api.tests.support.ApiScenarioSetup;
import atf.api.tests.support.ApiTestRef;
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
 * Programmatic proof for one selected API test from two different families.
 * The annotation is the suite definition; YAML remains the mock contract.
 */
@ApiScenarioSetup(tests = {
        @ApiTestRef(owner = UsersApiScenarios.class, method = UsersApiScenarios.GET_COLLECTION),
        @ApiTestRef(owner = PostsApiScenarios.class, method = PostsApiScenarios.CREATE_POST)
})
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
        UiReportAuditRule.CROSS_TAB
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Selected API tests — deep Extent UI audit")
class SelectedApiTestsReportUiTest {
    private UiReportAuditFixture fixture;

    @BeforeAll
    void openSelectedAudit() {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        UiReportAuditSpec spec = UiReportAuditSpec.resolve(getClass(), configuration);
        fixture = UiReportAuditFixture.open(getClass(), configuration, spec);
    }

    @AfterAll
    void closeSelectedAudit() {
        if (fixture != null) fixture.close();
    }

    @TestFactory
    Stream<DynamicNode> selectedRules() {
        return Stream.of(UiReportAuditDynamicEngine.container(fixture));
    }
}
