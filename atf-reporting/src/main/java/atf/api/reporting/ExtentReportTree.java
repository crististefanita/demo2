package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfConsoleEvent;
import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.ReportStatusAggregator;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.reporting.extent.ExtentScenarioScope;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Owns the Extent tree and creates the lifecycle scopes behind the session facade. */
public final class ExtentReportTree {
    private final ExtentReports reports;
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;
    private final ExtentEvidenceRenderer evidence;
    private final Runnable ensureSessionOpen;
    private final AtfConsoleSink console;
    private final AtfReportSemanticRecorder semantic = new AtfReportSemanticRecorder();
    private final Map<String, ExtentTest> classRoots = new LinkedHashMap<>();
    private final Map<String, ReportStatusAggregator> classStatuses = new LinkedHashMap<>();
    private final Map<String, String> classPackages = new LinkedHashMap<>();

    public ExtentReportTree(ExtentReports reports, ReportingSettings settings,
                            SensitiveDataRedactor redactor, Runnable ensureSessionOpen) {
        this(reports, settings, redactor, ensureSessionOpen, AtfConsoleSink.noOp());
    }

    public ExtentReportTree(ExtentReports reports, ReportingSettings settings,
                            SensitiveDataRedactor redactor, Runnable ensureSessionOpen,
                            AtfConsoleSink console) {
        this.reports = reports;
        this.settings = settings;
        this.redactor = redactor;
        this.evidence = new ExtentEvidenceRenderer(settings, redactor);
        this.ensureSessionOpen = ensureSessionOpen;
        this.console = console == null ? AtfConsoleSink.noOp() : console;
    }

    public AtfReportPorts.Scenario startScenario(String testClass, String scenario,
                                                  Collection<String> tags) {
        return startScenario(testClass, null, scenario, tags);
    }

    public AtfReportPorts.Scenario startScenario(String testClass, String testPackage,
                                                  String scenario, Collection<String> tags) {
        ensureOpen();
        String className = testClass == null || testClass.isBlank() ? "API tests" : testClass;
        String packageName = testPackage == null ? "" : testPackage.trim();
        ExtentTest classRoot = classRoots.computeIfAbsent(className, ignored -> {
            ExtentTest root = reports.createTest(className);
            if (!packageName.isBlank()) root.getModel().setDescription(packageName);
            ExtentNodeLifecycle.start(root);
            return root;
        });
        if (!packageName.isBlank()) classPackages.putIfAbsent(className, packageName);
        classStatuses.computeIfAbsent(className, ignored -> new ReportStatusAggregator());
        ExtentTest test = classRoot.createNode(scenario == null ? "Scenario" : scenario);
        ExtentNodeLifecycle.start(test);
        if (tags != null && !tags.isEmpty()) test.assignCategory(tags.toArray(String[]::new));
        console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.SCENARIO,
                className + " :: " + (scenario == null ? "Scenario" : scenario)));
        return new ExtentScenarioScope(this, test, className,
                semantic.startScenario(className, packageName, scenario, tags));
    }

    public void completeClassRoots() {
        classStatuses.forEach((name, status) -> {
            var root = classRoots.get(name);
            if (root != null) {
                ExtentNodeLifecycle.complete(root, status.aggregate());
                ExtentNodeLifecycle.end(root);
            }
        });
    }

    public void ensureOpen() { ensureSessionOpen.run(); }

    public ReportingSettings settings() { return settings; }
    public SensitiveDataRedactor redactor() { return redactor; }
    public AtfReportSemanticSnapshot semanticSnapshot() { return semantic.snapshot(); }
    public ExtentTest classRoot(String className) { return classRoots.get(className); }

    public String renderRequestTitle(String title, String method, String path) {
        return ExtentReportNodeSupport.renderRequestTitle(title, method, path);
    }

    public String normalizeDetailName(String name) {
        return ExtentReportNodeSupport.normalizeDetailName(name);
    }

    public void recordClassStatus(String className, AtfReportStatus status) {
        classStatuses.get(className).record(status);
    }

    public void complete(ExtentTest node, AtfReportStatus status) {
        ExtentNodeLifecycle.complete(node, status);
    }

    public void markStarted(ExtentTest node) { ExtentNodeLifecycle.start(node); }
    public void markEnded(ExtentTest node) { ExtentNodeLifecycle.end(node); }

    public void renderRequestDetails(ExtentTest node, List<String[]> details, List<String[]> json) {
        evidence.renderRequestDetails(node, details, json);
    }

    public void renderDetails(ExtentTest node, List<String[]> details) {
        evidence.renderDetails(node, details);
    }

    public void renderResponseDetails(ExtentTest node, List<String[]> details, List<String[]> json) {
        evidence.renderResponseDetails(node, details, json);
    }

    /** Publishes one renderer-neutral transport evidence block at the request boundary. */
    public void renderExecution(ExtentTest node, ApiExecutionResult execution) {
        evidence.renderExecution(node, execution);
    }

    public void renderScreenshots(ExtentTest node, List<String[]> screenshots) {
        evidence.renderScreenshots(node, screenshots);
    }

    public void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions) {
        evidence.renderUiActions(node, actions, List.of());
    }

    public void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions,
                                List<String[]> screenshots) {
        evidence.renderUiActions(node, actions, screenshots);
    }

    /** Renders a Playwright action table without embedding its screenshot. */
    public void renderUiActionsAsMedia(ExtentTest node, List<AtfUiActionRecord> actions) {
        evidence.renderUiActionsAsMedia(node, actions);
    }

    public void renderAssertions(ExtentTest node, Collection<AtfAssertion> assertions) {
        evidence.renderAssertions(node, assertions);
    }

    public void consoleStarted(AtfConsoleEvent.Scope scope, String name) {
        console.publish(AtfConsoleEvent.started(scope, name));
    }

    public void consoleDetail(AtfConsoleEvent.Scope scope, String name, String value) {
        console.publish(AtfConsoleEvent.detail(scope, name, value));
    }

    public void consoleCode(AtfConsoleEvent.Scope scope, String name, String value) {
        console.publish(AtfConsoleEvent.code(scope, name, value));
    }

    public void consoleCompleted(AtfConsoleEvent.Scope scope, String name,
                                 AtfReportStatus status, ExtentTest node) {
        console.publish(AtfConsoleEvent.completed(scope, name, status,
                ExtentNodeLifecycle.durationMillis(node)));
    }

}
