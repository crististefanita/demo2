package atf.api.reporting.ui.network;

import atf.api.reporting.evidence.ReportEvidenceTable;
import atf.api.reporting.evidence.markup.ReportEvidenceMarkup;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.AtfReportMarkup;
import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary;

import java.util.List;

/**
 * Renders the typed journey aggregate as one compact, nested disclosure.
 *
 * <p>The aggregate is an index, not a second request/response report. Each
 * section exposes only facts that help a reader navigate the canonical
 * exchange cards: authentication, correlation, transport, signatures and
 * diagnostics. Keeping this markup here gives Extent and fallback HTML the
 * same structure without moving HTML into the inspector model.</p>
 */
public final class ReportNetworkAggregateMarkup {
    private static final String NETWORK_AGGREGATE = "network-aggregate";
    private static final String NETWORK_AGGREGATE_SECTION = "network-aggregate-section";
    private static final String NETWORK_AGGREGATE_TITLE = "Network aggregate";
    private static final String AUTHENTICATION = "Authentication evidence";
    private static final String CORRELATION = "Scenario correlation";
    private static final String TRANSPORT = "Transport counters";
    private static final String SIGNATURES = "Unique request signatures";
    private static final String DIAGNOSTICS = "Technical diagnostics";

    private ReportNetworkAggregateMarkup() { }

    public static String disclosure(ReportNetworkInspector.JourneyAggregate aggregate) {
        if (aggregate == null) return "";
        StringBuilder sections = new StringBuilder();
        sections.append(section(AUTHENTICATION, aggregate.authentication().size(),
                ReportEvidenceTable.of(
                        List.of("METHOD", "SOURCE", "STATUS", "COUNT"),
                        aggregate.authentication().stream()
                                .map(item -> List.of(item.method(), item.source(),
                                        Integer.toString(item.status()), Integer.toString(item.count())))
                                .toList())));
        sections.append(section(CORRELATION, aggregate.correlation().size(),
                ReportEvidenceTable.of(
                        List.of("KEYWORD", "STEP", "EXCHANGES", "ASSERTIONS", "RESULT"),
                        aggregate.correlation().stream()
                                .map(item -> List.of(item.keyword(), item.title(),
                                        Integer.toString(item.exchangeCount()),
                                        Integer.toString(item.assertionCount()),
                                        item.passed() ? "PASS" : "FAIL"))
                                .toList())));
        sections.append(section(TRANSPORT, 1,
                ReportEvidenceTable.of(
                        List.of(AtfReportLabels.FIELD, AtfReportLabels.VALUE),
                        List.of(
                                List.of("OBSERVATIONS", Integer.toString(aggregate.transport().observations())),
                                List.of("SUCCESSFUL", Integer.toString(aggregate.transport().successful())),
                                List.of("FAILED", Integer.toString(aggregate.transport().failed())),
                                List.of("ATTEMPTS", Integer.toString(aggregate.transport().attempts())),
                                List.of("RETRIES", Integer.toString(aggregate.transport().retries())),
                                List.of("LIVE HTTP", Integer.toString(aggregate.transport().live())),
                                List.of("ACCEPTANCE MOCK", Integer.toString(aggregate.transport().mock())),
                                List.of("UNKNOWN", Integer.toString(aggregate.transport().unknown()))
                        ))));
        sections.append(section(SIGNATURES, aggregate.signatures().size(),
                ReportEvidenceTable.of(
                        List.of("SIGNATURE", "COUNT", "STATUSES"),
                        aggregate.signatures().stream()
                                .map(item -> List.of(item.signature(), Integer.toString(item.count()),
                                        item.statuses().toString()))
                                .toList())));
        sections.append(section(DIAGNOSTICS, aggregate.diagnostics().size(),
                ReportEvidenceTable.of(
                        List.of("METHOD", "ROUTE", "STATUS", "SIGNAL", "COUNT", "SOURCES"),
                        aggregate.diagnostics().stream()
                                .map(item -> List.of(item.method(), item.route(),
                                        Integer.toString(item.status()), item.signal(),
                                        Integer.toString(item.count()),
                                        item.sources().toString()))
                                .toList())));
        return ReportEvidenceMarkup.disclosure(NETWORK_AGGREGATE,
                NETWORK_AGGREGATE_TITLE + " · " + aggregate.observations() + " exchanges",
                sections.toString());
    }

    private static String section(String title, int count, ReportEvidenceTable table) {
        return ReportEvidenceMarkup.disclosure(NETWORK_AGGREGATE_SECTION,
                title + " · " + count,
                AtfReportMarkup.tableHtml(
                        AtfReportHtmlVocabulary.classList(
                                AtfReportHtmlVocabulary.CssClass.HELM_EVIDENCE_TABLE,
                                AtfReportHtmlVocabulary.CssClass.HELM_TRACE_TABLE), table));
    }

}
