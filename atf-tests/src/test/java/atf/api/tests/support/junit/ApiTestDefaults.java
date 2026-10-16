package atf.api.tests.support.junit;

import atf.api.tests.support.ApiEnvironment;

/** Shared test-only defaults for the offline API composition root. */
public final class ApiTestDefaults {
    public static final java.net.URI BASE_URI = ApiEnvironment.MOCK_BASE_URI;
    public static final String NO_AUTH_PROFILE = ApiEnvironment.NO_AUTH_PROFILE;

    private ApiTestDefaults() { }
}
