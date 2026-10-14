package atf.api.core.http;

import java.net.http.HttpRequest;
import java.util.LinkedHashMap;
import java.util.Map;

public record ApiRequest(
        HttpRequest.BodyPublisher body,
        String method,
        String path,
        Map<String, String> query,
        Map<String, String> headers,
        String authProfile,
        ApiRoute route) {

    public ApiRequest {
        if (method == null || method.isBlank()) throw new IllegalArgumentException("method must not be blank");
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        query = query == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(query));
        headers = headers == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(headers));
        authProfile = authProfile == null || authProfile.isBlank() ? "none" : authProfile;
    }

    public static ApiRequest of(String method, String path) {
        return new ApiRequest(HttpRequest.BodyPublishers.noBody(), method, path, Map.of(), Map.of(), "none", null);
    }
}
