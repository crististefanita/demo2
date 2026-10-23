package atf.api.ui.dom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CssStringEscaperTest {
    @Test
    void preservesUnicodeAndEscapesQuotesBackslashesAndControls() {
        String input = "Pliers 🔧 " + "'" + "\\" + "\n" + "\0";
        String expected = "Pliers 🔧 " + "\\'" + "\\\\" + "\\a " + "\\fffd ";

        assertEquals(expected, CssStringEscaper.escapeQuotedValue(input));
    }

    @Test
    void rejectsNullValuesBeforeSelectorConstruction() {
        assertThrows(NullPointerException.class, () -> CssStringEscaper.escapeQuotedValue(null));
    }
}
