package atf.api.reporting.asset;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.theme.tokens.AtfReportColors;
import atf.api.reporting.theme.tokens.AtfReportThemeTokens;
import atf.api.reporting.theme.css.AtfReportOverviewCss;

import java.util.Map;

/** Composition facade for the resource-backed fallback report assets. */
public final class ReportAssetComposer {
    private static final ReportAssetLoader LOADER = new ReportAssetLoader();
    private static final ReportAssetManifest MANIFEST = ReportAssetManifest.production();

    private ReportAssetComposer() {
    }

    /** Returns the validated, deterministic inventory used by the publication boundary. */
    public static ReportAssetManifest manifest() {
        return MANIFEST;
    }

    public static String fallbackCss() {
        return LOADER.render(ReportAsset.FALLBACK_CSS, Map.of(
                "CSS_VARIABLES", AtfReportThemeTokens.cssVariables(),
                "FONT_STACK", AtfReportThemeTokens.FONT_STACK,
                "SHADOW_SOFT", AtfReportColors.SHADOW_SOFT,
                "WHITE_02", AtfReportColors.WHITE_02));
    }

    public static String evidenceInteractionCss() {
        return LOADER.render(ReportAsset.EVIDENCE_INTERACTION_CSS,
                Map.of("WHITE_024", AtfReportColors.WHITE_024));
    }

    public static String evidenceTableCss() {
        return LOADER.render(ReportAsset.EVIDENCE_TABLE_CSS,
                Map.of("WHITE_024", AtfReportColors.WHITE_024));
    }

    public static String evidenceDisclosureCss() {
        return LOADER.load(ReportAsset.EVIDENCE_DISCLOSURE_CSS);
    }

    public static String evidenceStackCss() {
        return LOADER.load(ReportAsset.EVIDENCE_STACK_CSS);
    }

    public static String uiEvidenceCss(AtfReportHtmlVocabulary.ElementId sectionId,
                                       AtfReportHtmlVocabulary.CssClass screenshotLinkClass) {
        var sectionSelector = "#" + sectionId.value();
        return LOADER.render(ReportAsset.UI_EVIDENCE_CSS, Map.ofEntries(
                Map.entry("SECTION_SELECTOR", sectionSelector),
                Map.entry("LINK_CLASS", screenshotLinkClass.value()),
                Map.entry("UI_EVIDENCE_BORDER", AtfReportColors.UI_EVIDENCE_BORDER),
                Map.entry("UI_EVIDENCE_SURFACE", AtfReportColors.UI_EVIDENCE_SURFACE),
                Map.entry("UI_EVIDENCE_TEXT", AtfReportColors.UI_EVIDENCE_TEXT),
                Map.entry("UI_EVIDENCE_DETAIL_SURFACE", AtfReportColors.UI_EVIDENCE_DETAIL_SURFACE),
                Map.entry("UI_EVIDENCE_SUMMARY", AtfReportColors.UI_EVIDENCE_SUMMARY),
                Map.entry("UI_EVIDENCE_MUTED", AtfReportColors.UI_EVIDENCE_MUTED),
                Map.entry("UI_EVIDENCE_HOVER", AtfReportColors.UI_EVIDENCE_HOVER)));
    }

    public static String extentOverviewLayoutCss() {
        return LOADER.render(ReportAsset.EXTENT_OVERVIEW_LAYOUT_CSS, overviewTokens());
    }

    public static String extentOverviewTagAuditCss() {
        return LOADER.render(ReportAsset.EXTENT_OVERVIEW_TAG_AUDIT_CSS, overviewTokens());
    }

    public static String extentOverviewCategoryTablesCss() {
        return LOADER.render(ReportAsset.EXTENT_OVERVIEW_CATEGORY_TABLES_CSS, overviewTokens());
    }

    public static String extentOverviewCategoryStatusCss() {
        return LOADER.load(ReportAsset.EXTENT_OVERVIEW_CATEGORY_STATUS_CSS);
    }

