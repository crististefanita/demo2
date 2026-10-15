package atf.api.ui.playwright;

import atf.api.ui.config.UiScreenValidationConfiguration;
import com.microsoft.playwright.Page;

import java.util.Base64;
import java.util.Objects;

/**
 * Captures one visual state for a group of UI assertions.
 *
 * <p>The returned evidence contains only the inline Base64 payload and its
 * semantic title. It does not write image files and it does not know how the
 * report renderer stores the evidence. A caller can therefore collect many
 * assertions and publish them with one evidence object.</p>
 */
public final class UiScreenValidation {
    private UiScreenValidation() { }

    public static Evidence capture(Page page, UiScreenValidationConfiguration configuration,
                                   String title) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(configuration, "configuration");
        String safeTitle = title == null || title.isBlank() ? "Screen validation" : title.trim();
        if (!configuration.enabled()) return new Evidence(safeTitle, "", false);
        byte[] png = page.screenshot(new Page.ScreenshotOptions().setFullPage(configuration.fullPage()));
        String base64 = Base64.getEncoder().encodeToString(png);
        return new Evidence(safeTitle, base64, true);
    }

    public record Evidence(String title, String base64Png, boolean captured) {
        public Evidence {
            title = title == null || title.isBlank() ? "Screen validation" : title.trim();
            base64Png = base64Png == null ? "" : base64Png;
        }

        public boolean hasInlineImage() {
            return captured && !base64Png.isBlank();
        }
    }
}
