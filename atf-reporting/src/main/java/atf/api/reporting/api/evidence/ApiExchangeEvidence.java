package atf.api.reporting.api.evidence;

import atf.api.reporting.api.ApiJsonEvidence;
import atf.api.reporting.api.ApiManualReplay;
import atf.api.reporting.api.ApiEvidenceField;
import atf.api.reporting.api.ReportRequestSignature;
import atf.api.reporting.api.auth.ApiAuthenticationEvidence;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.model.ReportJsonBody;
import atf.api.reporting.model.ReportJsonBodyFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** One renderer-neutral projection; consumers retain ownership of titles and verdicts. */
public final class ApiExchangeEvidence {
    private ApiExchangeEvidence() { }

    public static void publish(AtfReportPorts.Request node, ReportExecutionEvidence evidence) {
        Objects.requireNonNull(node, "node");
        project(evidence, node::detail, node::body);
        node.execution(evidence);
    }

    /** Narrow write seam for legacy ports; no producer dependency leaks into Reporting. */
    public static void publishFields(ReportExecutionEvidence evidence,
                                     BiConsumer<String, String> detail, BiConsumer<String, String> json) {
        project(evidence, detail, body -> json.accept(body.label(), body.body()));
    }

    private static void project(ReportExecutionEvidence evidence,
                                BiConsumer<String, String> detail, Consumer<ReportJsonBody> bodies) {
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
        body(detail, bodies, ReportJsonBody.Direction.REQUEST, request.bodyText(),
                request.bodyAvailability(), header(request.headers(), "content-type"));
        detail.accept(ApiEvidenceField.HTTP_STATUS.label(), response.statusLabel());
        detail.accept("response.content.type", header(response.headers(), "content-type"));
        String headers = ApiManualReplay.relevantResponseHeaders(response.headers());
        detail.accept("response.headers", headers);
        body(detail, bodies, ReportJsonBody.Direction.RESPONSE, response.body(),
                response.bodyAvailability(), header(response.headers(), "content-type"));
    }

    private static void body(BiConsumer<String, String> detail, Consumer<ReportJsonBody> bodies,
                             ReportJsonBody.Direction direction, String body,
                             ReportBodyAvailability availability, String contentType) {
        String side = direction.name().toLowerCase(Locale.ROOT);
        detail.accept(side + ".body.capture", switch (availability) {
            case AVAILABLE -> "Captured";
            case ABSENT -> "Absent — confirmed no HTTP body";
            case NOT_CAPTURED -> "Not captured — body evidence unavailable"
                    + (body.isBlank() ? "" : ": " + body);
        });
        if (availability != ReportBodyAvailability.NOT_CAPTURED) {
            ReportJsonBody.Format format = format(body, availability, contentType);
            String suffix = switch (format) {
                case JSON -> "JSON";
                case FORM -> "FORM BODY";
                case TEXT -> "BODY";
            };
            bodies.accept(new ReportJsonBody(direction.name() + " " + suffix,
                    availability == ReportBodyAvailability.ABSENT ? ApiJsonEvidence.EMPTY_JSON : body,
                    direction, format));
        }
    }

    private static ReportJsonBody.Format format(String body, ReportBodyAvailability availability,
                                                String contentType) {
        // Preserve the explicit JSON null convention for confirmed body-less exchanges.
        if (availability == ReportBodyAvailability.ABSENT) return ReportJsonBody.Format.JSON;
        String mime = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (mime.equals("application/x-www-form-urlencoded") || mime.equals("multipart/form-data")) {
            return ReportJsonBody.Format.FORM;
        }
        return ReportJsonBodyFormatter.isJsonDocument(body)
                ? ReportJsonBody.Format.JSON : ReportJsonBody.Format.TEXT;
    }

    private static String header(Map<String, String> headers, String name) {
        return headers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue).findFirst().orElse("");
    }
}
