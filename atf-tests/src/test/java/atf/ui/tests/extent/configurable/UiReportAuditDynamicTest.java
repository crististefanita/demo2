package atf.ui.tests.extent.configurable;

import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.engine.UiReportAuditDynamicEngine;
import atf.ui.tests.extent.flows.UiReportAuditFixture;
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
 * One public entry point for every configured API-to-Extent UI audit.
 * Families and rule depth are data; no second class is needed for a new
 * collection.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Configurable Extent UI audit")
class UiReportAuditDynamicTest {
    private UiReportAuditFixture fixture;

    @BeforeAll
    void openConfiguredAudit() {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        UiReportAuditSpec spec = UiReportAuditSpec.resolve(getClass(), configuration);
        fixture = UiReportAuditFixture.open(getClass(), configuration, spec);
    }

    @AfterAll
    void closeConfiguredAudit() {
        if (fixture != null) fixture.close();
    }

    @TestFactory
    Stream<DynamicNode> configuredRules() {
        return Stream.of(UiReportAuditDynamicEngine.container(fixture));
    }
}
