package atf.api.execution;

import java.util.Locale;

/** Explicit transport intent captured at the API composition boundary. */
public enum ApiTransportMode {
    ACCEPTANCE,
    LIVE;

    public static ApiTransportMode parse(String value) {
        if (value == null || value.isBlank()) {
            return ACCEPTANCE;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException(
                    "Unknown API transport mode: " + value + " (expected acceptance or live)", error);
        }
    }
}
