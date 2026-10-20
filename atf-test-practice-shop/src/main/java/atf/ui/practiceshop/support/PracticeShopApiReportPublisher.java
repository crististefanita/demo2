package atf.ui.practiceshop.support;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.api.core.http.transport.ApiTransportProvenance;
import atf.api.core.reporting.console.AtfConsoleEvent;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.ui.practiceshop.api.PracticeShopApiOperation;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;

import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/** Publishes one Practice Shop API execution as a complete Extent evidence node. */
public final class PracticeShopApiReportPublisher {
    private PracticeShopApiReportPublisher() { }

    /**
     * Owns the standalone report lifecycle so component tests only describe
     * the API evidence they want to publish. Close/flush ordering stays here,
     * beside the publisher that owns the report nodes.
     */
    public static Path publishStandalone(Path reportPath,
                                         String testClass,
                                         String testPackage,
                                         String scenarioName,
                                         List<String> tags,
                                         List<ApiExecutionResult> executions,
                                         AtfConsoleSink console) {
        return publishStandalone(reportPath, testClass, testPackage, scenarioName, tags,
                executions, List.of(), false, console);
    }

    /**
     * Publishes the authoritative business artifact. It is deliberately a
     * separate entry point: deterministic component evidence must never be
     * mistaken for a live business verdict.
     */
    public static Path publishLiveStandalone(Path reportPath,
                                             String testClass,
                                             String testPackage,
                                             String scenarioName,
                                             List<String> tags,
                                             List<ApiExecutionResult> executions,
                                             List<PracticeShopApiReportValidation> validations,
                                             AtfConsoleSink console) {
        return publishStandalone(reportPath, testClass, testPackage, scenarioName, tags,
                executions, validations, true, console);
    }

    private static Path publishStandalone(Path reportPath,
                                         String testClass,
                                         String testPackage,
                                         String scenarioName,
                                         List<String> tags,
                                         List<ApiExecutionResult> executions,
                                         List<PracticeShopApiReportValidation> validations,
                                         boolean requireLive,
                                         AtfConsoleSink console) {
        Objects.requireNonNull(reportPath, "reportPath");
        Objects.requireNonNull(executions, "executions");
        Objects.requireNonNull(console, "console");
        try (ExtentReportSession session = new ExtentReportSession(reportPath)) {
            var scenario = session.startScenario(testClass, testPackage, scenarioName, tags);
            var phase = scenario.startPhase("TEST BODY");
            boolean passed = true;
            try {
                long sequence = 0;
                for (ApiExecutionResult execution : executions) {
                    passed &= publish(phase, execution, ++sequence, console) == AtfReportStatus.PASS;
                }
                publishTraceSummary(phase, executions);
                if (requireLive) {
                    passed &= publishLiveProvenance(phase, executions) == AtfReportStatus.PASS;
                }
                for (PracticeShopApiReportValidation validation : validations) {
                    passed &= publishValidation(phase, validation) == AtfReportStatus.PASS;
                }
                AtfReportStatus status = passed ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
                phase.complete(status);
                scenario.complete(status);
            } finally {
                phase.close();
                scenario.close();
            }
            return session.reportPath();
        }
    }

    public static AtfReportStatus publish(AtfReportPorts.Phase phase, ApiExecutionResult execution) {
        return publish(phase, execution, AtfConsoleSink.noOp());
    }

    /**
     * Publishes API evidence and optionally mirrors the exchange lifecycle to
     * the configured ATF console. Console output is diagnostic only and never
     * changes the report verdict.
     */
    public static AtfReportStatus publish(AtfReportPorts.Phase phase, ApiExecutionResult execution,
                               AtfConsoleSink console) {
        return publish(phase, execution, 0, console);
    }

