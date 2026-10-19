package atf.api.reporting.html.script;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.theme.tokens.AtfReportCssSelectors;

/**
 * Navigation behaviour for the renderer-neutral fallback report.
 *
 * <p>This is deliberately a single semantic asset: tab activation and the
 * Fails-to-Tests deep link belong to navigation, not to report lifecycle or
 * semantic model population.</p>
 */
public final class HtmlReportNavigationScript implements HtmlReportScript {
    private static final HtmlReportNavigationScript STANDARD = new HtmlReportNavigationScript();
    private static final String TAB_SELECTOR = AtfReportCssSelectors.Fallback.TAB.selector();
    private static final String TAB_PANEL_SELECTOR = AtfReportCssSelectors.Fallback.TAB_PANEL.selector();
    private static final String ACTIVE_CLASS = AtfReportHtmlVocabulary.CssClass.ACTIVE.value();
    private static final String FAIL_ROW_SELECTOR = AtfReportCssSelectors.Fallback.FAIL_ROW.selector();
    private static final String TAB_DATASET_PROPERTY = "tab";

    private HtmlReportNavigationScript() {
    }

    public static HtmlReportScript standard() {
        return STANDARD;
    }

    @Override
    public String source() {
        var script = new HtmlReportScriptBuilder()
                .append(HtmlReportScriptModule.TAB_QUERY, "const tabs=[...document.querySelectorAll(" + js(TAB_SELECTOR) + ")];")
                .append(HtmlReportScriptModule.PANEL_QUERY, "const panels=[...document.querySelectorAll(" + js(TAB_PANEL_SELECTOR) + ")];")
                .append(HtmlReportScriptModule.ACTIVATION, "function activate(id){"
                        + "tabs.forEach(t=>t.classList.toggle(" + js(ACTIVE_CLASS)
                        + ",t.dataset." + TAB_DATASET_PROPERTY + "===id));"
                        + "panels.forEach(p=>p.classList.toggle(" + js(ACTIVE_CLASS) + ",p.id===id));"
                        + "history.replaceState(null,'','#'+id)}")
                .append(HtmlReportScriptModule.TAB_CLICK, "tabs.forEach(t=>t.addEventListener('click',()=>activate(t.dataset."
                        + TAB_DATASET_PROPERTY + ")));")
                .append(HtmlReportScriptModule.DEFAULT_TAB, "activate(" + js(AtfReportHtmlVocabulary.FallbackTab.OVERVIEW.value()) + ");")
                .append(HtmlReportScriptModule.FAILS_DEEP_LINK, "document.querySelectorAll(" + js(FAIL_ROW_SELECTOR) + ")"
                        + ".forEach(a=>a.addEventListener('click',()=>activate("
                        + js(AtfReportHtmlVocabulary.FallbackTab.TESTS.value()) + ")));" );
        return script.build();
    }

    private static String js(String value) {
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
