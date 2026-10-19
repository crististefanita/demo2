package atf.api.reporting.api;
import atf.api.reporting.api.ReportJsonBodyPolicy;
import atf.api.reporting.contract.AtfReportPorts;
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
    public static final String EMPTY_JSON = ReportJsonBodyPolicy.NULL_DOCUMENT;

    private ApiJsonEvidence() { }

    public static void publish(AtfReportPorts.Request request,
                               String requestBody,
                               String responseBody) {
        request.json(REQUEST_LABEL, ReportJsonBodyPolicy.display(requestBody));
        request.json(RESPONSE_LABEL, ReportJsonBodyPolicy.display(responseBody));
    }

    /** Test/consumer migration overload for the former core request port. */
    public static void publish(Object request, String requestBody, String responseBody) {
        try {
            var json = request.getClass().getMethod("json", String.class, String.class);
            json.trySetAccessible();
            json.invoke(request, REQUEST_LABEL, ReportJsonBodyPolicy.display(requestBody));
            json.invoke(request, RESPONSE_LABEL, ReportJsonBodyPolicy.display(responseBody));
        } catch (ReflectiveOperationException error) {
            throw new IllegalArgumentException("request does not expose the reporting json port", error);
        }
    }

    public static String display(String body) {
        return ReportJsonBodyPolicy.display(body);
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
        return ReportJsonBodyPolicy.appliesTo(method)
                ? ReportJsonBodyPolicy.validateWriteExchange(requestBody, responseBody)
                : List.of();
    }
}
