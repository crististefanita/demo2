package atf.api.ui.config;

/**
 * Policy for visual evidence produced by a UI validation.
 *
 * <p>The policy is deliberately independent of the report renderer. The
 * Playwright adapter captures the page, while {@code atf-reporting} embeds
 * the resulting Base64 image in the audit artifact.</p>
 */
public record UiScreenValidationConfiguration(boolean enabled, boolean fullPage) {
    public static UiScreenValidationConfiguration defaults() {
        return new UiScreenValidationConfiguration(true, false);
    }
}
