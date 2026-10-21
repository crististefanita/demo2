package atf.api.ui.network;

import atf.api.ui.config.UiNetworkInspectionConfiguration;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** One redacted, bounded browser exchange; safe for a renderer-neutral adapter. */
public record UiNetworkObservation(
        long sequence,
        Instant capturedAt,
        String method,
        String url,
        String path,
        int status,
        Map<String, String> requestHeaders,
        Map<String, String> responseHeaders,
        String requestBody,
        String responseBody,
        String authMethod,
        String authSource,
        String service,
        String evidenceSource,
        String requestSignature,
        boolean responseBodyAvailable) {

    public UiNetworkObservation {
        capturedAt = capturedAt == null ? Instant.EPOCH : capturedAt;
        method = normalize(method).toUpperCase(Locale.ROOT);
        url = normalize(url);
        path = normalize(path);
        requestHeaders = Map.copyOf(requestHeaders == null ? Map.of() : requestHeaders);
        responseHeaders = Map.copyOf(responseHeaders == null ? Map.of() : responseHeaders);
        requestBody = normalize(requestBody);
        responseBody = normalize(responseBody);
        authMethod = normalize(authMethod);
        authSource = normalize(authSource);
        service = normalize(service);
        evidenceSource = normalize(evidenceSource);
        requestSignature = normalize(requestSignature);
    }

    static UiNetworkObservation from(Response response, long sequence,
                                     UiNetworkInspectionConfiguration configuration) {
        Request request = response.request();
        String url = request.url();
        String requestBody = bounded(request.postData(), configuration.maxBodyPreviewCharacters());
        String responseBody;
        boolean bodyAvailable = true;
        try {
            response.finished();
            responseBody = bounded(new String(response.body(), StandardCharsets.UTF_8),
                    configuration.maxBodyPreviewCharacters());
        } catch (RuntimeException unavailable) {
            bodyAvailable = false;
            responseBody = "<unavailable: " + unavailable.getClass().getSimpleName() + ">";
        }
        Map<String, String> requestHeaders = redact(request.headers());
        Map<String, String> responseHeaders = redact(response.headers());
        String[] auth = authentication(request.headers(), url);
        return new UiNetworkObservation(sequence, Instant.now(), request.method(), url,
                path(url), response.status(), requestHeaders, responseHeaders, requestBody,
                responseBody, auth[0], auth[1], host(url), "UI_BROWSER",
                signature(request.method(), url, requestBody), bodyAvailable);
    }

    public String displayTarget() {
        return method + " " + path + " → " + status;
    }

    private static String[] authentication(Map<String, String> headers, String url) {
        if (contains(headers, "authorization")) {
            String value = value(headers, "authorization").toLowerCase(Locale.ROOT);
            int space = value.indexOf(' ');
            String scheme = space > 0 ? value.substring(0, space) : "authorization";
            return new String[]{scheme, "request-header"};
        }
        if (contains(headers, "x-api-key") || contains(headers, "api-key")) {
            return new String[]{"api-key", "request-header"};
        }
        if (contains(headers, "cookie")) return new String[]{"cookie", "request-header"};
        if (hasSensitiveQuery(url)) return new String[]{"signed-url", "query"};
        return new String[]{"none", "none"};
    }

    private static boolean hasSensitiveQuery(String url) {
        try {
            String query = URI.create(url).getRawQuery();
            if (query == null) return false;
            return java.util.Arrays.stream(query.split("&"))
                    .map(part -> part.split("=", 2)[0].toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.contains("token") || name.contains("secret")
                            || name.equals("sig") || name.equals("code") || name.equals("key"));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static Map<String, String> redact(Map<String, String> headers) {
        Map<String, String> safe = new LinkedHashMap<>();
        if (headers == null) return safe;
        headers.forEach((key, value) -> safe.put(key,
                sensitive(key) ? "REDACTED" : normalize(value)));
        return safe;
    }

    private static boolean sensitive(String key) {
        String name = normalize(key).toLowerCase(Locale.ROOT);
        return name.equals("authorization") || name.equals("cookie")
                || name.contains("token") || name.contains("secret")
                || name.equals("x-api-key") || name.equals("api-key");
    }

    private static boolean contains(Map<String, String> headers, String name) {
        return headers != null && headers.keySet().stream()
                .anyMatch(key -> key.equalsIgnoreCase(name));
    }

    private static String value(Map<String, String> headers, String name) {
        if (headers == null) return "";
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue).findFirst().orElse("");
    }

    private static String bounded(String value, int limit) {
        if (value == null || value.isBlank()) return "";
        if (value.length() <= limit) return value;
        return value.substring(0, Math.max(0, limit - 14)) + "… [truncated]";
    }

    private static String signature(String method, String url, String body) {
        return normalize(method).toUpperCase(Locale.ROOT) + " " + safeTarget(url)
                + " #body=" + sha256(body == null ? "<absent>" : body.trim());
    }

    private static String safeTarget(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?<redacted-query>");
        } catch (RuntimeException ignored) {
            return "<unavailable-url>";
        }
    }

    private static String path(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (RuntimeException ignored) {
            return "<unavailable-path>";
        }
    }

    private static String host(String url) {
        try { return normalize(URI.create(url).getHost()); }
        catch (RuntimeException ignored) { return "unknown"; }
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte item : digest) result.append(String.format("%02x", item));
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }

    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
