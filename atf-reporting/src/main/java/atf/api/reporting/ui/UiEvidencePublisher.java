package atf.api.reporting.ui;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ApiManualReplay;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;

import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.markup.AtfReportEvidenceKeys;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Objects;

/**
 * Publishes the concrete UI evidence attached to one report phase.
 *
 * <p>{@link UiExtentReportSession} owns lifecycle state; this component owns
 * the evidence contract. Keeping those responsibilities separate prevents
 * screenshot, action and assertion rules from being duplicated across the
 * normal, media and failure paths.</p>
 */
public final class UiEvidencePublisher {
    private static final String SCREEN_VALIDATION = "Screen validation";
    private static final String METHOD = "method";
    private static final String PATH = "path";
    private static final String URL = "url";
    private static final String AUTH_TYPE = "auth.type";
    private static final String AUTH_PROFILE = "auth.profile";
    private static final String REQUEST_HEADERS = "request.headers";
    private static final String MANUAL_REPLAY = "manual.replay";
    private static final String QUERY_PARAMS = "query.params";
    private static final String TRANSPORT = "transport";
    private static final String STATUS = "status";
    private static final String CONTENT_TYPE = "content.type";
    private static final String RESPONSE_HEADERS = "response.headers";
    private static final String API_VALIDATION = "API validation";

    private final AtfReportPorts.Phase phase;
    private final ReportScreenshotPolicy screenshotPolicy;

    UiEvidencePublisher(AtfReportPorts.Phase phase) {
        this(phase, ReportScreenshotPolicy.EACH_CHECKPOINT);
    }

    UiEvidencePublisher(AtfReportPorts.Phase phase,
                        ReportScreenshotPolicy screenshotPolicy) {
        this.phase = Objects.requireNonNull(phase, "phase");
        this.screenshotPolicy = Objects.requireNonNull(screenshotPolicy, "screenshotPolicy");
    }

