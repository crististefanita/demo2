package atf.api.ui.playwright;

import atf.api.core.ui.AtfUiActionRecord;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.util.List;

/**
 * Stable facade for ordered Playwright evidence.
 *
 * <p>Execution is delegated to browser and locator action owners; this class
 * keeps only the public sequence vocabulary and recorder lifecycle.</p>
 */
public final class PlaywrightActionSequence {
    private final UiActionRecorder recorder = new UiActionRecorder();
    private final PlaywrightLocatorActions locatorActions = new PlaywrightLocatorActions(recorder);
    private final PlaywrightBrowserActions browserActions = new PlaywrightBrowserActions(recorder);

    public void navigate(Page page, URI uri) {
        navigate(page, uri, "document is loaded");
    }

    public void navigate(Page page, URI uri, String expected) {
        browserActions.navigate(page, uri, expected);
    }

    public void click(Locator locator, String locatorTarget, String expected) {
        locatorActions.click(locator, locatorTarget, expected);
    }

    public void click(AtfLocator element, String expected) {
        locatorActions.click(element, expected);
    }

    public Page clickAndWaitForPopup(Page page, Locator locator, String locatorTarget,
                                     String expected) {
        return browserActions.clickAndWaitForPopup(page, locator, locatorTarget, expected);
    }

    public String getText(Locator locator, String locatorTarget, String expected) {
        return locatorActions.getText(locator, locatorTarget, expected);
    }

    public String getText(AtfLocator element, String expected) {
        return locatorActions.getText(element, expected);
    }

    public Locator findElement(Locator locator, String locatorTarget) {
        return locatorActions.findElement(locator, locatorTarget);
    }

    public Locator findElement(AtfLocator element) {
        return locatorActions.findElement(element);
    }

    public String expectText(Locator locator, String locatorTarget, String expected) {
        return locatorActions.expectText(locator, locatorTarget, expected);
    }

    public String expectText(AtfLocator element, String expected) {
        return locatorActions.expectText(element, expected);
    }

    public void focus(Locator locator, String locatorTarget, String expected) {
        locatorActions.focus(locator, locatorTarget, expected);
    }

    public void focus(AtfLocator element, String expected) {
        locatorActions.focus(element, expected);
    }

    public void pressKey(Page page, String key, String locatorTarget, String expected) {
        browserActions.pressKey(page, key, locatorTarget, expected);
    }

    public void waitForVisible(Locator locator, String locatorTarget) {
        locatorActions.waitForVisible(locator, locatorTarget);
    }

    public void waitForVisible(AtfLocator element) {
        locatorActions.waitForVisible(element);
    }

    public void assertVisible(Locator locator, String locatorTarget) {
        locatorActions.assertVisible(locator, locatorTarget);
    }

    public void assertVisible(AtfLocator element) {
        locatorActions.assertVisible(element);
    }

    /** Waits on a DOM state transition instead of sleeping for an arbitrary duration. */
    public void waitForAttribute(Page page, AtfLocator element, String attribute,
                                 String expectedValue, String expected) {
        browserActions.waitForAttribute(page, element, attribute, expectedValue, expected);
    }

    /** Waits for the native disclosure state; the DOM's {@code details.open} is authoritative. */
    public void waitForDisclosureOpen(Page page, AtfLocator summary, String expected) {
        browserActions.waitForDisclosureOpen(page, summary, expected);
    }

    public void observation(String operation, String target, String expected, String observed) {
        recorder.observation(operation, target, expected, observed);
    }

    public List<AtfUiActionRecord> snapshot() {
        return recorder.snapshot();
    }

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
                || target.startsWith(":") || target.matches(".*[.#\\[\\]:>+~()].*")
                || target.matches("[A-Za-z][A-Za-z0-9_-]*");
    }
}
