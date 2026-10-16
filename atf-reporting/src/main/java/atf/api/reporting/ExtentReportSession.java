package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.security.SensitiveDataRedactor;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.Objects;

/** Public facade for the Extent boundary; tree scopes live behind this API. */
public final class ExtentReportSession implements AtfReportPorts.Session, AutoCloseable {
    private final Path reportPath;
    private final ExtentReportSessionPublication publication;
    private final ExtentReports reports;
    private final ExtentReportTree tree;
    private final long ownerThreadId = Thread.currentThread().getId();
    private boolean closed;

    public ExtentReportSession(Path reportPath) {
        this(reportPath, ReportingSettings.defaults(), new SensitiveDataRedactor());
    }

    public ExtentReportSession(Path reportPath, ReportingSettings settings) {
        this(reportPath, settings, new SensitiveDataRedactor());
    }

    /**
     * Opens the Extent boundary with an explicit evidence redactor. The
     * default constructors always use the conservative production policy so
     * callers cannot accidentally publish raw secrets to HTML.
     */
    public ExtentReportSession(Path reportPath, ReportingSettings settings, SensitiveDataRedactor redactor) {
        this(reportPath, settings, redactor, AtfConsoleSink.noOp());
    }

    public ExtentReportSession(Path reportPath, ReportingSettings settings,
                               SensitiveDataRedactor redactor, AtfConsoleSink console) {
        this.reportPath = Objects.requireNonNull(reportPath, "reportPath").toAbsolutePath().normalize();
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(redactor, "redactor");
        this.publication = ExtentReportSessionPublication.open(this.reportPath);
        var spark = new ExtentSparkReporter(publication.stagedReport().toString());
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Configurable API Automation");
        spark.config().setReportName("Configurable API Automation");
        spark.config().setOfflineMode(true);
        spark.config().setCss(AtfReportTheme.sparkCss());
        reports = new ExtentReports();
        reports.attachReporter(spark);
        ReportEnvironment.defaults().forEach(reports::setSystemInfo);
        tree = new ExtentReportTree(reports, settings, redactor, this::ensureOpen, console);
    }

    /** Opens an Extent session using the discovered atf.yaml/atf.yml configuration. */
    public static ExtentReportSession openConfigured() {
        return openConfigured(AtfReportConfigurationLoader.load());
    }

    /** Opens an Extent session using an explicit YAML configuration file. */
    public static ExtentReportSession openConfigured(Path configPath) {
        return openConfigured(AtfReportConfigurationLoader.load(configPath));
    }

    /** Opens a session from one already-resolved immutable configuration snapshot. */
    public static ExtentReportSession openConfigured(ExtentReportConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        return new ExtentReportSession(configuration.resolveReportPath(Clock.systemDefaultZone()),
                configuration.reportingSettings());
    }

    @Override public Path reportPath() { return reportPath; }

    /** Returns the immutable semantic population recorded by this session. */
    public AtfReportSemanticSnapshot semanticSnapshot() {
        return tree.semanticSnapshot();
    }

    @Override
    public AtfReportPorts.Scenario startScenario(String testClass, String scenario, Collection<String> tags) {
        ensureOpen();
        return tree.startScenario(testClass, scenario, tags);
    }

    @Override
    public AtfReportPorts.Scenario startScenario(String testClass, String testPackage,
                                                 String scenario, Collection<String> tags) {
        ensureOpen();
        return tree.startScenario(testClass, testPackage, scenario, tags);
    }

    @Override
    public void flush() {
        ensureOpen();
        reports.flush();
    }

    @Override
    public void close() {
        ensureOwnerThread();
        if (!closed) {
            try {
                tree.completeClassRoots();
                reports.flush();
                publication.publish();
            } catch (java.io.IOException error) {
                throw new IllegalStateException("Cannot finalize Extent report: " + reportPath, error);
            } finally {
                closed = true;
                publication.close();
            }
        }
    }

    private void ensureOpen() {
        ensureOwnerThread();
        if (closed) throw new IllegalStateException("Extent report session is closed");
    }

    private void ensureOwnerThread() {
        long currentThreadId = Thread.currentThread().getId();
        if (currentThreadId != ownerThreadId) {
            throw new IllegalStateException("Extent report session is thread-confined to owner thread "
                    + ownerThreadId + ", current thread is " + currentThreadId);
        }
    }
}
