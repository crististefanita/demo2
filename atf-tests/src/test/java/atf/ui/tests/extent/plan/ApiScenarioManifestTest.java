package atf.ui.tests.extent.plan;

import atf.api.mock.AtfMockConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiScenarioManifestTest {
    @Test
    void derivesOwnersAndWriteContractFromSelectedFamiliesAndMock() {
        ApiScenarioManifest manifest = ApiScenarioManifest.from(
                java.util.List.of("users", "posts"), AtfMockConfiguration.defaults());

        assertEquals(java.util.List.of("users", "posts"), manifest.families());
        assertEquals(java.util.List.of("UsersApiTests", "PostsApiTests"), manifest.expectedOwnerNames());
        assertTrue(manifest.writeRouteCount() >= 2);
        assertTrue(manifest.writeRoutesHaveJsonResponses());
        assertTrue(manifest.routes().stream().anyMatch(route -> route.identity().equals("POST /public/v2/posts")));
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
