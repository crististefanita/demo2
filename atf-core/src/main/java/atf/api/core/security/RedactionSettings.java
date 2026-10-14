package atf.api.core.security;

import java.util.Set;

/** Politici de redacție aplicate la limita către evidence/raport. */
public record RedactionSettings(boolean enabled, String replacement, boolean redactLocalPaths,
                                boolean piiEnabled, Set<String> sensitiveFields) {
    public RedactionSettings {
        replacement = replacement == null || replacement.isBlank() ? "***REDACTED***" : replacement;
        sensitiveFields = sensitiveFields == null ? Set.of() : sensitiveFields.stream()
                .map(value -> value.toLowerCase(java.util.Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public static RedactionSettings defaults() {
        return new RedactionSettings(true, "***REDACTED***", true, false,
                Set.of("password", "token", "access_token", "refresh_token", "client_secret", "api_key", "authorization"));
    }
}
