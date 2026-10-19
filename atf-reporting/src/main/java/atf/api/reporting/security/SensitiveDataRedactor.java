package atf.api.reporting.security;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Conservative renderer-boundary redactor; deliberately independent of ATF core. */
public final class SensitiveDataRedactor {
    private static final Pattern AUTH = Pattern.compile("(?i)(authorization\\s*[:=]\\s*)(bearer\\s+)?([^,;\\s]+)");
    private static final Pattern TOKEN = Pattern.compile("(?i)(bearer\\s+|token[=:]\\s*)([^\\s,;&]+)");
    private static final Pattern JSON_SECRET = Pattern.compile(
            "(?i)(\\\"(?:authorization|token|api[_-]?key|password|secret)\\\"\\s*:\\s*\\\")([^\\\"]*)(\\\")");
    private static final Set<String> SENSITIVE_FIELDS = Set.of("authorization", "cookie", "set-cookie", "token", "password", "secret", "api-key", "apikey");
    private final String replacement;
    public SensitiveDataRedactor() { this("***REDACTED***"); }
    public SensitiveDataRedactor(String replacement) { this.replacement = replacement == null || replacement.isBlank() ? "***REDACTED***" : replacement; }
    public String redact(String value) { return redactText(value); }
    public String redactText(String value) {
        if (value == null) return null;
        String safe = AUTH.matcher(value).replaceAll("$1$2" + replacement);
        safe = TOKEN.matcher(safe).replaceAll("$1" + replacement);
        return JSON_SECRET.matcher(safe).replaceAll("$1" + replacement + "$3");
    }
    public String redactField(String field, String value) {
        if (field != null && SENSITIVE_FIELDS.contains(field.trim().toLowerCase(Locale.ROOT))) return replacement;
        return redactText(value);
    }
    public String redactJson(String json) { return redactText(json); }
}
