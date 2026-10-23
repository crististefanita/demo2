package atf.api.reporting.ui;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.ui.model.AtfUiActionRecord;
import atf.api.reporting.ui.validation.UiApiValidationEvidence;

import java.util.List;

/** Immutable checkpoint buffered until its unified business flow is published. */
record UiUnifiedFlowCheckpoint(String keyword, String title, String target,
                               String base64Png, List<AtfAssertion> assertions,
                               AtfReportStatus status, String validationTitle,
                               List<AtfUiActionRecord> actions,
                               UiApiValidationEvidence apiValidation,
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
        apiValidation = apiValidation == null
                ? new UiApiValidationEvidence(List.of(), List.of()) : apiValidation;
        capturePoint = capturePoint == null
                ? ReportScreenshotPolicy.CapturePoint.CHECKPOINT : capturePoint;
    }

    UiUnifiedFlowCheckpoint(String keyword, String title, String target,
                            String base64Png, List<AtfAssertion> assertions,
                            AtfReportStatus status, String validationTitle,
                            List<AtfUiActionRecord> actions,
                            List<ReportExecutionEvidence> apiEvidence,
                            ReportScreenshotPolicy.CapturePoint capturePoint,
                            boolean mediaScreenshot) {
        this(keyword, title, target, base64Png, assertions, status, validationTitle,
                actions, new UiApiValidationEvidence(List.of(), apiEvidence == null ? List.of()
                        : apiEvidence.stream().filter(java.util.Objects::nonNull)
                        .map(evidence -> new UiApiValidationEvidence.Exchange(evidence, List.of(),
                                evidence.response() != null && evidence.response().status() < 0
                                        ? AtfReportStatus.worst(AtfReportStatus.orInfo(status), AtfReportStatus.WARNING)
                                        : status))
                        .toList()), capturePoint, mediaScreenshot);
    }

    List<ReportExecutionEvidence> apiEvidence() {
        return apiValidation.exchanges().stream()
                .map(UiApiValidationEvidence.Exchange::evidence).toList();
    }

    String gherkinTitle() {
        return ((keyword.isBlank() ? "" : keyword + " ") + title).trim();
    }
}
