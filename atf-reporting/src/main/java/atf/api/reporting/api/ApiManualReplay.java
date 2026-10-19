package atf.api.reporting.api;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.MediaTypes;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.api.core.security.SensitiveDataRedactor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Builds truthful, safe manual replay evidence for one API exchange. */
public final class ApiManualReplay {
    private static final SensitiveDataRedactor REDACTOR = new SensitiveDataRedactor();

    private ApiManualReplay() { }

    public static String describe(ApiTransportProvenance provenance, ApiRequest request,
                                  String url, String requestBody) {
        Objects.requireNonNull(provenance, "provenance");
        if (!provenance.networkExecuted()) {
            return provenance == ApiTransportProvenance.ACCEPTANCE_MOCK
                    ? "NOT EXECUTABLE — ACCEPTANCE / MOCK; network was not executed. "
                    + "The URL is route metadata only; configure an explicit live base URI before replay."
                    : "NOT VERIFIED — transport provenance is unavailable. "
                    + "Declare acceptance/mock or explicit live transport before replay.";
        }
        if (request == null || url == null || url.isBlank()) {
            return "NOT REPLAYABLE — live exchange lacks the request URL or method.";
        }
        return curl(request, url, requestBody);
    }

    /** Returns the effective non-secret headers a manual reader needs to reproduce the call. */
    public static String redactedHeaders(ApiRequest request) {
        if (request == null) return "[]";
        return effectiveHeaders(request).entrySet().stream()
                .map(entry -> entry.getKey() + "="
                        + REDACTOR.redactField(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String curl(ApiRequest request, String url, String requestBody) {
        StringBuilder command = new StringBuilder("curl --request ")
                .append(shellQuote(request.method()))
                .append(" --url ").append(shellQuote(REDACTOR.redactText(url)));
        effectiveHeaders(request).forEach((name, value) -> command.append(" --header ")
                .append(shellQuote(name + ": " + REDACTOR.redactField(name, value))));
        if (requestBody != null && !requestBody.isBlank()) {
            command.append(" --data-raw ")
                    .append(shellQuote(REDACTOR.redactJson(requestBody)));
        }
        if (!"none".equalsIgnoreCase(request.authProfile())) {
            command.append(" ; # auth profile '").append(request.authProfile())
                    .append("' is configured separately; credentials are redacted");
        }
        return command.toString();
    }

    private static Map<String, String> effectiveHeaders(ApiRequest request) {
        var headers = new LinkedHashMap<String, String>();
        headers.put(HttpHeader.ACCEPT.value(), MediaTypes.APPLICATION_JSON);
        headers.put(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON);
        request.headers().forEach(headers::put);
        return Map.copyOf(headers);
    }

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }
}
