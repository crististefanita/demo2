package atf.api.core.config;

public record ReportingSettings(int assertionMaxDisplayCharacters) {
    public static final int DEFAULT_ASSERTION_MAX_DISPLAY_CHARACTERS = 240;

    public ReportingSettings {
        if (assertionMaxDisplayCharacters <= 0) {
            throw new IllegalArgumentException("assertionMaxDisplayCharacters must be positive");
        }
    }

    public static ReportingSettings defaults() {
        return new ReportingSettings(DEFAULT_ASSERTION_MAX_DISPLAY_CHARACTERS);
    }
}
