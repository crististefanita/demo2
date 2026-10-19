package atf.api.reporting.ui;

import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfGherkinKeyword;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.reporting.configuration.AtfReportConfigurationLoader;
import atf.api.reporting.configuration.ExtentReportConfiguration;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.core.ui.AtfUiActionRecord;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
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

    private UiExtentReportSession(ExtentReportSession delegate) {
        this.delegate = delegate;
    }

    public static UiExtentReportSession openConfigured() {
        return openConfigured(AtfConsoleSink.noOp());
    }

    public static UiExtentReportSession openConfigured(AtfConsoleSink console) {
        var configuration = AtfReportConfigurationLoader.load();
        return openConfigured(configuration.outputDirectory(), console);
    }

    /** Opens the UI artifact in the directory selected by the UI YAML section. */
    public static UiExtentReportSession openConfigured(Path outputDirectory,
                                                       AtfConsoleSink console) {
        return openConfigured(outputDirectory, DEFAULT_FILE_NAME, console);
    }

    /** Opens a separately named UI audit artifact without changing API input. */
    public static UiExtentReportSession openConfigured(Path outputDirectory,
                                                       String fileName,
                                                       AtfConsoleSink console) {
        return openConfigured(outputDirectory, fileName, console, true);
    }

    /** Opens a report whose collision-resistant name was already resolved. */
    public static UiExtentReportSession openConfiguredResolved(Path outputDirectory,
                                                                String resolvedFileName,
                                                                AtfConsoleSink console) {
        return openConfigured(outputDirectory, resolvedFileName, console, false);
    }

    private static UiExtentReportSession openConfigured(Path outputDirectory,
                                                        String fileName,
                                                        AtfConsoleSink console,
                                                        boolean timestampEnabled) {
        var apiConfiguration = AtfReportConfigurationLoader.load();
        var uiConfiguration = new ExtentReportConfiguration(
                Objects.requireNonNull(outputDirectory, "outputDirectory"),
                Objects.requireNonNull(fileName, "fileName"),
                timestampEnabled && apiConfiguration.timestampEnabled(),
                apiConfiguration.timestampPattern(),
                apiConfiguration.timestampZone(),
                apiConfiguration.reportingSettings(),
                apiConfiguration.enabled());
        Path report = uiConfiguration.resolveReportPath(Clock.systemDefaultZone());
        return new UiExtentReportSession(new ExtentReportSession(report,
                uiConfiguration.reportingSettings(),
                new SensitiveDataRedactor(), console));
    }

    public Path reportPath() {
        return delegate.reportPath();
    }

    public UiScenario startScenario(String testClass, String testPackage,
                                    String scenario, Collection<String> tags) {
        return new UiScenario(new UiScenarioLifecycle(
                delegate.startScenario(testClass, testPackage, scenario, tags)));
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
