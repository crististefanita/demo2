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
            node.detail("auth", request.authProfile());
            node.detail("query", request.query().toString());
            if (!request.bodyText().isBlank()) {
                node.json("REQUEST JSON", PracticeShopApiEvidenceMapper.redactJson(request.bodyText()));
            }
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
}
