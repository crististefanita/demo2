package atf.api.reporting.extent;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.reporting.ExtentReportTree;
import com.aventstack.extentreports.ExtentTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class ExtentValidationScope implements AtfReportPorts.Validation {
    private final ExtentReportTree tree;
    private final ExtentRequestScope owner;
    private final ExtentTest validation;
    private final String validationTitle;
    private List<AtfAssertion> assertions = List.of();
    private final List<AtfUiActionRecord> uiActions = new ArrayList<>();
    private final List<String[]> screenshots = new ArrayList<>();
    private final List<String[]> mediaScreenshots = new ArrayList<>();
    private boolean completed;
    private boolean closed;

    ExtentValidationScope(ExtentReportTree tree, ExtentRequestScope owner, ExtentTest validation,
                          String validationTitle) {
        this.tree = tree;
        this.owner = owner;
        this.validation = validation;
        this.validationTitle = validationTitle;
        tree.markStarted(validation);
    }

    @Override public void assertions(Collection<AtfAssertion> rows) {
        assertions = rows == null ? List.of() : List.copyOf(rows);
    }

    @Override public void uiActions(Collection<AtfUiActionRecord> actions) {
        if (actions != null) uiActions.addAll(actions);
    }

    @Override public void screenshot(String title, String base64Png) {
        if (base64Png == null || base64Png.isBlank()) {
            throw new IllegalArgumentException("base64Png must not be blank");
        }
        screenshots.add(new String[]{title == null || title.isBlank() ? "UI screenshot" : title,
                base64Png});
    }

    @Override public void mediaScreenshot(String title, String base64Png) {
        if (base64Png == null || base64Png.isBlank()) {
            throw new IllegalArgumentException("base64Png must not be blank");
        }
        mediaScreenshots.add(new String[]{title == null || title.isBlank() ? "UI screenshot" : title,
                base64Png});
    }

    @Override public void complete(AtfReportStatus status) {
        tree.ensureOpen();
        if (completed) throw new IllegalStateException("Validation is already complete");
        if (mediaScreenshots.isEmpty()) {
            tree.renderUiActions(validation, uiActions, screenshots);
            if (uiActions.isEmpty()) tree.renderScreenshots(validation, screenshots);
        } else {
            if (!screenshots.isEmpty()) {
                throw new IllegalStateException("A validation cannot mix inline and media screenshots");
            }
            tree.renderUiActionsAsMedia(validation, uiActions);
            tree.renderScreenshots(validation, mediaScreenshots);
        }
        tree.renderAssertions(validation, assertions);
        tree.complete(validation, status);
        tree.markEnded(validation);
        tree.consoleCompleted(atf.api.core.reporting.AtfConsoleEvent.Scope.VALIDATION,
                validationTitle, status, validation);
        completed = true;
        owner.record(status);
        owner.validationCompleted();
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
