package atf.api.execution.routing;

/** Canonical route templates; metadata factories must compose from these values. */
public final class RoutePaths {
    public static final String USERS = "/public/v2/users";
    public static final String USER_BY_ID = USERS + "/{id}";
    public static final String POSTS = "/public/v2/posts";
    public static final String POST_BY_ID = POSTS + "/{id}";
    public static final String USER_POSTS = USERS + "/{userId}/posts";

    private RoutePaths() { }
}
