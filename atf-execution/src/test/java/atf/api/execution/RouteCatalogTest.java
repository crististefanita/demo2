package atf.api.execution;
import atf.api.testkit.AtfComponentTest;


import atf.api.execution.routing.ApiRoutes;
import atf.api.execution.routing.RoutePaths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AtfComponentTest

class RouteCatalogTest {
    @Test
    void routeMetadataIsComposedFromCanonicalTemplates() {
        assertEquals(RoutePaths.USERS, ApiRoutes.usersCollection().pathTemplate());
        assertEquals(RoutePaths.USER_BY_ID, ApiRoutes.userItem().pathTemplate());
        assertEquals(RoutePaths.POSTS, ApiRoutes.postsCollection().pathTemplate());
        assertEquals(RoutePaths.POST_BY_ID, ApiRoutes.postItem().pathTemplate());
        assertEquals(RoutePaths.USER_POSTS, ApiRoutes.userPosts().pathTemplate());
        assertEquals(RoutePaths.USERS, ApiRoutes.userCreate().pathTemplate());
        assertEquals(RoutePaths.POSTS, ApiRoutes.postCreate().pathTemplate());
    }
}
