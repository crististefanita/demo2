package atf.api.execution.routing;

/** Canonical route templates; metadata factories must compose from these values. */
public final class RoutePaths {
    public static final String USERS = "/public/v2/users";
    public static final String USER_BY_ID = USERS + "/{id}";
    public static final String POSTS = "/public/v2/posts";
    public static final String POST_BY_ID = POSTS + "/{id}";
    public static final String USER_POSTS = USERS + "/{userId}/posts";
    public static final String PROFILES = "/public/v2/profiles";
    public static final String PROFILE_BY_ID = PROFILES + "/{id}";
    public static final String CATALOG = "/public/v2/catalog";
    public static final String CATALOG_EXPORT = CATALOG + "/export";
    public static final String CATALOG_ITEM_42 = CATALOG + "/42";
    public static final String SEARCH = "/public/v2/search";
    public static final String BULK_IMPORT = "/public/v2/bulk-import";
    public static final String PREFERENCES = "/public/v2/preferences";
    public static final String POSTS_INVALID_JSON = POSTS + "/invalid-json";
    public static final String POSTS_MISSING_BODY = POSTS + "/missing-body";
    public static final String POSTS_WRONG_CONTENT_TYPE = POSTS + "/wrong-content-type";
    public static final String POSTS_MISSING_TITLE = POSTS + "/missing-title";
    public static final String POSTS_CONFLICT = POSTS + "/conflict";
    public static final String POSTS_UNKNOWN_AUTH = POSTS + "/unknown-auth";
    public static final String POSTS_SERVER_ERROR = POSTS + "/server-error";
    public static final String PROFILE_FORBIDDEN = PROFILES + "/1/forbidden";

    private RoutePaths() { }
}
