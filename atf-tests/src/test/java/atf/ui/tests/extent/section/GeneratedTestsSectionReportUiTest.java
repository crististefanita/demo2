package atf.ui.tests.extent.section;

import atf.api.tests.support.ApiScenarioSetup;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.ExtentSectionCheck;
import atf.ui.tests.extent.section.plan.ExtentJourney;
import atf.ui.tests.extent.section.plan.UiExtentReportSource;
import atf.ui.tests.extent.section.plan.UiExtentSectionAudit;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.stream.Stream;

/** Generated vertical slice: annotation → API setup → mock → report → Tests section. */
@ApiScenarioSetup(families = {"lifecycle"})
@UiExtentSectionAudit(source = UiExtentReportSource.GENERATED_API,
        sections = {ExtentSection.OVERVIEW, ExtentSection.TESTS, ExtentSection.CATEGORIES,
        ExtentSection.FAILS, ExtentSection.EVIDENCE}, checks = {ExtentSectionCheck.STRUCTURE},
        journey = ExtentJourney.FAILURE_NAVIGATION)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Generated API report — Tests section module")
class GeneratedTestsSectionReportUiTest {
    private SectionAuditFixture fixture;

    @BeforeAll
    void open() {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        fixture = SectionAuditFixture.open(getClass(), configuration);
    }

    @AfterAll
    void close() {
        if (fixture != null) fixture.close();
    }

    @TestFactory
    Stream<DynamicNode> testsSection() {
        return Stream.of(SectionAuditDynamicEngine.container(fixture));
    }
}

