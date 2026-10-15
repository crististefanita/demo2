package atf.api.mock;

import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiResponse;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** One deterministic method/path response owned by the ATF mock module. */
public record MockRoute(String method, String path, int status, String body,
                        Map<String, String> headers, Duration duration,
                        String authProfile, Map<String, String> query,
                        Map<String, String> requiredHeaders) {
    public MockRoute(String method, String path, int status, String body,
                     Map<String, String> headers, Duration duration) {
        this(method, path, status, body, headers, duration, "*", Map.of(), Map.of());
    }

    public MockRoute(String method, String path, int status, String body,
                     Map<String, String> headers, Duration duration,
                     String authProfile, Map<String, String> query) {
        this(method, path, status, body, headers, duration, authProfile, query, Map.of());
    }

    public MockRoute {
        method = normalizeMethod(method);
        path = normalizePath(path);
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("mock response status must be between 100 and 599");
        }
        body = body == null ? "" : body;
        headers = headers == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(headers));
        duration = duration == null ? Duration.ofMillis(12) : duration;
        if (duration.isNegative()) throw new IllegalArgumentException("mock duration must not be negative");
        authProfile = authProfile == null || authProfile.isBlank() ? "*" : authProfile.trim();
        query = query == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(query));
        requiredHeaders = requiredHeaders == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(requiredHeaders));
    }

    public String key() {
        return method + " " + path;
    }

    public boolean matches(ApiRequest request) {
        Objects.requireNonNull(request, "request");
        return (method.equals("*") || method.equalsIgnoreCase(request.method()))
                && pathMatches(request)
                && authMatches(request)
                && query.entrySet().stream()
                .allMatch(entry -> entry.getValue().equals(request.query().get(entry.getKey())))
                && requiredHeaders.entrySet().stream().allMatch(entry ->
                request.headers().entrySet().stream().anyMatch(actual ->
                        actual.getKey().equalsIgnoreCase(entry.getKey())
                                && actual.getValue().equals(entry.getValue())));
    }

    private boolean pathMatches(ApiRequest request) {
        return path.equals("/*") || path.equals(request.path());
    }

    private boolean authMatches(ApiRequest request) {
        return authProfile.equals("*") || authProfile.equals(request.authProfile());
    }

    public ApiResponse response(String url) {
        return new ApiResponse(status, body, headers, duration, url);
    }

    private static String normalizeMethod(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("mock method must not be blank");
        return value.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static String normalizePath(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("mock path must not be blank");
        String path = value.trim();
        return path.startsWith("/") ? path : "/" + path;
    }
}
