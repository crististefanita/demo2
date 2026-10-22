package atf.api.reporting.ui;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.model.ReportExecutionEvidence;

import java.util.List;

/** Immutable checkpoint buffered until its unified business flow is published. */
record UiUnifiedFlowCheckpoint(String keyword, String title, String target,
                               String base64Png, List<AtfAssertion> assertions,
                               AtfReportStatus status, String validationTitle,
                               List<AtfUiActionRecord> actions,
                               List<ReportExecutionEvidence> apiEvidence,
                               ReportScreenshotPolicy.CapturePoint capturePoint,
                               boolean mediaScreenshot) {
    UiUnifiedFlowCheckpoint {
        keyword = keyword == null ? "" : keyword.trim();
        title = title == null ? "" : title.trim();
        target = target == null ? "" : target.trim();
        base64Png = base64Png == null ? "" : base64Png;
        assertions = assertions == null ? List.of() : List.copyOf(assertions);
        status = status == null ? AtfReportStatus.PASS : status;
        validationTitle = validationTitle == null ? "" : validationTitle.trim();
        actions = actions == null ? List.of() : List.copyOf(actions);
        apiEvidence = apiEvidence == null ? List.of() : List.copyOf(apiEvidence);
        capturePoint = capturePoint == null
                ? ReportScreenshotPolicy.CapturePoint.CHECKPOINT : capturePoint;
    }

    String gherkinTitle() {
        return ((keyword.isBlank() ? "" : keyword + " ") + title).trim();
    }
}
