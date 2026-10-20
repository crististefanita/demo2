package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.locators.PracticeShopCssEscaper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfComponentTest
class PracticeShopCssEscaperTest {
    @Test
    void preservesUnicodeAndEscapesSelectorDelimitersAndControls() {
        String input = "Pliers 🔧 " + "'" + "\\" + "\n" + "\0";
        String expected = "Pliers 🔧 " + "\\'" + "\\\\" + "\\a " + "\\fffd ";

        assertEquals(expected, PracticeShopCssEscaper.quoted(input));
    }

    @Test
    void rejectsNullDynamicValuesBeforeBuildingASelector() {
        assertThrows(NullPointerException.class, () -> PracticeShopCssEscaper.quoted(null));
    }
}
