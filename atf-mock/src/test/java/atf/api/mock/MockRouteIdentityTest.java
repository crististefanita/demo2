package atf.api.mock;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MockRouteIdentityTest {
    @Test
    void routeKeyIncludesSortedQueryIdentityForSameMethodAndPathVariants() {
        var first = new MockRoute("GET", "/search", 200, "{}", Map.of(), Duration.ZERO,
                "bearer", Map.of("page", "2", "include", "details"));
        var second = new MockRoute("GET", "/search", 400, "{}", Map.of(), Duration.ZERO,
                "bearer", Map.of("include", "details", "page", "invalid"));

        assertEquals("GET /search?include=details&page=2", first.key());
        assertEquals("GET /search?include=details&page=invalid", second.key());
    }
}
