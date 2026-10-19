package atf.api.reporting.api;
import atf.api.core.http.response.JsonBodyPolicy;
import atf.api.core.reporting.contract.AtfReportPorts;
import java.util.List;

/**
 * Publishes the two JSON payload channels owned by every API request.
 *
 * <p>A missing HTTP body is represented explicitly as JSON {@code null}. This
 * keeps GET/204/304 evidence auditable without inventing an object or silently
 * dropping one side of the exchange.</p>
 */
public final class ApiJsonEvidence {
    public static final String REQUEST_LABEL = "REQUEST JSON";
    public static final String RESPONSE_LABEL = "RESPONSE JSON";
    public static final String EMPTY_JSON = JsonBodyPolicy.NULL_DOCUMENT;

    private ApiJsonEvidence() { }

    public static void publish(AtfReportPorts.Request request,
                               String requestBody,
                               String responseBody) {
        request.json(REQUEST_LABEL, JsonBodyPolicy.display(requestBody));
        request.json(RESPONSE_LABEL, JsonBodyPolicy.display(responseBody));
    }

    public static String display(String body) {
        return JsonBodyPolicy.display(body);
    }

    public static boolean hasBody(String body) {
        return body != null && !body.isBlank();
    }

    /**
     * Returns the write-side exchange violations that must be visible in the
     * report contract. GET/DELETE and body-less HTTP responses keep the
     * explicit {@code null} evidence convention; POST/PUT are stricter.
     */
    public static List<String> writeContractViolations(String method,
                                                        String requestBody,
                                                        String responseBody) {
        return JsonBodyPolicy.appliesTo(method)
                ? JsonBodyPolicy.validateWriteExchange(requestBody, responseBody)
                : List.of();
    }
}
