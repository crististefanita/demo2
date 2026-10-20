package atf.api.reporting.evidence.ui;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.markup.ExtentReportAssets;
import atf.api.reporting.markup.ExtentReportNodeSupport;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportScreenshot;
import atf.api.reporting.ui.AtfUiActionRecord;
import com.aventstack.extentreports.ExtentTest;

import java.util.List;

import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.actionRowStart;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.actionSummary;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.close;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.escape;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.hasFailure;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.open;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.publish;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.resultSpan;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.statusKey;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.tableHeader;

/** Renders only Playwright action-sequence evidence. */
public final class ExtentUiActionRenderer {
    private final ReportingSettings settings;

    public ExtentUiActionRenderer(ReportingSettings settings) {
        this.settings = settings;
    }

    public void render(ExtentTest node, List<AtfUiActionRecord> actions,
                       List<ReportScreenshot> screenshots) {
        if (actions == null || actions.isEmpty()) return;
        List<ReportScreenshot> distinctScreenshots = ExtentReportAssets.distinctScreenshotModels(screenshots);
        if (distinctScreenshots.size() != 1) {
            throw new IllegalStateException(
                    "A UI action sequence requires exactly one Base64 screenshot; found "
                            + distinctScreenshots.size());
        }
        StringBuilder html = actionTable(actions)
                .append(close(AtfReportHtmlVocabulary.Tag.TBODY))
                .append(close(AtfReportHtmlVocabulary.Tag.TABLE));
        for (ReportScreenshot screenshot : distinctScreenshots) {
            html.append(open(AtfReportHtmlVocabulary.Tag.DIV,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SCREENSHOT)))
                    .append(ExtentReportAssets.base64ScreenshotLink(
                            AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_SCREENSHOT_LINK.value(),
                            screenshot.title(), screenshot.base64Png()))
                    .append(close(AtfReportHtmlVocabulary.Tag.DIV));
        }
        publish(node, html.append(close(AtfReportHtmlVocabulary.Tag.DIV))
                .append(close(AtfReportHtmlVocabulary.Tag.DETAILS)).toString(), hasFailure(actions));
    }

    public void renderAsMedia(ExtentTest node, List<AtfUiActionRecord> actions) {
        if (actions == null || actions.isEmpty()) return;
        publish(node, actionTable(actions)
                .append(close(AtfReportHtmlVocabulary.Tag.TBODY))
                .append(close(AtfReportHtmlVocabulary.Tag.TABLE))
                .append(close(AtfReportHtmlVocabulary.Tag.DIV))
                .append(close(AtfReportHtmlVocabulary.Tag.DETAILS)).toString(), hasFailure(actions));
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
            html.append(actionRowStart()).append(escape(action.operation()))
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(open(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_NUMBER)))
                    .append("#").append(action.sequence())
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD)).append(escape(action.target()))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escape(ExtentReportNodeSupport.display(action.expected(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escape(ExtentReportNodeSupport.display(action.observed(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_TIME)))
                    .append(escape(action.durationText()))
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD)).append(resultSpan(action))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(close(AtfReportHtmlVocabulary.Tag.TR));
        }
        return html;
    }
}
