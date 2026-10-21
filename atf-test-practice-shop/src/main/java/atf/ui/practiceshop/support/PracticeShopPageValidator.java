package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Executes annotation-driven page contracts through the locator boundary. */
public final class PracticeShopPageValidator {
    private final PracticeShopLocators locators;

    public PracticeShopPageValidator(PracticeShopLocators locators) {
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    public PracticeShopPageValidationResult validate(Page page, Class<?> pageObjectType) {
        return validate(page, pageObjectType, null);
    }

    public PracticeShopPageValidationResult validate(Page page, Class<?> pageObjectType,
                                                      String expectedText) {
        PracticeShopPageContract contract = contract(pageObjectType);
        return validateControls(page, pageObjectType, expectedText, contract.requiredControls());
    }

    /** Validates one semantic slice of a page contract for a focused checkpoint. */
    public PracticeShopPageValidationResult validateControls(Page page, Class<?> pageObjectType,
                                                             PracticeShopControl... controls) {
        return validateControls(page, pageObjectType, null, controls);
    }

    private PracticeShopPageValidationResult validateControls(Page page, Class<?> pageObjectType,
                                                              String expectedText,
                                                              PracticeShopControl... controls) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(pageObjectType, "pageObjectType");
        PracticeShopPageContract contract = contract(pageObjectType);

        List<AtfAssertion> assertions = new ArrayList<>();
        String actualPath = pathOf(page.url());
        assertions.add(assertion("Page route", contract.route(), actualPath,
                routeMatches(contract.route(), actualPath)));
        PracticeShopControl[] selected = controls == null || controls.length == 0
                ? contract.requiredControls() : controls;
        for (PracticeShopControl control : selected) assertions.add(visible(control, page));
        if (!contract.bodyMarker().isBlank()) {
            assertions.add(text(PracticeShopControl.PAGE_BODY, page, contract.bodyMarker(), contract.bodyMarker()));
        }
        if (expectedText != null && !expectedText.isBlank()) {
            assertions.add(text(PracticeShopControl.PAGE_BODY, page, expectedText,
                    "page contains configured value"));
        }
        return new PracticeShopPageValidationResult(contract.screen(), assertions);
    }

    private static PracticeShopPageContract contract(Class<?> pageObjectType) {
        PracticeShopPageContract contract = pageObjectType.getAnnotation(PracticeShopPageContract.class);
        if (contract == null) throw new IllegalArgumentException(pageObjectType.getName()
                + " must declare @PracticeShopPageContract");
        return contract;
    }

    public PracticeShopPageValidationResult validateValues(Page page, Class<?> pageObjectType,
                                                           Map<PracticeShopControl, String> expectedValues) {
        PracticeShopPageValidationResult base = validate(page, pageObjectType);
        List<AtfAssertion> assertions = new ArrayList<>(base.assertions());
        if (expectedValues != null) {
            expectedValues.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> entry.getKey().ordinal()))
                    .forEach(entry -> assertions.add(value(entry.getKey(), page, entry.getValue())));
        }
        return new PracticeShopPageValidationResult(base.screen(), assertions);
    }

    /** Convenience entry point for a single form field in a component checkpoint. */
    public PracticeShopPageValidationResult validateValue(Page page, Class<?> pageObjectType,
                                                          PracticeShopControl control,
                                                          String expectedValue) {
        return validateValues(page, pageObjectType, Map.of(control, expectedValue));
    }

    /** Validates one semantic control without forcing an unrelated page contract. */
    public AtfAssertion validateVisible(Page page, PracticeShopControl control) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(control, "control");
        return visible(control, page);
    }

    private AtfAssertion visible(PracticeShopControl control, Page page) {
        try {
            var locator = locators.control(page, control).locator();
            int count = locator.count();
            boolean visible = count > 0 && locator.first().isVisible();
            String actual = count == 0 ? "not present" : (visible ? "visible" : "not visible");
            return assertion(control.label(), "visible", actual, visible);
        } catch (RuntimeException error) {
            return assertion(control.label(), "visible",
                    PracticeShopValidationDiagnostics.unavailable(error), false);
        }
    }

    private AtfAssertion text(PracticeShopControl control, Page page, String expected, String name) {
        try {
            String actual = locators.control(page, control).locator().innerText();
            return assertion(name, expected, compact(actual), actual.contains(expected));
        } catch (RuntimeException error) {
            return assertion(name, expected,
                    PracticeShopValidationDiagnostics.unavailable(error), false);
        }
    }

    private AtfAssertion value(PracticeShopControl control, Page page, String expected) {
        try {
            if (control == PracticeShopControl.COUNTRY) {
                var selected = locators.selectedCountry(page).locator();
                var option = new PracticeShopCountryOption(selected.getAttribute("value"),
                        selected.getAttribute("label"), selected.innerText());
                return assertion(control.label(), expected, option.displayValue(), option.matches(expected));
            }
            String actual = locators.control(page, control).locator().inputValue();
            return assertion(control.label(), expected, actual, expected.equals(actual));
        } catch (RuntimeException error) {
            return assertion(control.label(), expected,
                    PracticeShopValidationDiagnostics.unavailable(error), false);
        }
    }

    private static AtfAssertion assertion(String name, String expected, String actual, boolean passed) {
        return new AtfAssertion(name, expected, actual, passed);
    }

    private static String pathOf(String url) {
        try { return URI.create(url).getPath(); }
        catch (RuntimeException ignored) { return url == null ? "" : url; }
    }

    private static boolean routeMatches(String expected, String actual) {
        if (expected.endsWith("/*")) return actual.startsWith(expected.substring(0, expected.length() - 1));
        return expected.equals(actual);
    }

    private static String compact(String value) {
        if (value == null) return "";
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 180 ? normalized : normalized.substring(0, 177) + "...";
    }
}
