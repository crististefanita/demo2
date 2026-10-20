package atf.api.reporting.evidence.profile;

import atf.api.reporting.evidence.ReportScreenshotPolicy;

import java.util.Objects;

/**
 * Typed, browser-neutral composition of the three evidence axes.
 *
 * <p>The profile is policy and provenance, not a browser executor. A
 * composition root maps YAML or builder input to this object and supplies
 * the actual screenshot payload through the existing reporting lifecycle.</p>
 */
public record ReportEvidenceProfile(ReportEvidenceFlowMode flowMode,
                                    ReportScreenshotPolicy screenshotPolicy,
                                    ReportEvidenceCaptureSource captureSource) {
    public ReportEvidenceProfile {
        flowMode = Objects.requireNonNull(flowMode, "flowMode");
        screenshotPolicy = Objects.requireNonNull(screenshotPolicy, "screenshotPolicy");
        captureSource = Objects.requireNonNull(captureSource, "captureSource");
    }

    /** Backward-compatible profile for the existing checkpoint lifecycle. */
    public static ReportEvidenceProfile defaults() {
        return new ReportEvidenceProfile(
                ReportEvidenceFlowMode.SCREEN_CHECKPOINTS,
                ReportScreenshotPolicy.EACH_CHECKPOINT,
                ReportEvidenceCaptureSource.CUSTOM_PROVIDER);
    }

    public static ReportEvidenceProfile forScreenshotPolicy(ReportScreenshotPolicy policy) {
        return new ReportEvidenceProfile(
                ReportEvidenceFlowMode.SCREEN_CHECKPOINTS,
                Objects.requireNonNull(policy, "screenshotPolicy"),
                ReportEvidenceCaptureSource.CUSTOM_PROVIDER);
    }

    /** Maps the three documented YAML values at a composition boundary. */
    public static ReportEvidenceProfile fromConfig(String flowMode,
                                                   String screenshotPolicy,
                                                   String captureSource) {
        return new ReportEvidenceProfile(
                ReportEvidenceFlowMode.fromConfig(flowMode),
                ReportScreenshotPolicy.fromConfig(screenshotPolicy),
                ReportEvidenceCaptureSource.fromConfig(captureSource));
    }

    public boolean captures(ReportScreenshotPolicy.CapturePoint point) {
        return screenshotPolicy.captures(point);
    }

    public Builder toBuilder() {
        return builder().flowMode(flowMode)
                .screenshotPolicy(screenshotPolicy)
                .captureSource(captureSource);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Small typed builder for YAML/composition-root mapping. */
    public static final class Builder {
        private ReportEvidenceFlowMode flowMode = ReportEvidenceFlowMode.SCREEN_CHECKPOINTS;
        private ReportScreenshotPolicy screenshotPolicy = ReportScreenshotPolicy.EACH_CHECKPOINT;
        private ReportEvidenceCaptureSource captureSource = ReportEvidenceCaptureSource.CUSTOM_PROVIDER;

        public Builder flowMode(ReportEvidenceFlowMode value) {
            flowMode = Objects.requireNonNull(value, "flowMode");
            return this;
        }

        public Builder screenshotPolicy(ReportScreenshotPolicy value) {
            screenshotPolicy = Objects.requireNonNull(value, "screenshotPolicy");
            return this;
        }

        public Builder captureSource(ReportEvidenceCaptureSource value) {
            captureSource = Objects.requireNonNull(value, "captureSource");
            return this;
        }

        public ReportEvidenceProfile build() {
            return new ReportEvidenceProfile(flowMode, screenshotPolicy, captureSource);
        }
    }
}
