package atf.api.reporting.extent;
import atf.api.reporting.console.AtfConsoleEvent;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.contract.ReportStatusAggregator;
import atf.api.reporting.extent.session.ExtentReportTree;
import atf.api.reporting.semantic.AtfReportSemanticRecorder;
import atf.api.reporting.model.diagnostic.ReportPhaseDiagnostic;

import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;
import java.util.List;

public final class ExtentScenarioScope implements AtfReportPorts.Scenario {
    private final ExtentReportTree tree;
    private final ExtentTest test;
    private final String className;
    private final ReportStatusAggregator statuses = new ReportStatusAggregator();
    private final AtfReportSemanticRecorder.ScenarioHandle semantic;
    private boolean closed;

    public ExtentScenarioScope(ExtentReportTree tree, ExtentTest test, String className,
                               AtfReportSemanticRecorder.ScenarioHandle semantic) {
        this.tree = tree;
        this.test = test;
        this.className = className;
        this.semantic = semantic;
    }

    @Override public String id() { return test.getModel().getName(); }

    @Override public AtfReportPorts.Phase startPhase(String name) {
        ensureScenarioOpen();
        String phaseName = name == null ? "" : name;
        tree.consoleStarted(atf.api.reporting.console.AtfConsoleEvent.Scope.PHASE, phaseName);
        return new ExtentPhaseScope(tree, this, test.createNode(phaseName), phaseName,
                semantic.startPhase(phaseName));
    }

    @Override public void complete(AtfReportStatus status) {
        ensureScenarioOpen();
        var effective = AtfReportStatus.worst(statuses.aggregate(), status);
        tree.complete(test, effective);
        tree.markEnded(test);
        tree.markEnded(tree.classRoot(className));
        tree.recordClassStatus(className, effective);
        semantic.complete(effective);
        tree.consoleCompleted(atf.api.reporting.console.AtfConsoleEvent.Scope.SCENARIO,
                className + " :: " + test.getModel().getName(), effective, test);
        closed = true;
    }

    @Override public boolean hasWarnings() { return statuses.hasWarnings(); }

    void record(AtfReportStatus status) { statuses.record(status); }
    void renderDiagnostics(List<ReportPhaseDiagnostic> diagnostics) {
        ensureScenarioOpen();
        tree.renderPhaseDiagnostics(test, diagnostics);
    }
    @Override public void close() { closed = true; }

    private void ensureScenarioOpen() {
        tree.ensureOpen();
        if (closed) throw new IllegalStateException("Scenario is closed");
    }
}
