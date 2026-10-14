package atf.api.reporting.extent;

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
    private final List<String[]> details = new ArrayList<>();
    private final List<String[]> json = new ArrayList<>();
    private final ReportStatusAggregator statuses = new ReportStatusAggregator();
    private int openValidations;
    private boolean closed;

    ExtentRequestScope(ExtentReportTree tree, ExtentPhaseScope phase, ExtentTest request) {
        this.tree = tree;
        this.phase = phase;
        this.request = request;
        tree.markStarted(request);
    }

    @Override public void detail(String name, String value) {
        details.add(new String[]{tree.normalizeDetailName(name),
                tree.redactor().redactField(name, value)});
    }

    @Override public void json(String label, String value) {
        json.add(new String[]{label, tree.redactor().redactJson(value)});
    }

    @Override public void complete(AtfReportStatus status) {
        ensureRequestOpen();
        if (openValidations > 0) {
            throw new IllegalStateException("Request cannot complete while a validation is open");
        }
        tree.renderDetails(request, details);
        tree.renderJson(request, json);
        var effective = AtfReportStatus.worst(statuses.aggregate(), status);
        tree.complete(request, effective);
        tree.markEnded(request);
        phase.record(effective);
        closed = true;
    }

    @Override public AtfReportPorts.Validation startValidation(String title) {
        ensureRequestOpen();
        openValidations++;
        return new ExtentValidationScope(tree, this, phase.node().createNode(title));
    }

    void record(AtfReportStatus status) { statuses.record(status); }
    void validationCompleted() { openValidations--; }
    @Override public void close() { closed = true; }

    private void ensureRequestOpen() {
        tree.ensureOpen();
        if (closed) throw new IllegalStateException("Request is closed");
    }
}
