package atf.ui.tests.extent.support;

import java.util.List;

/** Closed vocabulary and stable identifiers of the rendered ATF Extent report. */
public final class ReportUiContract {
    public static final String INPUT_SOURCE_TEMP_PREFIX = "atf-input-source-";
    public static final String TEST_OWNER_SELECTOR = ".test-list .test-item";
    public static final String VISIBLE_DETAIL_SELECTOR = ".test-content-detail:visible";
    public static final String SCREENSHOT_MARKER = "📷 base64 img";
    public static final String BASE64_SCREENSHOT_ATTRIBUTE = "data-atf-screenshot";
    public static final String BASE64_SCREENSHOT_VALUE = "base64";
    public static final String JSON_REQUEST_CLASS = "request-json-body";
    public static final String JSON_RESPONSE_CLASS = "response-json-body";
    public static final String OVERVIEW_EXECUTION_METADATA_GRID = "#atf-execution-metadata-grid";
    public static final String OVERVIEW_SECONDARY_METRIC = ".atf-secondary-metric";
    public static final String OVERVIEW_SCENARIO_AUDIT = "#atf-audit-scenarios";
    public static final String BODY_SELECTOR = "body";
    public static final String TAG_FILTER_MENU = "#tag-toggle";
    public static final String VALIDATION_TABLE_SELECTOR = "table.helm-validation-table";
    public static final String ACTION_SEQUENCE_SELECTOR = "details.atf-ui-action-sequence";
    public static final String SCREENSHOT_LINK_SELECTOR = "a.atf-ui-action-screenshot-link";

    private ReportUiContract() { }

    public enum Tab {
        TESTS("#test-view"),
        OVERVIEW("#dashboard-view"),
        CATEGORIES("#category-view"),
        FAILS("#atf-fails-view");

        private final String hash;

        Tab(String hash) { this.hash = hash; }
        public String hash() { return hash; }
    }

    public enum Phase {
        SETUP("SETUP"),
        TEST_BODY("TEST BODY"),
        TEARDOWN("TEARDOWN");

        private final String displayName;

        Phase(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }

    public enum NodeRole {
        SCENARIO("scenario"),
        PHASE("phase"),
        REQUEST("request"),
        VALIDATION("validation");

        private final String attributeValue;

        NodeRole(String attributeValue) { this.attributeValue = attributeValue; }
        public String attributeValue() { return attributeValue; }
    }

    public static String nodeRoleSelector(NodeRole role) {
        return "[data-atf-node-role='" + role.attributeValue() + "']";
    }

    public static String phaseSelector(Phase phase) {
        return nodeRoleSelector(NodeRole.PHASE)
                + "[data-atf-phase-name='" + phase.displayName().toLowerCase().replace(' ', '-') + "']";
    }

    public enum OverviewSection {
        PARENT_ANALYSIS("#parent-analysis"),
        CHILD_ANALYSIS("#child-analysis"),
        GRANDCHILD_ANALYSIS("#grandchild-analysis"),
        TIMELINE("#timeline"),
        EXECUTION_METADATA("#atf-execution-metadata"),
        TAG_AUDIT("#atf-tag-audit"),
        CATEGORIES(".category-container"),
        SYSTEM_ENVIRONMENT(".sysenv-container"),
        SCENARIO_AUDIT("#atf-audit-scenarios"),
        PACKAGE_AUDIT("#atf-audit-packages"),
        TAG_AUDIT_TABLE("#atf-audit-tags");

        private final String selector;

        OverviewSection(String selector) { this.selector = selector; }
        public String selector() { return selector; }
    }

    public static List<String> overviewSelectors(boolean includeHierarchy) {
        if (includeHierarchy) {
            return List.of(
                    OverviewSection.PARENT_ANALYSIS.selector(),
                    OverviewSection.CHILD_ANALYSIS.selector(),
                    OverviewSection.GRANDCHILD_ANALYSIS.selector(),
                    OverviewSection.TIMELINE.selector(),
                    OverviewSection.EXECUTION_METADATA.selector(),
                    OverviewSection.TAG_AUDIT.selector(),
                    OverviewSection.SCENARIO_AUDIT.selector(),
                    OverviewSection.PACKAGE_AUDIT.selector(),
                    OverviewSection.TAG_AUDIT_TABLE.selector());
        }
        return List.of(
                OverviewSection.PARENT_ANALYSIS.selector(),
                OverviewSection.TIMELINE.selector(),
                OverviewSection.EXECUTION_METADATA.selector(),
                OverviewSection.TAG_AUDIT.selector(),
                OverviewSection.CATEGORIES.selector(),
                OverviewSection.SYSTEM_ENVIRONMENT.selector(),
                OverviewSection.SCENARIO_AUDIT.selector(),
                OverviewSection.PACKAGE_AUDIT.selector(),
                OverviewSection.TAG_AUDIT_TABLE.selector());
    }
}
