package atf.ui.practiceshop.support;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;

import java.util.List;

/** Publishes one Practice Shop API execution as a complete Extent evidence node. */
public final class PracticeShopApiReportPublisher {
    private PracticeShopApiReportPublisher() { }

    public static void publish(AtfReportPorts.Phase phase, ApiExecutionResult execution) {
        var request = execution.request();
        AtfReportPorts.Request node = phase.startRequest(
                request.method() + " " + request.path(), request.method(), request.path());
        try {
            node.detail("url", execution.response().url());
            node.detail("auth.type", request.authProfile());
            node.detail("query.params", request.query().toString());
            node.detail("request.headers", PracticeShopApiEvidenceMapper.redactHeaders(request.headers()).toString());
            if (requiresJsonRequest(request.method()) && request.bodyText().isBlank()) {
                throw new IllegalStateException("Practice Shop write request has no JSON body: "
                        + request.method() + " " + request.path());
            }
            if (!request.bodyText().isBlank()) {
                node.json("REQUEST JSON", PracticeShopApiEvidenceMapper.redactJson(request.bodyText()));
            }
            node.detail("response.content.type", execution.response().header("content-type"));
            node.detail("response.headers", PracticeShopApiEvidenceMapper.redactHeaders(
                    execution.response().headers()).toString());
            node.json("RESPONSE JSON", PracticeShopApiEvidenceMapper.redactJson(execution.response().body()));
            node.execution(PracticeShopApiEvidenceMapper.toExecution(execution));
            AtfReportPorts.Validation validation = node.startValidation("Contract validation");
            try {
                String contentType = String.valueOf(execution.response().header("content-type"));
                validation.assertions(List.of(
                        new AtfAssertion("HTTP status", "2xx", Integer.toString(execution.response().status()),
                                execution.response().status() >= 200 && execution.response().status() < 300),
                        new AtfAssertion("Content-Type", "application/json", contentType,
                                contentType.toLowerCase().contains("application/json"))));
                validation.complete(AtfReportStatus.PASS);
            } finally {
                validation.close();
            }
            node.complete(AtfReportStatus.PASS);
        } finally {
            node.close();
        }
    }

    private static boolean requiresJsonRequest(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method);
    }
}
