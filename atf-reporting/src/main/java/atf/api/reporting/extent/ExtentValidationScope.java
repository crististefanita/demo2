package atf.api.reporting.extent;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.reporting.ExtentReportTree;
import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;
import java.util.List;

final class ExtentValidationScope implements AtfReportPorts.Validation {
    private final ExtentReportTree tree;
    private final ExtentRequestScope owner;
    private final ExtentTest validation;
    private List<AtfAssertion> assertions = List.of();
    private boolean completed;
    private boolean closed;

    ExtentValidationScope(ExtentReportTree tree, ExtentRequestScope owner, ExtentTest validation) {
        this.tree = tree;
        this.owner = owner;
        this.validation = validation;
        tree.markStarted(validation);
    }

    @Override public void assertions(Collection<AtfAssertion> rows) {
        assertions = rows == null ? List.of() : List.copyOf(rows);
    }

    @Override public void complete(AtfReportStatus status) {
        tree.ensureOpen();
        if (completed) throw new IllegalStateException("Validation is already complete");
        tree.renderAssertions(validation, assertions);
        tree.complete(validation, status);
        tree.markEnded(validation);
        completed = true;
        owner.record(status);
        owner.validationCompleted();
    }

    @Override public void close() { closed = true; }
}
