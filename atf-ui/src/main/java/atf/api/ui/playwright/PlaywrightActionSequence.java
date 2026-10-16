package atf.api.ui.playwright;

import atf.api.core.ui.AtfUiActionRecord;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Records the ordered Playwright operations performed for one visual state.
 * The class owns execution timing and evidence only; it does not render HTML.
 */
public final class PlaywrightActionSequence {
    private final UiActionRecorder recorder = new UiActionRecorder();

    public void navigate(Page page, URI uri) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(uri, "uri");
        recorder.run("Navigate", uri.toString(), "document is loaded", () -> {
            page.navigate(uri.toString());
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            return "document loaded";
        });
    }

    public void click(Locator locator, String locatorTarget, String expected) {
        String target = requireElementTarget(locator, locatorTarget);
        recorder.run("Click", target, expected, () -> {
            locator.click();
            return "click completed";
        });
    }

    public String getText(Locator locator, String locatorTarget, String expected) {
        String target = requireElementTarget(locator, locatorTarget);
        return recorder.run("Get text", target, expected,
                () -> locator.innerText().trim());
    }

    /** Records the locator lookup itself as auditable Playwright evidence. */
    public Locator findElement(Locator locator, String locatorTarget) {
        String target = requireElementTarget(locator, locatorTarget);
        recorder.run("Find element", target, "element exists", () -> {
            if (locator.count() == 0) {
                throw new AssertionError("Element was not found: " + target);
            }
            return "element found";
        });
        return locator;
    }

    /** Reads and validates text through Playwright as one auditable operation. */
    public String expectText(Locator locator, String locatorTarget, String expected) {
        String target = requireElementTarget(locator, locatorTarget);
        String expectedText = expected == null ? "" : expected.trim();
        return recorder.run("Expect text", target, "text contains: " + expectedText, () -> {
            String observed = locator.innerText().trim();
            if (!observed.contains(expectedText)) {
                throw new AssertionError("Expected text '" + expectedText + "' but observed '"
                        + observed + "'");
            }
            return observed;
        });
    }

    public void focus(Locator locator, String locatorTarget, String expected) {
        String target = requireElementTarget(locator, locatorTarget);
        recorder.run("Focus", target, expected, () -> {
            locator.focus();
            return "focus completed";
        });
    }

    public void waitForVisible(Locator locator, String locatorTarget) {
        String target = requireElementTarget(locator, locatorTarget);
        recorder.run("Wait for visible", target, "state=Visible", () -> {
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            return "state reached";
        });
    }

    public void assertVisible(Locator locator, String locatorTarget) {
        String target = requireElementTarget(locator, locatorTarget);
        recorder.run("Expect visible", target, "locator is visible", () -> {
            if (!locator.isVisible()) throw new AssertionError("Locator is not visible: " + target);
            return "operation completed";
        });
    }

    /**
     * Records a short stabilization wait anchored to the UI locator whose state
     * is being observed. Prefer a semantic state wait when the page exposes one.
     */
    public void waitForTimeout(Page page, double seconds, Locator anchor,
                               String locatorTarget, String expected) {
        Objects.requireNonNull(page, "page");
        String target = requireElementTarget(anchor, locatorTarget);
        recorder.run("Wait", target, expected, () -> {
            anchor.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.waitForTimeout((float) Math.max(0, seconds * 1000));
            return "wait completed";
        });
    }

    /** Records a non-Playwright observation while keeping it in the same sequence. */
    public void observation(String operation, String target, String expected, String observed) {
        recorder.observation(operation, target, expected, observed);
    }

    /** Returns a stable snapshot without changing the sequence. */
    public List<AtfUiActionRecord> snapshot() {
        return recorder.snapshot();
    }

    /** Returns the current consecutive sequence and starts a fresh one. */
    public List<AtfUiActionRecord> drain() {
        return recorder.drain();
    }

    static String requireLocatorTarget(String locatorTarget) {
        if (locatorTarget == null || locatorTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("A Playwright action requires a non-blank locator target");
        }
        String target = locatorTarget.trim();
        if (!looksLikeCssSelector(target)) {
            throw new IllegalArgumentException(
                    "A Playwright action requires the effective CSS locator, not a prose description: " + target);
        }
        return target;
    }

    private static boolean looksLikeCssSelector(String target) {
        return target.startsWith("#") || target.startsWith(".") || target.startsWith("[")
                || target.startsWith(":") || target.matches(".*[.#\\[\\]:>+~()].*");
    }

    private static String requireElementTarget(Locator locator, String locatorTarget) {
        Objects.requireNonNull(locator, "locator");
        return requireLocatorTarget(locatorTarget);
    }
}
