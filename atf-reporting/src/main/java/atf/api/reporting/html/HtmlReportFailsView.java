package atf.api.reporting.html;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.markup.ReportEvidenceMarkup;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;
import atf.api.reporting.model.ReportExecutionModel;

import static atf.api.reporting.html.HtmlReportMarkupSupport.escape;

/** Renders the fallback Fails index from validation assertions only. */
public final class HtmlReportFailsView {
    String render(ReportExecutionModel snapshot) {
        var html = new StringBuilder();
        html.append(HtmlReportFallbackViewSupport.sectionStart(
                        AtfReportHtmlVocabulary.FallbackTab.FAILS))
                .append(HtmlReportFallbackViewSupport.heading(
                        AtfReportHtmlVocabulary.FallbackTab.FAILS))
                .append(AtfReportHtmlVocabulary.openTag(AtfReportHtmlVocabulary.Tag.DIV,
                        AtfReportHtmlVocabulary.classAttribute(
                                AtfReportHtmlVocabulary.CssClass.FAIL_LIST)));
        for (var failure : snapshot.failures().items()) {
            String status = ReportEvidenceMarkup.statusBadge(AtfReportStatus.FAIL);
            String content = status
                    + AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.STRONG,
                    escape(failure.assertionName()))
                    + AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.SPAN,
                    escape(failure.actual()));
            html.append(AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.A,
                    content,
                    AtfReportHtmlVocabulary.classAttribute(
                            AtfReportHtmlVocabulary.CssClass.FAIL_ROW),
                    AtfReportHtmlVocabulary.attribute(
                            AtfReportHtmlVocabulary.Attribute.HREF,
                            "#" + failure.scenarioId())));
        }
        if (snapshot.failures().isEmpty()) {
            html.append(AtfReportHtmlVocabulary.element(AtfReportHtmlVocabulary.Tag.P,
                    AtfReportLabels.NO_FAILED_ASSERTIONS,
                    AtfReportHtmlVocabulary.classAttribute(
                            AtfReportHtmlVocabulary.CssClass.EMPTY)));
        }
        return html.append(AtfReportHtmlVocabulary.closeTag(
                        AtfReportHtmlVocabulary.Tag.DIV))
                .append(HtmlReportFallbackViewSupport.sectionEnd())
                .toString();
    }
}
