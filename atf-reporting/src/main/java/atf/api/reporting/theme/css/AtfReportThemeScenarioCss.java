package atf.api.reporting.theme.css;

import atf.api.reporting.theme.tokens.AtfReportCssSelectors;
import atf.api.reporting.theme.tokens.AtfReportCssProperty;

/**
 * Scenario-title rules kept separate from the general detail layer.
 *
 * <p>Long, declarative Gherkin propositions are report content, not a reason
 * to hide text behind an ellipsis. The title keeps the inline plus/minus
 * affordance supplied by the parent card while wrapping its own text inside
 * the available column.</p>
 */
public final class AtfReportThemeScenarioCss {
    private AtfReportThemeScenarioCss() {
    }

    public static String css() {
        String scenarioNode = scenario(AtfReportCssSelectors.Component.SCENARIO_NODE);
        String scenarioTitle = scenario(AtfReportCssSelectors.Component.SCENARIO_TITLE);
        String scenarioHeader = scenario(AtfReportCssSelectors.Component.SCENARIO_HEADER);
        var css = new AtfReportCssBuilder();
        css.rule(scenarioNode)
                .important(AtfReportCssProperty.DISPLAY, "flex")
                .important(AtfReportCssProperty.ALIGN_ITEMS, "flex-start")
                .important(AtfReportCssProperty.JUSTIFY_CONTENT, "flex-start")
                .important(AtfReportCssProperty.HEIGHT, "auto")
                .important(AtfReportCssProperty.MIN_HEIGHT, "var(--atf-scenario-node-min-height)")
                .important(AtfReportCssProperty.MARGIN, "0")
                .important(AtfReportCssProperty.PADDING, "0")
                .important(AtfReportCssProperty.LINE_HEIGHT, "var(--atf-scenario-line-height)")
                .important(AtfReportCssProperty.WHITE_SPACE, "normal")
                .important(AtfReportCssProperty.OVERFLOW, "visible")
                .important(AtfReportCssProperty.TEXT_OVERFLOW, "clip")
                .important(AtfReportCssProperty.OVERFLOW_WRAP, "anywhere")
                .important(AtfReportCssProperty.WORD_BREAK, "break-word");
        css.rule(scenarioTitle)
                .important(AtfReportCssProperty.MIN_WIDTH, "0")
                .important(AtfReportCssProperty.MAX_WIDTH, "100%")
                .important(AtfReportCssProperty.OVERFLOW, "visible");
        css.rule(scenarioNode)
                .important(AtfReportCssProperty.MAX_WIDTH, "100%")
                .important(AtfReportCssProperty.MIN_WIDTH, "0")
                .important(AtfReportCssProperty.TEXT_ALIGN, "left");
        css.rule(scenarioHeader)
                .important(AtfReportCssProperty.MIN_HEIGHT, "var(--atf-scenario-header-min-height)")
                .important(AtfReportCssProperty.HEIGHT, "auto")
                .important(AtfReportCssProperty.FLEX_WRAP, "nowrap");
        css.rule(scenarioHeader + ":before")
                .important(AtfReportCssProperty.ALIGN_SELF, "flex-start")
                .important(AtfReportCssProperty.MARGIN_TOP, "var(--atf-scenario-glyph-offset)");
        return css.build();
    }

    private static String scenario(AtfReportCssSelectors.Component component) {
        String detail = AtfReportCssSelectors.descendant(
                AtfReportCssSelectors.Surface.TEST_VIEW,
                AtfReportCssSelectors.Component.TEST_CONTENT_DETAIL);
        return AtfReportCssSelectors.descendant(detail, component.selector());
    }
}
