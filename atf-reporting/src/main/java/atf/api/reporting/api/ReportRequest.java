package atf.api.reporting.api;

import java.util.LinkedHashMap;
import java.util.Map;

/** Minimal request snapshot required for safe evidence and manual replay. */
public record ReportRequest(String method, String path, Map<String,String> query,
                            Map<String,String> headers, String authProfile, String bodyText,
                            ReportAuthObservation authObservation) {
    /** Source/binary-compatible constructor for snapshots without explicit auth evidence. */
    public ReportRequest(String method, String path, Map<String, String> query,
                         Map<String, String> headers, String authProfile, String bodyText) {
        this(method, path, query, headers, authProfile, bodyText, null);
    }

    public ReportRequest {
        if (method == null || method.isBlank() || path == null || path.isBlank()) throw new IllegalArgumentException("method and path must not be blank");
        query = query == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(query));
        headers = headers == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(headers));
        authProfile = authProfile == null || authProfile.isBlank() ? "none" : authProfile;
        bodyText = bodyText == null ? "" : bodyText;
        authObservation = authObservation == null
                ? ReportAuthObservation.from(authProfile, headers, query) : authObservation;
    }

    public ReportAuthMethod authMethod() {
        return authObservation().method();
    }
}
