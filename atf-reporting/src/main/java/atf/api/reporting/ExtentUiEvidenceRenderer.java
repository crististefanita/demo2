package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.core.ui.AtfUiEvidenceSource;
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
        List<AtfUiActionRecord> browserActions = actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.ACTION)
                .toList();
        List<AtfUiActionRecord> observations = actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.OBSERVE)
                .toList();
        if (!browserActions.isEmpty()) renderBrowserActions(node, browserActions, screenshots);
        if (!observations.isEmpty()) renderObservations(node, observations);
    }

    private void renderBrowserActions(ExtentTest node, List<AtfUiActionRecord> actions,
                                      List<String[]> screenshots) {
        List<String[]> distinctScreenshots = ExtentReportAssets.distinctScreenshots(screenshots);
        long base64ScreenshotCount = distinctScreenshots.size();
        if (base64ScreenshotCount != 1) {
            throw new IllegalStateException(
                    "A UI action sequence requires exactly one Base64 screenshot; found "
                            + base64ScreenshotCount);
        }
        var html = actionTable(actions);
        html.append("</tbody></table>");
        for (String[] screenshot : distinctScreenshots) {
            String title = screenshot[0] == null || screenshot[0].isBlank()
                    ? "UI screenshot" : screenshot[0];
            html.append("<div class=\"atf-ui-action-screenshot\">")
                    .append(ExtentReportAssets.base64ScreenshotLink(
                            "atf-ui-action-screenshot-link", title, screenshot[1]))
                    .append("</div>");
        }
        String markup = html.append("</div></details>").toString();
        node.info(() -> markup);
    }

    private void renderObservations(ExtentTest node, List<AtfUiActionRecord> observations) {
        String summary = observationSummary(observations);
        var html = new StringBuilder("<details class=\"atf-ui-observations\"><summary aria-label=\"")
                .append(escapeHtml(summary)).append("\">")
                .append(escapeHtml(summary))
                .append("</summary><div class=\"atf-ui-action-table-wrap\"><table class=\"atf-ui-action-table atf-ui-observation-table\">")
                .append("<thead><tr><th>#</th><th>OBSERVATION</th><th>TARGET</th><th>EXPECTED</th>")
                .append("<th>OBSERVED</th><th>TIME</th><th>RESULT</th></tr></thead><tbody>");
        for (AtfUiActionRecord observation : observations) {
            html.append("<tr><td>").append(observation.sequence()).append("</td><td>")
                    .append(escapeHtml(observation.operation())).append("</td><td>")
                    .append(escapeHtml(observation.target())).append("</td><td>")
                    .append(escapeHtml(ExtentReportNodeSupport.display(observation.expected(), settings))).append("</td><td>")
                    .append(escapeHtml(ExtentReportNodeSupport.display(observation.observed(), settings))).append("</td><td>")
                    .append(escapeHtml(observation.durationText())).append("</td><td><span class=\"atf-ui-action-result atf-ui-action-result-")
                    .append(observation.status().name().toLowerCase(java.util.Locale.ROOT))
                    .append("\" role=\"img\" aria-label=\"").append(escapeHtml(observation.resultLabel()))
                    .append("\" title=\"").append(escapeHtml(observation.resultLabel())).append("\">")
                    .append(observation.resultEmoji()).append("</span></td></tr>");
        }
        node.info(() -> html.append("</tbody></table></div></details>").toString());
    }

    private static String observationSummary(List<AtfUiActionRecord> observations) {
        String checkpointLabel = observations.size() == 1 ? "checkpoint" : "checkpoints";
        return "Audit observations · " + observations.size() + " " + checkpointLabel;
    }

    /** Renders the action table only; the caller adds native media separately. */
    void renderUiActionsAsMedia(ExtentTest node, List<AtfUiActionRecord> actions) {
        if (actions == null || actions.isEmpty()) return;
        List<AtfUiActionRecord> browserActions = actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.ACTION)
                .toList();
        List<AtfUiActionRecord> observations = actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.OBSERVE)
                .toList();
        if (!browserActions.isEmpty()) {
            node.info(() -> actionTable(browserActions).append("</tbody></table></div></details>").toString());
        }
        if (!observations.isEmpty()) renderObservations(node, observations);
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
