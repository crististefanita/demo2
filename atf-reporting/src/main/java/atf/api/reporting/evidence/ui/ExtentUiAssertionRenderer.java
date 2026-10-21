package atf.api.reporting.evidence.ui;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.AtfReportMarkup;
import atf.api.reporting.markup.ExtentReportNodeSupport;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.security.SensitiveDataRedactor;
import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;

import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.close;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.open;
import static atf.api.reporting.evidence.ui.ExtentUiEvidenceMarkup.publish;

/** Renders only assertion-table evidence for a UI checkpoint. */
public final class ExtentUiAssertionRenderer {
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;

    public ExtentUiAssertionRenderer(ReportingSettings settings, SensitiveDataRedactor redactor) {
        this.settings = settings;
        this.redactor = redactor;
    }

    public void render(ExtentTest node, Collection<AtfAssertion> assertions) {
        if (assertions == null || assertions.isEmpty()) return;
        var table = new String[assertions.size() + 1][4];
        table[0] = new String[]{AtfReportLabels.ASSERTION, AtfReportLabels.EXPECTED,
                AtfReportLabels.ACTUAL, AtfReportLabels.RESULT};
        int index = 0;
        for (AtfAssertion row : assertions) {
            table[++index] = new String[]{
                    redactor.redactText(row.name()),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.expected()), settings),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.actual()), settings),
                    ExtentReportNodeSupport.resultMarker(row.resultStatus())};
        }
        boolean failed = assertions.stream().anyMatch(row -> row != null
                && ExtentUiEvidenceMarkup.isFailure(row.resultStatus()));
        String markup = AtfReportMarkup.table(AtfReportHtmlVocabulary.classList(
                AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE,
                AtfReportHtmlVocabulary.CssClass.HELM_VALIDATION_TABLE), table).getMarkup();
        markup = open(AtfReportHtmlVocabulary.Tag.DIV,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_VALIDATION_TABLE_WRAP))
                + markup
                + close(AtfReportHtmlVocabulary.Tag.DIV);
        publish(node, open(AtfReportHtmlVocabulary.Tag.DIV,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.ATF_UI_ASSERTIONS),
                        AtfReportHtmlVocabulary.attribute(
                                AtfReportHtmlVocabulary.Attribute.EVIDENCE_STATUS,
                                ExtentUiEvidenceMarkup.statusKey(failed)))
                + markup + close(AtfReportHtmlVocabulary.Tag.DIV), failed);
    }
}
