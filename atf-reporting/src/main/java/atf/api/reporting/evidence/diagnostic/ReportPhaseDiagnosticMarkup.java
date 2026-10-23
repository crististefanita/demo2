package atf.api.reporting.evidence.diagnostic;

import atf.api.reporting.evidence.ReportEvidenceTable;
import atf.api.reporting.evidence.markup.ReportEvidenceMarkup;
import atf.api.reporting.markup.AtfReportMarkup;
import atf.api.reporting.model.diagnostic.ReportPhaseDiagnostic;
import atf.api.reporting.security.SensitiveDataRedactor;

import java.util.List;

import static atf.api.reporting.markup.AtfReportLabels.EXECUTION_DIAGNOSTICS;
import static atf.api.reporting.markup.AtfReportLabels.FIELD;
import static atf.api.reporting.markup.AtfReportLabels.VALUE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.PHASE_NAME;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass.PHASE_DIAGNOSTICS;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Tag.DIV;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.attribute;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.closeTag;
import static atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.openTag;

/** Shared scenario-owned diagnostic projection, never an HTTP response or a contract assertion. */
public final class ReportPhaseDiagnosticMarkup {
    private ReportPhaseDiagnosticMarkup() { }

    public static String render(String phase, List<ReportPhaseDiagnostic> diagnostics,
                                SensitiveDataRedactor redactor) {
        if (diagnostics.isEmpty()) return "";
        List<List<String>> rows = diagnostics.stream()
                .map(diagnostic -> List.of(diagnostic.kind().label(),
                        redactor.redactField(diagnostic.kind().label(), diagnostic.message())))
                .toList();
        String table = AtfReportMarkup.tableHtml(HELM_EVIDENCE_TABLE.value(),
                ReportEvidenceTable.of(List.of(FIELD, VALUE), rows));
        String body = openTag(DIV, attribute(PHASE_NAME, phase)) + table + closeTag(DIV);
        return ReportEvidenceMarkup.disclosure(PHASE_DIAGNOSTICS.value(),
                String.format("%s — %s", EXECUTION_DIAGNOSTICS, phase), body);
    }
}
