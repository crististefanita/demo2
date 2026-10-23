package atf.api.core.http.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthProfilesTest {
    @Test
    void normalizesMissingProfilesToNoAuthentication() {
        assertEquals(AuthProfiles.NONE, AuthProfiles.normalize(null));
        assertEquals(AuthProfiles.NONE, AuthProfiles.normalize("  "));
        assertEquals("customer", AuthProfiles.normalize(" customer "));
    }

    @Test
    void derivesStableNamesForStandardAuthenticationTypes() {
        assertEquals("bearer", AuthProfiles.standard(AuthType.BEARER));
        assertEquals("api-key", AuthProfiles.standard(AuthType.API_KEY));
    }
}
