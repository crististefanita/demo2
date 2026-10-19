package atf.ui.tests.extent.support.config;

import java.nio.file.Path;
import java.util.Objects;

/** Process-boundary inputs captured once before loading UI configuration. */
public record AtfUiConfigurationContext(Path startDirectory, String systemPropertyConfig,
                                        String environmentConfig, String reportOverride) {
    public static final String CONFIG_PROPERTY = "atf.ui.config";
    public static final String CONFIG_ENVIRONMENT_VARIABLE = "ATF_UI_CONFIG";
    public static final String REPORT_PROPERTY = "atf.ui.report";

    public AtfUiConfigurationContext {
        startDirectory = Objects.requireNonNull(startDirectory, "startDirectory")
                .toAbsolutePath().normalize();
        systemPropertyConfig = normalize(systemPropertyConfig);
        environmentConfig = normalize(environmentConfig);
        reportOverride = normalize(reportOverride);
    }

    public AtfUiConfigurationContext(Path startDirectory, String systemPropertyConfig,
                                     String environmentConfig) {
        this(startDirectory, systemPropertyConfig, environmentConfig, null);
    }

    public static AtfUiConfigurationContext fromProcess() {
        return new AtfUiConfigurationContext(Path.of(System.getProperty("user.dir", ".")),
                System.getProperty(CONFIG_PROPERTY), System.getenv(CONFIG_ENVIRONMENT_VARIABLE),
                System.getProperty(REPORT_PROPERTY));
    }

    public Path explicitConfigPath() {
        String value = systemPropertyConfig != null ? systemPropertyConfig : environmentConfig;
        if (value == null) return null;
        Path path = Path.of(value);
        return path.isAbsolute() ? path.normalize() : startDirectory.resolve(path).normalize();
    }

    public Path reportOverridePath() {
        return reportOverridePath(startDirectory);
    }

    public Path reportOverridePath(Path baseDirectory) {
        if (reportOverride == null) return null;
        Path path = Path.of(reportOverride);
        Path base = Objects.requireNonNull(baseDirectory, "baseDirectory");
        return (path.isAbsolute() ? path : base.resolve(path))
                .toAbsolutePath().normalize();
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }
}
