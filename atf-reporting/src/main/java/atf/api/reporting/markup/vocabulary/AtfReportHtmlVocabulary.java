package atf.api.reporting.markup.vocabulary;

import atf.api.reporting.contract.AtfReportStatus;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Typed vocabulary for repeated semantic HTML/CSS tokens emitted by the report renderer.
 *
 * <p>The renderer owns the markup; this vocabulary owns the stable contract consumed by
 * static auditors and browser adapters. Keeping the tokens here prevents drift caused by
 * repeated raw strings while leaving report labels in {@code AtfReportLabels}.</p>
 */
public final class AtfReportHtmlVocabulary {
    private AtfReportHtmlVocabulary() {
    }

    /** Finite HTML shell vocabulary used by the renderer builders. */
    public enum Tag {
        HTML("html"),
        HEAD("head"),
        BODY("body"),
        META("meta"),
        TITLE("title"),
        STYLE("style"),
        SCRIPT("script"),
        NAV("nav"),
        MAIN("main"),
        DIV("div"),
        SECTION("section"),
        ARTICLE("article"),
        DETAILS("details"),
        SUMMARY("summary"),
        PRE("pre"),
        OL("ol"),
        LI("li"),
        H2("h2"),
        BUTTON("button"),
        A("a"),
        SPAN("span"),
        LINK("link"),
        TABLE("table"),
        COLGROUP("colgroup"),
        COL("col"),
        THEAD("thead"),
        TBODY("tbody"),
        TR("tr"),
        TH("th"),
        TD("td");

        private final String value;

