package atf.api.ui.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.net.URI;
import java.util.Objects;

/** Executes and records page navigation, keyboard and stabilization actions. */
final class PlaywrightBrowserActions {
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
            element.locator().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.waitForFunction("args => { const element = document.querySelector(args.selector); "
                    + "return !!element && element.getAttribute(args.attribute) === args.value; }",
                    java.util.Map.of("selector", element.selector(), "attribute", name, "value", value));
            return name + "=" + value;
        });
    }

    void waitForDisclosureOpen(Page page, AtfLocator summary, String expected) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(summary, "summary");
        recorder.run("Wait for disclosure", summary.selector(), expected, () -> {
            summary.locator().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.waitForFunction("selector => { const summary = document.querySelector(selector); "
                    + "const details = summary && summary.closest('details'); "
                    + "return !!details && details.open === true; }", summary.selector());
            return "details.open=true";
        });
    }

    private static void requireExactlyOne(AtfLocator element, String operation) {
        int count = element.locator().count();
        if (count != 1) {
            throw new AssertionError(operation + " locator must resolve to exactly one element: "
                    + element.selector() + " (matched " + count + ")");
        }
    }
}
