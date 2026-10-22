package atf.api.reporting.ui;

import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.security.SensitiveDataRedactor;
import atf.api.reporting.configuration.AtfReportConfigurationLoader;
import atf.api.reporting.configuration.ExtentReportConfiguration;
import atf.api.reporting.configuration.ReportEnvironmentSnapshot;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.ui.AtfUiActionRecord;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * UI-test reporting facade. It deliberately creates a report separate from
 * the API artifact being inspected; the API artifact is an input, never the
 * owner of Playwright execution evidence.
 *
 * <p>Lifecycle state lives in package-private collaborators so this class
 * remains the stable public composition boundary. The nested types are kept
 * for source and binary compatibility with existing UI tests.</p>
 */
public final class UiExtentReportSession implements AutoCloseable {
    public static final String DEFAULT_FILE_NAME = "ui-atf-extent-report.html";

    private final ExtentReportSession delegate;
    private final ReportEvidenceProfile evidenceProfile;

    private UiExtentReportSession(ExtentReportSession delegate,
                                  ReportEvidenceProfile evidenceProfile) {
        this.delegate = delegate;
        this.evidenceProfile = Objects.requireNonNull(evidenceProfile, "evidenceProfile");
    }

    public static UiExtentReportSession openConfigured() {
        return openConfigured(AtfConsoleSink.noOp());
    }

    public static UiExtentReportSession openConfigured(AtfConsoleSink console) {
        var configuration = AtfReportConfigurationLoader.load();
        return open(configuration, UiExtentReportOptions.builder(configuration.outputDirectory())
                .fileName(DEFAULT_FILE_NAME)
                .console(console)
                .evidenceProfile(ReportEvidenceProfile.forScreenshotPolicy(
                        configuration.screenshotPolicy()))
                .build());
    }

    /** Opens the UI artifact in the directory selected by the UI YAML section. */
    public static UiExtentReportSession openConfigured(Path outputDirectory,
                                                       AtfConsoleSink console) {
        return openConfigured(UiExtentReportOptions.builder(outputDirectory)
                .fileName(DEFAULT_FILE_NAME)
                .console(console)
                .build());
    }

    /** Opens a separately named UI audit artifact without changing API input. */
    public static UiExtentReportSession openConfigured(Path outputDirectory,
                                                       String fileName,
                                                       AtfConsoleSink console) {
        return openConfigured(UiExtentReportOptions.builder(outputDirectory)
                .fileName(fileName)
                .console(console)
                .build());
    }

    /** Opens a UI report with an explicit, browser-neutral screenshot policy. */
    public static UiExtentReportSession openConfigured(Path outputDirectory,
                                                       String fileName,
                                                       AtfConsoleSink console,
                                                       ReportScreenshotPolicy screenshotPolicy) {
        return openConfigured(UiExtentReportOptions.builder(outputDirectory)
                .fileName(fileName)
                .console(console)
                .evidenceProfile(ReportEvidenceProfile.forScreenshotPolicy(screenshotPolicy))
                .build());
    }

    /** Opens a UI report with all evidence axes supplied as one typed profile. */
    public static UiExtentReportSession openConfigured(Path outputDirectory,
                                                       String fileName,
                                                       AtfConsoleSink console,
                                                       ReportEvidenceProfile evidenceProfile) {
        return openConfigured(UiExtentReportOptions.builder(outputDirectory)
                .fileName(fileName)
                .console(console)
                .evidenceProfile(evidenceProfile)
                .build());
    }

    /** Canonical typed entry point; all legacy overloads delegate here. */
    public static UiExtentReportSession openConfigured(UiExtentReportOptions options) {
        var configuration = AtfReportConfigurationLoader.load();
        return open(configuration, options);
    }

    /** Opens a report whose collision-resistant name was already resolved. */
    public static UiExtentReportSession openConfiguredResolved(Path outputDirectory,
                                                                String resolvedFileName,
                                                                AtfConsoleSink console) {
        return openConfiguredResolved(outputDirectory, resolvedFileName, console, Map.of());
    }

    /** Opens a collision-resistant UI report with consumer-owned System metadata. */
    public static UiExtentReportSession openConfiguredResolved(Path outputDirectory,
                                                                String resolvedFileName,
                                                                AtfConsoleSink console,
                                                                Map<String, String> systemInfo) {
        var configuration = AtfReportConfigurationLoader.load();
        return open(configuration, UiExtentReportOptions.builder(outputDirectory)
                .fileName(resolvedFileName)
                .console(console)
                .evidenceProfile(ReportEvidenceProfile.forScreenshotPolicy(
                        configuration.screenshotPolicy()))
                .systemInfo(systemInfo)
                .timestampEnabled(false)
                .build());
    }

    /** Opens a resolved UI report with allowlisted semantic System metadata. */
    public static UiExtentReportSession openConfiguredResolved(Path outputDirectory,
                                                                String resolvedFileName,
                                                                AtfConsoleSink console,
                                                                ReportEnvironmentSnapshot systemInfo) {
        var configuration = AtfReportConfigurationLoader.load();
        return open(configuration, UiExtentReportOptions.builder(outputDirectory)
                .fileName(resolvedFileName)
                .console(console)
                .evidenceProfile(ReportEvidenceProfile.forScreenshotPolicy(
                        configuration.screenshotPolicy()))
                .environment(systemInfo)
                .timestampEnabled(false)
                .build());
    }

