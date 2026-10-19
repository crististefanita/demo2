package atf.ui.tests.extent.component.plan;
import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.extent.support.data.UiTestData;

import atf.api.mock.AtfMockConfiguration;
import atf.api.tests.api.posts.PostsApiScenarios;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ApiScenarioManifestTest {
    @Test
    void derivesOwnersAndWriteContractFromSelectedFamiliesAndMock() {
        ApiScenarioManifest manifest = ApiScenarioManifest.from(
                java.util.List.of(UiTestData.USERS_FAMILY, UiTestData.POSTS_FAMILY),
                AtfMockConfiguration.defaults());

        assertEquals(java.util.List.of(UiTestData.USERS_FAMILY, UiTestData.POSTS_FAMILY),
                manifest.families());
        assertEquals(java.util.List.of(UiTestData.USERS_OWNER, UiTestData.POSTS_OWNER),
                manifest.expectedOwnerNames());
        assertTrue(manifest.writeRouteCount() >= 2);
        assertTrue(manifest.writeRoutesHaveJsonResponses());
        assertTrue(manifest.routes().stream().anyMatch(route ->
                route.identity().equals(PostsApiScenarios.CREATE_ROUTE_IDENTITY)));
    }

    @Test
    void rejectsUnknownFamilyInsteadOfLearningExpectedOwnersFromDom() {
        IllegalArgumentException error = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ApiScenarioManifest.from(java.util.List.of("not-a-family"),
                        AtfMockConfiguration.defaults()));
        assertTrue(error.getMessage().contains("Unknown API family"));
    }
}
