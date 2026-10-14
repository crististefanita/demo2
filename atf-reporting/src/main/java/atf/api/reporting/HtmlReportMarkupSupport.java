package atf.api.reporting;

import java.util.Locale;

/** Small, renderer-neutral HTML helpers shared by fallback report views. */
final class HtmlReportMarkupSupport {
    private HtmlReportMarkupSupport() {
    }

    static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String phaseKey(String name) {
        return (name == null ? "" : name.trim()).toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}