    /** Opens a resolved report with independent typed evidence and System contracts. */
    public static UiExtentReportSession openConfiguredResolved(Path outputDirectory,
                                                                String resolvedFileName,
                                                                AtfConsoleSink console,
                                                                ReportEvidenceProfile evidenceProfile,
                                                                ReportEnvironmentSnapshot systemInfo) {
        var configuration = AtfReportConfigurationLoader.load();
        return open(configuration, UiExtentReportOptions.builder(outputDirectory)
                .fileName(resolvedFileName)
                .console(console)
                .evidenceProfile(evidenceProfile)
                .environment(systemInfo)
                .timestampEnabled(false)
                .build());
    }

    private static UiExtentReportSession open(ExtentReportConfiguration configuration,
                                              UiExtentReportOptions options) {
        var uiConfiguration = new ExtentReportConfiguration(
                options.outputDirectory(), options.fileName(),
                options.timestampEnabled() && configuration.timestampEnabled(),
                configuration.timestampPattern(), configuration.timestampZone(),
                configuration.reportingSettings(), configuration.enabled(),
                options.evidenceProfile().screenshotPolicy());
        Path report = uiConfiguration.resolveReportPath(Clock.systemDefaultZone());
        // Keep test identity in the Tests tree; Overview/System receives only
        // operational metadata from the typed environment projection.
        var systemInfo = new LinkedHashMap<String, String>(options.environment().systemDisplayValues());
        systemInfo.putAll(options.systemInfo());
        return new UiExtentReportSession(new ExtentReportSession(report,
                uiConfiguration.reportingSettings(), new SensitiveDataRedactor(), options.console(),
                systemInfo, options.evidenceProfile().actionDetailsMode()),
                options.evidenceProfile());
    }

    public Path reportPath() {
        return delegate.reportPath();
    }

    /** Returns the immutable evidence contract selected by the consumer. */
    public ReportEvidenceProfile evidenceProfile() {
        return evidenceProfile;
    }

    public UiScenario startScenario(String testClass, String testPackage,
                                    String scenario, Collection<String> tags) {
        return new UiScenario(new UiScenarioLifecycle(
                delegate.startScenario(testClass, testPackage, scenario, tags),
                evidenceProfile, scenario));
    }

    @Override
    public void close() {
        delegate.close();
    }

    public final class UiScenario implements AutoCloseable {
        private final UiScenarioLifecycle lifecycle;

        private UiScenario(UiScenarioLifecycle lifecycle) {
            this.lifecycle = lifecycle;
        }

        public UiPhase startPhase(String name) {
            return new UiPhase(lifecycle.startPhase(name));
        }

        public void complete(AtfReportStatus status) {
            lifecycle.complete(status);
        }

        @Override
        public void close() {
            lifecycle.close();
        }
    }

    public final class UiPhase implements AutoCloseable {
        private final UiPhaseLifecycle lifecycle;

        private UiPhase(UiPhaseLifecycle lifecycle) {
            this.lifecycle = lifecycle;
        }

        public void step(String keyword, String title, String target,
                         String base64Png, AtfReportStatus status) {
            lifecycle.step(keyword, title, target, base64Png, status);
        }

        public void step(AtfGherkinKeyword keyword, String title, String target,
                         String base64Png, AtfReportStatus status) {
            lifecycle.step(keyword, title, target, base64Png, status);
        }

        public void step(String keyword, String title, String target,
                         String base64Png, Collection<AtfAssertion> assertions,
                         AtfReportStatus status) {
            lifecycle.step(keyword, title, target, base64Png, assertions, status);
        }

        public void step(AtfGherkinKeyword keyword, String title, String target,
                         String base64Png, Collection<AtfAssertion> assertions,
                         AtfReportStatus status) {
            lifecycle.step(keyword, title, target, base64Png, assertions, status);
        }

        public void step(AtfGherkinKeyword keyword, String title, String target,
                         String base64Png, Collection<AtfAssertion> assertions,
                         Collection<AtfUiActionRecord> actions,
                         AtfReportStatus status) {
            lifecycle.step(keyword, title, target, base64Png, assertions, actions, status);
        }

        /** Publishes a checkpoint with an explicit lifecycle point for policy selection. */
        public void step(AtfGherkinKeyword keyword, String title, String target,
                         String base64Png, Collection<AtfAssertion> assertions,
                         Collection<AtfUiActionRecord> actions,
                         ReportScreenshotPolicy.CapturePoint capturePoint,
                         AtfReportStatus status) {
            lifecycle.step(keyword, title, target, base64Png, assertions, actions,
                    capturePoint, status);
        }

