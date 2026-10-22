package atf.ui.tests.practiceshop.component.evidence;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.locators.ShopCssEscaper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class ShopCssEscaperTest {
    @Test
    void preservesUnicodeAndEscapesSelectorDelimitersAndControls() {
        String input = "Pliers 🔧 " + "'" + "\\" + "\n" + "\0";
        String expected = "Pliers 🔧 " + "\\'" + "\\\\" + "\\a " + "\\fffd ";

        assertEquals(expected, ShopCssEscaper.quoted(input));
    }

    @Test
    void rejectsNullDynamicValuesBeforeBuildingASelector() {
        assertThrows(NullPointerException.class, () -> ShopCssEscaper.quoted(null));
    }
}
