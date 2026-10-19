package atf.api.reporting.runtime.core;

import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.AUTHOR;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.CLASS_ROOT_ID;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.SCENARIO_ID;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.SCENARIO_INDEX;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.SCENARIO_TAGS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.STATUS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.TAG;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Attribute.TEST_ID;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.CLASS_ITEMS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.DIRECT_SCENARIO_CARDS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.ESCAPE;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.IS_CLASS_ROOT;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.NORMALIZE_TEST_PACKAGE_METADATA;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.QUERY_ALL;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.READ_TAG_VALUES;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.STATUS_OF;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.TEST_CLASS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.TEST_ITEMS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.TEST_NAME;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.TEST_PACKAGE;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.TEST_TAGS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Function.TEXT;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.CARD_TOOLBAR_BADGE;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.PACKAGE_FALLBACK_NODE;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.SCENARIO_TITLE;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.TAG_BADGES;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.TEST_DETAIL_NAME;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.TEST_INFO;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.TEST_ITEM_HOST;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.TEST_STATUS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Selector.TEST_STATUS_NODE;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Status.FAIL;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Status.PASS;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Status.SKIP;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.Status.WARNING;
import static atf.api.reporting.runtime.core.ExtentReportRuntimeVocabulary.StatusClass.DANGER;

/** Shared DOM primitives used by the semantic report-runtime controllers. */
public final class ExtentReportRuntimeCore {
    private ExtentReportRuntimeCore() { }

    /**
     * Emits the canonical runtime vocabulary in dependency order.
     *
     * <p>The Java enums own names, selectors and attributes; the private
     * emitters own only one semantic function body each. This makes the core
     * component testable without parsing one opaque concatenated script and
     * keeps changes local when a vendor selector changes.</p>
     */
    public static String script() {
        return String.join("",
                function(QUERY_ALL, "s,c", "return Array.prototype.slice.call((c||document).querySelectorAll(s));"),
                function(ESCAPE, "v", escapeBody()),
                function(TEXT, "e,s", textBody()),
                classItemsFunction(),
                directScenarioCardsFunction(),
                isClassRootFunction(),
                testItemsFunction(),
                statusFunction(),
                testClassFunction(),
                testPackageFunction(),
                testNameFunction(),
                readTagValuesFunction(),
                testTagsFunction(),
                normalizeTestPackageMetadataFunction());
    }

    private static String classItemsFunction() {
        return function(CLASS_ITEMS, "", "return " + fn(QUERY_ALL) + "("
                + js(ExtentReportRuntimeVocabulary.Selector.CLASS_ITEMS) + ");");
    }

    private static String directScenarioCardsFunction() {
        return function(DIRECT_SCENARIO_CARDS, "root",
                "return " + fn(QUERY_ALL) + "("
                        + js(ExtentReportRuntimeVocabulary.Selector.DIRECT_SCENARIO_CARDS) + ",root);");
    }

    private static String isClassRootFunction() {
        return function(IS_CLASS_ROOT, "item",
                "return " + fn(DIRECT_SCENARIO_CARDS) + "(item).length>0;");
    }

    private static String testItemsFunction() {
        String body = "var result=[];" + fn(CLASS_ITEMS)
                + "().forEach(function(root){var cards=" + fn(IS_CLASS_ROOT) + "(root)?"
                + fn(DIRECT_SCENARIO_CARDS) + "(root):[root];cards.forEach(function(card,index){"
                + "card.setAttribute(" + js(SCENARIO_ID) + ",root.getAttribute(" + js(TEST_ID)
                + ")+'-'+index);card.setAttribute(" + js(CLASS_ROOT_ID) + ",root.getAttribute("
                + js(TEST_ID) + "));card.setAttribute(" + js(SCENARIO_INDEX)
                + ",String(index));result.push(card);});});return result;";
        return function(TEST_ITEMS, "", body);
    }

    private static String statusFunction() {
        String body = "var attr=item.getAttribute(" + js(STATUS) + ");if(attr)return attr;"
                + "var badge=item.querySelector(" + js(CARD_TOOLBAR_BADGE) + ");"
                + "var cls=badge?String(badge.className||''):'';"
                + "if(cls.indexOf(" + js(DANGER.value()) + ")>=0)return " + js(FAIL.value()) + ";"
                + "if(cls.indexOf(" + js(WARNING.value()) + ")>=0)return " + js(WARNING.value()) + ";"
                + "if(cls.indexOf(" + js(PASS.value()) + ")>=0)return " + js(PASS.value()) + ";"
                + "var value=String(badge&&badge.textContent||'').toLowerCase();"
                + "return value.indexOf(" + js(FAIL.matchToken()) + ")>=0?" + js(FAIL.value()) + ":"
                + "value.indexOf(" + js(WARNING.matchToken()) + ")>=0?" + js(WARNING.value()) + ":"
                + "value.indexOf(" + js(SKIP.matchToken()) + ")>=0?" + js(SKIP.value()) + ":" + js(PASS.value()) + ";";
        return function(STATUS_OF, "item", body);
    }

    private static String testClassFunction() {
        String body = "var host=item.closest(" + js(TEST_ITEM_HOST) + ");return "
                + fn(TEXT) + "(host," + js(TEST_DETAIL_NAME) + ")||" + fn(TEXT) + "(host,"
                + js(TEST_STATUS) + ")||" + js(ExtentReportRuntimeVocabulary.DEFAULT_TEST_CLASS) + ";";
        return function(TEST_CLASS, "item", body);
    }

