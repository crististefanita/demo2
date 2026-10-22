package atf.api.reporting.ui;

import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.reporting.markup.AtfReportEvidenceKeys;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.model.ReportExecutionEvidence;

import atf.api.reporting.ui.model.AtfUiActionRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Owns phase state and delegates all evidence publication to one publisher. */
public final class UiPhaseLifecycle implements AutoCloseable {
    private final AtfReportPorts.Phase delegate;
    private final UiEvidencePublisher evidencePublisher;
    private final ReportEvidenceFlowMode flowMode;
    private final String scenarioTitle;
    private final String phaseName;
    private final List<UiUnifiedFlowCheckpoint> unifiedCheckpoints = new ArrayList<>();
    private PendingNetworkSummary networkSummary;
    private boolean completed;
    private boolean unifiedFlowPublished;

    UiPhaseLifecycle(AtfReportPorts.Phase delegate,
                     ReportScreenshotPolicy screenshotPolicy,
                     ReportEvidenceFlowMode flowMode,
                     String scenarioTitle,
                     String phaseName) {
        this.delegate = delegate;
        this.evidencePublisher = new UiEvidencePublisher(delegate, screenshotPolicy);
        this.flowMode = flowMode;
        this.scenarioTitle = scenarioTitle == null ? "" : scenarioTitle.trim();
        this.phaseName = phaseName == null ? "" : phaseName.trim();
    }

    void step(String keyword, String title, String target, String base64Png,
              AtfReportStatus status) {
        step(keyword, title, target, base64Png, List.of(), status);
    }

    void step(AtfGherkinKeyword keyword, String title, String target, String base64Png,
              AtfReportStatus status) {
        step(keyword.text(), title, target, base64Png, status);
    }

    void step(String keyword, String title, String target, String base64Png,
              Collection<AtfAssertion> assertions, AtfReportStatus status) {
        action(keyword, title, target, base64Png, assertions, status,
                "UI validation", List.of());
    }

    void step(AtfGherkinKeyword keyword, String title, String target, String base64Png,
              Collection<AtfAssertion> assertions, AtfReportStatus status) {
        step(keyword.text(), title, target, base64Png, assertions, status);
    }

    void step(AtfGherkinKeyword keyword, String title, String target, String base64Png,
              Collection<AtfAssertion> assertions, Collection<AtfUiActionRecord> actions,
              AtfReportStatus status) {
        action(keyword.text(), title, target, base64Png, assertions, status,
                "UI validation", actions, ReportScreenshotPolicy.CapturePoint.CHECKPOINT);
    }

    void step(AtfGherkinKeyword keyword, String title, String target, String base64Png,
              Collection<AtfAssertion> assertions, Collection<AtfUiActionRecord> actions,
              ReportScreenshotPolicy.CapturePoint capturePoint, AtfReportStatus status) {
        action(keyword.text(), title, target, base64Png, assertions, status,
                "UI validation", actions, capturePoint);
    }

    void step(String keyword, String title, String target, String base64Png,
              Collection<AtfAssertion> assertions, Collection<AtfUiActionRecord> actions,
              Collection<ReportExecutionEvidence> apiEvidence, AtfReportStatus status) {
        step(keyword, title, target, base64Png, assertions, actions, apiEvidence,
                ReportScreenshotPolicy.CapturePoint.CHECKPOINT, status);
    }

    void step(String keyword, String title, String target, String base64Png,
              Collection<AtfAssertion> assertions, Collection<AtfUiActionRecord> actions,
              Collection<ReportExecutionEvidence> apiEvidence,
              ReportScreenshotPolicy.CapturePoint capturePoint, AtfReportStatus status) {
        action(keyword, title, target, base64Png, assertions, status,
                "UI validation", actions, apiEvidence, capturePoint);
    }

    void setup(String keyword, String title, String target,
               Collection<AtfAssertion> assertions, AtfReportStatus status) {
        setup(keyword, title, target, assertions, List.of(), List.of(), status);
    }

    void setup(String keyword, String title, String target,
               Collection<AtfAssertion> assertions,
               Collection<AtfUiActionRecord> actions,
               Collection<ReportExecutionEvidence> apiEvidence,
               AtfReportStatus status) {
        ensureOpen();
        evidencePublisher.publishSetup(keyword, title, target, assertions, actions, apiEvidence, status);
    }

