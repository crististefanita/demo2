package atf.ui.tests.extent.section;

import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.ExtentSectionCheck;
import atf.ui.tests.extent.section.plan.UiExtentReportSource;
import atf.ui.tests.extent.section.plan.UiExtentSectionAudit;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
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
 * Single browser gate for the rendered Screen validation evidence contract.
 * The input is a static UI report (for example RegressionExtent.html), so the
 * test never recursively audits the report it is currently generating.
 */
@EnabledIfSystemProperty(named = "atf.ui.screen.report", matches = ".+")
@UiExtentSectionAudit(source = UiExtentReportSource.STATIC_PATH,
        reportProperty = "atf.ui.screen.report", sections = {ExtentSection.SCREEN_VALIDATION},
        checks = {ExtentSectionCheck.STRUCTURE})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Static Extent report — Screen validation section module")
class StaticScreenValidationSectionReportUiTest {
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
    Stream<DynamicNode> screenValidationSection() {
        return Stream.of(SectionAuditDynamicEngine.container(fixture));
    }
}
