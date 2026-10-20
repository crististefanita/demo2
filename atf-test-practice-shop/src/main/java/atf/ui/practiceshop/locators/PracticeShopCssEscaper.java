package atf.ui.practiceshop.locators;

/** Escapes dynamic values embedded in a single-quoted Playwright CSS string. */
public final class PracticeShopCssEscaper {
    private PracticeShopCssEscaper() { }

    public static String quoted(String value) {
        if (value == null) throw new NullPointerException("value");
        StringBuilder escaped = new StringBuilder(value.length());
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (codePoint == '\\' || codePoint == '\'') {
                escaped.append('\\').appendCodePoint(codePoint);
            } else if (codePoint == 0) {
                escaped.append("\\fffd ");
            } else if (codePoint < 0x20 || codePoint == 0x7f) {
                escaped.append('\\').append(Integer.toHexString(codePoint)).append(' ');
            } else {
                escaped.appendCodePoint(codePoint);
            }
        }
        return escaped.toString();
    }
}
