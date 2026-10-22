package atf.ui.practiceshop.support;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;

/** Maps the consumer's legacy screenshot switch to the generic evidence profile. */
public final class PracticeShopScreenshotPolicy {
    private PracticeShopScreenshotPolicy() { }

    public static ReportEvidenceProfile profile(boolean screenshotOnFailure) {
        return ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS)
                .screenshotPolicy(screenshotOnFailure
                        ? ReportScreenshotPolicy.EACH_CHECKPOINT
                        : ReportScreenshotPolicy.FAILURE_ONLY)
                .captureSource(ReportEvidenceCaptureSource.BROWSER)
                .build();
    }

    /** One consolidated UI flow with only its final browser state published. */
    public static ReportEvidenceProfile singleFlowFinalScreenshot() {
        return ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.UNIFIED_FLOW)
                .screenshotPolicy(ReportScreenshotPolicy.FINAL_ONLY)
                .captureSource(ReportEvidenceCaptureSource.BROWSER)
                .build();
    }
}