    private static String testPackageFunction() {
        String body = "var host=item.closest(" + js(TEST_ITEM_HOST) + "),value=host&&String(host.getAttribute("
                + js(ExtentReportRuntimeVocabulary.Attribute.TEST_PACKAGE) + ")||host.getAttribute("
                + js(AUTHOR) + ")||'').trim();"
                + "if(!value&&host)value=" + fn(TEXT) + "(host," + js(PACKAGE_FALLBACK_NODE) + ");"
                + "return value||" + js(ExtentReportRuntimeVocabulary.DEFAULT_PACKAGE) + ";";
        return function(TEST_PACKAGE, "item", body);
    }

    private static String testNameFunction() {
        String body = "return " + fn(TEXT) + "(item," + js(TEST_DETAIL_NAME) + ")||" + fn(TEXT)
                + "(item," + js(SCENARIO_TITLE) + ")||"
                + js(ExtentReportRuntimeVocabulary.DEFAULT_SCENARIO) + ";";
        return function(TEST_NAME, "item", body);
    }

    private static String readTagValuesFunction() {
        String body = "return " + fn(QUERY_ALL) + "(" + js(TAG_BADGES)
                + ",container).map(function(e){return String(e.textContent||'').replace(/^\\s*[^a-zA-Z0-9_-]\\s*/,'').trim().toLowerCase();})"
                + ".filter(Boolean).filter(function(v,i,a){return a.indexOf(v)===i;});";
        return function(READ_TAG_VALUES, "container", body);
    }

    private static String testTagsFunction() {
        String body = "var stored=item.getAttribute(" + js(SCENARIO_TAGS) + "),values=stored?stored.split(/\\s+/).map(function(v){return String(v).trim().toLowerCase();}):"
                + fn(READ_TAG_VALUES) + "(item);if(!values.length){var owner=item.closest(" + js(TEST_ITEM_HOST)
                + "),tag=owner&&owner.getAttribute(" + js(TAG) + ");if(tag)values=tag.split(/\\s+/).map(function(v){return String(v).trim().toLowerCase();});}"
                + "return values.filter(function(v,i,a){return a.indexOf(v)===i;});";
        return function(TEST_TAGS, "item", body);
    }

    private static String normalizeTestPackageMetadataFunction() {
        String body = fn(QUERY_ALL) + "("
                + js(ExtentReportRuntimeVocabulary.Selector.CLASS_ITEMS)
                + ").forEach(function(item){var packageName=String(item.getAttribute("
                + js(ExtentReportRuntimeVocabulary.Attribute.TEST_PACKAGE)
                + ")||item.getAttribute(" + js(AUTHOR) + ")||'').trim();var info=item.querySelector("
                + js(TEST_INFO) + ");if(!packageName&&info)packageName=" + fn(TEXT) + "(info,"
                + js(PACKAGE_FALLBACK_NODE) + ");if(!packageName||!info)return;var packageNode=info.querySelector("
                + js(ExtentReportRuntimeVocabulary.Selector.PACKAGE_NODE) + ")||info.querySelector("
                + js(PACKAGE_FALLBACK_NODE) + ");if(!packageNode){packageNode=document.createElement('div');packageNode.className="
                + js(ExtentReportRuntimeVocabulary.CssClass.TEST_PACKAGE) + ";var title=info.querySelector("
                + js(TEST_STATUS_NODE) + ");if(title)title.insertAdjacentElement('afterend',packageNode);else info.insertBefore(packageNode,info.firstChild);}packageNode.classList.add("
                + js(ExtentReportRuntimeVocabulary.CssClass.TEST_PACKAGE) + ");packageNode.setAttribute("
                + js(ExtentReportRuntimeVocabulary.Attribute.TEST_PACKAGE)
                + ",packageName);packageNode.textContent=packageName;});";
        return function(NORMALIZE_TEST_PACKAGE_METADATA, "", body);
    }

    private static String escapeBody() {
        return "return String(v==null?'':v).replace(/[&<>\"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',\"'\":'&#39;'}[c];});";
    }

    private static String textBody() {
        return "var n=s?(e&&e.querySelector(s)):e;return n?String(n.textContent||'').trim():'';";
    }

    private static String function(ExtentReportRuntimeVocabulary.Function function,
                                   String parameters, String body) {
        return "function " + ExtentReportRuntimeVocabulary.function(function)
                + "(" + parameters + "){" + body + "}";
    }

    private static String fn(ExtentReportRuntimeVocabulary.Function function) {
        return ExtentReportRuntimeVocabulary.function(function);
    }

    private static String js(ExtentReportRuntimeVocabulary.Selector selector) {
        return ExtentReportRuntimeVocabulary.js(selector.value());
    }

    private static String js(ExtentReportRuntimeVocabulary.Attribute attribute) {
        return ExtentReportRuntimeVocabulary.js(attribute.value());
    }

    private static String js(ExtentReportRuntimeVocabulary.CssClass cssClass) {
        return ExtentReportRuntimeVocabulary.js(cssClass.value());
    }

    private static String js(String value) {
        return ExtentReportRuntimeVocabulary.js(value);
    }
}
