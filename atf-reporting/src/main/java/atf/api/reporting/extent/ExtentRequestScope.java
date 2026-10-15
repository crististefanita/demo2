package atf.api.reporting.extent;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.ReportStatusAggregator;
import atf.api.reporting.ExtentReportTree;
import com.aventstack.extentreports.ExtentTest;

import java.util.ArrayList;
import java.util.List;

final class ExtentRequestScope implements AtfReportPorts.Request {
    private final ExtentReportTree tree;
    private final ExtentPhaseScope phase;
    private final ExtentTest request;
    private final String requestTitle;
    private final List<String[]> details = new ArrayList<>();
    private final List<String[]> json = new ArrayList<>();
    private final ReportStatusAggregator statuses = new ReportStatusAggregator();
    private ApiExecutionResult execution;
    private int openValidations;
    private boolean closed;

    ExtentRequestScope(ExtentReportTree tree, ExtentPhaseScope phase, ExtentTest request,
                       String requestTitle) {
        this.tree = tree;
        this.phase = phase;
        this.request = request;
        this.requestTitle = requestTitle;
        tree.markStarted(request);
    }

    @Override public void detail(String name, String value) {
        String redacted = tree.redactor().redactField(name, value);
        details.add(new String[]{tree.normalizeDetailName(name), redacted});
        tree.consoleDetail(atf.api.core.reporting.AtfConsoleEvent.Scope.REQUEST,
                tree.normalizeDetailName(name), redacted);
    }

    @Override public void json(String label, String value) {
        String redacted = tree.redactor().redactJson(value);
        json.add(new String[]{label, redacted});
        tree.consoleCode(atf.api.core.reporting.AtfConsoleEvent.Scope.REQUEST, label, redacted);
    }

    @Override public void execution(ApiExecutionResult result) {
        execution = result;
    }

    @Override public void complete(AtfReportStatus status) {
        ensureRequestOpen();
        if (openValidations > 0) {
            throw new IllegalStateException("Request cannot complete while a validation is open");
        }
        tree.renderRequestDetails(request, details, json);
        tree.renderResponseDetails(request, details, json);
        tree.renderExecution(request, execution);
        var effective = AtfReportStatus.worst(statuses.aggregate(), status);
        tree.complete(request, effective);
        tree.markEnded(request);
        phase.record(effective);
        tree.consoleCompleted(atf.api.core.reporting.AtfConsoleEvent.Scope.REQUEST,
                requestTitle, effective, request);
        closed = true;
    }

    @Override public AtfReportPorts.Validation startValidation(String title) {
        ensureRequestOpen();
        openValidations++;
        String validationTitle = title == null ? "Validation" : title;
        tree.consoleStarted(atf.api.core.reporting.AtfConsoleEvent.Scope.VALIDATION, validationTitle);
        return new ExtentValidationScope(tree, this, phase.node().createNode(validationTitle), validationTitle);
    }

    void record(AtfReportStatus status) { statuses.record(status); }
    void validationCompleted() { openValidations--; }
    @Override public void close() { closed = true; }

    private void ensureRequestOpen() {
        tree.ensureOpen();
        if (closed) throw new IllegalStateException("Request is closed");
    }
}
