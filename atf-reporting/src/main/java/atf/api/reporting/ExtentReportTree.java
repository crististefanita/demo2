package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.ReportStatusAggregator;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.reporting.extent.ExtentScenarioScope;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Owns the Extent tree and creates the lifecycle scopes behind the session facade. */
public final class ExtentReportTree {
    private final ExtentReports reports;
    private final ReportingSettings settings;
    private final SensitiveDataRedactor redactor;
    private final Runnable ensureSessionOpen;
    private final Map<String, ExtentTest> classRoots = new LinkedHashMap<>();
    private final Map<String, ReportStatusAggregator> classStatuses = new LinkedHashMap<>();

    public ExtentReportTree(ExtentReports reports, ReportingSettings settings,
                            SensitiveDataRedactor redactor, Runnable ensureSessionOpen) {
        this.reports = reports;
        this.settings = settings;
        this.redactor = redactor;
        this.ensureSessionOpen = ensureSessionOpen;
    }

    public AtfReportPorts.Scenario startScenario(String testClass, String scenario,
                                                  Collection<String> tags) {
        ensureOpen();
        String className = testClass == null || testClass.isBlank() ? "API tests" : testClass;
        ExtentTest classRoot = classRoots.computeIfAbsent(className, ignored -> {
            ExtentTest root = reports.createTest(className);
            ExtentNodeLifecycle.start(root);
            return root;
        });
        classStatuses.computeIfAbsent(className, ignored -> new ReportStatusAggregator());
        ExtentTest test = classRoot.createNode(scenario == null ? "Scenario" : scenario);
        ExtentNodeLifecycle.start(test);
        if (tags != null && !tags.isEmpty()) test.assignCategory(tags.toArray(String[]::new));
        return new ExtentScenarioScope(this, test, className);
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

    public void renderDetails(ExtentTest node, List<String[]> details) {
        if (details.isEmpty()) return;
        node.info(AtfReportLabels.responseDetails(details.size()));
        node.info(AtfReportMarkup.table("helm-evidence-table helm-trace-table",
                EvidenceTableFormatter.toExtentRows(details)));
    }

    public void renderJson(ExtentTest node, List<String[]> json) {
        for (String[] item : json) {
            node.info(item[0] == null || item[0].isBlank() ? "JSON BODY" : item[0]);
            node.info(MarkupHelper.createCodeBlock(ExtentReportNodeSupport.display(item[1], settings)));
        }
    }

    public void renderAssertions(ExtentTest node, Collection<AtfAssertion> assertions) {
        var table = new String[assertions.size() + 1][4];
        table[0] = new String[]{AtfReportLabels.ASSERTION, AtfReportLabels.EXPECTED,
                AtfReportLabels.ACTUAL, AtfReportLabels.RESULT};
        int index = 0;
        for (var row : assertions) {
            table[++index] = new String[]{
                    redactor.redactText(row.name()),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.expected()), settings),
                    ExtentReportNodeSupport.display(redactor.redactField(row.name(), row.actual()), settings),
                    ExtentReportNodeSupport.resultMarker(row.resultStatus())};
        }
        node.info(AtfReportMarkup.table("helm-evidence-table helm-validation-table", table));
    }

}
