package atf.api.reporting.ui;

import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.model.ReportField;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Builds the compact, renderer-neutral network summary for a UI journey.
 * Detailed request/response bodies remain owned by their UI checkpoints; this
 * projection only answers which exchange happened, when, and why.
 */
public final class ReportNetworkInspector {
    private ReportNetworkInspector() {
    }

    public static List<ReportField> fields(Collection<ReportExecutionEvidence> evidence) {
        List<ReportExecutionEvidence> exchanges = evidence == null
                ? List.of()
                : evidence.stream().filter(Objects::nonNull).toList();
        Set<String> signatures = new LinkedHashSet<>();
        List<ReportField> fields = new java.util.ArrayList<>();
        fields.add(ReportField.of("network.observations", Integer.toString(exchanges.size())));
        for (ReportExecutionEvidence exchange : exchanges) {
            if (exchange.request() != null) {
                signatures.add(signature(exchange));
            }
        }
        fields.add(ReportField.of("network.unique.signatures", Integer.toString(signatures.size())));
        for (int index = 0; index < exchanges.size(); index++) {
            fields.add(ReportField.of("network.exchange." + (index + 1), summary(exchanges.get(index))));
        }
        return List.copyOf(fields);
    }

    public static String summary(ReportExecutionEvidence evidence) {
        if (evidence == null || evidence.request() == null || evidence.response() == null) {
            return "unavailable exchange";
        }
        var request = evidence.request();
        var response = evidence.response();
        var trace = evidence.trace();
        String event = trace != null && !trace.eventId().isBlank()
                ? trace.eventId() : "exchange";
        String phase = trace == null || trace.phase().isBlank() ? "unknown" : trace.phase();
        String contentType = header(response.headers(), "content-type");
        String transport = evidence.provenance().reportLabel();
        String operation = request.method() + " " + request.path() + " → "
                + response.status() + (contentType.isBlank() ? "" : " " + contentType);
        String signature = signature(evidence);
        String source = trace == null || trace.evidenceSource().isBlank()
                ? "unknown" : trace.evidenceSource();
        return event + " | phase=" + phase + " | " + operation
                + " | source=" + source + " | transport=" + transport
                + " | signature=" + signature;
    }

    private static String signature(ReportExecutionEvidence evidence) {
        if (evidence == null || evidence.request() == null) return "";
        if (evidence.trace() != null && !evidence.trace().requestSignature().isBlank()) {
            return evidence.trace().requestSignature();
        }
        return atf.api.reporting.api.ReportRequestSignature.forRequest(evidence.request());
    }

    private static String header(java.util.Map<String, String> headers, String name) {
        if (headers == null) return "";
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(java.util.Map.Entry::getValue)
                .findFirst().orElse("");
    }
}
