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
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private UiScreenValidation() { }

    public static Evidence capture(Page page, UiScreenValidationConfiguration configuration,
                                   String title) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(configuration, "configuration");
        String safeTitle = title == null || title.isBlank() ? "Screen validation" : title.trim();
        if (!configuration.enabled()) return new Evidence(safeTitle, "", false);
        byte[] png = page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(configuration.fullPage())
                .setTimeout(configuration.screenshotTimeout().toMillis()));
        String base64 = Base64.getEncoder().encodeToString(png);
        return new Evidence(safeTitle, base64, true);
    }

    public record Evidence(String title, String base64Png, boolean captured) {
        public Evidence {
            title = title == null || title.isBlank() ? "Screen validation" : title.trim();
            base64Png = base64Png == null ? "" : base64Png;
        }

        /** Returns true when the evidence contains a captured Base64 screenshot payload. */
        public boolean hasScreenshotPayload() {
            return captured && isValidPngPayload(base64Png);
        }

        /** @deprecated use {@link #hasScreenshotPayload()} for renderer-neutral terminology. */
        @Deprecated
        public boolean hasInlineImage() {
            return hasScreenshotPayload();
        }
    }

    /**
     * Validates the renderer-neutral screenshot contract without decoding or
     * writing an image file. The report stores raw Base64 PNG bytes, not a
     * data-URI prefix.
     */
    public static boolean isValidPngPayload(String base64Png) {
        if (base64Png == null || base64Png.isBlank()) return false;
        final byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Png.trim());
        } catch (IllegalArgumentException invalidBase64) {
            return false;
        }
        if (decoded.length < PNG_SIGNATURE.length) return false;
        for (int index = 0; index < PNG_SIGNATURE.length; index++) {
            if (decoded[index] != PNG_SIGNATURE[index]) return false;
        }
        return true;
    }
}
