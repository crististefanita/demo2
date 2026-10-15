package atf.api.core.http;

    /** Declares whether a route accepts, recommends, or requires authentication. */
public enum AuthRequirement {
    NONE,
    OPTIONAL,
    REQUIRED
}
