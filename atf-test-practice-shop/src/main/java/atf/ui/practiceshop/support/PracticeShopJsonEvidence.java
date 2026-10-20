package atf.ui.practiceshop.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** One parse-aware JSON/redaction boundary for all Practice Shop evidence. */
public final class PracticeShopJsonEvidence {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String REDACTED = "<redacted>";

    private PracticeShopJsonEvidence() { }

    public static JsonNode parse(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return JSON.readTree(value);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    public static boolean isJson(String value) {
        JsonNode node = parse(value);
        return node != null && !node.isMissingNode();
    }

    public static String redact(String value) {
        if (value == null || value.isBlank()) return value == null ? "" : value;
        JsonNode root = parse(value);
        if (root != null) {
            redactNode(root);
            try {
                return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            } catch (JsonProcessingException error) {
                throw new IllegalStateException("Cannot serialize redacted JSON evidence", error);
            }
        }
        // Malformed payloads still must not leak a simple secret before the
        // contract validator reports the malformed-body failure.
        return value.replaceAll(
                "(?i)(\\\"(?:email|password|token|access_token|accessToken|refresh_token|authorization|cookie|api[_-]?key)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")",
                "$1" + REDACTED + "$2");
    }

    public static Map<String, String> redactHeaders(Map<String, String> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        (headers == null ? Map.<String, String>of() : headers).forEach((name, value) -> {
            String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
            boolean sensitive = lower.contains("authorization") || lower.equals("cookie")
                    || lower.contains("token") || lower.contains("api-key")
                    || lower.contains("api_key") || lower.equals("apikey") || lower.contains("password");
            result.put(name == null ? "" : name, sensitive ? REDACTED : value);
        });
        return Map.copyOf(result);
    }

    private static void redactNode(JsonNode node) {
        if (node == null) return;
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitive(field.getKey())) {
                    field.setValue(JSON.getNodeFactory().textNode(REDACTED));
                } else {
                    redactNode(field.getValue());
                }
            }
        } else if (node.isArray()) {
            node.forEach(PracticeShopJsonEvidence::redactNode);
        }
    }

    private static boolean isSensitive(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        return lower.equals("email") || lower.equals("password") || lower.equals("token")
                || lower.equals("access_token") || lower.equals("accesstoken")
                || lower.equals("refresh_token") || lower.equals("authorization")
                || lower.equals("cookie") || lower.equals("api_key") || lower.equals("api-key")
                || lower.equals("apikey")
                || lower.contains("secret");
    }
}