    void publishOutcome(String keyword, String title, String target,
                       AtfReportStatus status) {
        String stepTitle = stepTitle(keyword, title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, stepTitle);
            detailTarget(request, target);
            request.detail(AtfReportEvidenceKeys.OUTCOME_TYPE, AtfReportEvidenceKeys.ARTIFACT_PUBLICATION);
            request.complete(defaultStatus(status, AtfReportStatus.PASS));
        } finally {
            request.close();
        }
    }

    /**
     * Publishes a runtime/browser diagnostic as a first-class diagnostic.
     * Diagnostics are deliberately not routed through {@code UI validation}:
     * that title means a visual checkpoint and would make a known guest
     * branch look like a screen assertion.
     */
    void publishDiagnostic(String keyword, String title, String target,
                           Collection<AtfAssertion> assertions, AtfReportStatus status) {
        String stepTitle = stepTitle(keyword, title);
        AtfReportStatus effectiveStatus = defaultStatus(status, AtfReportStatus.INFO);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, stepTitle);
            detailTarget(request, target);
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE,
                    AtfReportEvidenceKeys.RUNTIME_DIAGNOSTIC);
            request.detail("diagnostic.status", effectiveStatus.displayValue());
            var diagnostic = request.startValidation("Runtime diagnostic");
            diagnostic.assertions(assertions == null ? List.of() : assertions);
            diagnostic.complete(effectiveStatus);
            request.complete(effectiveStatus);
        } finally {
            request.close();
        }
    }

    /**
     * Publishes lifecycle/setup evidence without misclassifying it as a UI
     * validation. Setup proves that the input artifact is available; the
     * browser assertions belong to the TEST BODY Screen validation card.
     */
    void publishSetup(String keyword, String title, String target,
                      Collection<AtfAssertion> assertions, AtfReportStatus status) {
        String stepTitle = stepTitle(keyword, title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, stepTitle);
            detailTarget(request, target);
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE, AtfReportEvidenceKeys.REPORT_SETUP);
            request.detail(AtfReportEvidenceKeys.SETUP_RESULT,
                    defaultStatus(status, AtfReportStatus.PASS).displayValue());
            if (assertions != null) {
                int index = 0;
                for (AtfAssertion assertion : assertions) {
                    if (assertion == null) continue;
                    index++;
                    request.detail(AtfReportEvidenceKeys.setupCheck(index),
                            assertion.name() + " | expected=" + assertion.expected()
                                    + " | actual=" + assertion.actual()
                                    + " | result=" + assertion.resultStatus().displayValue());
                }
            }
            request.complete(defaultStatus(status, AtfReportStatus.PASS));
        } finally {
            request.close();
        }
    }

    void publishAction(String keyword, String title, String target, String base64Png,
                       Collection<AtfAssertion> assertions, AtfReportStatus status,
                       String validationTitle, Collection<AtfUiActionRecord> actions) {
        publishAction(keyword, title, target, base64Png, assertions, status,
                validationTitle, actions, List.of(), ReportScreenshotPolicy.CapturePoint.CHECKPOINT);
    }

    void publishAction(String keyword, String title, String target, String base64Png,
                       Collection<AtfAssertion> assertions, AtfReportStatus status,
                       String validationTitle, Collection<AtfUiActionRecord> actions,
                       Collection<ReportExecutionEvidence> apiEvidence) {
        publishAction(keyword, title, target, base64Png, assertions, status,
                validationTitle, actions, apiEvidence,
                ReportScreenshotPolicy.CapturePoint.CHECKPOINT);
    }

    void publishAction(String keyword, String title, String target, String base64Png,
                       Collection<AtfAssertion> assertions, AtfReportStatus status,
                       String validationTitle, Collection<AtfUiActionRecord> actions,
                       Collection<ReportExecutionEvidence> apiEvidence,
                       ReportScreenshotPolicy.CapturePoint capturePoint) {
        String stepTitle = stepTitle(keyword, title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, stepTitle);
            detailTarget(request, target);
            boolean visualValidation = SCREEN_VALIDATION.equals(validationTitle);
            boolean hasApiEvidence = hasApiEvidence(apiEvidence);
            AtfReportStatus effectiveStatus = defaultStatus(status, AtfReportStatus.PASS);
            if (hasApiEvidence && apiEvidence.stream().anyMatch(evidence -> evidence != null
                    && evidence.response() != null && evidence.response().status() < 0)) {
                effectiveStatus = AtfReportStatus.worst(effectiveStatus, AtfReportStatus.WARNING);
            }
            if (visualValidation) {
                request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE, AtfReportEvidenceKeys.SCREEN_VALIDATION);
                request.detail(AtfReportEvidenceKeys.EVIDENCE_POLICY,
                        "one Base64 screenshot link per visual state; policy="
                                + screenshotPolicy.configValue());
            }
            if (visualValidation || (assertions != null && !assertions.isEmpty())) {
                var validation = request.startValidation(validationTitle);
                publishActions(validation, actions);
                publishScreenshot(validation, target, title, base64Png, status, "UI assertion", capturePoint);
                validation.assertions(assertions);
                validation.complete(effectiveStatus);
            }
            if (hasApiEvidence) {
                var apiValidation = request.startValidation(API_VALIDATION);
                try {
                    publishApiEvidence(apiValidation, apiEvidence, effectiveStatus);
                    apiValidation.complete(effectiveStatus);
                } finally {
                    apiValidation.close();
                }
            }
            request.complete(effectiveStatus);
        } finally {
            request.close();
        }
    }

    /** Publishes the buffered phase as one Gherkin proposition with grouped evidence. */
    void publishUnifiedFlow(String scenarioTitle, String phaseName,
                            List<UiUnifiedFlowCheckpoint> checkpoints,
                            AtfReportStatus phaseStatus) {
        if (checkpoints == null || checkpoints.isEmpty()) return;
        String flowTitle = unifiedProposition(checkpoints);
        AtfReportStatus flowStatus = AtfReportStatus.orInfo(phaseStatus);
        AtfReportStatus uiStatus = AtfReportStatus.INFO;
        AtfReportStatus apiStatus = AtfReportStatus.INFO;
        List<AtfAssertion> assertions = checkpoints.stream()
                .flatMap(checkpoint -> checkpoint.assertions().stream()).toList();
        List<AtfUiActionRecord> actions = checkpoints.stream()
                .flatMap(checkpoint -> checkpoint.actions().stream()).toList();
        List<ReportExecutionEvidence> apiEvidence = checkpoints.stream()
                .flatMap(checkpoint -> checkpoint.apiEvidence().stream()).toList();
        boolean hasUiEvidence = false;
        for (UiUnifiedFlowCheckpoint checkpoint : checkpoints) {
            AtfReportStatus checkpointStatus = effectiveStatus(checkpoint);
            flowStatus = AtfReportStatus.worst(flowStatus, checkpointStatus);
            if (isUiEvidence(checkpoint)) {
                hasUiEvidence = true;
                uiStatus = AtfReportStatus.worst(uiStatus, checkpoint.status());
            }
            if (hasApiEvidence(checkpoint.apiEvidence())) {
                apiStatus = AtfReportStatus.worst(apiStatus, checkpointStatus);
            }
        }

        var flow = phase.startRequest(flowTitle, "", "");
        try {
            flow.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE,
                    AtfReportEvidenceKeys.UNIFIED_UI_FLOW);
            flow.detail(AtfReportEvidenceKeys.FLOW_MODE,
                    ReportEvidenceFlowMode.UNIFIED_FLOW.configValue());
            if (hasText(scenarioTitle)) {
                flow.detail(AtfReportEvidenceKeys.FLOW_SCENARIO, scenarioTitle.trim());
            }
            if (hasText(phaseName)) {
                flow.detail(AtfReportEvidenceKeys.FLOW_PHASE, phaseName);
            }

            if (hasUiEvidence) {
                var validation = flow.startValidation(SCREEN_VALIDATION);
                try {
                    publishActions(validation, actions);
                    for (UiUnifiedFlowCheckpoint checkpoint : checkpoints) {
                        publishUnifiedScreenshot(validation, checkpoint,
                                effectiveStatus(checkpoint));
                    }
                    validation.assertions(assertions);
                    validation.complete(uiStatus);
                } finally {
                    validation.close();
                }
            }
            if (hasApiEvidence(apiEvidence)) {
                var apiValidation = flow.startValidation(API_VALIDATION);
                try {
                    publishApiEvidence(apiValidation, apiEvidence, apiStatus);
                    apiValidation.complete(apiStatus);
                } finally {
                    apiValidation.close();
                }
            }
            flow.complete(flowStatus);
        } finally {
            flow.close();
        }
    }

    private static boolean isUiEvidence(UiUnifiedFlowCheckpoint checkpoint) {
        return SCREEN_VALIDATION.equals(checkpoint.validationTitle())
                || !checkpoint.assertions().isEmpty() || !checkpoint.actions().isEmpty()
                || hasText(checkpoint.base64Png());
    }

    private static AtfReportStatus effectiveStatus(UiUnifiedFlowCheckpoint checkpoint) {
        AtfReportStatus status = checkpoint.status();
        if (hasApiEvidence(checkpoint.apiEvidence())
                && checkpoint.apiEvidence().stream().anyMatch(evidence -> evidence != null
                && evidence.response() != null && evidence.response().status() < 0)) {
            return AtfReportStatus.worst(status, AtfReportStatus.WARNING);
        }
        return status;
    }

    private void publishUnifiedScreenshot(AtfReportPorts.Validation validation,
                                          UiUnifiedFlowCheckpoint checkpoint,
                                          AtfReportStatus status) {
        if (!shouldPublish(checkpoint.base64Png(), checkpoint.capturePoint())) return;
        requireTargetForScreenshot(checkpoint.target());
        String title = "UI screenshot — " + checkpoint.title() + " — "
                + defaultStatus(status, AtfReportStatus.PASS);
        if (checkpoint.mediaScreenshot()) {
            validation.mediaScreenshot(title, checkpoint.base64Png());
        } else {
            validation.screenshot("UI assertion — " + checkpoint.title() + " — "
                    + defaultStatus(status, AtfReportStatus.PASS), checkpoint.base64Png());
        }
    }

    private static String unifiedProposition(List<UiUnifiedFlowCheckpoint> checkpoints) {
        UiUnifiedFlowCheckpoint proposition = checkpoints.get(checkpoints.size() - 1);
        for (UiUnifiedFlowCheckpoint checkpoint : checkpoints) {
            if (AtfGherkinKeyword.THEN.text().equalsIgnoreCase(checkpoint.keyword())) {
                proposition = checkpoint;
            }
        }
        String title = proposition.gherkinTitle();
        return hasText(title) ? title : AtfGherkinKeyword.THEN.text() + " UI flow completed";
    }

    /** Publishes an explicit standalone API trace with nested exchange evidence. */
    void publishApiTrace(String keyword, String title, String target,
                         Collection<ReportExecutionEvidence> apiEvidence,
                         AtfReportStatus status) {
        String sourceStep = stepTitle(keyword, title);
        AtfReportStatus effectiveStatus = defaultStatus(status, AtfReportStatus.PASS);
        // Keep the trace node as an exact Gherkin sibling. The evidence type,
        // parent-step detail and TRACE method already identify its role; a
        // second visible prefix would make the step noisy and non-semantic.
        var request = phase.startRequest(sourceStep, "TRACE", hasText(target) ? target : "network");
        try {
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE, AtfReportEvidenceKeys.API_TRACE);
            request.detail("trace.parent.step", sourceStep);
            request.detail("trace.position", "causal sibling published immediately after UI step");
            var validation = request.startValidation(API_VALIDATION);
            try {
                publishApiEvidence(validation, apiEvidence, effectiveStatus);
                validation.complete(effectiveStatus);
            } finally {
                validation.close();
            }
            request.complete(effectiveStatus);
        } finally {
            request.close();
        }
    }

    /**
     * Projects each API exchange into a request child of one semantic
     * {@code API validation}. This is deliberately the same typed request /
     * response / execution pipeline used by direct API reports: one exchange
     * owns one request node, one request JSON, one response JSON and its own
     * attempt history. The parent trace carries only causal context.
     */
    private static void publishApiEvidence(AtfReportPorts.Validation validation,
                                           Collection<ReportExecutionEvidence> apiEvidence,
                                           AtfReportStatus exchangeStatus) {
        if (apiEvidence == null) return;
        int exchangeNumber = 0;
        for (ReportExecutionEvidence evidence : apiEvidence) {
            if (evidence == null || evidence.request() == null || evidence.response() == null) continue;
            exchangeNumber++;
            ReportRequest apiRequest = evidence.request();
            ReportResponse apiResponse = evidence.response();
            var request = validation.startRequest(exchangeTitle(apiRequest, exchangeNumber),
                    apiRequest.method(), apiRequest.path());
            request.detail("trace.exchange", "#" + exchangeNumber);
            request.detail(METHOD, apiRequest.method());
            request.detail(PATH, apiRequest.path());
            request.detail(URL, apiResponse.url());
            request.detail(AUTH_PROFILE, apiRequest.authProfile());
            request.detail(AUTH_TYPE,
                    apiRequest.authMethod().wireValue());
            if (!apiRequest.query().isEmpty()) {
                request.detail(QUERY_PARAMS, format(apiRequest.query()));
            }
            request.detail(REQUEST_HEADERS,
                    ApiManualReplay.redactedHeaders(apiRequest));
            request.detail(TRANSPORT,
                    evidence.provenance().reportLabel());
            request.detail(MANUAL_REPLAY,
                    ApiManualReplay.describe(evidence.provenance(), apiRequest,
                            apiResponse.url(), apiRequest.bodyText()));
            if (!apiRequest.bodyText().isBlank()) {
                request.json("REQUEST JSON", apiRequest.bodyText());
            }
            request.detail(STATUS, apiResponse.statusLabel());
            request.detail(CONTENT_TYPE,
                    header(apiResponse.headers(), "content-type"));
            String responseHeaders = ApiManualReplay.relevantResponseHeaders(apiResponse.headers());
            if (!"[]".equals(responseHeaders)) {
                request.detail(RESPONSE_HEADERS, responseHeaders);
            }
            if (apiResponse.status() >= 0 && !apiResponse.body().isBlank()) {
                request.json("RESPONSE JSON", apiResponse.body());
            }
            request.execution(evidence);
            request.complete(apiResponse.status() < 0
                    ? AtfReportStatus.WARNING : exchangeStatus);
        }
    }

    private static String exchangeTitle(ReportRequest request, int number) {
        return "Exchange " + number;
    }

    private static String header(Map<String, String> headers, String name) {
        if (headers == null) return "";
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue).findFirst().orElse("");
    }

    private static String format(Map<String, String> values) {
        return values == null || values.isEmpty() ? "{}" : values.toString();
    }

    void publishScreenValidationAsMedia(String title, String target, String base64Png,
                                        Collection<AtfAssertion> assertions,
                                        Collection<AtfUiActionRecord> actions,
                                        AtfReportStatus status) {
        publishScreenValidationAsMedia(title, target, base64Png, assertions, actions,
                ReportScreenshotPolicy.CapturePoint.CHECKPOINT, status);
    }

    void publishScreenValidationAsMedia(String title, String target, String base64Png,
                                        Collection<AtfAssertion> assertions,
                                        Collection<AtfUiActionRecord> actions,
                                        ReportScreenshotPolicy.CapturePoint capturePoint,
                                        AtfReportStatus status) {
        String stepTitle = stepTitle(AtfGherkinKeyword.THEN.text(), title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail("request.action", stepTitle);
            detailTarget(request, target);
            request.detail("evidence.type", "screen-validation");
            request.detail("evidence.policy", "one visible Base64 image inside one safe clickable link; policy="
                    + screenshotPolicy.configValue());
            var validation = request.startValidation(SCREEN_VALIDATION);
            publishActions(validation, actions);
            if (shouldPublish(base64Png, capturePoint)) {
                requireTargetForScreenshot(target);
                validation.mediaScreenshot("UI screenshot — " + title + " — "
                        + defaultStatus(status, AtfReportStatus.PASS), base64Png);
            }
            validation.assertions(assertions);
            validation.complete(defaultStatus(status, AtfReportStatus.PASS));
            request.complete(defaultStatus(status, AtfReportStatus.PASS));
        } finally {
            request.close();
        }
    }

    void publishFailure(String title, String target, Collection<AtfUiActionRecord> actions,
                        Throwable failure, String base64Png) {
        String safeTitle = !hasText(title) ? "UI audit failure" : title.trim();
        String cause = failure == null ? "failure recorded" : failure.getClass().getSimpleName()
                + ": " + (failure.getMessage() == null ? "" : failure.getMessage());
        var request = phase.startRequest(AtfGherkinKeyword.THEN.text() + " " + safeTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, safeTitle);
            detailTarget(request, target);
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE, AtfReportEvidenceKeys.UI_FAILURE);
            request.detail(AtfReportEvidenceKeys.EVIDENCE_POLICY, !shouldPublish(base64Png,
                    ReportScreenshotPolicy.CapturePoint.FAILURE)
                    ? "preserve executed actions; screenshot disabled or unavailable; policy="
                    + screenshotPolicy.configValue()
                    : "preserve executed actions; screenshot captured by policy="
                    + screenshotPolicy.configValue());
            var validation = request.startValidation("Failure evidence");
            publishActions(validation, actions);
            if (shouldPublish(base64Png, ReportScreenshotPolicy.CapturePoint.FAILURE)) {
                requireTargetForScreenshot(target);
                validation.screenshot("UI failure screenshot", base64Png);
            }
            validation.assertions(List.of(new AtfAssertion(
                    "Failure cause", "no failure", cause, false)));
            validation.completeFailure(AtfReportStatus.FAIL);
            request.complete(AtfReportStatus.FAIL);
        } finally {
            request.close();
        }
    }

    private static void publishActions(AtfReportPorts.Validation validation,
                                       Collection<AtfUiActionRecord> actions) {
        if (actions != null && !actions.isEmpty()) validation.uiActions(actions);
    }

    private void publishScreenshot(AtfReportPorts.Validation validation, String target,
                                   String title, String base64Png, AtfReportStatus status,
                                   String prefix, ReportScreenshotPolicy.CapturePoint capturePoint) {
        if (shouldPublish(base64Png, capturePoint)) {
            requireTargetForScreenshot(target);
            validation.screenshot(prefix + " — " + title + " — "
                    + defaultStatus(status, AtfReportStatus.PASS), base64Png);
        }
    }

    private boolean shouldPublish(String base64Png, ReportScreenshotPolicy.CapturePoint capturePoint) {
        return hasText(base64Png) && screenshotPolicy.captures(capturePoint);
    }

    boolean acceptsScreenshot(String base64Png,
                              ReportScreenshotPolicy.CapturePoint capturePoint) {
        return shouldPublish(base64Png, capturePoint);
    }

    private static void requireTargetForScreenshot(String target) {
        if (!hasText(target)) {
            throw new IllegalArgumentException(
                    "A screenshot target is required when screenshot evidence is published");
        }
    }

    private static void detailTarget(AtfReportPorts.Request request, String target) {
        if (hasText(target)) request.detail(AtfReportEvidenceKeys.REQUEST_TARGET, target);
    }

    private static String stepTitle(String keyword, String title) {
        return ((keyword == null ? "" : keyword.trim()) + " "
                + (title == null ? "" : title.trim())).trim();
    }

    private static boolean hasApiEvidence(Collection<ReportExecutionEvidence> apiEvidence) {
        return apiEvidence != null && apiEvidence.stream()
                .anyMatch(evidence -> evidence != null
                        && evidence.request() != null
                        && evidence.response() != null);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static AtfReportStatus defaultStatus(AtfReportStatus status, AtfReportStatus fallback) {
        return status == null ? fallback : status;
    }
}
