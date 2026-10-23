package atf.ui.autoex.support;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;

import java.util.Objects;

/** Builds the browser-evidence profile shared by the report session and each E2E run. */
public final class AutoExEvidenceProfile {
    private AutoExEvidenceProfile() { }

    public static ReportEvidenceProfile forRun(AutoExEvidenceMode.Mode mode,
                                        ReportScreenshotPolicy configuredPolicy) {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(configuredPolicy, "configuredPolicy");
        ReportScreenshotPolicy selectedPolicy = switch (mode) {
            case CONFIGURED -> configuredPolicy;
            case CHECKPOINT_SCREENSHOTS -> ReportScreenshotPolicy.EACH_CHECKPOINT;
            case FINAL_SCREENSHOT_ONLY -> ReportScreenshotPolicy.FINAL_ONLY;
        };
        ReportEvidenceFlowMode flowMode = switch (mode) {
            case FINAL_SCREENSHOT_ONLY -> ReportEvidenceFlowMode.UNIFIED_FLOW;
            case CONFIGURED, CHECKPOINT_SCREENSHOTS -> ReportEvidenceFlowMode.SCREEN_CHECKPOINTS;
        };
        return ReportEvidenceProfile.builder()
                .flowMode(flowMode)
                .screenshotPolicy(selectedPolicy)
                .captureSource(ReportEvidenceCaptureSource.BROWSER)
                .build();
    }
}
