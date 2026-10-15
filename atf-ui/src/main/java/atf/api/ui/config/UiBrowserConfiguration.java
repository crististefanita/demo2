package atf.api.ui.config;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

/** Immutable, validated browser policy. It contains no Playwright types. */
public record UiBrowserConfiguration(
        URI baseUrl,
        String browser,
        boolean headless,
        Duration timeout,
        Duration navigationTimeout,
        int viewportWidth,
        int viewportHeight,
        boolean screenshotOnFailure) {

    public UiBrowserConfiguration {
        baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        browser = Objects.requireNonNull(browser, "browser").trim().toLowerCase(Locale.ROOT);
        if (!browser.equals("chromium") && !browser.equals("firefox") && !browser.equals("webkit")) {
            throw new IllegalArgumentException("ui.browser must be chromium, firefox or webkit: " + browser);
        }
        timeout = positive(timeout, "timeout");
        navigationTimeout = positive(navigationTimeout, "navigationTimeout");
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            throw new IllegalArgumentException("ui.viewport dimensions must be positive");
        }
    }

    public static UiBrowserConfiguration defaults() {
        return new UiBrowserConfiguration(URI.create("about:blank"), "chromium", true,
                Duration.ofSeconds(10), Duration.ofSeconds(15), 1440, 900, true);
    }

    private static Duration positive(Duration value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
