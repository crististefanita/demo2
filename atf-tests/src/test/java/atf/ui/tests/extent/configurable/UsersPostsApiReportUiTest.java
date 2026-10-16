package atf.ui.tests.extent.configurable;

import atf.api.tests.support.ApiScenarioSetup;
import atf.ui.tests.extent.flows.AbstractApiReportUiContractTest;
import atf.ui.tests.extent.flows.ApiReportUiTestProfile;
import org.junit.jupiter.api.TestInstance;

/** Declarative API-to-UI proof for the Users and Posts mock families. */
@ApiScenarioSetup(families = {
        ApiReportUiTestProfile.USERS_FAMILY,
        ApiReportUiTestProfile.POSTS_FAMILY
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsersPostsApiReportUiTest extends AbstractApiReportUiContractTest {
    @Override
    protected ApiReportUiTestProfile profile() {
        return ApiReportUiTestProfile.USERS_POSTS;
    }
}
