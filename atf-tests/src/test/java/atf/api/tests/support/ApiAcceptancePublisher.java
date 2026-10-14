package atf.api.tests.support;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.validation.contract.ResponseContract;
import atf.api.validation.publishing.ResponseContractReporter;

import java.net.URI;

/** Publishes one API execution and its contract evidence into the report port. */
public final class ApiAcceptancePublisher {
    private static final ResponseContractReporter VALIDATOR = new ResponseContractReporter();

    private ApiAcceptancePublisher() { }

    public static void publish(AtfReportPorts.Phase phase, String title,
                               ApiExecutionResult execution, ResponseContract contract,
                               boolean warningMode, String requestJson) {
        var response = execution.response();
        var request = phase.startRequest(title, executionRequestMethod(title),
                URI.create(response.url()).getPath());
        request.detail("Duration", response.duration().toMillis() + " ms");
        request.detail("URL", response.url());
        request.detail("HTTP STATUS", Integer.toString(response.status()));
        request.detail("Content-Type", String.valueOf(response.header("Content-Type")));
        request.detail("Attempt", Integer.toString(execution.attempts()));
        if (requestJson != null) request.json("REQUEST JSON", requestJson);
        if (!response.body().isBlank()) request.json("RESPONSE JSON", response.body());
        atf.api.validation.evaluation.ValidationDecision decision;
        try {
            decision = VALIDATOR.validate(request, "Contract validation", response, contract, warningMode);
            request.complete(decision.status());
        } catch (RuntimeException failure) {
            request.complete(AtfReportStatus.FAIL);
            throw failure;
        } finally {
            request.close();
        }
        if (decision.status() == AtfReportStatus.WARNING) {
            throw new atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.WarningSignal(decision.message());
        }
    }

    private static String executionRequestMethod(String title) {
        return title.substring(0, title.indexOf(' '));
    }
}