    void setup(AtfGherkinKeyword keyword, String title, String target,
               Collection<AtfAssertion> assertions, AtfReportStatus status) {
        setup(keyword.text(), title, target, assertions, status);
    }

    void setup(AtfGherkinKeyword keyword, String title, String target,
               Collection<AtfAssertion> assertions,
               Collection<AtfUiActionRecord> actions,
               Collection<ReportExecutionEvidence> apiEvidence,
               AtfReportStatus status) {
        setup(keyword.text(), title, target, assertions, actions, apiEvidence, status);
    }

    void networkInspector(String title,
                          Collection<ReportExecutionEvidence> executionEvidence) {
        networkInspector(title, executionEvidence, List.of());
    }

    void networkInspector(String title,
                          Collection<ReportExecutionEvidence> executionEvidence,
                          Collection<ReportNetworkInspector.Step> steps) {
        ensureOpen();
        if (networkSummary != null) {
            throw new IllegalStateException(
                    "Only one journey-level network inspector summary can be registered per UI phase");
        }
        networkSummary = new PendingNetworkSummary(
                title == null || title.isBlank() ? "API network inspector" : title.trim(),
                immutableCopy(executionEvidence), immutableCopy(steps));
    }

    private void publishNetworkSummary() {
        if (networkSummary == null) return;
        PendingNetworkSummary summary = networkSummary;
        var request = delegate.startRequest(
                summary.title(), "TRACE", "network");
        try {
            request.detail("request.action", "API network inspector");
            request.detail("request.target", "ordered exchanges from the same UI browser session");
            request.detail("evidence.role", "journey summary; not an additional business step");
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE,
                    AtfReportEvidenceKeys.API_TRACE);
            ReportNetworkInspector.fields(summary.evidence(), summary.steps()).stream()
                    .filter(field -> !field.name().startsWith("network.aggregate."))
                    .forEach(field -> request.detail(field.name(), field.value()));
            request.networkAggregate(ReportNetworkInspector.aggregate(
                    summary.evidence(), summary.steps()));
            // This node is a passive journey summary, not an API assertion.
            // Failed exchanges retain their evidence here; checkpoint/API validation
            // nodes own the verdict. No independent browser runtime signals are accepted.
            request.complete(AtfReportStatus.PASS);
        } finally {
            request.close();
        }
    }

    void outcome(String keyword, String title, String target, AtfReportStatus status) {
        ensureOpen();
        evidencePublisher.publishOutcome(keyword, title, target, status);
    }

    void outcome(AtfGherkinKeyword keyword, String title, String target, AtfReportStatus status) {
        outcome(keyword.text(), title, target, status);
    }

    void apiTrace(AtfGherkinKeyword keyword, String title, String target,
                  Collection<ReportExecutionEvidence> apiEvidence, AtfReportStatus status) {
        ensureOpen();
        evidencePublisher.publishApiTrace(keyword.text(), title, target, apiEvidence, status);
    }

    void screenValidation(String title, String target, String base64Png,
                          Collection<AtfAssertion> assertions, AtfReportStatus status) {
        action(AtfGherkinKeyword.THEN.text(), title, target, base64Png, assertions, status,
                "Screen validation", List.of(), ReportScreenshotPolicy.CapturePoint.CHECKPOINT);
    }

    void screenValidation(String title, String target, String base64Png,
                          Collection<AtfAssertion> assertions,
                          Collection<AtfUiActionRecord> actions,
                          AtfReportStatus status) {
        action(AtfGherkinKeyword.THEN.text(), title, target, base64Png, assertions, status,
                "Screen validation", actions, ReportScreenshotPolicy.CapturePoint.CHECKPOINT);
    }

    void screenValidation(String title, String target, String base64Png,
                          Collection<AtfAssertion> assertions,
                          Collection<AtfUiActionRecord> actions,
                          ReportScreenshotPolicy.CapturePoint capturePoint,
                          AtfReportStatus status) {
        action(AtfGherkinKeyword.THEN.text(), title, target, base64Png, assertions, status,
                "Screen validation", actions, capturePoint);
    }

    void screenValidationAsMedia(String title, String target, String base64Png,
                                 Collection<AtfAssertion> assertions,
                                 Collection<AtfUiActionRecord> actions,
                                 AtfReportStatus status) {
        ensureOpen();
        if (flowMode == ReportEvidenceFlowMode.UNIFIED_FLOW) {
            unifiedCheckpoints.add(new UiUnifiedFlowCheckpoint(
                    AtfGherkinKeyword.THEN.text(), title, target,
                    checkpointScreenshot(base64Png,
                            ReportScreenshotPolicy.CapturePoint.CHECKPOINT),
                    assertions == null ? List.of() : List.copyOf(assertions), status,
                    "Screen validation", immutableCopy(actions), List.of(),
                    ReportScreenshotPolicy.CapturePoint.CHECKPOINT, true));
            return;
        }
        evidencePublisher.publishScreenValidationAsMedia(title, target, base64Png,
                assertions, actions, ReportScreenshotPolicy.CapturePoint.CHECKPOINT, status);
    }

    void failureEvidence(String title, String target,
                         Collection<AtfUiActionRecord> actions,
                         Throwable failure, String base64Png) {
        ensureOpen();
        evidencePublisher.publishFailure(title, target, actions, failure, base64Png);
    }

    void complete(AtfReportStatus status) {
        ensureOpen();
        publishUnifiedFlow(status);
        publishNetworkSummary();
        delegate.complete(status == null ? AtfReportStatus.INFO : status);
        completed = true;
    }

    @Override
    public void close() {
        if (!completed) complete(AtfReportStatus.FAIL);
    }

    private void action(String keyword, String title, String target,
                        String base64Png, Collection<AtfAssertion> assertions,
                        AtfReportStatus status, String validationTitle,
                        Collection<AtfUiActionRecord> actions) {
        action(keyword, title, target, base64Png, assertions, status,
                validationTitle, actions, List.of());
    }

    private void action(String keyword, String title, String target,
                        String base64Png, Collection<AtfAssertion> assertions,
                        AtfReportStatus status, String validationTitle,
                        Collection<AtfUiActionRecord> actions,
                        Collection<ReportExecutionEvidence> apiEvidence) {
        action(keyword, title, target, base64Png, assertions, status, validationTitle,
                actions, apiEvidence, ReportScreenshotPolicy.CapturePoint.CHECKPOINT);
    }

    private void action(String keyword, String title, String target,
                        String base64Png, Collection<AtfAssertion> assertions,
                        AtfReportStatus status, String validationTitle,
                        Collection<AtfUiActionRecord> actions,
                        Collection<ReportExecutionEvidence> apiEvidence,
                        ReportScreenshotPolicy.CapturePoint capturePoint) {
        ensureOpen();
        if (flowMode == ReportEvidenceFlowMode.UNIFIED_FLOW) {
            String eligibleScreenshot = checkpointScreenshot(base64Png, capturePoint);
            unifiedCheckpoints.add(new UiUnifiedFlowCheckpoint(keyword, title, target,
                    eligibleScreenshot, immutableCopy(assertions), status, validationTitle,
                    immutableCopy(actions), immutableCopy(apiEvidence), capturePoint, false));
            return;
        }
        evidencePublisher.publishAction(keyword, title, target, base64Png, assertions,
                status, validationTitle, actions, apiEvidence, capturePoint);
    }

    private void action(String keyword, String title, String target,
                        String base64Png, Collection<AtfAssertion> assertions,
                        AtfReportStatus status, String validationTitle,
                        Collection<AtfUiActionRecord> actions,
                        ReportScreenshotPolicy.CapturePoint capturePoint) {
        action(keyword, title, target, base64Png, assertions, status,
                validationTitle, actions, List.of(), capturePoint);
    }

    private void ensureOpen() {
        if (completed) throw new IllegalStateException("UI phase is already complete");
    }

    private void publishUnifiedFlow(AtfReportStatus status) {
        if (flowMode != ReportEvidenceFlowMode.UNIFIED_FLOW
                || unifiedCheckpoints.isEmpty() || unifiedFlowPublished) return;
        unifiedFlowPublished = true;
        evidencePublisher.publishUnifiedFlow(scenarioTitle, phaseName,
                List.copyOf(unifiedCheckpoints), status);
        unifiedCheckpoints.clear();
    }

    private static <T> List<T> immutableCopy(Collection<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private String checkpointScreenshot(String base64Png,
                                        ReportScreenshotPolicy.CapturePoint capturePoint) {
        return evidencePublisher.acceptsScreenshot(base64Png, capturePoint) ? base64Png : "";
    }

    private record PendingNetworkSummary(String title,
                                        List<ReportExecutionEvidence> evidence,
                                        List<ReportNetworkInspector.Step> steps) { }
}
