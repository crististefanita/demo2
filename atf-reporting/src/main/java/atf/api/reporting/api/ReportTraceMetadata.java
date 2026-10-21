package atf.api.reporting.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

/**
 * Renderer-neutral causal metadata shared by API evidence projections.
 * Empty optional values mean that the producer did not know the value; they
 * must never be replaced with a guessed title or a synthetic business fact.
 */
public record ReportTraceMetadata(
        String eventId,
        long sequence,
        String phase,
        String role,
        String stepId,
        String initiator,
        String correlationKey,
        String requestSignature,
        String requestBodyDigest,
        String responseBodyDigest,
        String evidenceSource,
        String expected,
        String observed) {

    public ReportTraceMetadata {
        eventId = normalize(eventId);
        if (sequence < 0) throw new IllegalArgumentException("sequence must not be negative");
        phase = normalize(phase);
        role = normalize(role);
        stepId = normalize(stepId);
        initiator = normalize(initiator);
        correlationKey = normalize(correlationKey);
        requestSignature = normalize(requestSignature);
        requestBodyDigest = normalize(requestBodyDigest);
        responseBodyDigest = normalize(responseBodyDigest);
        evidenceSource = normalize(evidenceSource);
        expected = normalize(expected);
        observed = normalize(observed);
    }

    public static ReportTraceMetadata empty() {
        return new ReportTraceMetadata("", 0, "", "", "", "", "", "", "", "", "", "", "");
    }

    public static ReportTraceMetadata from(ReportRequest request) {
        if (request == null) return empty();
        return new ReportTraceMetadata("", 0, "", "", "", "",
                ReportRequestSignature.correlationKey(request),
                ReportRequestSignature.forRequest(request),
                ReportRequestSignature.bodyDigest(request.bodyText()), "", "", "", "");
    }

    public boolean present() {
        return !eventId.isBlank() || sequence > 0 || !phase.isBlank() || !role.isBlank()
                || !stepId.isBlank() || !initiator.isBlank() || !correlationKey.isBlank()
                || !requestSignature.isBlank();
    }

    public Map<String, String> fields() {
        Map<String, String> fields = new LinkedHashMap<>();
        add(fields, "EVENT ID", eventId);
        add(fields, "SEQUENCE", sequence > 0 ? Long.toString(sequence) : "");
        add(fields, "PHASE", phase);
        add(fields, "ROLE", role);
        add(fields, "STEP ID", stepId);
        add(fields, "INITIATOR", initiator);
        add(fields, "CORRELATION", correlationKey);
        add(fields, "REQUEST SIGNATURE", ReportRequestSignature.displaySignature(requestSignature));
        add(fields, "REQUEST BODY DIGEST", ReportRequestSignature.displayDigest(requestBodyDigest));
        add(fields, "RESPONSE BODY DIGEST", ReportRequestSignature.displayDigest(responseBodyDigest));
        add(fields, "SOURCE", evidenceSource);
        add(fields, "EXPECTED", expected);
        add(fields, "OBSERVED", observed);
        return Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }

    public ReportTraceMetadata withCapture(String eventId, long sequence, String phase,
                                            String role, String stepId, String initiator,
                                            String evidenceSource, String responseBodyDigest,
                                            String expected, String observed) {
        return new ReportTraceMetadata(eventId, sequence, phase, role, stepId, initiator,
                correlationKey, requestSignature, requestBodyDigest, responseBodyDigest,
                evidenceSource, expected, observed);
    }

    private static void add(Map<String, String> fields, String name, String value) {
        if (value != null && !value.isBlank()) fields.put(name, value);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
