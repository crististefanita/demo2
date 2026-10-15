package atf.api.mock;

import atf.api.core.http.ApiRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable route catalog and fallback policy for one mock execution. */
public record AtfMockConfiguration(List<MockRoute> routes, MockRoute fallback) {
    public AtfMockConfiguration {
        routes = routes == null ? List.of() : List.copyOf(routes);
        fallback = Objects.requireNonNull(fallback, "fallback");
    }

    public Optional<MockRoute> routeFor(ApiRequest request) {
        return routes.stream().filter(route -> route.matches(request)).findFirst();
    }

    /** Built-in compatibility fixture used when no mock YAML is supplied. */
    public static AtfMockConfiguration defaults() {
        Map<String, String> json = Map.of("Content-Type", "application/json");
        Duration duration = Duration.ofMillis(12);
        return new AtfMockConfiguration(List.of(
                new MockRoute("GET", "/public/v2/users", 200,
                        "[{\"id\":1,\"name\":\"ATF User\",\"email\":\"atf@example.test\"}]", json, duration),
                new MockRoute("GET", "/public/v2/users/1", 200,
                        "{\"id\":1,\"name\":\"ATF User\",\"email\":\"atf@example.test\"}", json, duration),
                new MockRoute("POST", "/public/v2/users", 401,
                        "{\"message\":\"Authentication required\"}", json, duration),
                new MockRoute("GET", "/public/v2/posts", 200,
                        "[{\"id\":101,\"user_id\":1,\"title\":\"ATF post\",\"body\":\"contract\"}]", json, duration),
                new MockRoute("GET", "/public/v2/posts/101", 200,
                        "{\"id\":101,\"user_id\":1,\"title\":\"ATF post\",\"body\":\"contract\"}", json, duration),
                new MockRoute("GET", "/public/v2/users/1/posts", 200,
                        "[{\"id\":101,\"user_id\":1,\"title\":\"ATF post\"}]", json, duration),
                new MockRoute("GET", "/public/v2/posts/999999", 404,
                        "{\"message\":\"Not found\"}", json, duration)),
                new MockRoute("*", "/*", 500,
                        "{\"message\":\"Unexpected mock route\"}", json, duration));
    }
}
