package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.pages.PracticeShopHomePage;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.Objects;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** Focused, reusable checks for the catalog landing screen. */
public final class PracticeShopHomeScreenValidator {
    private final PracticeShopLocators locators;
    private final PracticeShopPageValidator pages;

    public PracticeShopHomeScreenValidator(PracticeShopLocators locators) {
        this.locators = Objects.requireNonNull(locators, "locators");
        this.pages = new PracticeShopPageValidator(locators);
    }

    public PracticeShopPageValidationResult structure(Page page) {
        return pages.validateControls(page, PracticeShopHomePage.class,
                PracticeShopControl.HOME_NAVIGATION,
                PracticeShopControl.HOME_BRAND,
                PracticeShopControl.HOME_BANNER);
    }

    public List<AtfAssertion> structureStyle(Page page) {
        return List.of(
                style(page, PracticeShopControl.HOME_NAVIGATION, "display", "flex"),
                style(page, PracticeShopControl.HOME_BANNER, "visibility", "visible"),
                hasArea(page, PracticeShopControl.HOME_BANNER));
    }

    public PracticeShopPageValidationResult filters(Page page) {
        return pages.validateControls(page, PracticeShopHomePage.class,
                PracticeShopControl.HOME_SORT,
                PracticeShopControl.HOME_PRICE_RANGE,
                PracticeShopControl.SEARCH_QUERY,
                PracticeShopControl.HOME_SEARCH_RESET,
                PracticeShopControl.HOME_SEARCH_SUBMIT,
                PracticeShopControl.HOME_CATEGORY_FILTERS,
                PracticeShopControl.HOME_BRAND_FILTERS,
                PracticeShopControl.HOME_ECO_FILTER);
    }

    public List<AtfAssertion> filterIntegrity(Page page) {
        return List.of(
                countAtLeast(page, PracticeShopControl.HOME_PRICE_RANGE, 2),
                countAtLeast(page, PracticeShopControl.HOME_CATEGORY_FILTERS, 1),
                countAtLeast(page, PracticeShopControl.HOME_BRAND_FILTERS, 1),
                style(page, PracticeShopControl.HOME_SORT, "display", "block"),
                style(page, PracticeShopControl.SEARCH_QUERY, "visibility", "visible"));
    }

    public PracticeShopPageValidationResult productGrid(Page page) {
        return pages.validateControls(page, PracticeShopHomePage.class,
                PracticeShopControl.HOME_PRODUCT_GRID,
                PracticeShopControl.HOME_PRODUCT_NAMES,
                PracticeShopControl.HOME_PRODUCT_IMAGES,
                PracticeShopControl.HOME_PAGINATION,
                PracticeShopControl.HOME_FOOTER);
    }

    public List<AtfAssertion> productGridIntegrity(Page page) {
        int cards = count(page, PracticeShopControl.HOME_PRODUCT_GRID);
        int names = count(page, PracticeShopControl.HOME_PRODUCT_NAMES);
        int images = count(page, PracticeShopControl.HOME_PRODUCT_IMAGES);
        return List.of(
                check("Product cards", "> 0", Integer.toString(cards), cards > 0),
                check("Product card names", Integer.toString(cards), Integer.toString(names), cards == names),
                check("Product card images", Integer.toString(cards), Integer.toString(images), cards == images),
                countAtLeast(page, PracticeShopControl.HOME_PAGINATION, 1),
                style(page, PracticeShopControl.HOME_PRODUCT_GRID, "display", "flex"),
                style(page, PracticeShopControl.HOME_FOOTER, "display", "block"));
    }

    private AtfAssertion countAtLeast(Page page, PracticeShopControl control, int minimum) {
        int actual = count(page, control);
        return check(control.label() + " count", ">= " + minimum,
                Integer.toString(actual), actual >= minimum);
    }

    private int count(Page page, PracticeShopControl control) {
        try { return locators.control(page, control).locator().count(); }
        catch (RuntimeException error) { return 0; }
    }

    private AtfAssertion style(Page page, PracticeShopControl control,
                               String property, String expected) {
        try {
            Object observed = locators.control(page, control).locator().first()
                    .evaluate("element => getComputedStyle(element).getPropertyValue('" + property + "')");
            String actual = observed == null ? "" : observed.toString().trim();
            return check("Style · " + control.label() + " · " + property,
                    expected, actual, expected.equals(actual));
        } catch (RuntimeException error) {
            return check("Style · " + control.label() + " · " + property,
                    expected, PracticeShopValidationDiagnostics.unavailable(error), false);
        }
    }

    private AtfAssertion hasArea(Page page, PracticeShopControl control) {
        try {
            var box = locators.control(page, control).locator().first().boundingBox();
            boolean valid = box != null && box.width > 0 && box.height > 0;
            String actual = box == null ? "none" : Math.round(box.width) + "x" + Math.round(box.height);
            return check(control.label() + " render area", "> 0 x > 0", actual, valid);
        } catch (RuntimeException error) {
            return check(control.label() + " render area", "> 0 x > 0",
                    PracticeShopValidationDiagnostics.unavailable(error), false);
        }
    }
}
