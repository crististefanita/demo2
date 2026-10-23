package atf.ui.autoex.support.catalog;

/** Exact text matching for configured catalog values rendered in product details. */
public final class AutoExCatalogAssertions {
    private AutoExCatalogAssertions() { }

    public static boolean containsExactLine(String renderedDetails, String expectedValue) {
        if (renderedDetails == null || expectedValue == null) return false;
        return renderedDetails.lines().map(String::trim).anyMatch(expectedValue::equals);
    }
}
