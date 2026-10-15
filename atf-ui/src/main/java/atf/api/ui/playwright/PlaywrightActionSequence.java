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

    public void click(Locator locator, String locatorDescription, String expected) {
        recorder.run("Click", locatorDescription, expected, () -> {
            locator.click();
            return "click completed";
        });
    }

    public String getText(Locator locator, String locatorDescription, String expected) {
        return recorder.run("Get text", locatorDescription, expected,
                () -> locator.innerText().trim());
    }

    /** Records the locator lookup itself as auditable Playwright evidence. */
    public Locator findElement(Locator locator, String locatorDescription) {
        Objects.requireNonNull(locator, "locator");
        recorder.run("Find element", locatorDescription, "element exists", () -> {
            if (locator.count() == 0) {
                throw new AssertionError("Element was not found: " + locatorDescription);
            }
            return "element found";
        });
        return locator;
    }

    /** Reads and validates text through Playwright as one auditable operation. */
    public String expectText(Locator locator, String locatorDescription, String expected) {
        Objects.requireNonNull(locator, "locator");
        String expectedText = expected == null ? "" : expected.trim();
        return recorder.run("Expect text", locatorDescription, "text contains: " + expectedText, () -> {
            String observed = locator.innerText().trim();
            if (!observed.contains(expectedText)) {
                throw new AssertionError("Expected text '" + expectedText + "' but observed '"
                        + observed + "'");
            }
            return observed;
        });
    }

    public void focus(Locator locator, String locatorDescription, String expected) {
        recorder.run("Focus", locatorDescription, expected, () -> {
            locator.focus();
            return "focus completed";
        });
    }

    public void waitForVisible(Locator locator, String locatorDescription) {
        recorder.run("Wait for visible", locatorDescription, "state=Visible", () -> {
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            return "state reached";
        });
    }

    public void assertVisible(Locator locator, String locatorDescription) {
        recorder.run("Expect visible", locatorDescription, "locator is visible", () -> {
            if (!locator.isVisible()) throw new AssertionError("Locator is not visible: " + locatorDescription);
            return "operation completed";
        });
    }

    public void waitForTimeout(Page page, double seconds, String expected) {
        Objects.requireNonNull(page, "page");
        recorder.run("Wait", "page", expected, () -> {
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
}
