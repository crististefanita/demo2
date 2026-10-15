package atf.api.tests.support;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.http.ApiRequest;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.validation.contract.ResponseContract;
import atf.api.validation.evaluation.ApiValidationException;
import atf.api.validation.publishing.ResponseContractReporter;

import java.net.URI;

/** Publishes one API execution and its contract evidence into the report port.
 *
 * <p>This is reusable scenario composition, not a JUnit test. Executable tests
 * remain in {@code src/test/java}; keeping the publisher here lets report-host
 * tests and resource scenarios share the same evidence rules.</p>
 */
public final class ApiAcceptancePublisher {
    private static final ResponseContractReporter VALIDATOR = new ResponseContractReporter();

    private ApiAcceptancePublisher() { }

    public static void publish(AtfReportPorts.Phase phase, String title,
                               ApiExecutionResult execution, ResponseContract contract,
                               boolean warningMode, String requestJson) {
        publish(phase, title, execution, contract, warningMode, requestJson, null);
    }

    /** Publishes request-owned metadata when a scenario builds a custom route. */
    public static void publish(AtfReportPorts.Phase phase, String title,
                               ApiExecutionResult execution, ResponseContract contract,
                               boolean warningMode, String requestJson, ApiRequest sourceRequest) {
        var decision = publishDecision(phase, title, execution, contract, warningMode, requestJson, sourceRequest);
        if (decision.status() == AtfReportStatus.WARNING) {
            throw new atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.WarningSignal(decision.message());
        }
        if (decision.status() == AtfReportStatus.FAIL) {
            throw new atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.FailureSignal(decision.message());
        }
    }

    /**
     * Publishes the same request-owned evidence as the acceptance scenario
     * path, but leaves the final decision to the calling JUnit test. This is
     * the bridge used by direct IntelliJ resource tests.
     */
    public static atf.api.validation.evaluation.ValidationDecision publishDecision(
            AtfReportPorts.Phase phase, String title,
            ApiExecutionResult execution, ResponseContract contract,
            boolean warningMode, String requestJson, ApiRequest sourceRequest) {
        var response = execution.response();
        String method = sourceRequest == null ? executionRequestMethod(title) : sourceRequest.method();
        String path = sourceRequest == null ? URI.create(response.url()).getPath() : sourceRequest.path();
        var request = phase.startRequest(title, method, path);
        request.execution(execution);
        request.detail("Duration", response.duration().toMillis() + " ms");
        request.detail("URL", response.url());
        request.detail("HTTP STATUS", Integer.toString(response.status()));
        request.detail("Content-Type", String.valueOf(response.header("Content-Type")));
        request.detail("Attempt", Integer.toString(execution.attempts()));
        if (sourceRequest != null) {
            request.detail("Auth profile", sourceRequest.authProfile());
            if (sourceRequest.route() != null) {
                request.detail("Auth type", sourceRequest.route().authType().name());
            }
            if (!sourceRequest.query().isEmpty()) {
                request.detail("Query params", sourceRequest.query().toString());
            }
            if (!sourceRequest.headers().isEmpty()) {
                request.detail("Request headers", sourceRequest.headers().toString());
            }
        }
        var responseHeaders = response.headers().entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase("Content-Type"))
                .toList();
        if (!responseHeaders.isEmpty()) {
            request.detail("Response headers", responseHeaders.toString());
        }
        String effectiveRequestJson = requestJson;
        if (effectiveRequestJson == null && sourceRequest != null && !sourceRequest.bodyText().isBlank()) {
            effectiveRequestJson = sourceRequest.bodyText();
        }
        if (effectiveRequestJson != null) request.json("REQUEST JSON", effectiveRequestJson);
        if (!response.body().isBlank()) request.json("RESPONSE JSON", response.body());
        atf.api.validation.evaluation.ValidationDecision decision;
        try {
            decision = VALIDATOR.validate(request, "Contract validation", response, contract,
                    warningMode, false);
            request.complete(decision.status());
        } catch (ApiValidationException | RuntimeException failure) {
            request.complete(AtfReportStatus.FAIL);
            throw failure;
        } finally {
            request.close();
        }
        return decision;
    }

    private static String executionRequestMethod(String title) {
        return title.substring(0, title.indexOf(' '));
    }
}
