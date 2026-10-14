package atf.api.execution.routing;

import atf.api.core.http.ApiRoute;
import atf.api.core.http.AuthRequirement;
import atf.api.core.http.AuthType;

import java.util.Set;

public final class ApiRoutes {
    private ApiRoutes() { }

    public static ApiRoute usersCollection() {
        return new ApiRoute("users.collection", "GET", RoutePaths.USERS, "users", "collection", "default",
                AuthType.NONE, AuthRequirement.OPTIONAL, "anonymous or configured", Set.of("api", "users", "contract"));
    }

    public static ApiRoute userItem() {
        return new ApiRoute("users.item", "GET", RoutePaths.USER_BY_ID, "users", "item", "default",
                AuthType.NONE, AuthRequirement.OPTIONAL, "anonymous or configured", Set.of("api", "users", "contract"));
    }

    public static ApiRoute postsCollection() {
        return new ApiRoute("posts.collection", "GET", RoutePaths.POSTS, "posts", "collection", "default",
                AuthType.NONE, AuthRequirement.OPTIONAL, "anonymous or configured", Set.of("api", "posts", "contract"));
    }

    public static ApiRoute postItem() {
        return new ApiRoute("posts.item", "GET", RoutePaths.POST_BY_ID, "posts", "item", "default",
                AuthType.NONE, AuthRequirement.OPTIONAL, "anonymous or configured", Set.of("api", "posts", "contract"));
    }

    public static ApiRoute userPosts() {
        return new ApiRoute("users.posts", "GET", RoutePaths.USER_POSTS, "posts", "relationship", "default",
                AuthType.NONE, AuthRequirement.OPTIONAL, "anonymous or configured", Set.of("api", "users", "posts", "relationship"));
    }

    public static ApiRoute userCreate() {
        return new ApiRoute("users.create", "POST", RoutePaths.USERS, "users", "create", "default",
                AuthType.BEARER, AuthRequirement.REQUIRED, "bearer required", Set.of("api", "users", "write"));
    }

    public static ApiRoute postCreate() {
        return new ApiRoute("posts.create", "POST", RoutePaths.POSTS, "posts", "create", "default",
                AuthType.BEARER, AuthRequirement.REQUIRED, "bearer required", Set.of("api", "posts", "write"));
    }
}