    private static AtfReportStatus publish(AtfReportPorts.Phase phase, ApiExecutionResult execution,
                                           long sequence, AtfConsoleSink console) {
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(execution, "execution");
        Objects.requireNonNull(console, "console");
        var request = execution.request();
        var response = execution.response();
        var responseBody = response.body();
        var requestBody = request.bodyText();
        var redactedRequestBody = PracticeShopApiEvidenceMapper.redactJson(requestBody);
        var redactedResponseBody = PracticeShopApiEvidenceMapper.redactJson(responseBody);
        var responseContentType = String.valueOf(response.header("content-type"));
        PracticeShopApiOperation operation = PracticeShopApiOperation.resolve(
                request.method(), request.path());
        String requestName = PracticeShopApiOperation.reportTitle(request.method(), request.path());
        String reportMethod = operation == PracticeShopApiOperation.UNKNOWN
                ? request.method() : operation.reportMethod();
        long requestStartedAt = System.nanoTime();
        consolePublish(console, AtfConsoleEvent.started(AtfConsoleEvent.Scope.REQUEST, requestName));
        consolePublish(console, AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                "auth.type", request.authProfile()));
        consolePublish(console, AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                "transport", execution.provenance().reportLabel()));
        AtfReportPorts.Request node = phase.startRequest(
                requestName, reportMethod, request.path());
        AtfReportStatus requestStatus = AtfReportStatus.FAIL;
        boolean validationStarted = false;
        boolean validationCompleted = false;
        try {
            node.detail("operation.kind", operation.name());
            if (!reportMethod.equalsIgnoreCase(request.method())) {
                node.detail("observed.method", request.method());
            }
            if (!operation.routeTemplate().isBlank()) {
                node.detail("route.template", operation.routeTemplate());
            }
            node.detail("url", response.url());
            node.detail("auth.type", request.authProfile());
            node.detail("transport.provenance", execution.provenance().reportLabel());
            if (sequence > 0) {
                var traced = PracticeShopApiExecutionExchange.from(execution, sequence,
                        phaseFor(request.path()));
                node.detail("request.signature", traced.signature());
                node.detail("capture.sequence", Long.toString(traced.sequence()));
                node.detail("capture.phase", traced.phase());
            }
            node.detail("query.params", request.query().toString());
            node.detail("request.headers", PracticeShopApiEvidenceMapper.redactHeaders(request.headers()).toString());
            if (!requestBody.isBlank()) {
                node.json("REQUEST JSON", redactedRequestBody);
            }
            node.detail("response.content.type", responseContentType);
            node.detail("response.headers", PracticeShopApiEvidenceMapper.redactHeaders(
                    response.headers()).toString());
            node.json("RESPONSE JSON", redactedResponseBody);
            node.execution(PracticeShopApiEvidenceMapper.toExecution(execution, sequence,
                    phaseFor(request.path()), "BusinessFlow", phaseFor(request.path()),
                    requestName, execution.provenance().reportLabel(),
                    "2xx response with JSON contract", "status=" + response.status()));
            AtfReportPorts.Validation validation = node.startValidation("Contract validation");
            validationStarted = true;
            long validationStartedAt = System.nanoTime();
            consolePublish(console, AtfConsoleEvent.started(AtfConsoleEvent.Scope.VALIDATION,
                    "Contract validation"));
            try {
                boolean responseIsEmpty = responseBody == null || responseBody.isBlank();
                boolean noContent = PracticeShopApiHttpStatus.NO_CONTENT.matches(response.status());
                boolean requestNeedsJson = requiresJsonRequest(request.method());
                List<AtfAssertion> assertions = List.of(
                        new AtfAssertion("HTTP status", "2xx", Integer.toString(response.status()),
                                PracticeShopApiHttpStatus.isSuccess(response.status())),
                        new AtfAssertion("Request JSON", requestNeedsJson
                                ? "non-empty parseable JSON" : "not required for " + request.method(),
                                requestBody.isBlank() ? "empty" : redactedRequestBody,
                                !requestNeedsJson || PracticeShopJsonEvidence.isJson(requestBody)),
                        new AtfAssertion("Content-Type", MediaTypes.APPLICATION_JSON, responseContentType,
                                isJsonResponse(response)),
                        new AtfAssertion("Response JSON", noContent
                                ? "204 may have no body" : "non-empty parseable JSON",
                                redactedResponseBody, noContent || (!responseIsEmpty
                                && PracticeShopJsonEvidence.isJson(responseBody))));
                validation.assertions(assertions);
                boolean passed = assertions.stream().allMatch(AtfAssertion::passed);
                requestStatus = passed ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
                validation.complete(requestStatus);
                validationCompleted = true;
                consolePublish(console, AtfConsoleEvent.completed(AtfConsoleEvent.Scope.VALIDATION,
                        "Contract validation", consoleStatus(requestStatus), elapsedMillis(validationStartedAt)));
                node.complete(requestStatus);
            } finally {
                validation.close();
            }
        } finally {
            if (validationStarted && !validationCompleted) {
                consolePublish(console, AtfConsoleEvent.completed(AtfConsoleEvent.Scope.VALIDATION,
                        "Contract validation", consoleStatus(AtfReportStatus.FAIL), 0));
            }
            consolePublish(console, AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                    requestName, consoleStatus(requestStatus), elapsedMillis(requestStartedAt)));
            node.close();
        }
        return requestStatus;
    }

    /** Publishes the compact projection without discarding the ordered exchanges above. */
    private static void publishTraceSummary(AtfReportPorts.Phase phase,
                                            List<ApiExecutionResult> executions) {
        var node = phase.startRequest("API diagnostics — unique request signatures",
                "TRACE", "api-trace");
        try {
            Map<String, List<String>> groups = new LinkedHashMap<>();
            long sequence = 0;
            for (ApiExecutionResult execution : executions) {
                var exchange = PracticeShopApiExecutionExchange.from(execution, ++sequence,
                        phaseFor(execution.request().path()));
                groups.computeIfAbsent(exchange.signature(), ignored -> new ArrayList<>())
                        .add(exchange.method() + " " + exchange.path() + " → " + exchange.status());
            }
            node.detail("trace.contract", "logical signature = method + safe URL + canonical JSON body digest");
            node.detail("trace.observations", Integer.toString(executions.size()));
            node.detail("trace.unique.signatures", Integer.toString(groups.size()));
            int index = 0;
            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
                node.detail("signature." + (++index), "count=" + entry.getValue().size()
                        + "; " + entry.getKey() + "; observations=" + entry.getValue());
            }
            node.complete(AtfReportStatus.PASS);
        } finally {
            node.close();
        }
    }

    private static AtfReportStatus publishLiveProvenance(AtfReportPorts.Phase phase,
                                                          List<ApiExecutionResult> executions) {
        var node = phase.startRequest("LIVE_HTTP provenance", "ASSERT", "transport");
        AtfReportStatus status = AtfReportStatus.FAIL;
        try {
            boolean passed = !executions.isEmpty() && executions.stream()
                    .allMatch(execution -> execution.provenance() == ApiTransportProvenance.LIVE_HTTP);
            String observed = executions.stream()
                    .map(execution -> execution.provenance().reportLabel())
                    .distinct().toList().toString();
            var validation = node.startValidation("Transport provenance");
            try {
                validation.assertions(List.of(new AtfAssertion(
                        "Network provenance", "LIVE_HTTP for every exchange", observed, passed)));
                status = passed ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
                validation.complete(status);
                node.complete(status);
            } finally {
                validation.close();
            }
        } finally {
            node.close();
        }
        return status;
    }

    private static AtfReportStatus publishValidation(AtfReportPorts.Phase phase,
                                                       PracticeShopApiReportValidation reportValidation) {
        var node = phase.startRequest(reportValidation.title(), "ASSERT", "business-flow");
        AtfReportStatus status = AtfReportStatus.FAIL;
        try {
            node.detail("validation.scope", "business flow");
            var validation = node.startValidation("Business flow validation");
            try {
                validation.assertions(reportValidation.assertions());
                status = reportValidation.passed() ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
                validation.complete(status);
                node.complete(status);
            } finally {
                validation.close();
            }
        } finally {
            node.close();
        }
        return status;
    }

    private static long elapsedMillis(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }

    private static void consolePublish(AtfConsoleSink console, AtfConsoleEvent event) {
        try {
            console.publish(event);
        } catch (RuntimeException ignored) {
            // The console is a best-effort diagnostic projection only.
        }
    }

    private static atf.api.core.reporting.contract.AtfReportStatus consoleStatus(
            AtfReportStatus status) {
        return atf.api.core.reporting.contract.AtfReportStatus.valueOf(status.name());
    }

    private static boolean requiresJsonRequest(String method) {
        return HttpMethod.POST.value().equalsIgnoreCase(method)
                || HttpMethod.PUT.value().equalsIgnoreCase(method);
    }

    private static boolean isJsonResponse(ApiResponse response) {
        if (PracticeShopApiHttpStatus.NO_CONTENT.matches(response.status())) return true;
        String contentType = String.valueOf(response.header("content-type"));
        return contentType.toLowerCase(java.util.Locale.ROOT).contains(MediaTypes.APPLICATION_JSON);
    }

    private static String phaseFor(String path) {
        if (path == null) return "unknown";
        if (path.startsWith(PracticeShopApiRoutes.LOGIN)
                || path.startsWith(PracticeShopApiRoutes.CURRENT_USER)) return "authentication";
        if (path.startsWith(PracticeShopApiRoutes.PRODUCTS)
                || path.startsWith(PracticeShopApiRoutes.PRODUCT_COLLECTION)) return "catalog";
        if (path.startsWith(PracticeShopApiRoutes.CARTS)) return "cart";
        if (path.startsWith(PracticeShopApiRoutes.PAYMENT_CHECK)
                || path.startsWith(PracticeShopApiRoutes.POSTCODE_LOOKUP)) return "checkout";
        return "unknown";
    }
}
