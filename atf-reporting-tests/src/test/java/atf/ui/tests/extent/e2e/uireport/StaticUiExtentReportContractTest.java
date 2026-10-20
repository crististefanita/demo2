package atf.ui.tests.extent.e2e.uireport;
import atf.api.testkit.AtfE2eTest;
import atf.ui.tests.extent.support.uireport.UiReportContractDynamicEngine;
import atf.ui.tests.extent.support.uireport.browser.UiReportContractFixture;
import atf.api.reporting.ui.contract.UiExtentReportContract;

import atf.ui.tests.extent.support.config.ReportingTestResources;
import atf.ui.tests.extent.support.config.UiConfiguration;
import atf.api.core.config.module.AtfModule;
import atf.api.reporting.test.AtfModuleTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.stream.Stream;

/**
 * Plug-and-play browser gate for a previously generated UI Extent report.
 *
 * <p>Run with {@code -Datf.ui.report=<previous-ui-report.html>}. It does not
 * generate API setup, does not read business expectations from mock YAML and
 * never audits the UI report that it is currently publishing.</p>
 */
@EnabledIfSystemProperty(named = "atf.ui.report", matches = ".+")
@AtfModuleTest(requires = {AtfModule.REPORTING, AtfModule.UI})
@UiExtentReportContract
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Static UI Extent report — all tabs and renderer contract")
@AtfE2eTest
class StaticUiExtentReportContractTest {
    private UiReportContractFixture fixture;

    @BeforeAll
    void open() {
        UiConfiguration configuration = ReportingTestResources.uiConfiguration();
        fixture = UiReportContractFixture.open(getClass(), configuration);
    }

    @AfterAll
    void close() {
        if (fixture != null) fixture.close();
    }

    @TestFactory
    Stream<DynamicNode> allTabs() {
        return Stream.of(UiReportContractDynamicEngine.container(fixture));
    }
}
