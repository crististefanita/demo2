package atf.api.reporting.evidence;
import atf.api.core.config.ReportingSettings;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.core.ui.AtfUiEvidenceSource;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.AtfReportMarkup;
import atf.api.reporting.markup.ExtentReportAssets;
import atf.api.reporting.markup.ExtentReportNodeSupport;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportScreenshot;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.Markup;

import java.util.Collection;
import java.util.List;

/** Renders UI action, screenshot and assertion evidence. */
public final class ExtentUiEvidenceRenderer {
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
                         List<ReportScreenshot> screenshots) {
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
                                      List<ReportScreenshot> screenshots) {
        List<ReportScreenshot> distinctScreenshots = ExtentReportAssets.distinctScreenshotModels(screenshots);
        long base64ScreenshotCount = distinctScreenshots.size();
        if (base64ScreenshotCount != 1) {
            throw new IllegalStateException(
                    "A UI action sequence requires exactly one Base64 screenshot; found "
                            + base64ScreenshotCount);
        }
        var html = actionTable(actions);
        html.append(close(AtfReportHtmlVocabulary.Tag.TBODY))
                .append(close(AtfReportHtmlVocabulary.Tag.TABLE));
        for (ReportScreenshot screenshot : distinctScreenshots) {
            String title = screenshot.title();
            html.append(open(AtfReportHtmlVocabulary.Tag.DIV,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SCREENSHOT)))
                    .append(ExtentReportAssets.base64ScreenshotLink(
                            AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SCREENSHOT_LINK.value(),
                            title, screenshot.base64Png()))
                    .append(close(AtfReportHtmlVocabulary.Tag.DIV));
        }
        String markup = html.append(close(AtfReportHtmlVocabulary.Tag.DIV))
                .append(close(AtfReportHtmlVocabulary.Tag.DETAILS)).toString();
        publishEvidence(node, markup, hasFailure(actions));
    }

    private void renderObservations(ExtentTest node, List<AtfUiActionRecord> observations) {
        boolean failed = hasFailure(observations);
        String summary = observationSummary(observations, failed);
        var html = new StringBuilder(open(AtfReportHtmlVocabulary.Tag.DETAILS,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_OBSERVATIONS),
                        AtfReportHtmlVocabulary.attribute(
                                AtfReportHtmlVocabulary.Attribute.EVIDENCE_STATUS, statusKey(failed))))
                .append(open(AtfReportHtmlVocabulary.Tag.SUMMARY,
                        AtfReportHtmlVocabulary.attribute(
                                AtfReportHtmlVocabulary.Attribute.ARIA_LABEL, summary)))
                .append(escapeHtml(summary))
                .append(close(AtfReportHtmlVocabulary.Tag.SUMMARY))
                .append(open(AtfReportHtmlVocabulary.Tag.DIV,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_TABLE_WRAP)))
                .append(open(AtfReportHtmlVocabulary.Tag.TABLE,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_TABLE,
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_OBSERVATION_TABLE)))
                .append(tableHeader(AtfReportLabels.ACTION, AtfReportLabels.TARGET,
                        AtfReportLabels.EXPECTED, AtfReportLabels.OBSERVED,
                        AtfReportLabels.RESULT))
                .append(open(AtfReportHtmlVocabulary.Tag.TBODY));
        for (AtfUiActionRecord observation : observations) {
            html.append(actionRowStart()).append(escapeHtml(observation.operation()))
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(open(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SEQUENCE)))
                    .append("#").append(observation.sequence())
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escapeHtml(observation.target()))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escapeHtml(ExtentReportNodeSupport.display(observation.expected(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escapeHtml(ExtentReportNodeSupport.display(observation.observed(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(resultSpan(observation))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(close(AtfReportHtmlVocabulary.Tag.TR));
        }
        publishEvidence(node, html.append(close(AtfReportHtmlVocabulary.Tag.TBODY))
                .append(close(AtfReportHtmlVocabulary.Tag.TABLE))
                .append(close(AtfReportHtmlVocabulary.Tag.DIV))
                .append(close(AtfReportHtmlVocabulary.Tag.DETAILS)).toString(), failed);
    }

    private static String observationSummary(List<AtfUiActionRecord> observations, boolean failed) {
        String checkpointLabel = observations.size() == 1 ? "checkpoint" : "checkpoints";
        if (failed) {
            long failures = observations.stream().filter(ExtentUiEvidenceRenderer::isFailure).count();
            return ExtentReportNodeSupport.resultMarker(AtfReportStatus.FAIL)
                    + " Audit observations · " + failures + " failed of " + observations.size();
        }
        return AtfReportLabels.AUDIT_OBSERVATIONS + " · " + observations.size() + " " + checkpointLabel;
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
            publishEvidence(node,
                    actionTable(browserActions)
                            .append(close(AtfReportHtmlVocabulary.Tag.TBODY))
                            .append(close(AtfReportHtmlVocabulary.Tag.TABLE))
                            .append(close(AtfReportHtmlVocabulary.Tag.DIV))
                            .append(close(AtfReportHtmlVocabulary.Tag.DETAILS)).toString(),
                    hasFailure(browserActions));
        }
        if (!observations.isEmpty()) renderObservations(node, observations);
    }

    private StringBuilder actionTable(List<AtfUiActionRecord> actions) {
        boolean failed = hasFailure(actions);
        var html = new StringBuilder(open(AtfReportHtmlVocabulary.Tag.DETAILS,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SEQUENCE),
                        AtfReportHtmlVocabulary.attribute(
                                AtfReportHtmlVocabulary.Attribute.EVIDENCE_STATUS, statusKey(failed))))
                .append(open(AtfReportHtmlVocabulary.Tag.SUMMARY))
                .append(actionSummary(actions, failed))
                .append(close(AtfReportHtmlVocabulary.Tag.SUMMARY))
                .append(open(AtfReportHtmlVocabulary.Tag.DIV,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_TABLE_WRAP)))
                .append(open(AtfReportHtmlVocabulary.Tag.TABLE,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_TABLE)))
                .append(tableHeader(AtfReportLabels.ACTION, AtfReportLabels.TARGET,
                        AtfReportLabels.EXPECTED, AtfReportLabels.OBSERVED,
                        AtfReportLabels.TIME, AtfReportLabels.RESULT))
                .append(open(AtfReportHtmlVocabulary.Tag.TBODY));
        for (AtfUiActionRecord action : actions) {
            html.append(actionRowStart()).append(escapeHtml(action.operation()))
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(open(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SEQUENCE)))
                    .append("#").append(action.sequence())
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escapeHtml(action.target())).append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escapeHtml(ExtentReportNodeSupport.display(action.expected(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escapeHtml(ExtentReportNodeSupport.display(action.observed(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_TIME)))
                    .append(escapeHtml(action.durationText()))
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(resultSpan(action))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(close(AtfReportHtmlVocabulary.Tag.TR));
        }
        return html;
    }

    private static String actionRowStart() {
        return open(AtfReportHtmlVocabulary.Tag.TR)
                + open(AtfReportHtmlVocabulary.Tag.TD,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_IDENTITY))
                + open(AtfReportHtmlVocabulary.Tag.SPAN,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_OPERATION));
    }

    private static String resultSpan(AtfUiActionRecord action) {
        String stateClass = "atf-ui-action-result-"
                + action.status().displayValue();
        return open(AtfReportHtmlVocabulary.Tag.SPAN,
                AtfReportHtmlVocabulary.classAttribute(
                        AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_RESULT.value(), stateClass),
                AtfReportHtmlVocabulary.attribute(AtfReportHtmlVocabulary.Attribute.ROLE, "img"),
                AtfReportHtmlVocabulary.attribute(AtfReportHtmlVocabulary.Attribute.ARIA_LABEL,
                        action.resultLabel()),
                AtfReportHtmlVocabulary.attribute(AtfReportHtmlVocabulary.Attribute.TITLE,
                        action.resultLabel()))
                + action.resultEmoji() + close(AtfReportHtmlVocabulary.Tag.SPAN);
    }

    void renderAssertions(ExtentTest node, Collection<AtfAssertion> assertions) {
        if (assertions == null || assertions.isEmpty()) return;
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
        boolean failed = assertions.stream().anyMatch(row -> row != null && isFailure(row.resultStatus()));
        String markup = AtfReportMarkup.table("helm-evidence-table helm-validation-table", table).getMarkup();
        publishEvidence(node,
                open(AtfReportHtmlVocabulary.Tag.DIV,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ASSERTIONS),
                        AtfReportHtmlVocabulary.attribute(
                                AtfReportHtmlVocabulary.Attribute.EVIDENCE_STATUS, statusKey(failed)))
                        + markup + close(AtfReportHtmlVocabulary.Tag.DIV), failed);
    }

    private static String actionSummary(List<AtfUiActionRecord> actions, boolean failed) {
        if (!failed) return AtfReportLabels.actionSequence(actions.size());
        long failures = actions.stream().filter(ExtentUiEvidenceRenderer::isFailure).count();
        return ExtentReportNodeSupport.resultMarker(AtfReportStatus.FAIL)
                + " " + AtfReportLabels.failedActions(failures, actions.size());
    }

    private static boolean hasFailure(Collection<AtfUiActionRecord> actions) {
        return actions != null && actions.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(action -> isFailure(action.status()));
    }

    private static boolean isFailure(AtfUiActionRecord action) {
        return action != null && isFailure(action.status());
    }

    private static boolean isFailure(AtfReportStatus status) {
        return AtfReportStatus.FAIL == status;
    }

    private static String statusKey(boolean failed) {
        return (failed ? AtfReportStatus.FAIL : AtfReportStatus.PASS).displayValue();
    }

    private static String tableHeader(String... labels) {
        StringBuilder html = new StringBuilder(open(AtfReportHtmlVocabulary.Tag.THEAD))
                .append(open(AtfReportHtmlVocabulary.Tag.TR));
        for (String label : labels) {
            html.append(open(AtfReportHtmlVocabulary.Tag.TH))
                    .append(escapeHtml(label))
                    .append(close(AtfReportHtmlVocabulary.Tag.TH));
        }
        return html.append(close(AtfReportHtmlVocabulary.Tag.TR))
                .append(close(AtfReportHtmlVocabulary.Tag.THEAD)).toString();
    }

    private static String open(AtfReportHtmlVocabulary.Tag tag, String... attributes) {
        return AtfReportHtmlVocabulary.openTag(tag, attributes);
    }

    private static String close(AtfReportHtmlVocabulary.Tag tag) {
        return AtfReportHtmlVocabulary.closeTag(tag);
    }

    private static void publishEvidence(ExtentTest node, String markup, boolean failed) {
        Markup evidence = () -> markup;
        if (failed) {
            node.fail(evidence);
        } else {
            node.info(evidence);
        }
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
