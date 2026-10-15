package atf.api.reporting;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.ui.AtfUiActionRecord;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * UI-test reporting facade. It deliberately creates a report separate from
 * the API artifact being inspected; the API artifact is an input, never the
 * owner of Playwright execution evidence.
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
        var apiConfiguration = AtfReportConfigurationLoader.load();
        var uiConfiguration = new ExtentReportConfiguration(
                Objects.requireNonNull(outputDirectory, "outputDirectory"),
                Objects.requireNonNull(fileName, "fileName"),
                apiConfiguration.timestampEnabled(),
                apiConfiguration.timestampPattern(),
                apiConfiguration.timestampZone(),
                apiConfiguration.reportingSettings(),
                apiConfiguration.enabled());
        Path report = uiConfiguration.resolveReportPath(Clock.systemDefaultZone());
        return new UiExtentReportSession(new ExtentReportSession(report,
                uiConfiguration.reportingSettings(),
                new atf.api.core.security.SensitiveDataRedactor(), console));
    }

    public Path reportPath() {
        return delegate.reportPath();
    }

    public UiScenario startScenario(String testClass, String testPackage,
                                    String scenario, Collection<String> tags) {
        return new UiScenario(delegate.startScenario(testClass, testPackage, scenario, tags));
    }

    @Override public void close() {
        delegate.close();
    }

    public final class UiScenario implements AutoCloseable {
        private final AtfReportPorts.Scenario delegateScenario;
        private boolean completed;

        private UiScenario(AtfReportPorts.Scenario delegateScenario) {
            this.delegateScenario = delegateScenario;
        }

        /**
         * Starts an explicitly requested lifecycle phase.
         *
         * <p>Phases are opt-in: a UI audit normally needs only {@code SETUP}
         * and {@code TEST BODY}. The caller may start {@code TEARDOWN} when
         * it has real cleanup or a lifecycle outcome to record; the session
         * never invents that phase and still publishes the report on close.</p>
         */
        public UiPhase startPhase(String name) {
            ensureOpen();
            return new UiPhase(delegateScenario.startPhase(name));
        }

        public void complete(AtfReportStatus status) {
            ensureOpen();
            delegateScenario.complete(status == null ? AtfReportStatus.INFO : status);
            completed = true;
        }

        @Override public void close() {
            if (!completed) complete(AtfReportStatus.FAIL);
        }

        private void ensureOpen() {
            if (completed) throw new IllegalStateException("UI scenario is already complete");
        }
    }

    public final class UiPhase implements AutoCloseable {
        private final AtfReportPorts.Phase delegatePhase;
        private boolean completed;

        private UiPhase(AtfReportPorts.Phase delegatePhase) {
            this.delegatePhase = delegatePhase;
        }

        /** Adds one Gherkin-shaped Playwright step and optional Base64 screenshot link. */
        public void step(String keyword, String title, String target,
                         String base64Png, AtfReportStatus status) {
            step(keyword, title, target, base64Png, List.of(), status);
        }

        /** Adds a UI step and renders its validations in the shared API table. */
        public void step(String keyword, String title, String target,
                         String base64Png, Collection<AtfAssertion> assertions,
                         AtfReportStatus status) {
            action(keyword, title, target, base64Png, assertions, status,
                    "UI validation", List.of());
        }

        /**
         * Publishes a lifecycle outcome as a standalone Gherkin step.
         *
         * <p>An outcome such as "the UI artifact was published" is not a UI
         * assertion and must not acquire a synthetic {@code UI validation}
         * child. Use {@link #screenValidation(String, String, String,
         * Collection, AtfReportStatus)} for visual evidence and assertions.</p>
         */
        public void outcome(String keyword, String title, String target,
                            AtfReportStatus status) {
            ensureOpen();
            String stepTitle = ((keyword == null ? "" : keyword.trim()) + " "
                    + (title == null ? "" : title.trim())).trim();
            var request = delegatePhase.startRequest(stepTitle, "", "");
            try {
                request.detail("request.action", stepTitle);
                if (target != null && !target.isBlank()) request.detail("request.target", target);
                request.detail("outcome.type", "artifact-publication");
                request.complete(status == null ? AtfReportStatus.PASS : status);
            } finally {
                request.close();
            }
        }

        /**
         * Publishes one collapsible screen-validation zone: one Base64 screenshot
         * link for the current page state and one aggregate table for all
         * assertions collected before the page changes.
         */
        public void screenValidation(String title, String target, String base64Png,
                                     Collection<AtfAssertion> assertions,
                                     AtfReportStatus status) {
            action("THEN", title, target, base64Png, assertions,
                    status, "Screen validation", List.of());
        }

        /** Publishes one visual state together with its consecutive Playwright actions. */
        public void screenValidation(String title, String target, String base64Png,
                                     Collection<AtfAssertion> assertions,
                                     Collection<AtfUiActionRecord> actions,
                                     AtfReportStatus status) {
            action("THEN", title, target, base64Png, assertions,
                    status, "Screen validation", actions);
        }

        /**
         * Publishes the consecutive Playwright actions in a table and the
         * visual state as native Base64 media. The screenshot is a compact
         * clickable report artifact, not an inline HTML image.
         */
        public void screenValidationAsMedia(String title, String target, String base64Png,
                                            Collection<AtfAssertion> assertions,
                                            Collection<AtfUiActionRecord> actions,
                                            AtfReportStatus status) {
            ensureOpen();
            String stepTitle = ("THEN " + (title == null ? "" : title.trim())).trim();
            var request = delegatePhase.startRequest(stepTitle, "", "");
            try {
                request.detail("request.action", stepTitle);
                if (target != null && !target.isBlank()) request.detail("request.target", target);
                request.detail("evidence.type", "screen-validation");
                request.detail("evidence.policy", "Base64 native media link; no inline image");
                var validation = request.startValidation("Screen validation");
                if (actions != null && !actions.isEmpty()) validation.uiActions(actions);
                if (base64Png != null && !base64Png.isBlank()) {
                    validation.mediaScreenshot("UI screenshot — " + title + " — "
                            + (status == null ? AtfReportStatus.PASS : status), base64Png);
                }
                validation.assertions(assertions);
                validation.complete(status == null ? AtfReportStatus.PASS : status);
                request.complete(status == null ? AtfReportStatus.PASS : status);
            } finally {
                request.close();
            }
        }

        private void action(String keyword, String title, String target,
                            String base64Png, Collection<AtfAssertion> assertions,
                            AtfReportStatus status, String validationTitle,
                            Collection<AtfUiActionRecord> actions) {
            ensureOpen();
            String stepTitle = ((keyword == null ? "" : keyword.trim()) + " "
                    + (title == null ? "" : title.trim())).trim();
            var request = delegatePhase.startRequest(stepTitle, "", "");
            try {
                request.detail("request.action", stepTitle);
                if (target != null && !target.isBlank()) request.detail("request.target", target);
                if ("Screen validation".equals(validationTitle)) {
                    request.detail("evidence.type", "screen-validation");
                    request.detail("evidence.policy", "one Base64 screenshot link per visual state");
                }
                boolean visualValidation = "Screen validation".equals(validationTitle);
                if (visualValidation || (assertions != null && !assertions.isEmpty())) {
                    var validation = request.startValidation(validationTitle);
                    if (actions != null && !actions.isEmpty()) validation.uiActions(actions);
                    if (base64Png != null && !base64Png.isBlank()) {
                        validation.screenshot("UI assertion — " + title + " — "
                                        + (status == null ? AtfReportStatus.PASS : status),
                                base64Png);
                    }
                    validation.assertions(assertions);
                    validation.complete(status == null ? AtfReportStatus.PASS : status);
                }
                request.complete(status == null ? AtfReportStatus.PASS : status);
            } finally {
                request.close();
            }
        }

        public void complete(AtfReportStatus status) {
            ensureOpen();
            delegatePhase.complete(status == null ? AtfReportStatus.INFO : status);
            completed = true;
        }

        @Override public void close() {
            if (!completed) complete(AtfReportStatus.FAIL);
        }

        private void ensureOpen() {
            if (completed) throw new IllegalStateException("UI phase is already complete");
        }
    }
}
