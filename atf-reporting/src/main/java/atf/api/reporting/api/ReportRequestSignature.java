package atf.api.reporting.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Canonical identity for one logical request. It is deliberately owned by
 * reporting so every consumer projects the same signature without duplicating
 * hashing, query redaction or JSON ordering rules.
 */
public final class ReportRequestSignature {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String ABSENT_BODY = "<absent>";

    private ReportRequestSignature() {
    }

    public static String forRequest(ReportRequest request) {
        if (request == null) return "";
        return request.method().trim().toUpperCase(Locale.ROOT) + " "
                + safeTarget(request.path(), request.query())
                + "#body=" + sha256(canonicalBody(request.bodyText()));
    }

    public static String bodyDigest(String body) {
        return sha256(canonicalBody(body));
    }

    public static String correlationKey(ReportRequest request) {
        if (request == null) return "";
        return request.method().trim().toUpperCase(Locale.ROOT) + " "
                + normalizePath(request.path());
    }

    public static String canonicalBody(String body) {
        if (body == null || body.isBlank()) return ABSENT_BODY;
        try {
            JsonNode node = JSON.readTree(body);
            return node == null ? ABSENT_BODY : canonicalJson(node);
        } catch (Exception ignored) {
            // Non-JSON bodies remain distinct and deterministic. JSON
            // contracts are validated separately by ReportJsonBodyPolicy.
            return body.trim();
        }
    }

    private static String canonicalJson(JsonNode node) {
        if (node.isObject()) {
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            names.sort(Comparator.naturalOrder());
            StringJoiner result = new StringJoiner(",", "{", "}");
            for (String name : names) {
                result.add(quote(name) + ":" + canonicalJson(node.get(name)));
            }
            return result.toString();
        }
        if (node.isArray()) {
            StringJoiner result = new StringJoiner(",", "[", "]");
            node.elements().forEachRemaining(value -> result.add(canonicalJson(value)));
            return result.toString();
        }
        return node.toString();
    }

    private static String safeTarget(String path, Map<String, String> query) {
        String base = normalizePath(path);
        if (query == null || query.isEmpty()) return base;
        StringJoiner values = new StringJoiner("&");
        query.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> values.add(encode(entry.getKey()) + "="
                        + encode(isSensitive(entry.getKey()) ? "***REDACTED***" : entry.getValue())));
        return base + "?" + values;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        int fragment = path.indexOf('#');
        int end = fragment < 0 ? path.length() : fragment;
        String value = path.substring(0, end);
        int query = value.indexOf('?');
        return query < 0 ? value : value.substring(0, query);
    }

    private static boolean isSensitive(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return normalized.contains("token") || normalized.contains("secret")
                || normalized.contains("password") || normalized.contains("credential")
                || normalized.equals("auth") || normalized.equals("authorization")
                || normalized.equals("cookie");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String quote(String value) {
        return JSON.valueToTree(value).toString();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte item : digest) result.append(String.format("%02x", item));
            return result.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