        Tag(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum CssClass {
        ASSERTIONS("assertions"),
        ACTIVE("active"),
        ATF_SETUP_EVIDENCE("atf-setup-evidence"),
        ATF_SETUP_EVIDENCE_TABLE("atf-setup-evidence-table"),
        ATF_FAILS_LIST("atf-fails-list"),
        ATF_FAILS_VIEW("atf-fails-view"),
        ATF_SECONDARY_BODY("atf-secondary-body"),
        ATF_SECONDARY_GRID("atf-secondary-grid"),
        ATF_SECONDARY_OVERVIEW("atf-secondary-overview"),
        ATF_SECONDARY_OVERVIEW_BADGE("atf-secondary-overview-badge"),
        ATF_SECONDARY_OVERVIEW_TITLE("atf-secondary-overview-title"),
        ATF_TAG_AUDIT_BODY("atf-tag-audit-body"),
        ATF_TAG_AUDIT_DISCLOSURE("atf-tag-audit-disclosure"),
        ATF_TAG_GRID("atf-tag-grid"),
        ATF_TAG_SCROLL("atf-tag-scroll"),
        ATF_TAG_TABLE("atf-tag-table"),
        ATF_TAG_CARD("atf-tag-card"),
        ATF_UI_ACTION_IDENTITY("atf-ui-action-identity"),
        ATF_UI_ACTION_OPERATION("atf-ui-action-operation"),
        ATF_UI_ACTION_SEQUENCE("atf-ui-action-sequence"),
        ATF_UI_ACTION_NUMBER("atf-ui-action-number"),
        ATF_UI_ACTION_SCREENSHOT("atf-ui-action-screenshot"),
        ATF_UI_ACTION_SCREENSHOT_LINK("atf-ui-action-screenshot-link"),
        ATF_UI_ACTION_TABLE("atf-ui-action-table"),
        ATF_UI_ACTION_TABLE_WRAP("atf-ui-action-table-wrap"),
        ATF_UI_ACTION_TIME("atf-ui-action-time"),
        ATF_UI_ACTION_RESULT("atf-ui-action-result"),
        ATF_UI_OBSERVATIONS("atf-ui-observations"),
        ATF_UI_OBSERVATION_TABLE("atf-ui-observation-table"),
        ATF_UI_ASSERTIONS("atf-ui-assertions"),
        ATF_UI_EVIDENCE_SCREENSHOT_LINK("atf-ui-evidence-screenshot-link"),
        BADGE("badge"),
        BADGE_GRADIENT_PRIMARY("badge-gradient-primary"),
        EVIDENCE_KEY_COLUMN("atf-evidence-key-column"),
        EVIDENCE_VALUE_COLUMN("atf-evidence-value-column"),
        DISCLOSURE_GLYPH("atf-disclosure-glyph"),
        EXECUTION_EVIDENCE("execution-evidence"),
        EXECUTION_EVIDENCE_BODY("execution-evidence-body"),
        EXECUTION_TABLE("execution-table"),
        HELM_API_CLASSIFICATION("helm-api-classification"),
        HELM_API_CLASSIFICATION_LABEL("helm-api-classification-label"),
        HELM_EVIDENCE_TABLE("helm-evidence-table"),
        HELM_REQUEST_TABLE("helm-request-table"),
        HELM_VALIDATION_TABLE("helm-validation-table"),
        HELM_REQUEST_DETAILS("helm-request-details"),
        HELM_RESPONSE_DETAILS("helm-response-details"),
        HELM_TRACE_TABLE("helm-trace-table"),
        HELM_SCROLL_BLOCK_TITLE("helm-scroll-block-title"),
        HELM_SCROLL_BLOCK_BODY("helm-scroll-block-body"),
        JSON_CODE("json-code"),
        LAYOUT("layout"),
        FAIL_ROW("fail-row"),
        NODE_TITLE("node-title"),
        PHASE("phase"),
        PHASE_DETAILS("phase-details"),
        REQUEST("request"),
        REQUEST_DETAILS_CONTENT("request-details-content"),
        REQUEST_EVIDENCE("request-evidence"),
        REQUEST_JSON_BODY("request-json-body"),
        RESPONSE_EVIDENCE("response-evidence"),
        RESPONSE_JSON_BODY("response-json-body"),
        RESPONSE_DETAILS_CONTENT("response-details-content"),
        RETRY_HISTORY_TABLE("retry-history-table"),
        SCENARIO("scenario"),
        SCENARIO_TITLE("scenario-title"),
        SE_TITLE("se-title"),
        SETUP_EVIDENCE("setup-evidence"),
        STATUS("status"),
        STEP_DETAIL("step-detail"),
        TECHNICAL("technical"),
        TAB("tab"),
        TABS("tabs"),
        TAB_PANEL("tab-panel"),
        TEST_CONTENT("test-content"),
        TEST_LINK("test-link"),
        VALIDATION("validation"),
        BODY("body");

        private final String value;

        CssClass(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Attribute {
        HREF("href"),
        SRC("src"),
        TARGET("target"),
        REL("rel"),
        STYLE("style"),
        TITLE("title"),
        ARIA_LABEL("aria-label"),
        ARIA_EXPANDED("aria-expanded"),
        ARIA_HIDDEN("aria-hidden"),
        ROLE("role"),
        FEATHERLIGHT("data-featherlight"),
        INLINE("data-atf-inline"),
        EVIDENCE_STATUS("data-atf-evidence-status"),
        EVIDENCE_TYPE("data-atf-evidence-type"),
        NODE_ROLE("data-atf-node-role"),
        PHASE_NAME("data-atf-phase-name"),
        SCREENSHOT("data-atf-screenshot"),
        TAB("data-atf-tab"),
        FALLBACK_TAB("data-tab");

        private final String value;

        Attribute(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum AttributeValue {
        BASE64("base64"),
        IMAGE("image"),
        BLANK("_blank"),
        NOOPENER("noopener"),
        TRUE("true"),
        FALSE("false");

        private final String value;

        AttributeValue(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum ViewId {
        OVERVIEW("dashboard-view"),
        TESTS("test-view"),
        CATEGORIES("category-view"),
        FAILS("atf-fails-view");

        private final String value;

        ViewId(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum TabKey {
        OVERVIEW("overview"),
        TESTS("test"),
        CATEGORIES("category"),
        FAILS("fails");

        private final String value;

        TabKey(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    /**
     * Navigation vocabulary of the standalone HTML fallback.
     *
     * <p>The Extent document has its own vendor-facing tab ids, while the
     * fallback document is renderer-neutral. Keeping the fallback ids typed
     * prevents the HTML views, CSS and navigation asset from drifting apart.</p>
     */
    public enum FallbackTab {
        TESTS("tests", "Tests"),
        CATEGORY("category", "Category"),
        FAILS("fails", "Fails"),
        OVERVIEW("overview", "Overview");

        private final String value;
        private final String label;

        FallbackTab(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String value() {
            return value;
        }

        public String label() {
            return label;
        }
    }

    public enum ElementId {
        EXECUTION_METADATA("atf-execution-metadata"),
        EXECUTION_METADATA_GRID("atf-execution-metadata-grid"),
        TAG_AUDIT("atf-tag-audit"),
        FAILURE_LIST("atf-failure-list"),
        FAILURE_DETAIL("atf-failure-detail"),
        UI_EVIDENCE("atf-ui-exploratory-evidence"),
        UI_EVIDENCE_STYLE("atf-ui-evidence-style");

        private final String value;

        ElementId(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public static String classAttribute(CssClass... classes) {
        return classAttribute(Arrays.stream(classes)
                .filter(Objects::nonNull)
                .map(CssClass::value)
                .toArray(String[]::new));
    }

    public static String classAttribute(String... classes) {
        return "class=\"" + classList(classes) + "\"";
    }

    public static String classList(String... classes) {
        return Arrays.stream(classes)
                .filter(Objects::nonNull)
                .map(AtfReportHtmlVocabulary::escape)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    public static String classList(CssClass... classes) {
        String[] values = Arrays.stream(classes)
                .filter(Objects::nonNull)
                .map(CssClass::value)
                .toArray(String[]::new);
        return classList(values);
    }

    public static String attribute(Attribute attribute, String value) {
        return attribute.value() + "=\"" + escape(value) + "\"";
    }

    public static String attribute(Attribute attribute, AttributeValue value) {
        return attribute(attribute, Objects.requireNonNull(value, "value").value());
    }

    /** Serializes an already-escaped attribute list without introducing raw tag names in views. */
    public static String openTag(Tag tag, String... attributes) {
        Objects.requireNonNull(tag, "tag");
        String suffix = Arrays.stream(attributes == null ? new String[0] : attributes)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" "));
        return "<" + tag.value() + (suffix.isBlank() ? "" : " " + suffix) + ">";
    }

    public static String closeTag(Tag tag) {
        return "</" + Objects.requireNonNull(tag, "tag").value() + ">";
    }

    public static String idAttribute(String id) {
        return "id=\"" + escape(id) + "\"";
    }

    public static String idAttribute(ViewId id) {
        return idAttribute(Objects.requireNonNull(id, "id").value());
    }

    public static String idAttribute(ElementId id) {
        return idAttribute(Objects.requireNonNull(id, "id").value());
    }

    public static String[] statusClasses(AtfReportStatus status) {
        return new String[]{CssClass.STATUS.value(), statusClass(status)};
    }

    public static String statusClass(AtfReportStatus status) {
        return stateClass("status-", status);
    }

    public static String statusValue(AtfReportStatus status) {
        return Objects.requireNonNull(status, "status").displayValue();
    }

    public static String statusKey(boolean failed) {
        return failed ? "status-fail" : "status-pass";
    }

    public static String stateClass(String prefix, AtfReportStatus status) {
        return Objects.requireNonNull(prefix, "prefix")
                + statusValue(status);
    }

    /** Escapes user/test evidence before it is emitted into HTML text or attributes. */
    public static String escapeHtml(String value) {
        return escape(value);
    }

    private static String escape(String value) {
        return Objects.requireNonNullElse(value, "")
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&#39;");
    }
}
