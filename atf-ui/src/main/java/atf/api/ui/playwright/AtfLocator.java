package atf.api.ui.playwright;

import com.microsoft.playwright.Locator;

import java.util.Objects;

/** Immutable pair carrying the Playwright locator and its effective CSS selector. */
public record AtfLocator(Locator locator, String selector) {
    public AtfLocator {
        locator = Objects.requireNonNull(locator, "locator");
        selector = PlaywrightActionSequence.requireLocatorTarget(selector);
    }
}
