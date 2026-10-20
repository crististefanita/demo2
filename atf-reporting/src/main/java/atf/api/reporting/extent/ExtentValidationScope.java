package atf.api.reporting.extent;
import atf.api.reporting.console.AtfConsoleEvent;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportTree;
import atf.api.reporting.model.ReportScreenshot;
import atf.api.reporting.semantic.AtfReportSemanticRecorder;

import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.ui.AtfUiEvidenceSource;
import com.aventstack.extentreports.ExtentTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class ExtentValidationScope implements AtfReportPorts.Validation {
    private final ExtentReportTree tree;
    private final ExtentRequestScope owner;
    private final ExtentTest validation;
    private final String validationTitle;
    private final AtfReportSemanticRecorder.ValidationHandle semantic;
    private List<AtfAssertion> assertions = List.of();
    private final List<AtfUiActionRecord> uiActions = new ArrayList<>();
    private final List<ReportScreenshot> screenshots = new ArrayList<>();
    private final List<ReportScreenshot> mediaScreenshots = new ArrayList<>();
    private boolean completed;
    private boolean closed;
    private boolean screenshotOptional;

    ExtentValidationScope(ExtentReportTree tree, ExtentRequestScope owner, ExtentTest validation,
                          String validationTitle, AtfReportSemanticRecorder.ValidationHandle semantic) {
        this.tree = tree;
        this.owner = owner;
        this.validation = validation;
        this.validationTitle = validationTitle;
        this.semantic = semantic;
        tree.markStarted(validation);
    }

    @Override public void assertions(Collection<AtfAssertion> rows) {
        assertions = rows == null ? List.of() : List.copyOf(rows);
        semantic.assertions(rows);
    }

    @Override public void uiActions(Collection<AtfUiActionRecord> actions) {
        if (actions != null) uiActions.addAll(actions);
        semantic.uiActions(actions);
    }

    @Override public void screenshot(String title, String base64Png) {
        if (base64Png == null || base64Png.isBlank()) {
            throw new IllegalArgumentException("base64Png must not be blank");
        }
        screenshots.add(new ReportScreenshot(title, base64Png));
        semantic.screenshot();
    }

    @Override public void mediaScreenshot(String title, String base64Png) {
        if (base64Png == null || base64Png.isBlank()) {
            throw new IllegalArgumentException("base64Png must not be blank");
        }
        mediaScreenshots.add(new ReportScreenshot(title, base64Png));
        semantic.screenshot();
    }

    @Override public void complete(AtfReportStatus status) {
        tree.ensureOpen();
        if (completed) throw new IllegalStateException("Validation is already complete");
        if (screenshotOptional) {
            tree.renderUiActionsAsMedia(validation, uiActions);
        } else if (mediaScreenshots.isEmpty()) {
            tree.renderUiActions(validation, uiActions, screenshots);
            if (uiActions.stream().noneMatch(action -> action.source() == AtfUiEvidenceSource.ACTION)) {
                tree.renderScreenshots(validation, screenshots);
            }
        } else {
            if (!screenshots.isEmpty()) {
                throw new IllegalStateException("A validation cannot mix inline and media screenshots");
            }
            // Media mode still uses the same local contract as screenshot mode
            // whenever browser actions exist: one action table followed by
            // exactly one Base64 data link. Observation-only validations keep
            // their screenshot even though no action table is required.
            boolean hasBrowserActions = uiActions.stream()
                    .anyMatch(action -> action.source() == AtfUiEvidenceSource.ACTION);
            if (hasBrowserActions) {
                tree.renderUiActions(validation, uiActions, mediaScreenshots);
            } else {
                tree.renderUiActions(validation, uiActions, List.of());
                tree.renderScreenshots(validation, mediaScreenshots);
            }
        }
        tree.renderAssertions(validation, assertions);
        tree.complete(validation, status);
        tree.markEnded(validation);
        tree.consoleCompleted(atf.api.reporting.console.AtfConsoleEvent.Scope.VALIDATION,
                validationTitle, status, validation);
        completed = true;
        owner.record(status);
        semantic.complete(status);
        owner.validationCompleted();
    }

    @Override public void completeFailure(AtfReportStatus status) {
        // A failure may happen before a page can be captured, but an already
        // supplied screenshot is still first-class evidence and must not be
        // discarded by the failure completion path.
        screenshotOptional = screenshots.isEmpty() && mediaScreenshots.isEmpty();
        complete(status);
    }

    @Override public void close() {
        if (!closed) {
            // A validation may exit through an exception before its normal
            // complete() call. Release the owner's open-scope guard so the
            // request can still publish its failure and close the console
            // nesting deterministically.
            if (!completed) owner.validationCompleted();
            closed = true;
        }
    }
}
