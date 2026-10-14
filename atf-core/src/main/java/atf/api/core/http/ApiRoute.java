package atf.api.core.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public record ApiRoute(
        String name,
        String method,
        String pathTemplate,
        String resource,
        String operation,
        String authProfile,
        AuthType authType,
        AuthRequirement authRequirement,
        String authPolicy,
        java.util.Set<String> tags) {

    public ApiRoute(String name, String method, String pathTemplate, String resource, String operation,
                    String authProfile) {
        this(name, method, pathTemplate, resource, operation, authProfile,
                AuthType.NONE, AuthRequirement.NONE, "anonymous", java.util.Set.of());
    }

    public ApiRoute {
        require(name, "name");
        require(method, "method");
        require(pathTemplate, "pathTemplate");
        require(resource, "resource");
        require(operation, "operation");
        authProfile = authProfile == null || authProfile.isBlank() ? "none" : authProfile;
        authType = authType == null ? AuthType.NONE : authType;
        authRequirement = authRequirement == null ? AuthRequirement.NONE : authRequirement;
        authPolicy = authPolicy == null || authPolicy.isBlank() ? authRequirement.name().toLowerCase() : authPolicy;
        tags = tags == null ? java.util.Set.of() : java.util.Set.copyOf(tags);
    }

    public String resolvePath(Map<String, ?> values) {
        String resolved = pathTemplate;
        for (var entry : values.entrySet()) {
            String encoded = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            resolved = resolved.replace("{" + entry.getKey() + "}", encoded);
        }
        if (resolved.contains("{") || resolved.contains("}")) {
            throw new IllegalArgumentException("Unresolved route token in " + resolved);
        }
        return resolved;
    }

    public ApiRequest request(Map<String, ?> values) {
        return new ApiRequest(java.net.http.HttpRequest.BodyPublishers.noBody(), method,
                resolvePath(values), Map.of(), Map.of(), authProfile, this);
    }

    public ApiRequest requestBody(String bodyJson, Map<String, ?> values) {
        if (bodyJson == null) throw new IllegalArgumentException("bodyJson must not be null");
        return new ApiRequest(java.net.http.HttpRequest.BodyPublishers.ofString(bodyJson), method,
                resolvePath(values), Map.of(), Map.of(), authProfile, this);
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    }
}
