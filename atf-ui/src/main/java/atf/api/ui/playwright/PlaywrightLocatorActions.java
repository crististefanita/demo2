package atf.api.ui.playwright;

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.microsoft.playwright.options.WaitForSelectorState;

/** Executes and records actions whose target is a Playwright locator. */
final class PlaywrightLocatorActions {
    private final UiActionRecorder recorder;

    PlaywrightLocatorActions(UiActionRecorder recorder) {
        this.recorder = Objects.requireNonNull(recorder, "recorder");
    }

    void click(Locator locator, String locatorTarget, String expected) {
        click(new AtfLocator(locator, locatorTarget), expected);
    }

    void click(AtfLocator element, String expected) {
        String target = element.selector();
        recorder.run("Click", target, expected, () -> {
            requireExactlyOne(element, "Click");
            element.locator().click();
            return "click completed";
        });
    }

    String getText(Locator locator, String locatorTarget, String expected) {
        return getText(new AtfLocator(locator, locatorTarget), expected);
    }

    String getText(AtfLocator element, String expected) {
        String target = element.selector();
        return recorder.run("Get text", target, expected,
                () -> readableText(element.locator()));
    }

    Locator findElement(Locator locator, String locatorTarget) {
        return findElement(new AtfLocator(locator, locatorTarget));
    }

    Locator findElement(AtfLocator element) {
        String target = element.selector();
        recorder.run("Find element", target, "element exists", () -> {
            if (element.locator().count() == 0) {
                throw new AssertionError("Element was not found: " + target);
            }
            return "element found";
        });
        return element.locator();
    }

    String expectText(Locator locator, String locatorTarget, String expected) {
        return expectText(new AtfLocator(locator, locatorTarget), expected);
    }

    String expectText(AtfLocator element, String expected) {
        String target = element.selector();
        String expectedText = expected == null ? "" : expected.trim();
        return recorder.run("Expect text", target, "text contains: " + expectedText, () -> {
            String observed = readableText(element.locator());
            if (!observed.contains(expectedText)) {
                throw new AssertionError("Expected text '" + expectedText + "' but observed '"
                        + observed + "'");
            }
            return observed;
        });
    }

    void focus(Locator locator, String locatorTarget, String expected) {
        focus(new AtfLocator(locator, locatorTarget), expected);
    }

    void focus(AtfLocator element, String expected) {
        String target = element.selector();
        recorder.run("Focus", target, expected, () -> {
            element.locator().focus();
            return "focus completed";
        });
    }

    void waitForVisible(Locator locator, String locatorTarget) {
        waitForVisible(new AtfLocator(locator, locatorTarget));
    }

    void waitForVisible(AtfLocator element) {
        String target = element.selector();
        recorder.run("Wait for visible", target, "state=Visible", () -> {
            element.locator().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            return "state reached";
        });
    }

    void assertVisible(Locator locator, String locatorTarget) {
        assertVisible(new AtfLocator(locator, locatorTarget));
    }

    void assertVisible(AtfLocator element) {
        String target = element.selector();
        recorder.run("Expect visible", target, "locator is visible", () -> {
            if (!element.locator().isVisible()) {
                throw new AssertionError("Locator is not visible: " + target);
            }
            return "operation completed";
        });
    }

    private static void requireExactlyOne(AtfLocator element, String operation) {
        int count = element.locator().count();
        if (count != 1) {
            throw new AssertionError(operation + " locator must resolve to exactly one element: "
                    + element.selector() + " (matched " + count + ")");
        }
    }

    private static String readableText(Locator locator) {
        List<Locator> rows = locator.locator("tr").all();
        if (!rows.isEmpty()) {
            String tableText = rows.stream()
                    .map(row -> String.join(" | ", row.locator("th,td").allInnerTexts()))
                    .collect(Collectors.joining("\n"))
                    .trim();
            if (!tableText.isBlank()) return tableText;
        }
        return locator.innerText().trim();
    }
}
