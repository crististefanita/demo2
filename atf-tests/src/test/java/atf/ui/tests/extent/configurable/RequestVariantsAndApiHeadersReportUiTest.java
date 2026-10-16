package atf.ui.tests.extent.configurable;

import atf.api.tests.support.ApiScenarioSetup;
import atf.ui.tests.extent.flows.AbstractApiReportUiContractTest;
import atf.ui.tests.extent.flows.ApiReportUiTestProfile;
import org.junit.jupiter.api.TestInstance;

/** Declarative UI contract proof for request-shape and header edge-case APIs. */
@ApiScenarioSetup(families = {
        ApiReportUiTestProfile.REQUESTS_FAMILY,
        ApiReportUiTestProfile.HEADERS_FAMILY
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequestVariantsAndApiHeadersReportUiTest extends AbstractApiReportUiContractTest {
    @Override
    protected ApiReportUiTestProfile profile() {
        return ApiReportUiTestProfile.REQUESTS_HEADERS;
    }
}
