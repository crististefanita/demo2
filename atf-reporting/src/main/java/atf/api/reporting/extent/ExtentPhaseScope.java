package atf.api.reporting.extent;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.ReportStatusAggregator;
import atf.api.reporting.ExtentReportTree;
import com.aventstack.extentreports.ExtentTest;

import java.util.ArrayList;
import java.util.List;

final class ExtentPhaseScope implements AtfReportPorts.Phase {
    private final ExtentReportTree tree;
    private final ExtentScenarioScope scenario;
    private final ExtentTest phase;
    private final String phaseName;
    private final ReportStatusAggregator statuses = new ReportStatusAggregator();
    private final List<String[]> details = new ArrayList<>();
    private boolean closed;

    ExtentPhaseScope(ExtentReportTree tree, ExtentScenarioScope scenario, ExtentTest phase,
                     String phaseName) {
        this.tree = tree;
        this.scenario = scenario;
        this.phase = phase;
        this.phaseName = phaseName;
        tree.markStarted(phase);
    }

    @Override public AtfReportPorts.Request startRequest(String title, String method, String path) {
        ensurePhaseOpen();
        String requestTitle = tree.renderRequestTitle(title, method, path);
        tree.consoleStarted(atf.api.core.reporting.AtfConsoleEvent.Scope.REQUEST, requestTitle);
        return new ExtentRequestScope(tree, this, phase.createNode(requestTitle), requestTitle);
    }

    @Override public void detail(String name, String value) {
        details.add(new String[]{name == null || name.isBlank() ? "DETAIL" : name,
                tree.redactor().redactField(name, value)});
        tree.consoleDetail(atf.api.core.reporting.AtfConsoleEvent.Scope.PHASE,
                name, tree.redactor().redactField(name, value));
    }

    @Override public void complete(AtfReportStatus status) {
        ensurePhaseOpen();
        tree.renderDetails(phase, details);
        var effective = AtfReportStatus.worst(statuses.aggregate(), status);
        tree.complete(phase, effective);
        tree.markEnded(phase);
        scenario.record(effective);
        tree.consoleCompleted(atf.api.core.reporting.AtfConsoleEvent.Scope.PHASE,
                phaseName, effective, phase);
        closed = true;
    }

    void record(AtfReportStatus status) { statuses.record(status); }
    ExtentTest node() { return phase; }
    @Override public void close() { closed = true; }

    private void ensurePhaseOpen() {
        tree.ensureOpen();
        if (closed) throw new IllegalStateException("Phase is closed");
    }
}
