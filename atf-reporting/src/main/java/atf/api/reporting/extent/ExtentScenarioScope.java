package atf.api.reporting.extent;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.ReportStatusAggregator;
import atf.api.reporting.ExtentReportTree;
import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;

public final class ExtentScenarioScope implements AtfReportPorts.Scenario {
    private final ExtentReportTree tree;
    private final ExtentTest test;
    private final String className;
    private final ReportStatusAggregator statuses = new ReportStatusAggregator();
    private boolean closed;

    public ExtentScenarioScope(ExtentReportTree tree, ExtentTest test, String className) {
        this.tree = tree;
        this.test = test;
        this.className = className;
    }

    @Override public String id() { return test.getModel().getName(); }

    @Override public AtfReportPorts.Phase startPhase(String name) {
        ensureScenarioOpen();
        String phaseName = name == null ? "" : name;
        tree.consoleStarted(atf.api.core.reporting.AtfConsoleEvent.Scope.PHASE, phaseName);
        return new ExtentPhaseScope(tree, this, test.createNode(phaseName), phaseName);
    }

    @Override public void complete(AtfReportStatus status) {
        ensureScenarioOpen();
        var effective = AtfReportStatus.worst(statuses.aggregate(), status);
        tree.complete(test, effective);
        tree.markEnded(test);
        tree.markEnded(tree.classRoot(className));
        tree.recordClassStatus(className, effective);
        tree.consoleCompleted(atf.api.core.reporting.AtfConsoleEvent.Scope.SCENARIO,
                className + " :: " + test.getModel().getName(), effective, test);
        closed = true;
    }

    @Override public boolean hasWarnings() { return statuses.hasWarnings(); }

    void record(AtfReportStatus status) { statuses.record(status); }
    @Override public void close() { closed = true; }

    private void ensureScenarioOpen() {
        tree.ensureOpen();
        if (closed) throw new IllegalStateException("Scenario is closed");
    }
}
