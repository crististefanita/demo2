package atf.api.tests.support;

import java.net.URI;

/** Canonical environment values for deterministic API scenarios and mocks. */
public final class ApiEnvironment {
    public static final URI MOCK_BASE_URI = URI.create("https://atf.local");
    public static final String NO_AUTH_PROFILE = "none";

    private ApiEnvironment() { }
}