    public static String themeResponsiveCss() {
        return LOADER.render(ReportAsset.EXTENT_THEME_RESPONSIVE_CSS,
                Map.of("NARROW_VIEWPORT", AtfReportThemeTokens.NARROW_VIEWPORT));
    }

    public static String themeBaseCss() {
        return LOADER.render(ReportAsset.EXTENT_THEME_BASE_CSS, Map.of(
                "FONT_STACK", AtfReportThemeTokens.FONT_STACK,
                "SHADOW_STRONG", AtfReportColors.SHADOW_STRONG));
    }

    public static String themeCategoryStatusCss() {
        return LOADER.render(ReportAsset.EXTENT_THEME_CATEGORY_STATUS_CSS, Map.of(
                "CATEGORY_STATUS_SELECTOR", ".category-view .test-list .test-detail .meta .badge.log,.category-view .test-contents .info .badge.log,.category-view .test-contents td .badge.log"));
    }

    public static String themeFailsLayoutCss() {
        return LOADER.load(ReportAsset.EXTENT_THEME_FAILS_LAYOUT_CSS);
    }

    public static String themeFailsStatusCss() {
        return LOADER.load(ReportAsset.EXTENT_THEME_FAILS_STATUS_CSS);
    }

    public static String themeCategoryCss() {
        return LOADER.load(ReportAsset.EXTENT_THEME_CATEGORY_CSS);
    }

    public static String themeNavigationCss() {
        return LOADER.render(ReportAsset.EXTENT_THEME_NAVIGATION_CSS, Map.of(
                "FONT_STACK", AtfReportThemeTokens.FONT_STACK,
                "SHADOW_SOFT", AtfReportColors.SHADOW_SOFT,
                "ACCENT_045", AtfReportColors.ACCENT_045,
                "WHITE", AtfReportColors.WHITE));
    }

    public static String themeScenarioCss() {
        return LOADER.load(ReportAsset.EXTENT_THEME_SCENARIO_CSS);
    }

    public static String themeDetailCss() {
        return LOADER.render(ReportAsset.EXTENT_THEME_DETAIL_CSS, Map.of(
                "FAILS_STATUS_CSS", themeFailsStatusCss(),
                "OVERVIEW_METRICS_CSS", AtfReportOverviewCss.metrics(),
                "CATEGORY_CSS", themeCategoryCss(),
                "OVERVIEW_CATEGORY_STATUS_CSS", AtfReportOverviewCss.categoryStatus(),
                "FAILURE_CAUSE_TEXT", AtfReportColors.FAILURE_CAUSE_TEXT,
                "ACCENT_12", AtfReportColors.ACCENT_12,
                "DANGER_055", AtfReportColors.DANGER_055,
                "ACCENT_045", AtfReportColors.ACCENT_045,
                "WHITE_025", AtfReportColors.WHITE_025,
                "DARK", AtfReportColors.DARK));
    }

    public static String testFiltersScript(Map<String, String> variables) {
        return LOADER.render(ReportAsset.EXTENT_TEST_FILTERS_JS, variables);
    }

    public static String extentNavigationDomScript() {
        return LOADER.load(ReportAsset.EXTENT_NAVIGATION_DOM_JS);
    }

    public static String extentNavigationDisclosureScript() {
        return LOADER.load(ReportAsset.EXTENT_NAVIGATION_DISCLOSURE_JS);
    }

    public static String extentNavigationControllerScript() {
        return LOADER.load(ReportAsset.EXTENT_NAVIGATION_CONTROLLER_JS);
    }

    public static String extentOverviewAuditScript() {
        return LOADER.load(ReportAsset.EXTENT_OVERVIEW_AUDIT_JS);
    }

    public static String extentOverviewLabelsScript() {
        return LOADER.load(ReportAsset.EXTENT_OVERVIEW_LABELS_JS);
    }

    public static String extentOverviewChartsScript() {
        return LOADER.load(ReportAsset.EXTENT_OVERVIEW_CHARTS_JS);
    }

    public static String extentOverviewCleanupScript() {
        return LOADER.load(ReportAsset.EXTENT_OVERVIEW_CLEANUP_JS);
    }

