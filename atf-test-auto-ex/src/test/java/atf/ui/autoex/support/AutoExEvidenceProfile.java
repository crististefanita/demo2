package atf.ui.autoex.support;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.testkit.evidence.AtfUiEvidenceFlow;
import atf.api.testkit.evidence.AtfUiScreenshotPolicy;

import java.util.Objects;

/** Builds the browser-evidence profile shared by the report session and each E2E run. */
public final class AutoExEvidenceProfile {
    private AutoExEvidenceProfile() { }

    public static ReportEvidenceProfile forRun(AtfUiEvidenceFlow flow,
                                               AtfUiScreenshotPolicy screenshots,
                                               ReportScreenshotPolicy configuredPolicy) {
        Objects.requireNonNull(flow, "flow");
        Objects.requireNonNull(screenshots, "screenshots");
        Objects.requireNonNull(configuredPolicy, "configuredPolicy");
        ReportScreenshotPolicy selectedPolicy = switch (screenshots) {
            case CONFIGURED -> configuredPolicy;
            case EACH_CHECKPOINT -> ReportScreenshotPolicy.EACH_CHECKPOINT;
            case FINAL_ONLY -> ReportScreenshotPolicy.FINAL_ONLY;
            case FAILURE_ONLY -> ReportScreenshotPolicy.FAILURE_ONLY;
            case FINAL_AND_FAILURE -> ReportScreenshotPolicy.FINAL_AND_FAILURE;
            case NONE -> ReportScreenshotPolicy.NONE;
        };
        ReportEvidenceFlowMode flowMode = switch (flow) {
            case CONFIGURED, SCREEN_CHECKPOINTS -> ReportEvidenceFlowMode.SCREEN_CHECKPOINTS;
            case UNIFIED_FLOW -> ReportEvidenceFlowMode.UNIFIED_FLOW;
        };
        return ReportEvidenceProfile.builder()
                .flowMode(flowMode)
                .screenshotPolicy(selectedPolicy)
                .captureSource(ReportEvidenceCaptureSource.BROWSER)
                .build();
    }
}
