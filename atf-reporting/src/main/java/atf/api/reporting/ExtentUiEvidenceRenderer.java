package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.core.ui.AtfUiActionRecord;
import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;
import java.util.List;

/** Renders UI action, screenshot and assertion evidence. */
final class ExtentUiEvidenceRenderer {
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;

    ExtentUiEvidenceRenderer(ReportingSettings settings, SensitiveDataRedactor redactor) {
        this.settings = settings;
        this.redactor = redactor;
    }

    void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions) {
        renderUiActions(node, actions, List.of());
    }

    void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions,
                         List<String[]> screenshots) {
        if (actions == null || actions.isEmpty()) return;
        long base64ScreenshotCount = screenshots == null ? 0 : screenshots.stream()
                .filter(screenshot -> screenshot != null
                        && screenshot.length >= 2
                        && screenshot[1] != null
                        && !screenshot[1].isBlank())
                .count();
        if (base64ScreenshotCount != 1) {
            throw new IllegalStateException(
                    "A UI action sequence requires exactly one Base64 screenshot; found "
                            + base64ScreenshotCount);
        }
        var html = actionTable(actions);
        html.append("</tbody></table>");
        for (String[] screenshot : screenshots) {
            if (screenshot == null || screenshot.length < 2 || screenshot[1].isBlank()) continue;
            String title = screenshot[0] == null || screenshot[0].isBlank()
                    ? "UI screenshot" : screenshot[0];
            html.append("<div class=\"atf-ui-action-screenshot\">")
                    .append("<a class=\"atf-ui-action-screenshot-link\" data-atf-screenshot=\"base64\"")
                    .append(" href=\"data:image/png;base64,")
                    .append(escapeHtml(screenshot[1]))
                    .append("\" target=\"_blank\" rel=\"noopener\" data-featherlight=\"image\" aria-label=\"Open ")
                    .append(escapeHtml(title))
                    .append("><span class=\"badge badge-gradient-primary\">base64 img</span></a></div>");
        }
        String markup = html.append("</div></details>").toString();
        node.info(() -> markup);
    }

    /** Renders the action table only; the caller adds native media separately. */
    void renderUiActionsAsMedia(ExtentTest node, List<AtfUiActionRecord> actions) {
        if (actions == null || actions.isEmpty()) return;
        node.info(() -> actionTable(actions).append("</tbody></table></div></details>").toString());
    }

    private StringBuilder actionTable(List<AtfUiActionRecord> actions) {
        var html = new StringBuilder("<details class=\"atf-ui-action-sequence\"><summary>")
                .append("Playwright action sequence (").append(actions.size()).append(" entries)")
                .append("</summary><div class=\"atf-ui-action-table-wrap\"><table class=\"atf-ui-action-table\">")
                .append("<thead><tr><th>#</th><th>OPERATION</th><th>TARGET</th><th>EXPECTED</th>")
                .append("<th>OBSERVED</th><th>TIME</th><th>RESULT</th></tr></thead><tbody>");
        for (AtfUiActionRecord action : actions) {
            html.append("<tr><td>").append(action.sequence()).append("</td><td>")
                    .append(escapeHtml(action.operation())).append("</td><td>")
                    .append(escapeHtml(action.target())).append("</td><td>")
                    .append(escapeHtml(ExtentReportNodeSupport.display(action.expected(), settings))).append("</td><td>")
                    .append(escapeHtml(ExtentReportNodeSupport.display(action.observed(), settings))).append("</td><td>")
                    .append(escapeHtml(action.durationText())).append("</td><td><span class=\"atf-ui-action-result atf-ui-action-result-")
                    .append(action.status().name().toLowerCase(java.util.Locale.ROOT))
                    .append("\" role=\"img\" aria-label=\"")
                    .append(escapeHtml(action.resultLabel())).append("\" title=\"")
                    .append(escapeHtml(action.resultLabel())).append("\">")
                    .append(action.resultEmoji()).append("</span></td></tr>");
        }
        return html;
    }

    void renderAssertions(ExtentTest node, Collection<AtfAssertion> assertions) {
        var table = new String[assertions.size() + 1][4];
        table[0] = new String[]{AtfReportLabels.ASSERTION, AtfReportLabels.EXPECTED,
                AtfReportLabels.ACTUAL, AtfReportLabels.RESULT};
        int index = 0;
        for (var row : assertions) {
            table[++index] = new String[]{
                    redactor.redactText(row.name()),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.expected()), settings),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.actual()), settings),
                    ExtentReportNodeSupport.resultMarker(row.resultStatus())};
        }
        node.info(AtfReportMarkup.table("helm-evidence-table helm-validation-table", table));
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