        public void step(AtfGherkinKeyword keyword, String title, String target,
                         String base64Png, Collection<AtfAssertion> assertions,
                         Collection<AtfUiActionRecord> actions,
                         Collection<ReportExecutionEvidence> apiEvidence,
                         AtfReportStatus status) {
            lifecycle.step(keyword.text(), title, target, base64Png, assertions, actions,
                    apiEvidence, status);
        }

        /** Keeps API evidence on the same checkpoint with an explicit capture point. */
        public void step(AtfGherkinKeyword keyword, String title, String target,
                         String base64Png, Collection<AtfAssertion> assertions,
                         Collection<AtfUiActionRecord> actions,
                         Collection<ReportExecutionEvidence> apiEvidence,
                         ReportScreenshotPolicy.CapturePoint capturePoint,
                         AtfReportStatus status) {
            lifecycle.step(keyword.text(), title, target, base64Png, assertions, actions,
                    apiEvidence, capturePoint, status);
        }

        /** Adds a compact journey-level network projection without duplicating JSON bodies. */
        public void networkInspector(String title,
                                     Collection<atf.api.reporting.model.ReportExecutionEvidence> evidence) {
            lifecycle.networkInspector(title, evidence);
        }

        /** Publishes ordered UI checkpoints plus their compact network projection. */
        public void networkInspector(String title,
                                     Collection<atf.api.reporting.model.ReportExecutionEvidence> evidence,
                                     Collection<ReportNetworkInspector.Step> steps) {
            lifecycle.networkInspector(title, evidence, steps);
        }

        /** Publishes the journey aggregate with deduplicated runtime diagnostics. */
        public void networkInspector(String title,
                                     Collection<atf.api.reporting.model.ReportExecutionEvidence> evidence,
                                     Collection<ReportNetworkInspector.Step> steps,
                                     Collection<ReportNetworkInspector.Diagnostic> diagnostics) {
            lifecycle.networkInspector(title, evidence, steps, diagnostics);
        }

        public void setup(String keyword, String title, String target,
                          Collection<AtfAssertion> assertions, AtfReportStatus status) {
            lifecycle.setup(keyword, title, target, assertions, status);
        }

        public void setup(AtfGherkinKeyword keyword, String title, String target,
                          Collection<AtfAssertion> assertions, AtfReportStatus status) {
            lifecycle.setup(keyword, title, target, assertions, status);
        }

        public void outcome(String keyword, String title, String target,
                            AtfReportStatus status) {
            lifecycle.outcome(keyword, title, target, status);
        }

        public void outcome(AtfGherkinKeyword keyword, String title, String target,
                            AtfReportStatus status) {
            lifecycle.outcome(keyword, title, target, status);
        }

        /** Publishes a non-visual browser/runtime diagnostic without a UI-validation label. */
        public void diagnostic(AtfGherkinKeyword keyword, String title, String target,
                               Collection<AtfAssertion> assertions, AtfReportStatus status) {
            lifecycle.diagnostic(keyword, title, target, assertions, status);
        }

        /** Publishes API evidence as a causal sibling immediately after its UI checkpoint. */
        public void apiTrace(AtfGherkinKeyword keyword, String title, String target,
                             Collection<ReportExecutionEvidence> apiEvidence,
                             AtfReportStatus status) {
            lifecycle.apiTrace(keyword, title, target, apiEvidence, status);
        }

        public void screenValidation(String title, String target, String base64Png,
                                     Collection<AtfAssertion> assertions,
                                     AtfReportStatus status) {
            lifecycle.screenValidation(title, target, base64Png, assertions, status);
        }

        public void screenValidation(String title, String target, String base64Png,
                                     Collection<AtfAssertion> assertions,
                                     Collection<AtfUiActionRecord> actions,
                                     AtfReportStatus status) {
            lifecycle.screenValidation(title, target, base64Png, assertions, actions, status);
        }

        /** Publishes a screen validation at an explicit checkpoint/final point. */
        public void screenValidation(String title, String target, String base64Png,
                                     Collection<AtfAssertion> assertions,
                                     Collection<AtfUiActionRecord> actions,
                                     ReportScreenshotPolicy.CapturePoint capturePoint,
                                     AtfReportStatus status) {
            lifecycle.screenValidation(title, target, base64Png, assertions, actions,
                    capturePoint, status);
        }

        public void screenValidationAsMedia(String title, String target, String base64Png,
                                            Collection<AtfAssertion> assertions,
                                            Collection<AtfUiActionRecord> actions,
                                            AtfReportStatus status) {
            lifecycle.screenValidationAsMedia(title, target, base64Png, assertions, actions, status);
        }

        public void failureEvidence(String title, String target,
                                    Collection<AtfUiActionRecord> actions,
                                    Throwable failure) {
            lifecycle.failureEvidence(title, target, actions, failure, null);
        }

        public void failureEvidence(String title, String target,
                                    Collection<AtfUiActionRecord> actions,
                                    Throwable failure, String base64Png) {
            lifecycle.failureEvidence(title, target, actions, failure, base64Png);
        }

        public void complete(AtfReportStatus status) {
            lifecycle.complete(status);
        }

        @Override
        public void close() {
            lifecycle.close();
        }
    }
}
