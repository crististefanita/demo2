package atf.api.reporting.ui;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;


/** Owns scenario completion state while the public UI facade stays thin. */
public final class UiScenarioLifecycle implements AutoCloseable {
    private final AtfReportPorts.Scenario delegate;
    private final ReportScreenshotPolicy screenshotPolicy;
    private final ReportEvidenceFlowMode flowMode;
    private final String scenarioTitle;
    private boolean completed;

    UiScenarioLifecycle(AtfReportPorts.Scenario delegate, ReportEvidenceProfile evidenceProfile,
                        String scenarioTitle) {
        this.delegate = delegate;
        this.screenshotPolicy = evidenceProfile.screenshotPolicy();
        this.flowMode = evidenceProfile.flowMode();
        this.scenarioTitle = scenarioTitle == null ? "" : scenarioTitle.trim();
    }

    UiPhaseLifecycle startPhase(String name) {
        ensureOpen();
        return new UiPhaseLifecycle(delegate.startPhase(name), screenshotPolicy,
                flowMode, scenarioTitle, name);
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

    private void ensureOpen() {
        if (completed) throw new IllegalStateException("UI scenario is already complete");
    }
}
