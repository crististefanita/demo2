package atf.ui.tests.extent.component.support;
import atf.api.testkit.AtfComponentTest;

import atf.api.core.http.request.HttpMethod;
import atf.api.mock.MockRoute;
import atf.api.mock.MockRequestBodyMode;
import atf.ui.tests.extent.support.config.ReportingTestResources;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps reporting tests independent from the project-root fixture discovery policy. */
@AtfComponentTest
class ReportingTestResourcesTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void reportingOwnsTheUiAndMockResources() {
        assertTrue(ReportingTestResources.resourcePath(ReportingTestResources.UI_RESOURCE)
                .toString().endsWith(ReportingTestResources.UI_RESOURCE));
        assertTrue(ReportingTestResources.resourcePath(ReportingTestResources.MOCK_RESOURCE)
                .toString().endsWith(ReportingTestResources.MOCK_RESOURCE));

        var configuration = ReportingTestResources.mockConfiguration();
        assertFalse(configuration.routes().isEmpty());
        assertTrue(configuration.routes().stream()
                .filter(ReportingTestResourcesTest::isJsonWrite)
                .allMatch(ReportingTestResourcesTest::isJsonContract));
    }

    private static boolean isJsonWrite(MockRoute route) {
        return HttpMethod.POST.value().equals(route.method())
                || HttpMethod.PUT.value().equals(route.method());
    }

    private static boolean isJsonContract(MockRoute route) {
        try {
            MockRequestBodyMode.from(route.requestBodyMode());
            return route.requestBodyMode() != null
                    && JSON.readTree(route.body()) != null;
        } catch (Exception error) {
            return false;
        }
    }
}
