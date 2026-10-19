package atf.api.reporting.ui;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfGherkinKeyword;
import atf.api.core.reporting.contract.AtfReportPorts;
import atf.api.core.reporting.contract.AtfReportStatus;

import atf.api.core.ui.AtfUiActionRecord;

import java.util.Collection;
import java.util.List;

/** Owns phase state and delegates all evidence publication to one publisher. */
public final class UiPhaseLifecycle implements AutoCloseable {
    private final AtfReportPorts.Phase delegate;
    private final UiEvidencePublisher evidencePublisher;
    private boolean completed;

    UiPhaseLifecycle(AtfReportPorts.Phase delegate) {
        this.delegate = delegate;
        this.evidencePublisher = new UiEvidencePublisher(delegate);
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

    void setup(String keyword, String title, String target,
               Collection<AtfAssertion> assertions, AtfReportStatus status) {
        ensureOpen();
        evidencePublisher.publishSetup(keyword, title, target, assertions, status);
    }

    void setup(AtfGherkinKeyword keyword, String title, String target,
               Collection<AtfAssertion> assertions, AtfReportStatus status) {
        setup(keyword.text(), title, target, assertions, status);
    }

    void outcome(String keyword, String title, String target, AtfReportStatus status) {
        ensureOpen();
        evidencePublisher.publishOutcome(keyword, title, target, status);
    }

    void outcome(AtfGherkinKeyword keyword, String title, String target, AtfReportStatus status) {
        outcome(keyword.text(), title, target, status);
    }

    void screenValidation(String title, String target, String base64Png,
                          Collection<AtfAssertion> assertions, AtfReportStatus status) {
        action(AtfGherkinKeyword.THEN.text(), title, target, base64Png, assertions, status,
                "Screen validation", List.of());
    }

    void screenValidation(String title, String target, String base64Png,
                          Collection<AtfAssertion> assertions,
                          Collection<AtfUiActionRecord> actions,
                          AtfReportStatus status) {
        action(AtfGherkinKeyword.THEN.text(), title, target, base64Png, assertions, status,
                "Screen validation", actions);
    }

    void screenValidationAsMedia(String title, String target, String base64Png,
                                 Collection<AtfAssertion> assertions,
                                 Collection<AtfUiActionRecord> actions,
                                 AtfReportStatus status) {
        ensureOpen();
        evidencePublisher.publishScreenValidationAsMedia(title, target, base64Png,
                assertions, actions, status);
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
        ensureOpen();
        evidencePublisher.publishAction(keyword, title, target, base64Png, assertions,
                status, validationTitle, actions);
    }

    private void ensureOpen() {
        if (completed) throw new IllegalStateException("UI phase is already complete");
    }
}