    public static String extentScenariosScript() {
        return LOADER.load(ReportAsset.EXTENT_SCENARIOS_JS);
    }

    public static String extentCategoriesScript() {
        return LOADER.load(ReportAsset.EXTENT_CATEGORIES_JS);
    }

    public static String fallbackNavigationScript() {
        return LOADER.load(ReportAsset.FALLBACK_NAVIGATION_JS);
    }

    public static String extentEvidenceRolesScript() {
        return LOADER.load(ReportAsset.EXTENT_EVIDENCE_ROLES_JS);
    }

    public static String extentEvidenceDisclosureScript() {
        return LOADER.load(ReportAsset.EXTENT_EVIDENCE_DISCLOSURE_JS);
    }

    public static String extentEvidenceFailuresScript() {
        return LOADER.load(ReportAsset.EXTENT_EVIDENCE_FAILURES_JS);
    }

    public static String extentEvidenceTableLayoutScript() {
        return LOADER.load(ReportAsset.EXTENT_EVIDENCE_TABLE_LAYOUT_JS);
    }

    public static String extentFailureDetectionScript() {
        return LOADER.load(ReportAsset.EXTENT_FAILURE_DETECTION_JS);
    }

    public static String extentFailureContextScript() {
        return LOADER.load(ReportAsset.EXTENT_FAILURE_CONTEXT_JS);
    }

    public static String extentFailureCauseChainScript() {
        return LOADER.load(ReportAsset.EXTENT_FAILURE_CAUSE_CHAIN_JS);
    }

    public static String extentFailureProjectionScript() {
        return LOADER.load(ReportAsset.EXTENT_FAILURE_PROJECTION_JS);
    }

    public static String runtimeBootScript(String moduleScripts) {
        return LOADER.render(ReportAsset.RUNTIME_BOOT_JS, Map.of(
                "RUNTIME_BOOT_FLAG", "__atfRuntimeBoot",
                "RUNTIME_MODULES", moduleScripts,
                "RUNTIME_READY_FLAG", "__atfRuntimeReady",
                "RUNTIME_READY_ATTRIBUTE", "data-atf-runtime-ready",
                "RUNTIME_ERROR_FLAG", "__atfRuntimeError"));
    }

    public static String durationParsingScript() {
        return LOADER.load(ReportAsset.DURATION_PARSING_JS);
    }

    public static String durationDomScript() {
        return LOADER.load(ReportAsset.DURATION_DOM_JS);
    }

    public static String durationMemoryScript() {
        return LOADER.load(ReportAsset.DURATION_MEMORY_JS);
    }

    public static String durationAggregationScript() {
        return LOADER.load(ReportAsset.DURATION_AGGREGATION_JS);
    }

    public static String durationProjectionLookupScript() {
        return LOADER.load(ReportAsset.DURATION_PROJECTION_LOOKUP_JS);
    }

    public static String durationProjectionViewsScript() {
        return LOADER.load(ReportAsset.DURATION_PROJECTION_VIEWS_JS);
    }

    public static String durationProjectionPublicationScript() {
        return LOADER.load(ReportAsset.DURATION_PROJECTION_PUBLICATION_JS);
    }

    public static String durationSchedulingScript() {
        return LOADER.load(ReportAsset.DURATION_SCHEDULING_JS);
    }

    public static String durationLifecycleScript() {
        return LOADER.load(ReportAsset.DURATION_LIFECYCLE_JS);
    }

    public static String runtimeCoreDomScript() {
        return LOADER.load(ReportAsset.RUNTIME_CORE_DOM_JS);
    }

    public static String runtimeCoreScenariosScript() {
        return LOADER.load(ReportAsset.RUNTIME_CORE_SCENARIOS_JS);
    }

    public static String runtimeCoreMetadataScript() {
        return LOADER.load(ReportAsset.RUNTIME_CORE_METADATA_JS);
    }

    private static Map<String, String> overviewTokens() {
        return Map.of(
                "DARK", AtfReportColors.DARK,
                "WHITE_02", AtfReportColors.WHITE_02,
                "WHITE_018", AtfReportColors.WHITE_018);
    }
}







