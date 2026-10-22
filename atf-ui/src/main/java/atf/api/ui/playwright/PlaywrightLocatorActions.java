package atf.api.ui.playwright;

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.SelectOption;

/** Executes and records actions whose target is a Playwright locator. */
final class PlaywrightLocatorActions {
    private static final Pattern SENSITIVE_TARGET = Pattern.compile(
            "password|token|secret|credential|authorization|cookie|api[-_]?key",
            Pattern.CASE_INSENSITIVE);
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

    void fill(AtfLocator element, String value, String expected) {
        String target = element.selector();
        String displayValue = displayValue(target, value);
        recorder.run("Fill", target, withValue(expected, displayValue), () -> {
            requireExactlyOne(element, "Fill");
            element.locator().fill(value == null ? "" : value);
            return "value entered: " + displayValue;
        });
    }

    void selectOption(AtfLocator element, String label, String expected) {
        String target = element.selector();
        String displayLabel = displayValue(target, label);
        recorder.run("Select option", target, withValue(expected, displayLabel), () -> {
            requireExactlyOne(element, "Select option");
            element.locator().selectOption(new SelectOption().setLabel(label));
            return "option selected: " + displayLabel;
        });
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

    String expectValue(AtfLocator element, String expectedValue) {
        String target = element.selector();
        String expected = expectedValue == null ? "" : expectedValue;
        return recorder.run("Expect value", target, "value equals: " + expected, () -> {
            String observed = element.locator().inputValue();
            if (!observed.equals(expected)) {
                throw new AssertionError("Expected value '" + expected + "' but observed '" + observed + "'");
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

    private static String withValue(String expected, String value) {
        String context = expected == null ? "" : expected.trim();
        return (context.isBlank() ? "value" : context) + " · value=" + value;
    }

    private static String displayValue(String target, String value) {
        if (isSensitiveTarget(target)) return "[REDACTED]";
        return value == null || value.isBlank() ? "<empty>" : value;
    }

    private static boolean isSensitiveTarget(String target) {
        return target != null && SENSITIVE_TARGET.matcher(target).find();
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
