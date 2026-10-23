package atf.ui.tests.autoex.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.autoex.support.catalog.AutoExCatalogAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class AutoExCatalogAssertionsComponentTest {
    @Test
    void configuredPriceMustMatchACompleteRenderedLine() {
        String details = "Blue Top\nCategory: Women > Tops\nRs. 500\nAvailability: In Stock";

        assertTrue(AutoExCatalogAssertions.containsExactLine(details, "Rs. 500"));
        assertFalse(AutoExCatalogAssertions.containsExactLine(details, "Rs. 50"));
        assertFalse(AutoExCatalogAssertions.containsExactLine(details, "Rs. 5000"));
        assertFalse(AutoExCatalogAssertions.containsExactLine(details, null));
    }
}
