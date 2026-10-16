package atf.ui.tests.extent.section;

import atf.api.tests.api.posts.PostsApiScenarios;
import atf.api.tests.api.users.UsersApiScenarios;
import atf.api.tests.support.ApiScenarioSetup;
import atf.api.tests.support.ApiTestRef;
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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.stream.Stream;

/** Browser proof for the setup/mock projection consumed by Tests and Fails. */
@ApiScenarioSetup(tests = {
        @ApiTestRef(owner = UsersApiScenarios.class, method = UsersApiScenarios.GET_COLLECTION),
        @ApiTestRef(owner = PostsApiScenarios.class, method = PostsApiScenarios.CREATE_POST)
})
@UiExtentSectionAudit(source = UiExtentReportSource.GENERATED_API,
        sections = {ExtentSection.TESTS, ExtentSection.FAILS},
        checks = {ExtentSectionCheck.STRUCTURE})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Selected generated report — projection and Screen validation")
class SelectedGeneratedSectionsReportUiTest {
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
    Stream<DynamicNode> selectedSections() {
        return Stream.of(SectionAuditDynamicEngine.container(fixture));
    }
}
