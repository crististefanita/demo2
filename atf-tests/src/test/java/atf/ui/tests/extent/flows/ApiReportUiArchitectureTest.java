package atf.ui.tests.extent.flows;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Source boundary for the declarative API-to-UI test path. */
class ApiReportUiArchitectureTest {
    @Test
    void usersPostsScenarioContainsIntentOnlyAndNoPlaywrightSelectors() throws Exception {
        Path source = Path.of(ApiReportUiArchitectureTest.class
                .getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("../../src/test/java/atf/ui/tests/extent/configurable/UsersPostsApiReportUiTest.java")
                .normalize();
        String text = Files.readString(source);

        assertTrue(text.contains("@ApiScenarioSetup"));
        assertTrue(text.contains("ApiReportUiTestProfile.USERS_FAMILY"));
        assertTrue(text.contains("ApiReportUiTestProfile.POSTS_FAMILY"));
        assertTrue(text.contains("extends AbstractApiReportUiContractTest"));
        assertTrue(text.contains("ApiReportUiTestProfile.USERS_POSTS"));
        assertTrue(!text.contains(".locator("), "UI test intent must not build selectors");
        assertTrue(!text.contains("getByRole("), "UI test intent must not build role selectors");
        assertTrue(!text.contains("getByText("), "UI test intent must not build text selectors");
    }
}
