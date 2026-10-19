package atf.api.reporting.configuration;

/** Immutable settings owned by the standalone reporting module. */
public record ReportingSettings(int assertionMaxDisplayCharacters, int bodyMaxDisplayCharacters) {
    public static final int DEFAULT_ASSERTION_MAX_DISPLAY_CHARACTERS = 240;
    public static final int DEFAULT_BODY_MAX_DISPLAY_CHARACTERS = 12_000;
    public ReportingSettings(int assertionMaxDisplayCharacters) { this(assertionMaxDisplayCharacters, DEFAULT_BODY_MAX_DISPLAY_CHARACTERS); }
    public ReportingSettings {
        if (assertionMaxDisplayCharacters <= 0 || bodyMaxDisplayCharacters <= 0) throw new IllegalArgumentException("display limits must be positive");
    }
    public static ReportingSettings defaults() { return new ReportingSettings(DEFAULT_ASSERTION_MAX_DISPLAY_CHARACTERS, DEFAULT_BODY_MAX_DISPLAY_CHARACTERS); }
}
