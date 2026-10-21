package atf.api.reporting.ui;

import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.reporting.markup.AtfReportEvidenceKeys;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.model.ReportExecutionEvidence;

import atf.api.reporting.ui.AtfUiActionRecord;

import java.util.Collection;
import java.util.List;

/** Owns phase state and delegates all evidence publication to one publisher. */
public final class UiPhaseLifecycle implements AutoCloseable {
    private final AtfReportPorts.Phase delegate;
    private final UiEvidencePublisher evidencePublisher;
    private boolean completed;

    UiPhaseLifecycle(AtfReportPorts.Phase delegate,
                     ReportScreenshotPolicy screenshotPolicy) {
        this.delegate = delegate;
        this.evidencePublisher = new UiEvidencePublisher(delegate, screenshotPolicy);
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
        ensureOpen();
        evidencePublisher.publishSetup(keyword, title, target, assertions, status);
    }

    void setup(AtfGherkinKeyword keyword, String title, String target,
               Collection<AtfAssertion> assertions, AtfReportStatus status) {
        setup(keyword.text(), title, target, assertions, status);
    }

    void networkInspector(String title,
                          Collection<ReportExecutionEvidence> executionEvidence) {
        networkInspector(title, executionEvidence, List.of());
    }

    void networkInspector(String title,
                          Collection<ReportExecutionEvidence> executionEvidence,
                          Collection<ReportNetworkInspector.Step> steps) {
        networkInspector(title, executionEvidence, steps, List.of());
    }

    void networkInspector(String title,
                          Collection<ReportExecutionEvidence> executionEvidence,
                          Collection<ReportNetworkInspector.Step> steps,
                          Collection<ReportNetworkInspector.Diagnostic> diagnostics) {
        ensureOpen();
        var request = delegate.startRequest(
                title == null || title.isBlank() ? "API network inspector" : title.trim(),
                "TRACE", "network");
        try {
            request.detail("request.action", "API network inspector");
            request.detail("request.target", "ordered exchanges from the same UI browser session");
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE,
                    AtfReportEvidenceKeys.API_TRACE);
            ReportNetworkInspector.fields(executionEvidence, steps, diagnostics).stream()
                    .filter(field -> !field.name().startsWith("network.aggregate."))
                    .forEach(field -> request.detail(field.name(), field.value()));
            request.networkAggregate(ReportNetworkInspector.aggregate(executionEvidence, steps, diagnostics));
            boolean requestFailed = executionEvidence != null && executionEvidence.stream()
                    .anyMatch(evidence -> evidence != null && evidence.response() != null
                            && evidence.response().status() < 0);
            request.complete(requestFailed ? AtfReportStatus.WARNING : AtfReportStatus.PASS);
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

    void diagnostic(String keyword, String title, String target,
                    Collection<AtfAssertion> assertions, AtfReportStatus status) {
        ensureOpen();
        evidencePublisher.publishDiagnostic(keyword, title, target, assertions, status);
    }

    void diagnostic(AtfGherkinKeyword keyword, String title, String target,
                    Collection<AtfAssertion> assertions, AtfReportStatus status) {
        diagnostic(keyword.text(), title, target, assertions, status);
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
        evidencePublisher.publishAction(keyword, title, target, base64Png, assertions,
                status, validationTitle, actions, apiEvidence,
                capturePoint);
    }

    private void action(String keyword, String title, String target,
                        String base64Png, Collection<AtfAssertion> assertions,
                        AtfReportStatus status, String validationTitle,
                        Collection<AtfUiActionRecord> actions,
                        ReportScreenshotPolicy.CapturePoint capturePoint) {
        ensureOpen();
        evidencePublisher.publishAction(keyword, title, target, base64Png, assertions,
                status, validationTitle, actions, List.of(), capturePoint);
    }

    private void ensureOpen() {
        if (completed) throw new IllegalStateException("UI phase is already complete");
    }
}
