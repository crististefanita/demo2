package atf.api.tests.support;

import atf.api.core.http.HttpHeader;
import atf.api.core.security.SensitiveDataRedactor;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.http.ApiRequest;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.validation.contract.ResponseContract;
import atf.api.validation.evaluation.ApiValidationException;
import atf.api.validation.evaluation.ValidationFailureEvidence;
import atf.api.validation.publishing.ResponseContractReporter;

import java.net.URI;
import java.util.function.Consumer;

/** Publishes one API execution and its contract evidence into the report port.
 *
 * <p>This is reusable scenario composition, not a JUnit test. Executable tests
 * remain in {@code src/test/java}; keeping the publisher here lets report-host
 * tests and resource scenarios share the same evidence rules.</p>
 */
public final class ApiAcceptancePublisher {
    private static final ResponseContractReporter VALIDATOR = new ResponseContractReporter();
    private static final SensitiveDataRedactor REDACTOR = new SensitiveDataRedactor();

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
        var decision = publishDecisionInternal(phase, title, execution, contract, warningMode, requestJson, sourceRequest, null);
        if (decision.status() == AtfReportStatus.WARNING) {
            throw new atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.WarningSignal(decision.message());
        }
        if (decision.status() == AtfReportStatus.FAIL) {
            throw failureSignal(decision, execution);
        }
    }

    /** Publishes a response and appends flow-level assertions inside that request's evidence. */
    public static void publish(AtfReportPorts.Phase phase, String title,
                               ApiExecutionResult execution, ResponseContract contract,
                               boolean warningMode, String requestJson, ApiRequest sourceRequest,
                               Consumer<AtfReportPorts.Request> flowEvidence) {
        var decision = publishDecisionInternal(phase, title, execution, contract, warningMode,
                requestJson, sourceRequest, flowEvidence);
        if (decision.status() == AtfReportStatus.WARNING) {
            throw new atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.WarningSignal(decision.message());
        }
        if (decision.status() == AtfReportStatus.FAIL) {
            throw failureSignal(decision, execution);
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
        return publishDecisionInternal(phase, title, execution, contract, warningMode,
                requestJson, sourceRequest, null);
    }

    public static atf.api.validation.evaluation.ValidationDecision publishDecision(
            AtfReportPorts.Phase phase, String title,
            ApiExecutionResult execution, ResponseContract contract,
            boolean warningMode, String requestJson, ApiRequest sourceRequest,
            Consumer<AtfReportPorts.Request> flowEvidence) {
        return publishDecisionInternal(phase, title, execution, contract, warningMode, requestJson,
                sourceRequest, flowEvidence);
    }

    /** Publishes a modeled negative outcome while retaining the normal JSON write contract. */
    public static atf.api.validation.evaluation.ValidationDecision publishNegativeDecision(
            AtfReportPorts.Phase phase, String title,
            ApiExecutionResult execution, ResponseContract contract,
            boolean warningMode, ApiRequest sourceRequest) {
        return publishDecisionInternal(phase, title, execution, contract, warningMode, null,
                sourceRequest, null);
    }

    private static atf.api.validation.evaluation.ValidationDecision publishDecisionInternal(
            AtfReportPorts.Phase phase, String title,
            ApiExecutionResult execution, ResponseContract contract,
            boolean warningMode, String requestJson, ApiRequest sourceRequest,
            Consumer<AtfReportPorts.Request> flowEvidence) {
        var response = execution.response();
        ApiRequest capturedRequest = sourceRequest != null ? sourceRequest : execution.request();
        String method = capturedRequest == null ? executionRequestMethod(title) : capturedRequest.method();
        String path = capturedRequest == null ? URI.create(response.url()).getPath() : capturedRequest.path();
        var request = phase.startRequest(title, method, path);
        request.execution(execution);
        detail(request, ApiEvidenceField.DURATION, response.duration().toMillis() + " ms");
        detail(request, ApiEvidenceField.URL, response.url());
        detail(request, ApiEvidenceField.HTTP_STATUS, Integer.toString(response.status()));
        detail(request, ApiEvidenceField.CONTENT_TYPE, String.valueOf(response.header(HttpHeader.CONTENT_TYPE.value())));
        detail(request, ApiEvidenceField.ATTEMPTS, Integer.toString(execution.attempts()));
        if (capturedRequest != null) {
            detail(request, ApiEvidenceField.AUTH_PROFILE, capturedRequest.authProfile());
            if (capturedRequest.route() != null) {
                detail(request, ApiEvidenceField.AUTH_TYPE, capturedRequest.route().authType().name());
            }
            if (!capturedRequest.queryParameters().isEmpty()) {
                detail(request, ApiEvidenceField.QUERY_PARAMS, capturedRequest.queryParameters().display());
            }
            if (!capturedRequest.headers().isEmpty()) {
                detail(request, ApiEvidenceField.REQUEST_HEADERS, redactHeaders(capturedRequest.headers().entrySet()));
            }
        }
        var responseHeaders = response.headers().entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase(HttpHeader.CONTENT_TYPE.value()))
                .toList();
        if (!responseHeaders.isEmpty()) {
            detail(request, ApiEvidenceField.RESPONSE_HEADERS, redactHeaders(responseHeaders));
        }
        String effectiveRequestJson = requestJson;
        if (effectiveRequestJson == null && capturedRequest != null && !capturedRequest.bodyText().isBlank()) {
            effectiveRequestJson = capturedRequest.bodyText();
        }
        ApiJsonEvidence.publish(request, effectiveRequestJson, response.body());
        var jsonContractViolations = ApiJsonEvidence.writeContractViolations(
                method, effectiveRequestJson, response.body());
        if (!jsonContractViolations.isEmpty()) {
            String message = "JSON request/response contract failed for " + method + " " + path
                    + ": " + String.join("; ", jsonContractViolations);
            detail(request, ApiEvidenceField.JSON_CONTRACT, message);
            request.complete(AtfReportStatus.FAIL);
            request.close();
            throw new atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.FailureSignal(message);
        }
        atf.api.validation.evaluation.ValidationDecision decision;
        try {
            decision = VALIDATOR.validate(request, "Contract validation", response, contract,
                    warningMode, false);
            if (flowEvidence != null) {
                flowEvidence.accept(request);
            }
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

    private static atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.FailureSignal failureSignal(
            atf.api.validation.evaluation.ValidationDecision decision,
            ApiExecutionResult execution) {
        ValidationFailureEvidence evidence = decision.failureEvidence();
        var details = new java.util.LinkedHashMap<>(evidence.details());
        details.put("Attempts", Integer.toString(execution.attempts()));
        return new atf.api.execution.lifecycle.ApiScenarioLifecycleRunner.FailureSignal(
                evidence.cause(), details);
    }

    private static void detail(AtfReportPorts.Request request, ApiEvidenceField field, String value) {
        request.detail(field.label(), value);
    }

    private static String redactHeaders(Iterable<java.util.Map.Entry<String, String>> headers) {
        java.util.List<String> values = new java.util.ArrayList<>();
        headers.forEach(entry -> values.add(entry.getKey() + "="
                + REDACTOR.redactField(entry.getKey(), entry.getValue())));
        return values.toString();
    }
}
