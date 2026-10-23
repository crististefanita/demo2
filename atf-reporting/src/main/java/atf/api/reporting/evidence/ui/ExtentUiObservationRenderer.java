package atf.api.reporting.evidence.ui;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.table.ReportTableViewport;
import atf.api.reporting.markup.ExtentReportNodeSupport;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.ui.model.AtfUiActionRecord;
import com.aventstack.extentreports.ExtentTest;

import java.util.List;

import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.close;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.escape;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.hasFailure;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.open;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.observationSummary;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.publish;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.resultSpan;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.statusKey;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.tableHeader;

/** Renders only reporter observation/checkpoint evidence. */
public final class ExtentUiObservationRenderer {
    private final ReportingSettings settings;

    public ExtentUiObservationRenderer(ReportingSettings settings) {
        this.settings = settings;
    }

    public void render(ExtentTest node, List<AtfUiActionRecord> observations) {
        if (observations == null || observations.isEmpty()) return;
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
                .append(escape(summary)).append(close(AtfReportHtmlVocabulary.Tag.SUMMARY))
                .append(ReportTableViewport.ACTIONS.open())
                .append(open(AtfReportHtmlVocabulary.Tag.TABLE,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_TABLE,
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_OBSERVATION_TABLE)))
                .append(tableHeader(AtfReportLabels.ACTION, AtfReportLabels.TARGET,
                        AtfReportLabels.EXPECTED, AtfReportLabels.OBSERVED,
                        AtfReportLabels.RESULT))
                .append(open(AtfReportHtmlVocabulary.Tag.TBODY));
        for (AtfUiActionRecord observation : observations) {
            html.append(ExtentUiEvidenceMarkup.actionRowStart()).append(escape(observation.operation()))
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(open(AtfReportHtmlVocabulary.Tag.SPAN,
                            AtfReportHtmlVocabulary.classAttribute(
                                    AtfReportHtmlVocabulary.CssClass.ATF_UI_ACTION_NUMBER)))
                    .append("#").append(observation.sequence())
                    .append(close(AtfReportHtmlVocabulary.Tag.SPAN))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD)).append(escape(observation.target()))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escape(ExtentReportNodeSupport.display(observation.expected(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD))
                    .append(escape(ExtentReportNodeSupport.display(observation.observed(), settings)))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(open(AtfReportHtmlVocabulary.Tag.TD)).append(resultSpan(observation))
                    .append(close(AtfReportHtmlVocabulary.Tag.TD))
                    .append(close(AtfReportHtmlVocabulary.Tag.TR));
        }
        publish(node, html.append(close(AtfReportHtmlVocabulary.Tag.TBODY))
                .append(close(AtfReportHtmlVocabulary.Tag.TABLE))
                .append(close(AtfReportHtmlVocabulary.Tag.DIV))
                .append(close(AtfReportHtmlVocabulary.Tag.DETAILS)).toString(), failed);
    }
}
