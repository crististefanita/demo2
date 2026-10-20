package atf.ui.tests.extent.support.uireport.browser;

import com.microsoft.playwright.Locator;

import java.util.Objects;

/** Normalizes browser-computed colors for renderer-neutral UI evidence. */
public final class UiComputedStyleColorEvidence {
    private static final String SURFACE_SCRIPT = """
            element => {
              const toHex = value => {
                const parts = String(value).match(/\\d+(?:\\.\\d+)?/g);
                if (!parts || parts.length < 3) return String(value).trim().toLowerCase();
                const channel = part => Math.max(0, Math.min(255, Math.round(Number(part))));
                const hex = number => number.toString(16).padStart(2, '0');
                const channels = parts.slice(0, 3).map(part => hex(channel(part)));
                if (parts.length > 3) {
                  channels.push(hex(Math.max(0, Math.min(255,
                          Math.round(Number(parts[3]) * 255)))));
                }
                return '#' + channels.join('');
              };
              const style = getComputedStyle(element);
              return toHex(style.backgroundColor) + ' / ' + toHex(style.color);
            }
            """;

    private UiComputedStyleColorEvidence() {
    }

    public static String surface(Locator locator) {
        Objects.requireNonNull(locator, "locator");
        return String.valueOf(locator.evaluate(SURFACE_SCRIPT));
    }
}
