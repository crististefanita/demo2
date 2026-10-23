package atf.api.reporting.api.evidence;

import atf.api.reporting.api.ApiJsonEvidence;
import atf.api.reporting.api.ApiManualReplay;
import atf.api.reporting.api.ApiEvidenceField;
import atf.api.reporting.api.ReportRequestSignature;
import atf.api.reporting.api.auth.ApiAuthenticationEvidence;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.model.ReportExecutionEvidence;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/** One renderer-neutral projection; consumers retain ownership of titles and verdicts. */
public final class ApiExchangeEvidence {
    private ApiExchangeEvidence() { }

    public static void publish(AtfReportPorts.Request node, ReportExecutionEvidence evidence) {
        Objects.requireNonNull(node, "node");
        publishFields(evidence, node::detail, node::json);
        node.execution(evidence);
    }

    /** Narrow write seam for legacy ports; no producer dependency leaks into Reporting. */
    public static void publishFields(ReportExecutionEvidence evidence,
                                     BiConsumer<String, String> detail, BiConsumer<String, String> json) {
        var request = Objects.requireNonNull(evidence.request(), "captured request");
        var response = Objects.requireNonNull(evidence.response(), "captured response");
        detail.accept("method", request.method());
        detail.accept("path", request.path());
        detail.accept("url", response.url());
        detail.accept("request.signature", ReportRequestSignature.displaySignature(
                ReportRequestSignature.forRequest(request)));
        ApiAuthenticationEvidence.publishTo(detail, request);
        if (!request.query().isEmpty()) detail.accept("query.params", request.query().toString());
        detail.accept("request.headers", ApiManualReplay.redactedHeaders(request));
        detail.accept("transport.provenance", evidence.provenance().reportLabel());
        detail.accept("manual.replay", ApiManualReplay.describe(evidence.provenance(), request,
                response.url(), request.bodyText()));
        body(detail, json, true, request.bodyText(), request.bodyAvailability());
        detail.accept(ApiEvidenceField.HTTP_STATUS.label(), response.statusLabel());
        detail.accept("response.content.type", header(response.headers(), "content-type"));
        String headers = ApiManualReplay.relevantResponseHeaders(response.headers());
        detail.accept("response.headers", headers);
        body(detail, json, false, response.body(), response.bodyAvailability());
    }

    private static void body(BiConsumer<String, String> detail, BiConsumer<String, String> json,
                             boolean request, String body,
                             ReportBodyAvailability availability) {
        String side = request ? "request" : "response";
        detail.accept(side + ".body.capture", switch (availability) {
            case AVAILABLE -> "Captured";
            case ABSENT -> "Absent — confirmed no HTTP body";
            case NOT_CAPTURED -> "Not captured — body evidence unavailable"
                    + (body.isBlank() ? "" : ": " + body);
        });
        if (availability != ReportBodyAvailability.NOT_CAPTURED) {
            json.accept(request ? ApiJsonEvidence.REQUEST_LABEL : ApiJsonEvidence.RESPONSE_LABEL,
                    ApiJsonEvidence.display(body));
        }
    }

    private static String header(Map<String, String> headers, String name) {
        return headers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue).findFirst().orElse("");
    }
}
