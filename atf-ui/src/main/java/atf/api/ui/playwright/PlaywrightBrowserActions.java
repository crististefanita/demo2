package atf.api.ui.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.Objects;

import java.net.URI;

/** Executes and records page navigation, keyboard and stabilization actions. */
final class PlaywrightBrowserActions {
    private static final int STATE_POLL_INTERVAL_MS = 25;
    private static final int STATE_TIMEOUT_MS = 10_000;
    private final UiActionRecorder recorder;

    PlaywrightBrowserActions(UiActionRecorder recorder) {
        this.recorder = Objects.requireNonNull(recorder, "recorder");
    }

    void navigate(Page page, URI uri, String expected) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(uri, "uri");
        recorder.run("Navigate", uri.toString(), expected, () -> {
            page.navigate(uri.toString());
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            return "document loaded";
        });
    }

    Page clickAndWaitForPopup(Page page, Locator locator, String locatorTarget, String expected) {
        Objects.requireNonNull(page, "page");
        AtfLocator element = new AtfLocator(locator, locatorTarget);
        return recorder.run("Click", element.selector(), expected, () -> {
            requireExactlyOne(element, "Click");
            return page.waitForPopup(() -> element.locator().click());
        });
    }

    void pressKey(Page page, String key, String locatorTarget, String expected) {
        Objects.requireNonNull(page, "page");
        String target = PlaywrightActionSequence.requireLocatorTarget(locatorTarget);
        String pressedKey = Objects.requireNonNull(key, "key").trim();
        if (pressedKey.isEmpty()) throw new IllegalArgumentException("key must not be blank");
        recorder.run("Press key", target, expected, () -> {
            page.keyboard().press(pressedKey);
            return pressedKey + " pressed";
        });
    }

    void waitForAttribute(Page page, AtfLocator element, String attribute,
                          String expectedValue, String expected) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(element, "element");
        String name = Objects.requireNonNull(attribute, "attribute").trim();
        String value = Objects.requireNonNull(expectedValue, "expectedValue");
        if (name.isEmpty()) throw new IllegalArgumentException("attribute must not be blank");
        recorder.run("Wait for state", element.selector(), expected, () -> {
            Locator target = element.locator();
            target.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            waitUntil(page, () -> value.equals(target.getAttribute(name)),
                    name + "=" + value + " on " + element.selector());
            return name + "=" + value;
        });
    }

    void waitForText(Page page, AtfLocator element, String expectedText, String expected) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(element, "element");
        String value = Objects.requireNonNull(expectedText, "expectedText");
        if (value.isBlank()) throw new IllegalArgumentException("expectedText must not be blank");
        recorder.run("Wait for text", element.selector(), expected, () -> {
            Locator target = element.locator();
            target.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            waitUntil(page, () -> target.isVisible() && target.innerText().contains(value),
                    "text contains '" + value + "' on " + element.selector());
            return value;
        });
    }

    void waitForDisclosureOpen(Page page, AtfLocator summary, String expected) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(summary, "summary");
        recorder.run("Wait for disclosure", summary.selector(), expected, () -> {
            Locator summaryTarget = summary.locator();
            summaryTarget.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            Locator details = summaryTarget.locator("xpath=..");
            waitUntil(page, () -> details.getAttribute("open") != null,
                    "details.open=true on " + summary.selector());
            return "details.open=true";
        });
    }

    private static void waitUntil(Page page, java.util.function.BooleanSupplier condition,
                                  String description) {
        long deadline = System.nanoTime()
                + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(STATE_TIMEOUT_MS);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) return;
            page.waitForTimeout(STATE_POLL_INTERVAL_MS);
        }
        throw new IllegalStateException("Timed out waiting for " + description);
    }

    private static void requireExactlyOne(AtfLocator element, String operation) {
        int count = element.locator().count();
        if (count != 1) {
            throw new AssertionError(operation + " locator must resolve to exactly one element: "
                    + element.selector() + " (matched " + count + ")");
        }
    }
}
